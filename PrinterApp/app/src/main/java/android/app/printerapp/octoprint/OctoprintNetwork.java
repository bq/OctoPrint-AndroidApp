package android.app.printerapp.octoprint;

import java.io.UnsupportedEncodingException;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.devices.discovery.PrintNetworkManager;
import android.app.printerapp.devices.discovery.PrintNetworkReceiver;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;

public class OctoprintNetwork {
	
	/**
	 * Obtain the network list available to the server to configure one
	 * TODO Somehow there are a lot of calls instead of one
	 * @param controller
	 * @param url current printer
	 */
	public static void getNetworkList(final PrintNetworkManager controller, final String url){
		
		HttpClientHandler.get(url + HttpUtils.URL_NETWORK, null, new JsonHttpResponseHandler(){
			
			@Override
			public void onProgress(int bytesWritten, int totalSize) {

			}
						
			@Override
			public void onSuccess(int statusCode, Header[] headers,
					JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				
				//Send the network list to the Network manager
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
	public static void configureNetwork(final PrintNetworkReceiver pr, final Context context, String ssid, String psk, String url){

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
                pr.register();
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);

                pr.register();
				Log.i("OUT",responseString);

			}

			

			@Override
			public void onFailure(int statusCode, Header[] headers,
					Throwable throwable, JSONObject errorResponse) {
				
				super.onFailure(statusCode, headers, throwable, errorResponse);
                pr.register();
				Log.i("OUT","Failure while connecting " + statusCode);

                Toast.makeText(context,"There was an error configuring the Network",Toast.LENGTH_LONG).show();
            }
		});
	}

}
