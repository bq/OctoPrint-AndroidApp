package android.app.printerapp.octoprint;

import android.content.Context;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by alberto-baeza on 12/9/14.
 */
public class OctoprintProfiles {

    public static void uploadProfile(final Context context, final String url, final JSONObject profile){

        StringEntity entity = null;

        try {

            profile.put("current",true);
            entity = new StringEntity(profile.toString(), "UTF-8");
            Log.i("OUT", profile.toString());

        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        HttpClientHandler.post(context,url + HttpUtils.URL_PROFILES,
                entity, "application/json", new JsonHttpResponseHandler(){

                    @Override
                    public void onProgress(int bytesWritten,
                                           int totalSize) {
                    }

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.i("OUT", "Profile Upload successful");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("OUT", "Errorcico " + responseString);
                    }
                });



    }

    public static void selectProfile(final Context context, final String url, final String profile){

        JSONObject object = new JSONObject();
        StringEntity entity = null;

        try {
            object.put("current", true);
            entity = new StringEntity(object.toString(), "UTF-8");

        } catch (JSONException e) {		e.printStackTrace();
        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
        }


        HttpClientHandler.patch(context,url + HttpUtils.URL_PROFILES + "/" + "bq_witbox",
                entity, "application/json", new JsonHttpResponseHandler(){

                    @Override
                    public void onProgress(int bytesWritten,
                                           int totalSize) {
                    }

                    @Override
                    public void onSuccess(int statusCode,
                                          Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Log.i("OUT", "Profile Select successful");
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("OUT", "Errorcito " + responseString);
                    }
                });


    }


}
