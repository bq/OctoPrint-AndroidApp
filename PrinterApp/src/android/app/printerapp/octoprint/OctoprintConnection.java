package android.app.printerapp.octoprint;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.devices.DevicesFragment;
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
	private static WebSocketConnection mConnection = new WebSocketConnection();
	private static final String GET_SOCK = CUSTOM_PORT + "/sockjs/websocket";
	
	
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
			   //mConnection is a new websocket connection
		      mConnection.connect(wsuri, new WebSocketHandler() {
		 
		    	  //When the websocket opens
		         @Override
		         public void onOpen() {
		            Log.i("SOCK", "Status: Connected to " + wsuri);
		         }
		 
		         
		         //On message received
		         @Override
		         public void onTextMessage(String payload) {
		            Log.i("SOCK", "Got echo: " + payload);
		            
		            try {
		            	//Get the json string for "current" status
		            	JSONObject response = new JSONObject(payload).getJSONObject("current");
		            	
		            	//Send "state" to the updater
						JSONObject jsonState = response.getJSONObject("state");		
						Log.i("OUT",jsonState.toString());
						
						//Update job with current status
						p.getJob().updateJob(response);
						
						DevicesFragment.notifyAdapter();
						
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
