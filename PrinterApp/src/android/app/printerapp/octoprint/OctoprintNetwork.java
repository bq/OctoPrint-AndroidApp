package android.app.printerapp.octoprint;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.devices.discovery.PrintNetworkManagerOctoprint;
import android.content.Context;
import android.util.Log;
import com.loopj.android.http.JsonHttpResponseHandler;

public class OctoprintNetwork {
	
	/**
	 * Obtain the network list available to the server to configure one
	 * TODO Somehow there are a lot of calls instead of one
	 * @param context
	 * @param url current printer
	 */
	public static void getNetworkList(final PrintNetworkManagerOctoprint controller, final String url){
		
		HttpClientHandler.get(url + HttpUtils.URL_NETWORK, null, new JsonHttpResponseHandler(){
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {

			}
						
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				
				controller.selectNetworkPrinter(response,url);
				
				Log.i("OUT",response.toString());
				
			}
			
			

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				
				super.onFailure(statusCode, headers, throwable, errorResponse);
				
				Log.i("OUT","Failure while connecting " + statusCode);
			}
			
		});

	}
	
	
	
	
	
	
	
	
	/*******************
	 * 
	 * @param context
	 * @param ssid
	 * @param psk
	 * @param url
	 */
	public static void configureNetwork(Context context, String ssid, String psk, String url){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", "configure_wifi");
			object.put("ssid", ssid);
			object.put("psk", psk);
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
		
		HttpClientHandler.post(context,url + HttpUtils.URL_NETWORK, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
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
				
				
				Log.i("OUT",responseString);

			}

			

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				
				super.onFailure(statusCode, headers, throwable, errorResponse);
				
				Log.i("OUT","Failure while connecting " + statusCode);
			}
		});
	}

}
