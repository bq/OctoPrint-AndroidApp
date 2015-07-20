package android.app.printerapp.octoprint;

import android.app.printerapp.Log;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.AuthenticationUtils;
import android.app.printerapp.devices.discovery.PrintNetworkManager;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;

import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * Created by alberto-baeza on 11/21/14.
 */
public class OctoprintAuthentication {

    private static final String API_INVALID_MSG = "Invalid app";


    /**
     * Send an authentication petition to retrieve an unverified api key
     * @param context
     * @param p target printer
     * @param retry whether we should re-display the dialog or not
     */

    public static void getAuth(final Context context, final ModelPrinter p, final boolean retry){

        HttpClientHandler.get(p.getAddress() + HttpUtils.URL_AUTHENTICATION, null, new JsonHttpResponseHandler(){

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.i("Connection", "Success! " + response.toString());

                try {
                    postAuth(context,response.getString("unverifiedKey"), p, retry);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);

                Log.i("Connection", "Failure! " + responseString);
            }
        });

    }

    /**
     * Add the verified API key to the server
     * @param context
     * @param key
     * @param p
     * @param retry
     */
    public static void postAuth(final Context context, String key, final ModelPrinter p, final boolean retry){


        Log.i("OUT", "Posting auth");

        JSONObject object = new JSONObject();
        StringEntity entity = null;
        try {

            object.put("appid", "com.bq.octoprint.android");
            object.put("key",key);
            object.put("_sig", AuthenticationUtils.signStuff(context, key));
            entity = new StringEntity(object.toString(), "UTF-8");

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        HttpClientHandler.post(context, p.getAddress() + HttpUtils.URL_AUTHENTICATION,
                entity, "application/json", new JsonHttpResponseHandler() {

                    //Override onProgress because it's faulty
                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                        try {
                            DatabaseController.handlePreference(DatabaseController.TAG_KEYS, PrintNetworkManager.getNetworkId(p.getName()),response.getString("key"),true);
                            Log.i("Connection", "Adding API key " + response.getString("key") + " for ID: " + PrintNetworkManager.getNetworkId(p.getName()));
                            //OctoprintConnection.doConnection(context,p);


                            if (!retry){

                                Log.i("CONNECTION", "Connection from: AUTH");
                                OctoprintConnection.doConnection(context,p);

                            } else OctoprintConnection.getNewConnection(context, p);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        Log.i("Connection", responseString + " for " + p.getAddress());

                        if (statusCode == 401 && responseString.contains(API_INVALID_MSG)) {

                            //Remove element and show dialog to add manually
                            DevicesListController.removeElement(p.getPosition());
                            OctoprintConnection.showApiDisabledDialog(context);
                        }


                    }
                });

    }


}
