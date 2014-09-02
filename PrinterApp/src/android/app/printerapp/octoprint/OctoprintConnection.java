package android.app.printerapp.octoprint;

import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.StateUtils;
import android.app.printerapp.model.ModelPrinter;
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
	
	//OCTOPRINT SERVER LISTENING PORT
	private static final String CUSTOM_PORT = ":5000";
	
	
	//Websockets
	
	private static final String GET_SOCK = CUSTOM_PORT + "/sockjs/websocket";
	
	//Old api url
	private static final String POST_CONNECTION = CUSTOM_PORT + "/ajax/control/connection";
	
	/**
	 * Works on the OLD API.
	 * Post parameters to handle connection. JSON for the new API is made 
	 * but never used.
	 * 
	 * Simulates POST /api/connection on the NEW API.
	 */
	public static void startConnection(String url){
				
		RequestParams params = new RequestParams();
		params.put("command", "connect"); //Send a connection request
		
		
		HttpClientHandler.post(url + POST_CONNECTION, params, new JsonHttpResponseHandler(){				

			//Override onProgress because it's faulty
			@Override
			public void onProgress(int bytesWritten, int totalSize) {						
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
	public static void getSettings(final ModelPrinter p){
		
		//Web socket URI
		final String wsuri = "ws:/" + p.getAddress() + GET_SOCK;
		 
		   try {
			   
			  WebSocketConnection mConnection = new WebSocketConnection();
			   
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
		            	
		            	
		            	//Auto-connect if it's online, TODO: should be on server-side maybe
						if (p.getStatus()==StateUtils.STATE_NONE){
							
							Log.i("OUT","CONNECTING");
							startConnection(p.getAddress());
							
						}
		            	
		            	//Get the json string for "current" status
		            	JSONObject response = new JSONObject(payload).getJSONObject("current");
		            	
						//Update job with current status
						p.updatePrinter(response);
						
						ItemListActivity.notifyAdapters();

					} catch (JSONException e) {
						Log.i("CONNECTION","Invalid JSON");
					}	
		        
  
		         }
		 
		         @Override
		         public void onClose(int code, String reason) {
		            Log.i("SOCK", "Connection lost at " + code + " because " + reason);
		         }
		      });
		   } catch (WebSocketException e) {
		 
		      Log.i("SOCK", e.toString());
		   }

	}
}
