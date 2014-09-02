package android.app.printerapp.octoprint;

import org.apache.http.Header;
import org.json.JSONObject;

import android.app.printerapp.ItemListActivity;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * This class will issue commands to the server. Mainly control commands like Start, Pause and Cancel.
 * @author alberto-baeza
 *
 */
public class OctoprintControl {
	
	//OCTOPRINT SERVER LISTENING PORT
	private static final String CUSTOM_PORT = ":5000";
	
	//Old api url
	private static final String POST_LOAD = CUSTOM_PORT + "/ajax/control/job";
	private static final String POST_JOG = CUSTOM_PORT + "/ajax/control/jog";
	
	public static void sendCommand(String url, String command){
		
		RequestParams params = new RequestParams();

		//TODO: Change HttpClientHandler to handle POST API keys instead of hardcoding them here
		params.put("apikey", "5A41D8EC149F406F9F222DCF93304B43");
		params.put("command", command);
		
		HttpClientHandler.post(url + POST_LOAD, params, new JsonHttpResponseHandler(){
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
				
				Log.i("out",response.toString());
			};
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				
				ItemListActivity.showDialog("Server error: Please load another gcode");
			}
		});
		
	
	}
	
	public static void jogPrinterHead(String url, String axis, String dist){
		
		RequestParams params = new RequestParams();
		params.put(axis, dist);
		
		Log.i("out","Params: " + axis + " " + dist);
		
		HttpClientHandler.post(url + POST_JOG, params, new JsonHttpResponseHandler() {
			
			//Override onProgress because it's faulty
			@Override
			public void onProgress(int bytesWritten, int totalSize) {
				   			
			}
			
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);

			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				super.onFailure(statusCode, headers, responseString, throwable);
			}
			
			
		});

	}
}
