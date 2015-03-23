package android.app.printerapp.octoprint;

import android.app.Dialog;
import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * This class will issue commands to the server. Mainly control commands like Start, Pause and Cancel.
 * Also will control print head jog and extruder.
 * @author alberto-baeza
 *
 */
public class OctoprintControl {
		

	/**
	 * Send a command to the server to start/pause/stop a job
	 * @param context
	 * @param url
	 * @param command
	 */
	public static void sendCommand(Context context, String url, String command){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", command);
			entity = new StringEntity(object.toString(), "UTF-8");
            entity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}

        //Get progress dialog UI
        View waitingForServiceDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_progress_content_horizontal, null);
        ((TextView) waitingForServiceDialogView.findViewById(R.id.progress_dialog_text)).setText(R.string.devices_configure_waiting);

        //Show progress dialog
        MaterialDialog.Builder connectionDialogBuilder = new MaterialDialog.Builder(context);
        connectionDialogBuilder.customView(waitingForServiceDialogView, true)
                .autoDismiss(false);

        final Dialog connectionDialog = connectionDialogBuilder.build();
        connectionDialog.show();

		
		HttpClientHandler.post(context,url + HttpUtils.URL_CONTROL, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
						//Dismiss progress dialog
                connectionDialog.dismiss();
					}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);

				//Dismiss progress dialog
                connectionDialog.dismiss();
                MainActivity.showDialog(responseString);
			}
		});
		
	
	}
	
	/**
	 * Send a printer head command to jog or move home
	 * @param context
	 * @param url
	 * @param command
	 * @param axis
	 * @param amount
	 */
	public static void sendHeadCommand(Context context, String url, String command, String axis, double amount){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;

		try {
			
			object.put("command", command);
			if (command.equals("home")){
				
				//Must be array list to be able to convert a JSONArray in API < 19
				ArrayList<String> s = new ArrayList<String>();
				if (axis.equals("xy")){
                    s.add("x");
                    s.add("y");
                }

                if (axis.equals("z"))s.add("z");
				
				
				object.put("axes", new JSONArray(s));
				
			} else object.put(axis,amount);
			
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}

		
		HttpClientHandler.post(context,url + HttpUtils.URL_PRINTHEAD, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
					public void onProgress(int bytesWritten, int totalSize) {
					}
			
			@Override
					public void onSuccess(int statusCode, Header[] headers,
							JSONObject response) {
						super.onSuccess(statusCode, headers, response);
						
					}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {

				super.onFailure(statusCode, headers, responseString, throwable);
                MainActivity.showDialog(responseString);
			}
		});
		
	}

    public static void sendToolCommand(Context context, String url, String command, String tool, double amount){

        JSONObject object = new JSONObject();
        StringEntity entity = null;
        String destinationUrl = HttpUtils.URL_TOOL;

        try {

            object.put("command", command);

            JSONObject json = new JSONObject();


            if (tool!=null){

                if (tool.equals("bed")){

                    destinationUrl = HttpUtils.URL_BED;
                    object.put("target",amount);

                } else {
                    json.put(tool,amount);
                    object.put("targets", json);
                }

            } else {

                object.put("amount",amount);

            }

            entity = new StringEntity(object.toString(), "UTF-8");

        } catch (JSONException e) {		e.printStackTrace();
        } catch (UnsupportedEncodingException e) {	e.printStackTrace();
        }


        Log.i("TOOL", "Sending: " + object.toString());
        HttpClientHandler.post(context,url + destinationUrl,
                entity, "application/json", new JsonHttpResponseHandler(){

                    @Override
                    public void onProgress(int bytesWritten, int totalSize) {
                    }

                    @Override
                    public void onSuccess(int statusCode, Header[] headers,
                                          JSONObject response) {
                        super.onSuccess(statusCode, headers, response);

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers,
                                          String responseString, Throwable throwable) {

                        super.onFailure(statusCode, headers, responseString, throwable);
                        MainActivity.showDialog(responseString);
                    }
                });

    }

}
