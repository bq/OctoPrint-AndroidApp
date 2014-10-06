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

  private static AsyncHttpClient client = new AsyncHttpClient();
  
  //GET method for both APIs
  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  client.get(getAbsoluteUrl(url), params, responseHandler);

  }
  
  //POST method for multipart forms
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  client.post( getAbsoluteUrl(url), params, responseHandler);
  }

  //POST method for the new API
  public static void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
	  client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }
  
  //PUT method for the new API
  public static void put(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
	  client.put(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }
  
  //DELETE method
  public static void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
	  client.delete(context, getAbsoluteUrl(url), responseHandler);
  }
  

  private static String getAbsoluteUrl(String relativeUrl) {
	  client.addHeader("X-Api-Key", HttpUtils.API_KEY);
	  Log.i("OUT","Http resuqekjlraj " +BASE_URL + relativeUrl);
      return BASE_URL + relativeUrl;
  }

}
