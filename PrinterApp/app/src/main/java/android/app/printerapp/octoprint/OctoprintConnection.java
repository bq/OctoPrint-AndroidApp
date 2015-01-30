package android.app.printerapp.octoprint;

import android.app.ProgressDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.devices.discovery.PrintNetworkReceiver;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.settings.EditPrinterDialog;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

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

        Log.i("Profile","Start connection on " + profile);
		
		HttpClientHandler.post(context,url + HttpUtils.URL_CONNECTION, 
				entity, "application/json", new JsonHttpResponseHandler(){

			//Override onProgress because it's faulty
			@Override
			public void onProgress(int bytesWritten, int totalSize) {						
			}

			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				
				
				Log.i("CONNECTION","Failure because: " + responseString);
				
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

                    Log.i("Connection","Printer already connected to " + p.getPort());
                    //p.startUpdate(context);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.i("OUT","STATE WHEN FAILURE " + p.getStatus() + "REASON " + responseString);
                OctoprintAuthentication.getAuth(context, p, false);
            }
        });

    }
	
	/**
	 * Obtains the current state of the machine and issues new connection commands
	 * @param p printer
	 */
	public static void getNewConnection(final Context context, final ModelPrinter p){
        //Progress dialog to notify command events
        final ProgressDialog pd = new ProgressDialog(context);

        //Display a dialog to connect to the server

        try{

            pd.setMessage(context.getString(R.string.devices_command_waiting) + p.getAddress().replace("/"," "));
            pd.show();
        } catch (WindowManager.BadTokenException e){

            e.printStackTrace();
        }


        //Get connection status
        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_CONNECTION, null, new JsonHttpResponseHandler(){
						
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);


                //TODO Random crash
                try{
                    pd.dismiss();
                } catch (ArrayIndexOutOfBoundsException e){

                    e.printStackTrace();

                }catch (NullPointerException e){

                    e.printStackTrace();

                }


                //Check for current status
                JSONObject current = null;

                try {
                    current = response.getJSONObject("current");

                    Log.i("CONNECTION","CURRENT CONNECTION " + response.toString());

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
                                Log.i("Connection","Printer already connected to " + p.getPort());

                                String network = PrintNetworkReceiver.getCurrentNetwork();
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
				Log.i("Connection","Failure while connecting " + responseString);
				super.onFailure(statusCode, headers, responseString, throwable);

                Log.i("OUT","STATE WHEN FAILURE " + p.getStatus());

                pd.dismiss();
                OctoprintAuthentication.getAuth(context, p, true);
			}
			
		});
		
		
		
	}

    private static void convertType(ModelPrinter p, String type){

        if (type.equals(ModelProfile.WITBOX_PROFILE)) p.setType(1, ModelProfile.WITBOX_PROFILE);
        else if (type.equals(ModelProfile.PRUSA_PROFILE)) p.setType(2, ModelProfile.PRUSA_PROFILE);
        else p.setType(3, type);


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


                Log.i("Connection",response.toString());

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

                Log.i("Connection","Can't retrieve settings for " + p.getAddress() + ": " + responseString);
            }
        });



    }

    /**
     * Function to get the settings from the server
     * @param p
     */
    public static void getSettings(final ModelPrinter p){

        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_SETTINGS, null, new JsonHttpResponseHandler(){

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                try {
                    JSONObject appearance = response.getJSONObject("appearance");

                    Log.i("Connection",appearance.toString());

                    String newName = appearance.getString("name");
                    if(!newName.equals("")) {

                        p.setDisplayName(newName);
                        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, p.getId(), newName);
                        }

                    p.setDisplayColor(convertColor(appearance.getString("color")));


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

                Log.i("Connection","Settings failure: " + responseString);
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

                        Log.i("Connection","Settings failure: " + responseString);
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
                     Log.i("CONNECTION","Connection from: SOCKET");
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
                                    Intent intentN = new Intent();
                                    intentN.setAction("android.app.printerapp.NotificationReceiver");
                                    intentN.putExtra("printer", p.getId());
                                    intentN.putExtra("progress", d.intValue());
                                    intentN.putExtra("type","print");
                                    context.sendBroadcast(intentN);
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

                                        Log.i("Slicer","LETS SLICE " + response.getJSONObject("payload").getString("file"));
                                        }

		            		}
                            if (response.getString("type").equals("PrintStarted")){
                                p.setLoaded(true);
                            }

                            if (response.getString("type").equals("Connected")){
                                p.setPort(response.getJSONObject("payload").getString("port"));
                                Log.i("OUT","UPDATED PORT " + p.getPort());
                            }

                            if (response.getString("type").equals("PrintDone")){

                                //SEND NOTIFICATION

                                Intent intentN = new Intent();
                                intentN.setAction("android.app.printerapp.NotificationReceiver");
                                intentN.putExtra("printer", p.getId());
                                intentN.putExtra("progress", 100);
                                intentN.putExtra("type","finish");
                                context.sendBroadcast(intentN);


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

                                      Intent intent = new Intent();
                                      intent.setAction("android.app.printerapp.NotificationReceiver");
                                      intent.putExtra("printer", p.getId());
                                      intent.putExtra("progress", progress);
                                      intent.putExtra("type","slice");
                                      context.sendBroadcast(intent);
                                  }
                              } catch (NullPointerException e){

                                  //e.printStackTrace();
                                  Log.i("OUT","Null slicing");
                              }





                          }




		            } catch (JSONException e) {
						e.printStackTrace();
						Log.i("CONNECTION","Invalid JSON");

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

                                Log.i("OUT","Timeout expired, reconnecting to " + p.getAddress());

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
		
		//Log.i("FLAGSSS",flags.toString());
		
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

            Log.i("Slicer","Slice done received for " + payload.getString("stl"));

            //Search for files waiting for slice
            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last")!=null)
            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last").equals( payload.getString("stl")))
            {

                Log.i("Slicer","Changed PREFERENCE [Last]: " + payload.getString("gcode"));
                DatabaseController.handlePreference(DatabaseController.TAG_SLICING,"Last",payload.getString("gcode"), true);

                ViewerMainFragment.showProgressBar(StateUtils.SLICER_DOWNLOAD, 0);

                OctoprintSlicing.getMetadata(url, payload.getString("gcode"));
                OctoprintFiles.downloadFile(context, url + HttpUtils.URL_DOWNLOAD_FILES,
                LibraryController.getParentFolder() + "/temp/", payload.getString("gcode"));
                OctoprintFiles.deleteFile(context,url,payload.getString("stl"), "/local/");

            }else {

                Log.i("Slicer","Slicing NOPE for me!");

            }

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}

    //External method to convert seconds to HHmmss
    public static String ConvertSecondToHHMMString(String secondtTime) {
        String time = "--:--:--";

        if (!secondtTime.equals("null")) {

            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
            df.setTimeZone(tz);
            time = df.format(new Date(Integer.parseInt(secondtTime) * 1000L));
        }


        return time;

    }

}
