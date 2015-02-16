package android.app.printerapp.octoprint;

import android.app.printerapp.Log;
import android.content.Context;

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


    /**
     * Uploads a profile and then connects to the server with that profile on the specified port
     * @param context
     * @param url server address
     * @param profile printer profile selected
     * @param port preferred port to connect
     */
    public static void uploadProfile(final Context context, final String url, final JSONObject profile, final String port){

        StringEntity entity = null;
        String id = null;

        try {

            JSONObject finalProfile = new JSONObject();;

            finalProfile.put("profile",profile);

            entity = new StringEntity(finalProfile.toString(), "UTF-8");
            Log.i("OUT", "Profile to add:" + finalProfile.toString());

            id = profile.getString("id");

        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        final String finalId = id;
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

                        OctoprintConnection.startConnection(url, context, port, finalId);


                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);                    }
                });



    }

    /**
     * Delete profiles from the server
     * @param context
     * @param url
     * @param profile
     */
    public static void deleteProfile(final Context context, final String url, final String profile){

        HttpClientHandler.delete(context, url + HttpUtils.URL_PROFILES + "/" + profile, new JsonHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }
        });



    }

    /**
     * Delete profiles from the server
     * @param context
     * @param url
     * @param profile
     */
    public static void updateProfile(final Context context, final String url, final String profile){

        StringEntity entity = null;
        try {

            JSONObject finalProfile = new JSONObject();
            JSONObject settings = new JSONObject();
            settings.put("default",true);

            finalProfile.put("profile",settings);

            entity = new StringEntity(finalProfile.toString(), "UTF-8");
            Log.i("OUT", "Profile to add:" + finalProfile.toString());

        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        HttpClientHandler.patch(context,url + HttpUtils.URL_PROFILES + "/" + profile,
                entity, "application/json", new JsonHttpResponseHandler(){


            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);



                Log.i("OUT", "Profile PATCH  successful");


            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.i("OUT", "Profile PATCH  FOESNT EXIST");
            }
        });



    }

}
