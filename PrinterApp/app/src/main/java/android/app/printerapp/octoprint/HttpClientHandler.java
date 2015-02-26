package android.app.printerapp.octoprint;

import android.app.printerapp.Log;
import android.content.Context;

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
  private static final int DEFAULT_TIMEOUT = 30000;
    private static final int BIG_TIMEOUT = 70000;

  
  //GET method for both APIs
  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
	  generateAsyncHttpClient(url).get(getAbsoluteUrl(url), params, responseHandler);

  }

    //GET method for synchronous calls
  public static void sync_get(String url, RequestParams params, ResponseHandlerInterface responseHandler){

      SyncHttpClient sync_client = new SyncHttpClient();
      sync_client.addHeader("X-Api-Key", HttpUtils.getApiKey(url));
      sync_client.get(getAbsoluteUrl(url), params, responseHandler);
  }

  //POST method for multipart forms
  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler){
      AsyncHttpClient client = new AsyncHttpClient();
      client.addHeader("X-Api-Key", HttpUtils.getApiKey(url));
      client.setTimeout(BIG_TIMEOUT);
      client.setResponseTimeout(BIG_TIMEOUT);
      client.post(getAbsoluteUrl(url), params, responseHandler);
  }

  //POST method for the new API
  public static void post(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
      generateAsyncHttpClient(url).post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }

    //POST method for synchronous calls
    public static void sync_post(Context context, String url, HttpEntity entity, String contentType, ResponseHandlerInterface responseHandler) {
       // sync_client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }
  
  //PUT method for the new API
  public static void put(Context context, String url, HttpEntity entity, String contentType, AsyncHttpResponseHandler responseHandler) {
      generateAsyncHttpClient(url).put(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
  }

        //TODO Temporal patch method until it's implemented on the AsyncHttpClient library
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
            HttpPatch httpPatch = new HttpPatch(new URI(getAbsoluteUrl( url) ));
            httpPatch.addHeader("Content-Type", "application/json");
            httpPatch.addHeader("X-Api-Key", HttpUtils.getApiKey(url));
            httpPatch.setEntity(entity);
            response = httpClient.execute(httpPatch);

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
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //client.post(context, getAbsoluteUrl(url), entity, contentType, responseHandler);
    }
  
  //DELETE method
  public static void delete(Context context, String url, AsyncHttpResponseHandler responseHandler) throws IllegalArgumentException{
      generateAsyncHttpClient(url).delete(context, getAbsoluteUrl(url), responseHandler);
  }
  

  private static String getAbsoluteUrl(String relativeUrl) {


      Log.i("Connection", BASE_URL + relativeUrl + "?apikey=" + HttpUtils.getApiKey(relativeUrl));
      return BASE_URL + relativeUrl;
  }

    /**
     * Generate a client for this session
     * @param relativeUrl
     * @return
     */

    private static AsyncHttpClient generateAsyncHttpClient(String relativeUrl){

        AsyncHttpClient client = new AsyncHttpClient();
        client.addHeader("Content-Type", "application/json");

        if (!relativeUrl.contains(HttpUtils.URL_AUTHENTICATION)){
            client.addHeader("X-Api-Key", HttpUtils.getApiKey(relativeUrl));
        }

        client.setTimeout(DEFAULT_TIMEOUT);
        client.setResponseTimeout(DEFAULT_TIMEOUT);

        return client;

    }


}
