package android.app.printerapp.octoprint;

import java.io.File;
import java.io.FileNotFoundException;

import org.apache.http.Header;
import org.json.JSONObject;

import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Class used for Load and Print services on Octoprint's API. Right now it works on the new API,
 * but since we're using REquestParams, the API_KEY is hardcoded.
 * @author alberto-baeza
 *
 */
public class OctoprintLoadAndPrint {
	
	//OCTOPRINT SERVER LISTENING PORT
	private static final String CUSTOM_PORT = ":5000";
	
	//New api url
	private static final String POST_LOAD = CUSTOM_PORT + "/api/load";

	//Old api url
	private static final String POST_FILE = CUSTOM_PORT + "/ajax/gcodefiles/load";
	
	/**
	 * Upload a new file to the server using the new API. 
	 * @param file
	 */
	public static void uploadFile(String url, File file, boolean flag){
				

		RequestParams params = new RequestParams();
		
		try {
			
			//TODO: Change HttpClientHandler to handle POST API keys instead of hardcoding them here
			params.put("apikey", "5A41D8EC149F406F9F222DCF93304B43");
			params.put("file",file);
			
			//If Load & Print is enabled
			if (flag){
				//booleans must be strings for the server to parse
				params.put("print", "true");
				Log.i("PRINT", "Print true");
			} else {
				params.put("print", "false");
				Log.i("PRINT", "Print false");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
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
				
				Log.i("out", responseString);
			}
		});
		
	}
	
	/**
	 * Select an internal file to print with the Old API
	 * @param url
	 * @param file
	 * @param flag
	 * @param sd
	 */
	public static void printInternalFile(String url, String file, boolean flag, boolean sd){
		
		RequestParams params = new RequestParams();
			
		params.put("filename",file);
		
		//If it's sd, we set the param
		if (sd) params.put("target", "sd");
		
		//If Load & Print is enabled
		if (flag){
			//booleans must be strings for the server to parse
			params.put("print", "true");
		} else {
			params.put("print", "false");;
		}

		
		
		HttpClientHandler.post(url + POST_FILE, params, new JsonHttpResponseHandler(){
			
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
				
				Log.i("out", responseString);
			}
		});
		
	}

}
