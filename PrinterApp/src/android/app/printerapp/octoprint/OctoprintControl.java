package android.app.printerapp.octoprint;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.content.Context;
import com.loopj.android.http.JsonHttpResponseHandler;

/**
 * This class will issue commands to the server. Mainly control commands like Start, Pause and Cancel.
 * @author alberto-baeza
 *
 */
public class OctoprintControl {
		

	
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
				ItemListActivity.showDialog(responseString);
			}
		});
		
	
	}

}
