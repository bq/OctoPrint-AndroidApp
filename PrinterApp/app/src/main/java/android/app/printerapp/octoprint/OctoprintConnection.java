package android.app.printerapp.octoprint;

import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
    private static final String DEFAULT_PORT = null;

	/**
	 * 
	 * Post parameters to handle connection. JSON for the new API is made 
	 * but never used.
	 * 
	 */
	private static void startConnection(String url, Context context, String port){
					
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		try {
			object.put("command","connect");
            if (port!=null) object.put("port",port);
            object.put("save", true);
			object.put("autoconnect","true");
			entity = new StringEntity(object.toString(), "UTF-8");
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		
		HttpClientHandler.post(context,url + HttpUtils.URL_CONNECTION, 
				entity, "application/json", new JsonHttpResponseHandler()
		
		/*HttpClientHandler.post(url + POST_CONNECTION, params, new JsonHttpResponseHandler()*/{				

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
	
	/**
	 * Obtains the current state of the machine and issues new connection commands
	 * @param p printer
     * @param dialog true for manual connection else automatic
	 */
	public static void getConnection(final Context context, final ModelPrinter p, final boolean dialog){
		
		HttpClientHandler.get(p.getAddress() + HttpUtils.URL_CONNECTION, null, new JsonHttpResponseHandler(){
						
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);


                //Check for current status
                JSONObject current = null;
                try {
                    current = response.getJSONObject("current");

                    Log.i("CONNECTION","CURRENT CONNECTION " + response.toString());


                    //if closed or error
                    if ((current.getString("state").contains("Closed"))
                            ||(current.getString("state").contains("Error"))
                            ) {

                        if (dialog) { //Manual connection


                            //Create dialog
                            AlertDialog.Builder adb = new AlertDialog.Builder(context);
                            adb.setTitle("Select port for " + p.getDisplayName());

                            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                            View v = inflater.inflate(R.layout.devices_connection_dialog, null);
                            final Spinner s_port = (Spinner) v.findViewById(R.id.devices_connection_dialog_spinner);


                            //Show port list
                            JSONArray ports = response.getJSONObject("options").getJSONArray("ports");

                            ArrayList<String> ports_array = new ArrayList<String>();

                            for (int i = 0; i < ports.length(); i++) {

                                ports_array.add(ports.get(i).toString());

                            }
                            ArrayAdapter<String> ports_adapter = new ArrayAdapter<String>(context,
                                    R.layout.print_panel_spinner_item, ports_array);

                            s_port.setAdapter(ports_adapter);



                            adb.setView(v);

                            adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    Log.i("CONNECTION", "START CONNECTION MANUALLY ON " + s_port.getSelectedItem().toString());

                                    //Start connection with the selected port
                                    startConnection(p.getAddress(), context, s_port.getSelectedItem().toString());
                                }
                            });

                            adb.show();


                        } else { //Automatic connection

                            Log.i("CONNECTION", "START CONNECTION AUTOMATICALLY ON " + DEFAULT_PORT);

                            //TODO default port should be per machine
                            startConnection(p.getAddress(), context, DEFAULT_PORT);

                        }

                    } else Log.i("OUT","Printer already connected");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				Log.i("OUT","Failure while connecting " + responseString);
				super.onFailure(statusCode, headers, responseString, throwable);
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
	public static void getSettings(final ModelPrinter p, final Context context){
		
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


		            Log.i("SOCK", "Status: Connected to " + wsuri);
                    getConnection(context,p,false);

                     //Get a new set of files
                    OctoprintFiles.getFiles(p);

                     //Get a new set of profiles
                     OctoprintSlicing.retrieveProfiles(context,p);


		         }
		 
		         
		         //On message received
		         @Override
		         public void onTextMessage(String payload) {
		            
		        	 Log.i("SOCK", "Got echo [" + p.getAddress() + "]: " + payload);
		        	 
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
                            //TODO we don't always receive this confirmation
		            		if (response.getString("type").equals("Upload")){
		            			
		            			p.setLoaded(true);
		            			
		            		}

		            	}

                          //update slicing progress in the print panel fragment
                          if (object.has("slicingProgress")){

                              JSONObject response = new JSONObject(payload).getJSONObject("slicingProgress");


                              //Check if it's our file
                              if (DatabaseController.isPreference("Slicing", response.getString("source_path"))){

                                  int progress = response.getInt("progress");


                                  //TODO
                                  ViewerMainFragment.showProgressBar(progress);
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

            //Search for files waiting for slice
            if (DatabaseController.isPreference("Slicing", payload.getString("stl"))) {

                OctoprintFiles.downloadFile(context, url + HttpUtils.URL_DOWNLOAD_FILES,
                LibraryController.getParentFolder() + "/temp/", payload.getString("gcode"));
                DatabaseController.handlePreference("Slicing",payload.getString("stl"),null, false);
                OctoprintFiles.deleteFile(context,url,payload.getString("stl"), "/local/");

            }else {

                Log.i("OUT","Slicing NOPE for me!");

            }

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
	}
}
