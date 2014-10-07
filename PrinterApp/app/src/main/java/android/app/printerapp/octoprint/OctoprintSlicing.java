package android.app.printerapp.octoprint;

import java.io.File;
import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class OctoprintSlicing {
	
	/**
	 * Send a command to the server to start/pause/stop a job
	 * @param context
	 * @param url
	 */
	public static void sendProfile(Context context, String url){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		
		try {
			object.put("displayName", "testerino");
			object.put("description", "this is just a testerino");
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
				
		//Progress dialog to notify command events
		final ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage(context.getString(R.string.devices_command_waiting));
		pd.show();

		
		HttpClientHandler.put(context,url + HttpUtils.URL_SLICING, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
						Log.i("OUT",response.toString());
						//Dismiss progress dialog
						pd.dismiss();
					}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				Log.i("OUT",responseString.toString());
				//Dismiss progress dialog
				pd.dismiss();
				ItemListActivity.showDialog(responseString);
			}
		});
		
	
	}
	
	public static void retrieveProfiles(String url){
		
		HttpClientHandler.get(url + HttpUtils.URL_SLICING_PROFILES, null, new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
						Log.i("OUT",response.toString());
					}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				Log.i("OUT",responseString.toString());
			}
		});
		
	}
	
	public static void sliceCommand(final Context context, String url, final File file, String target){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", "slice");
			object.put("slicer", "cura");
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
				

		HttpClientHandler.post(context,url + HttpUtils.URL_FILES + target + file.getName(), 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
			public void onProgress(int bytesWritten,
					int totalSize) {
			}
			
			@Override
			public void onSuccess(int statusCode,
					Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				
				Log.i("OUT","Slicing @" + response.toString());
			
				Toast.makeText(context, "Slicing...", Toast.LENGTH_LONG).show();
;
				try {
					
					DatabaseController.handlePreference("Slicing", response.getString("name"), file.getAbsolutePath(), true);

				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
				
			}
			
			
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				Log.i("OUT",responseString.toString());
			}
		});
						
		
		
	}
	

}
