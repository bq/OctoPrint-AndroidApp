package android.app.printerapp.octoprint;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;

import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;
import android.util.Log;

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
		
	/**
	 * 
	 * Post parameters to handle connection. JSON for the new API is made 
	 * but never used.
	 * 
	 */
	public static void startConnection(String url, Context context){
					
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		try {
			object.put("command","connect");
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
	
	/**
	 * Obtains the current state of the machine and issues new connection commands
	 * @param p
	 */
	public static void getConnection(final Context context, final ModelPrinter p){
		
		HttpClientHandler.get(p.getAddress() + HttpUtils.URL_CONNECTION, null, new JsonHttpResponseHandler(){
						
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				
				try {
					JSONObject current = response.getJSONObject("current");
										
					if (current.getString("state").contains("Closed")){
						
						Log.i("OUT", "Start connection porque " + response);
						startConnection(p.getAddress(), context);
						
					}
					
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
	 * Works on the NEW API.
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
		            OctoprintFiles.getFiles(p);
		         }
		 
		         
		         //On message received
		         @Override
		         public void onTextMessage(String payload) {
		            
		        	 Log.i("SOCK", "Got echo: " + payload);
		        	 
		        	  try {
		        		  
		        	 JSONObject object = new JSONObject(payload);
		            		          
		            	//Get the json string for "current" status
		            	if (object.has("current")){
		            		
		            		JSONObject response = new JSONObject(payload).getJSONObject("current");
			            	
							//Update job with current status
			            	//We'll add every single parameter
							p.updatePrinter(response.getJSONObject("state").getString("text"), createStatus(response.getJSONObject("state").getJSONObject("flags")),
									response);
		            	}
		            	
		            	if (object.has("event")){
		            			            		
		            		JSONObject response = new JSONObject(payload).getJSONObject("event");
		            		
		            		if (response.getString("type").equals("SlicingDone")){
		            			
		            			JSONObject slicingPayload = response.getJSONObject("payload");

		            			createSliceDialog(context, slicingPayload,p.getAddress());
		            			
		            		}
		            		
		            		
		            		
		            	//{"event": {"type": "SlicingDone", "payload": {"stl": "coin.stl", "gcode": "coin.gco", "time": 24.528998136520386}}}

		            		
		            	}
		            
												
		            } catch (JSONException e) {
						e.printStackTrace();
						Log.i("CONNECTION","Invalid JSON");
												
					}	
					
		            ItemListActivity.notifyAdapters();

					
		        
  
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
								
								 p.startUpdate(context);
								
							}
						}, 1000);
			           
		            
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
					// TODO Auto-generated catch block
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
	private static void createSliceDialog(final Context context, final JSONObject payload, final String url){
		
		
		
		try {
			
			//Search for files waiting for slice
			if (DatabaseController.isPreference("Slicing", payload.getString("gcode"))){

				final String path = DatabaseController.getPreference("Slicing", payload.getString("gcode"));
					
				AlertDialog.Builder adb = new AlertDialog.Builder(context);
				adb.setTitle("Slicing done...");
				adb.setMessage("Download: " + payload.getString("gcode") + " into project folder?");
				adb.setPositiveButton(R.string.ok, new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {

									File f = new File(path);
									
									try {
										
										OctoprintFiles.downloadFile(context, url + HttpUtils.URL_DOWNLOAD_FILES , 
												f.getParentFile().getParent() + "/_gcode/", payload.getString("gcode"));
									
									
									} catch (JSONException e) {
										e.printStackTrace();
									}
								

					}
				});
				
				adb.setNegativeButton(R.string.cancel, null);
				
				adb.show();
				
				//Delete file from preferences
				DatabaseController.handlePreference("Slicing", payload.getString("gcode"), null, false);
				
				
				
				}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
