package android.app.printerapp.octoprint;

import android.app.printerapp.Log;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.AuthenticationUtils;
import android.app.printerapp.devices.discovery.PrintNetworkManager;
import android.app.printerapp.devices.discovery.PrintNetworkReceiver;
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

public class OctoprintNetwork {
	
	/**
	 * Obtain the network list available to the server to configure one
	 * TODO MOST STUPID METHOD EVER
	 * @param controller
	 */
	public static void getNetworkList(final PrintNetworkManager controller, final ModelPrinter p){


        HttpClientHandler.sync_get(p.getAddress() + HttpUtils.URL_AUTHENTICATION, null, new JsonHttpResponseHandler()
        {

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);


                Log.i("OUT", "Posting auth");

                JSONObject object = new JSONObject();
                StringEntity entity = null;
                try {

                    object.put("appid", "com.bq.octoprint.android");
                    object.put("key",response.getString("unverifiedKey"));
                    object.put("_sig", AuthenticationUtils.signStuff(controller.getContext(), response.getString("unverifiedKey")));
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

                HttpClientHandler.post(controller.getContext(), p.getAddress() + HttpUtils.URL_AUTHENTICATION,
                        entity, "application/json", new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                                super.onSuccess(statusCode, headers, response);

                                try {

                                    DatabaseController.handlePreference(DatabaseController.TAG_KEYS, PrintNetworkManager.getNetworkId(p.getName()), response.getString("key"), true);
                                    //Log.i("Connection","Adding API key " + response.getString("key") + " for ID: " + PrintNetworkManager.getNetworkId(p.getName()));

                                    HttpClientHandler.get(p.getAddress() + HttpUtils.URL_NETWORK, null, new JsonHttpResponseHandler(){

                                        @Override
                                        public void onProgress(int bytesWritten, int totalSize) {

                                        }

                                        @Override
                                        public void onSuccess(int statusCode, Header[] headers,
                                                              JSONObject response) {
                                            super.onSuccess(statusCode, headers, response);

                                            //Send the network list to the Network manager
                                            controller.selectNetworkPrinter(response,p.getAddress());

                                        }



                                        @Override
                                        public void onFailure(int statusCode, Header[] headers,
                                                              Throwable throwable, JSONObject errorResponse) {

                                            super.onFailure(statusCode, headers, throwable, errorResponse);

                                            Log.i("Connection", "Failure while connecting " + statusCode);
                                        }

                                    });


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }


                        });

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

        Log.i("Manager", "Configure Network for: " + ssid);
		
		try {
			object.put("command", "configure_wifi");
			object.put("ssid", ssid);

			if (psk!=null) object.put("psk", psk);
            else object.put("psk", "");

			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
		
		HttpClientHandler.post(context,url + HttpUtils.URL_NETWORK,
				entity, "application/json", new JsonHttpResponseHandler(){});
	}

}
