package android.app.printerapp.octoprint;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.ItemListActivity;
import android.content.Context;
import android.util.Log;

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
				

		HttpClientHandler.post(context,url + HttpUtils.URL_CONTROL, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);

				
				Log.i("OUT",responseString);
				ItemListActivity.showDialog(responseString);
			}
		});
		
	
	}

}
