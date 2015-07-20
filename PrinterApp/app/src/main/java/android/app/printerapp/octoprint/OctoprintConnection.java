package android.app.printerapp.octoprint;

import android.app.Dialog;
import android.app.printerapp.ListContent;
import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.devices.discovery.DiscoveryController;
import android.app.printerapp.devices.discovery.PrintNetworkManager;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.settings.EditPrinterDialog;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;

/**
 * Class for Connection handling with Octoprint's API. Since the API is still on developement
 * we need more than one method, and they need interaction between them, this may change.
 * @author alberto-baeza
 *
 */
public class OctoprintConnection {

    private static final int SOCKET_TIMEOUT = 10000;
    public static final String DEFAULT_PORT = "/dev/ttyUSB0";
    private static final String DEFAULT_PROFILE = "_default";
    private static final String API_DISABLED_MSG = "API disabled";
    private static final String API_INVALID_MSG = "Invalid API key";

	/**
	 * 
	 * Post parameters to handle connection. JSON for the new API is made 
	 * but never used.
	 * 
	 */
	public static void startConnection(String url, final Context context, String port, String profile ){
					
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		try {
			object.put("command","connect");
            object.put("port",port);
            object.put("printerProfile", profile);
            object.put("save", true);
			object.put("autoconnect","true");
			entity = new StringEntity(object.toString(), "UTF-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        Log.i("Profile", "Start connection on " + profile);
		
		HttpClientHandler.post(context,url + HttpUtils.URL_CONNECTION, 
				entity, "application/json", new JsonHttpResponseHandler(){

			//Override onProgress because it's faulty
			@Override
			public void onProgress(int bytesWritten, int totalSize) {						
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				
				
				Log.i("CONNECTION", "Failure because: " + responseString);
				
				super.onFailure(statusCode, headers, responseString, throwable);
			}
			
		});	
		
		
		
	}
    public static void disconnect(Context context, String url){

        JSONObject object = new JSONObject();
        StringEntity entity = null;
        try {
            object.put("command","disconnect");
            entity = new StringEntity(object.toString(), "UTF-8");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpClientHandler.post(context,url + HttpUtils.URL_CONNECTION,
                entity, "application/json", new JsonHttpResponseHandler(){

                    //Override onProgress because it's faulty
                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                    }
                });
    }

    public static void getLinkedConnection(final Context context, final ModelPrinter p){

        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_CONNECTION, null, new JsonHttpResponseHandler(){

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                JSONObject current = null;
                try {
                    current = response.getJSONObject("current");
                    p.setPort(current.getString("port"));
                    convertType(p, current.getString("printerProfile"));

                    //retrieve settings
                    //getUpdatedSettings(p,current.getString("printerProfile"));
                    getSettings(p);

                    Intent intent = new Intent("notify");
                    intent.putExtra("message", "Devices");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                    Log.i("Connection", "Printer already connected to " + p.getPort());
                    //p.startUpdate(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                if (statusCode == 401 && responseString.equals(API_DISABLED_MSG)){
                    Log.i("Connection", responseString);
                } else {
                    OctoprintAuthentication.getAuth(context, p, false);
                }


            }
        });

    }
	
	/**
	 * Obtains the current state of the machine and issues new connection commands
	 * @param p printer
	 */
	public static void getNewConnection(final Context context, final ModelPrinter p){

        //Get progress dialog UI
        View configurePrinterDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress_content_horizontal, null);
        ((TextView) configurePrinterDialogView.findViewById (R.id.progress_dialog_text)).setText(R.string.devices_discovery_connect);


//        try{
            //Show progress dialog
            final MaterialDialog.Builder configurePrinterDialogBuilder = new MaterialDialog.Builder(context);
            configurePrinterDialogBuilder.title(R.string.devices_discovery_title)
                    .customView(configurePrinterDialogView, true)
                    .cancelable(true)
                    .negativeText(R.string.cancel)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onNegative(MaterialDialog dialog) {
                            super.onNegative(dialog);
                            dialog.setOnDismissListener(null);
                            dialog.dismiss();


                        }
                    })
                    .autoDismiss(false);
            //Progress dialog to notify command events
            final Dialog progressDialog = configurePrinterDialogBuilder.build();
            progressDialog.show();
//        } catch (WindowManager.BadTokenException e){
//            e.printStackTrace();
//        }


        //Get connection status
        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_CONNECTION, null, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);


                //TODO Random crash
                try{
                    if (progressDialog.isShowing()) progressDialog.dismiss();
                   else return;
                } catch (ArrayIndexOutOfBoundsException e){

                    e.printStackTrace();

                }catch (NullPointerException e){

                    e.printStackTrace();

                    return;

                }


                //Check for current status
                JSONObject current = null;

                try {
                    current = response.getJSONObject("current");

                        //if closed or error
                        if ((current.getString("state").contains("Closed"))
                                ||(current.getString("state").contains("Error"))
                                 || (current.getString("printerProfile").equals(DEFAULT_PROFILE))) {

                            //configure new printer
                            new EditPrinterDialog(context, p, response);

                        } else {


                            //already connected
                            if (p.getStatus() == StateUtils.STATE_NEW){

                                //load information
                                p.setPort(current.getString("port"));
                                convertType(p, current.getString("printerProfile"));
                                //getUpdatedSettings(p,current.getString("printerProfile"));
                                getSettings(p);
                                Log.i("Connection", "Printer already connected to " + p.getPort());

                                String network = MainActivity.getCurrentNetwork(context);
                                p.setNetwork(network);

                                p.setId(DatabaseController.writeDb(p.getName(), p.getAddress(), String.valueOf(p.getPosition()), String.valueOf(p.getType()), network));

                                p.startUpdate(context);

                            }


                        }


                } catch (JSONException e) {
                    e.printStackTrace();
                }
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);


                Log.i("Connection", "Failure while connecting " + statusCode + " == " + responseString);

                if (statusCode == 401 && responseString.equals(API_DISABLED_MSG)){
                    showApiDisabledDialog(context);
                } else {
                    OctoprintAuthentication.getAuth(context, p, true);
                }
                progressDialog.dismiss();

			}

		});
		
		
		
	}

    public static void showApiDisabledDialog(final Context context){

        new MaterialDialog.Builder(context)
                .title(R.string.error)
                .content(R.string.connection_error_api_disabled)
                .positiveText(R.string.ok)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        super.onPositive(dialog);
                        new DiscoveryController(context).optionAddPrinter();
                    }
                }).show();


    }

    private static void convertType(ModelPrinter p, String type){

        Log.i("Profile","Converting type " + type);

        if (type.equals(ModelProfile.WITBOX_PROFILE)) p.setType(1, ModelProfile.WITBOX_PROFILE);
        else if (type.equals(ModelProfile.PRUSA_PROFILE)) p.setType(2, ModelProfile.PRUSA_PROFILE);
        else if (p.getProfile() == null)  {

            Log.i("Profile","Setting type ");

            p.setType(3, type);
        } else if (!p.getProfile().equals("_default")){
            Log.i("Profile","Setting type default");

            p.setType(3, type);

        } else  Log.i("Profile", "Basura " + p.getProfile());

        Log.i("Profile","Get type " + p.getProfile());


    }

    /*************************************************
     * SETTINGS
     **************************************************/

    public static int convertColor(String color){

        if (color.equals("default")) return Color.TRANSPARENT;
        if (color.equals("red")) return Color.RED;
        if (color.equals("orange")) return Color.rgb(255,165,0);
        if (color.equals("yellow")) return Color.YELLOW;
        if (color.equals("green")) return Color.GREEN;
        if (color.equals("blue")) return Color.BLUE;
        if (color.equals("violet")) return Color.rgb(138,43,226);

        return Color.BLACK;

    }

    public static void getUpdatedSettings(final ModelPrinter p, String profile){

        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_PROFILES + "/" + profile, null, new JsonHttpResponseHandler(){


            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                Log.i("Connection", response.toString());

                try {
                    String name = response.getString("name");
                    String color = response.getString("color");

                    if(!name.equals("")) {

                        p.setDisplayName(name);
                        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, p.getId(), name);
                    }

                    p.setDisplayColor(convertColor(color));



                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });



    }

    /**
     * Function to get the settings from the server
     * @param p
     */
    public static void getSettings(final ModelPrinter p){

        final String PREFIX = "http:/";

        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_SETTINGS, null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    JSONObject appearance = response.getJSONObject("appearance");

                    Log.i("Connection", appearance.toString());

                    String newName = appearance.getString("name");
                    if(!newName.equals("")) {

                        p.setDisplayName(newName);
                        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, p.getId(), newName);
                        }

                    p.setDisplayColor(convertColor(appearance.getString("color")));

                } catch (JSONException e) {
                    e.printStackTrace();
                }



                try {

                    JSONObject webcam = response.getJSONObject("webcam");

                    if (webcam.has("streamUrl")){
                        if (webcam.getString("streamUrl").startsWith("/")) {
                            p.setWebcamAddress(PREFIX + p.getAddress() + webcam.getString("streamUrl"));
                        } else {
                            p.setWebcamAddress(webcam.getString("streamUrl"));
                        }
                    }


                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onProgress(int bytesWritten, int totalSize) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.i("Connection", "Settings failure: " + responseString);
                DatabaseController.handlePreference(DatabaseController.TAG_KEYS, PrintNetworkManager.getNetworkId(p.getAddress()), null, false);
                MainActivity.showDialog(responseString);
            }
        });

    }

    /**
     * Function to set the settings to the server
     */
    public static void setSettings(final ModelPrinter p, String newName, final String newColor, Context context){

        JSONObject object = new JSONObject();
        JSONObject appearance = new JSONObject();
        StringEntity entity = null;
        try {
            appearance.put("name", newName);
            appearance.put("color", newColor);
            object.put("appearance",appearance);
            entity = new StringEntity(object.toString(), "UTF-8");
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpClientHandler.post(context, p.getAddress() + HttpUtils.URL_SETTINGS,
                entity, "application/json", new JsonHttpResponseHandler() {

                    //Override onProgress because it's faulty
                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        //if (newColor!=null) p.setDisplayColor(convertColor(newColor));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);

                        Log.i("Connection", "Settings failure: " + responseString);
                    }
                });

    }
	
	
	/**
	 * 
	 * Obtains the state of the machine that will be on the Connection API, that's why this is here.
	 * Works in conjunction with GET /api/connection on the NEW API.
	 * 
	 * New client implementation uses Websockets to receive status updates from the server
	 * so we only need to open a new connection and parse the payload.
	 */
	public static void openSocket(final ModelPrinter p, final Context context){
		
		p.setConnecting();
		
		//Web socket URI
		final String wsuri = "ws:/" + p.getAddress() + HttpUtils.URL_SOCKET;

		   try {

			  final WebSocketConnection mConnection = new WebSocketConnection();

			   //mConnection is a new websocket connection
		      mConnection.connect(wsuri, new WebSocketHandler() {

		    	  //When the websocket opens
		         @Override
		         public void onOpen() {



                     //TODO unify this method
		            Log.i("Connection", "Status: Connected to " + wsuri);
                     Log.i("CONNECTION", "Connection from: SOCKET");
                    doConnection(context,p);

		         }


		         //On message received
		         @Override
		         public void onTextMessage(String payload) {

		        	    //Log.i("SOCK", "Got echo [" + p.getAddress() + "]: " + payload);

		        	  try {

		        	 JSONObject object = new JSONObject(payload);

		            	//Get the json string for "current" status
		            	if (object.has("current")){

		            		JSONObject response = new JSONObject(payload).getJSONObject("current");

							//Update job with current status
			            	//We'll add every single parameter
							p.updatePrinter(response.getJSONObject("state").getString("text"), createStatus(response.getJSONObject("state").getJSONObject("flags")),
									response);


                            //SEND NOTIFICATION

                            Intent intent = new Intent("notify");
                            intent.putExtra("message", "Devices");
                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                            if (!response.getJSONObject("progress").getString("completion").equals("null")){

                                Double d = Double.parseDouble(response.getJSONObject("progress").getString("completion"));

                                if ((d>0) && (p.getStatus() == StateUtils.STATE_PRINTING)){

                                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                    if (sharedPref.getBoolean(context.getResources().getString(R.string.shared_preferences_print), true)){

                                        Intent intentN = new Intent();
                                        intentN.setAction("android.app.printerapp.NotificationReceiver");
                                        intentN.putExtra("printer", p.getId());
                                        intentN.putExtra("progress", d.intValue());
                                        intentN.putExtra("type","print");
                                        context.sendBroadcast(intentN);


                                    }



                                }


                            }


		            	}

                         //Check for events in the server
		            	if (object.has("event")){

		            		JSONObject response = new JSONObject(payload).getJSONObject("event");

                            //Slicing finished should be handled in another method
		            		if (response.getString("type").equals("SlicingDone")){

		            			JSONObject slicingPayload = response.getJSONObject("payload");

		            			sliceHandling(context, slicingPayload, p.getAddress());

		            		}

                            //A file was uploaded
		            		if (response.getString("type").equals("Upload")){

		            			//p.setLoaded(true);
                                if (DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last")!=null)
                                    if ((DatabaseController.getPreference("Slicing","Last")).equals(response.getJSONObject("payload").getString("file"))){

                                        //Log.i("Slicer","LETS SLICE " + response.getJSONObject("payload").getString("file"));
                                        }

		            		}
                            if (response.getString("type").equals("PrintStarted")){
                                p.setLoaded(true);
                            }

                            if (response.getString("type").equals("Connected")){
                                p.setPort(response.getJSONObject("payload").getString("port"));
                                Log.i("OUT", "UPDATED PORT " + p.getPort());
                            }

                            if (response.getString("type").equals("PrintDone")){

                                //SEND NOTIFICATION

                                Log.i("OUT", "PRINT FINISHED! " + response.toString());

                                if (p.getJobPath()!=null)addToHistory(p,response.getJSONObject("payload"));

                                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                if (sharedPref.getBoolean(context.getResources().getString(R.string.shared_preferences_print), true)) {

                                    Intent intentN = new Intent();
                                    intentN.setAction("android.app.printerapp.NotificationReceiver");
                                    intentN.putExtra("printer", p.getId());
                                    intentN.putExtra("progress", 100);
                                    intentN.putExtra("type", "finish");
                                    context.sendBroadcast(intentN);
                                }


                            }

                            if (response.getString("type").equals("SettingsUpdated")){


                                getLinkedConnection(context,p);


                            }

		            	}



                          //update slicing progress in the print panel fragment
                          if (object.has("slicingProgress")){

                              JSONObject response = new JSONObject(payload).getJSONObject("slicingProgress");

                              //TODO random crash because not yet created
                              try{
                                  //Check if it's our file
                                  if(!DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last").equals(null))
                                  if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last").equals( response.getString("source_path"))){

                                      //Log.i("Slicer","Progress received for " + response.getString("source_path"));

                                      int progress = response.getInt("progress");


                                      //TODO
                                      ViewerMainFragment.showProgressBar(StateUtils.SLICER_SLICE, progress);

                                      //SEND NOTIFICATION

                                      SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
                                      if (sharedPref.getBoolean(context.getResources().getString(R.string.shared_preferences_slice), true)) {

                                          Intent intent = new Intent();
                                          intent.setAction("android.app.printerapp.NotificationReceiver");
                                          intent.putExtra("printer", p.getId());
                                          intent.putExtra("progress", progress);
                                          intent.putExtra("type", "slice");
                                          context.sendBroadcast(intent);
                                      }
                                  }
                              } catch (NullPointerException e){

                                  //e.printStackTrace();
                                  //Log.i("OUT","Null slicing");
                              }





                          }




		            } catch (JSONException e) {
						e.printStackTrace();
						Log.i("CONNECTION", "Invalid JSON");

					}







		         }

		         @Override
		         public void onClose(int code, String reason) {
		            Log.i("SOCK", "Connection lost at " + code + " because " + reason);

		            	mConnection.disconnect();

		            	//Timeout for reconnection
		            	Handler handler = new Handler();
		            	handler.postDelayed(new Runnable() {

							@Override
							public void run() {

                                Log.i("OUT", "Timeout expired, reconnecting to " + p.getAddress());

								 p.startUpdate(context);

							}
						}, SOCKET_TIMEOUT);


		         }
		      });
		   } catch (WebSocketException e) {

		      Log.i("SOCK", e.toString());
		   }

	}

    //TODO
    //Method to invoke connection handling
    public static void doConnection(Context context, ModelPrinter p){


        getLinkedConnection(context, p);

        //Get printer settings

        //getSettings(p);

        //Get a new set of files
        OctoprintFiles.getFiles(context, p, null);

        //Get a new set of profiles
        //OctoprintSlicing.retrieveProfiles(context,p); //Don't retrieve profiles yet



    }

    public static int createStatus(JSONObject flags){

				try {
					if (flags.getBoolean("paused")) return StateUtils.STATE_PAUSED;
					if (flags.getBoolean("printing")) return StateUtils.STATE_PRINTING;
					if (flags.getBoolean("operational")) return StateUtils.STATE_OPERATIONAL;
					if (flags.getBoolean("error")) return StateUtils.STATE_ERROR;
					if (flags.getBoolean("paused")) return StateUtils.STATE_PAUSED;
					if (flags.getBoolean("closedOrError")) return StateUtils.STATE_CLOSED;

				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				return StateUtils.STATE_NONE;
		
	}
	
	/**
	 * This method will create a dialog to handle the sliced file from the server.
	 * @param context
	 * @param payload sliced file data from the server
	 * @param url server address
	 */
	private static void sliceHandling(final Context context, final JSONObject payload, final String url){

		try {

            Log.i("Slicer", "Slice done received for " + payload.getString("stl"));

            //Search for files waiting for slice
            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last")!=null)
            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last").equals( payload.getString("stl")))
            {

                Log.i("Slicer", "Changed PREFERENCE [Last]: " + payload.getString("gcode"));
                DatabaseController.handlePreference(DatabaseController.TAG_SLICING,"Last",payload.getString("gcode"), true);

                ViewerMainFragment.showProgressBar(StateUtils.SLICER_DOWNLOAD, 0);

                OctoprintSlicing.getMetadata(url, payload.getString("gcode"));
                OctoprintFiles.downloadFile(context, url + HttpUtils.URL_DOWNLOAD_FILES,
                LibraryController.getParentFolder() + "/temp/", payload.getString("gcode"));
                OctoprintFiles.deleteFile(context,url,payload.getString("stl"), "/local/");

            }else {

                Log.i("Slicer", "Slicing NOPE for me!");

            }

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

    public static void addToHistory(ModelPrinter p, JSONObject history){


        try {
            String name = history.getString("filename");
            String path = p.getJobPath();
            String time = ConvertSecondToHHMMString(history.getString("time"));
            String type = p.getProfile();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            String date =  sdf.format(new Date());

            if (path!=null)
            if(!path.contains("/temp/")){

                LibraryController.addToHistory(new ListContent.DrawerListItem(type,name,time,date,path));
                DatabaseController.writeDBHistory(name, path, time, type, date);
            }


        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    //External method to convert seconds to HHmmss
    public static String ConvertSecondToHHMMString(String secondtTime) {
        String time = "--:--:--";

        if (!secondtTime.equals("null")) {

            int value = (int)Float.parseFloat(secondtTime);

            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
            df.setTimeZone(tz);
            time = df.format(new Date(value * 1000L));
        }


        return time;

    }

}
