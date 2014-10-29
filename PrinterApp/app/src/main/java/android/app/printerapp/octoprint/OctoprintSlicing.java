package android.app.printerapp.octoprint;

import android.app.ProgressDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

public class OctoprintSlicing {

    /**
     * Upload a profile to the server with custom parameters
     * @param context
     * @param p
     * @param profile
     */
	public static void sendProfile(final Context context, final ModelPrinter p, JSONObject profile){

        StringEntity entity = null;
        String key = null;

        try {
            entity = new StringEntity(profile.toString(), "UTF-8");
            key = profile.getString("key");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //Progress dialog to notify command events
		final ProgressDialog pd = new ProgressDialog(context);
		pd.setMessage(context.getString(R.string.devices_command_waiting));
		pd.show();

		
		HttpClientHandler.put(context,p.getAddress() + HttpUtils.URL_SLICING + "/" + key,
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
						Log.i("OUT",response.toString());
						//Dismiss progress dialog
						pd.dismiss();


                        //Reload profiles
                        retrieveProfiles(context,p);



            }
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				Log.i("OUT",responseString.toString());
				//Dismiss progress dialog
				pd.dismiss();
				ItemListActivity.showDialog(responseString);
			}
		});
		
	
	}


    /**
     * Method to retrieve slice profiles before sending the file to the actual printer
     *
     */
	public static void retrieveProfiles(final Context context, final ModelPrinter p){
		
		HttpClientHandler.get(p.getAddress() + HttpUtils.URL_SLICING, null, new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
                super.onSuccess(statusCode, headers, response);

                Log.i("PROFILES", response.toString());

                p.getProfiles().clear();

                Iterator<String> keys = response.keys();
                int selectedItem = 0;

                final ArrayAdapter<String> profileList = new ArrayAdapter<String>(context, android.R.layout.select_dialog_singlechoice);

                int count = 0;
                while(keys.hasNext()) {

                    String current = keys.next();

                    profileList.add(current);

                    //TODO adding profiles manually
                    //if (!p.getProfiles().contains(current)) p.getProfiles().add(current);

                    try {

                        if (response.getJSONObject(current).getBoolean("default")){
                            selectedItem = count;
                            Log.i("OUT","Selected item is " + response.getJSONObject(current).getString("key"));
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    count++;

                    HttpClientHandler.get(p.getAddress() + HttpUtils.URL_SLICING + "/" + current , null, new JsonHttpResponseHandler() {


                        @Override
                        public void onSuccess(int statusCode, Header[] headers,
                                              JSONObject response) {
                            super.onSuccess(statusCode, headers, response);


                            p.getProfiles().add(response);

                        }


                    });

                }


            }
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
				Log.i("OUT",responseString.toString());
			}
		});
		
	}


    /**
     * Send a slice command by uploading the file first and then send the command, the result
     * will be handled in the socket payload response
     * @param context
     * @param url
     * @param file
     */
	public static void sliceCommand(final Context context, final String url, final File file, final JSONObject extras){

        RequestParams params = new RequestParams();
        try {
            params.put("file", file);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        HttpClientHandler.post(url + HttpUtils.URL_FILES + "/local",
                params, new JsonHttpResponseHandler(){

                    //Override onProgress because it's faulty
                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                    }

                    //If success, the file was uploaded correctly
                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);


                        Log.i("SUCCESS", response.toString());

                        JSONObject object = extras ;
                        StringEntity entity = null;

                        try {
                            object.put("command", "slice");
                            object.put("slicer", "cura");

                            //TODO select profile

                            //object.put("profile", profile);
                            object.put("gcode", "temp.gco");
                            entity = new StringEntity(object.toString(), "UTF-8");

                            Log.i("OUT","Uploading " + object.toString());

                        } catch (JSONException e) {		e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
                        }


                        HttpClientHandler.post(context,url + HttpUtils.URL_FILES + "/local/" + file.getName(),
                                entity, "application/json", new JsonHttpResponseHandler(){

                                    @Override
                                    public void onProgress(int bytesWritten,
                                                           int totalSize) {
                                    }

                                    @Override
                                    public void onSuccess(int statusCode,
                                                          Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);


                                        Log.i("OUT","Slicing @" + response.toString());

                                        //TODO we can actually use the null field to hold the original project name
                                        DatabaseController.handlePreference("Slicing",file.getName(), null, true);

                                    }



                                    @Override
                                    public void onFailure(int statusCode, Header[] headers,
                                                          String responseString, Throwable throwable) {

                                        super.onFailure(statusCode, headers, responseString, throwable);
                                        Log.i("OUT",responseString.toString());
                                    }
                                });

                    }

                });








						
		
		
	}
	

}
