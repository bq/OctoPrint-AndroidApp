package android.app.printerapp.octoprint;

import android.app.ProgressDialog;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * This class will issue commands to the server. Mainly control commands like Start, Pause and Cancel.
 * Also will control print head jog and extruder.
 * @author alberto-baeza
 *
 */
public class OctoprintControl {
		

	/**
	 * Send a command to the server to start/pause/stop a job
	 * @param context
	 * @param url
	 * @param command
	 */
	public static void sendCommand(Context context, String url, String command){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", command);
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
				
		//Progress dialog to notify command events
		final ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage(context.getString(R.string.devices_command_waiting));
		pd.show();

		
		HttpClientHandler.post(context,url + HttpUtils.URL_CONTROL, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
						//Dismiss progress dialog
						pd.dismiss();
					}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);

				//Dismiss progress dialog
				pd.dismiss();
                MainActivity.showDialog(responseString);
			}
		});
		
	
	}
	
	/**
	 * Send a printer head command to jog or move home
	 * @param context
	 * @param url
	 * @param command
	 * @param axis
	 * @param amount
	 */
	public static void sendHeadCommand(Context context, String url, String command, String axis, int amount){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			
			object.put("command", command);
			if (command.equals("home")){
				
				//Must be array list to be able to convert a JSONArray in API < 19
				ArrayList<String> s = new ArrayList<String>();
				s.add("x");
				s.add("y");
				s.add("z");
				
				
				object.put("axes", new JSONArray(s));
				
			} else object.put(axis,amount);
			
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}

		
		HttpClientHandler.post(context,url + HttpUtils.URL_PRINTHEAD, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
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
                MainActivity.showDialog(responseString);
			}
		});
		
	}

}
