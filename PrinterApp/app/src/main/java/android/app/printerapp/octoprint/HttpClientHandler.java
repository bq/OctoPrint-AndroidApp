package android.app.printerapp.octoprint;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.net.ssl.SSLContext;

/**
 * Static class to handle Http requests with the old API or the new one (with API_KEY)
 * @author alberto-baeza
 *
 */
public class HttpClientHandler {
	 
  //Base URL to handle http requests, only needs one slash because services come with another one
  private static final String BASE_URL = "http:/";

  private static AsyncHttpClient client = new AsyncHttpClient();
  private static SyncHttpClient sync_client = new SyncHttpClient();
  
  //GET method for both APIs
  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  client.get(getAbsoluteUrl(url), params, responseHandler);

  }

    //GET method for synchronous calls
  public static void sync_get(String url, RequestParams params, ResponseHandlerInterface responseHandler){
        sync_client.get(getAbsoluteUrl(url), params, responseHandler);
  }
  
  //POST method for multipart forms
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  client.post( getAbsoluteUrl(url), params, responseHandler);
  }

  //POST method for the new API
  public static void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
	  client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }

    //POST method for synchronous calls
    public static void sync_post(Context context, String url, HttpEntity entity, String contentType, ResponseHandlerInterface responseHandler) {
        sync_client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }
  
  //PUT method for the new API
  public static void put(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
	  client.put(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }

    //PUT method for the new API
    public static void patch(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {


        CloseableHttpResponse response = null;
        try {
            SSLContext sslContext = SSLContexts.createSystemDefault();
            SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
                    sslContext,
                    SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            CloseableHttpClient  httpClient= HttpClientBuilder.create()
                    .setSSLSocketFactory(sslsf)
                    .build();

            //CloseableHttpClient httpClient = HttpClients.custom().build();
            HttpPatch httpPatch = null;
            httpPatch = new HttpPatch(new URI(getAbsoluteUrl(url) + "?apikey=" + HttpUtils.getApiKey(url)));
            httpPatch.setEntity(entity);
            response = httpClient.execute(httpPatch);

            Log.i("OUT", "Dafux: " + EntityUtils.toString(response.getEntity()));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){

            e.printStackTrace();
        } finally {
            try {
                response.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }
  
  //DELETE method
  public static void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) {
	  client.delete(context, getAbsoluteUrl(url), responseHandler);
  }
  

  private static String getAbsoluteUrl(String relativeUrl) {
	  if (!relativeUrl.contains(HttpUtils.URL_AUTHENTICATION)){
          client.addHeader("X-Api-Key", HttpUtils.getApiKey(relativeUrl));
      }

      Log.i("Connection", BASE_URL + relativeUrl + "?apikey=" + HttpUtils.getApiKey(relativeUrl));
      return BASE_URL + relativeUrl;
  }





}
