package android.app.printerapp.octoprint;

import org.apache.http.HttpEntity;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.*;

/**
 * Static class to handle Http requests with the old API or the new one (with API_KEY)
 * @author alberto-baeza
 *
 */
public class HttpClientHandler {
	
  //Base URL to handle http requests, only needs one slash because services come with another one
  private static final String BASE_URL = "http:/";
  
  //TODO: This API key is not used yet since we're putting params on RequestParams instead of entity
  private static final String API_KEY = "5A41D8EC149F406F9F222DCF93304B43";//"59CA6531001F41298FE768EB2F2A5320";

  private static AsyncHttpClient client = new AsyncHttpClient();
  
  //GET method for both APIs
  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
      client.get(getAbsoluteUrl(url), params, responseHandler);
      
      Log.i("FILES", "LEL: " + getAbsoluteUrl(url));
  }
  
  //POST method for the old API
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  client.post( getAbsoluteUrl(url), params, responseHandler);
	  Log.i("FILES", "LEL: " + getAbsoluteUrl(url));
  }

  //POST method for the new API
  public static void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
	
	  client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl + "?apikey=" + API_KEY;
  }

}
