package android.app.printerapp.octoprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

public class OctoprintFiles {
	
	/**
	 * Get the whole filelist from the server.
	 */
	public static void getFiles(final ModelPrinter p){
				
		HttpClientHandler.get(p.getAddress() + HttpUtils.URL_FILES, null, new JsonHttpResponseHandler(){
			
		//Override onProgress because it's faulty
		@Override
		public void onProgress(int bytesWritten, int totalSize) {						
		}
			
		@Override
		public void onSuccess(int statusCode, Header[] headers,
				JSONObject response) {
			super.onSuccess(statusCode, headers, response);
			
					
			try {
				
				JSONArray json = response.getJSONArray("files");				
  
				 if (json != null) { 
					 
				    for (int i=0;i<json.length();i++){ 
				    	
				    //Retrieve every file	
				     JSONObject object = json.getJSONObject(i);
				     
				     
				     File m;
				     
				     //If it has an origin we need to set it for the printer
				     if (!object.getString("origin").equals("local")){
				    	 
				    	//Set the storage to sd
				    	 m = new File("sd/" +object.getString("name"));
				     }
				     else   {
				    	 
				    	//Set the storage to Witbox
				    	 m = new File("witbox/" +object.getString("name"));
					     
				     }
				     
				     //Add to storage file list
				     p.updateFiles(m);
				     //TODO NOPE!
				     //StorageController.addToList(m);
				     
				     Log.i("FILES","Adding " + object.getString("name"));
				    } 
				 } 

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		
		});
		
		
	}
	
	public static void fileCommand(Context context, String url, String filename, String target){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", "select");
			entity = new StringEntity(object.toString(), "UTF-8");
			
		} catch (JSONException e) {		e.printStackTrace();
		} catch (UnsupportedEncodingException e) {	e.printStackTrace();
		}
				

		HttpClientHandler.post(context,url + HttpUtils.URL_FILES + target + filename, 
				entity, "application/json", new JsonHttpResponseHandler(){
			
			@Override
			public void onProgress(int bytesWritten,
					int totalSize) {
			}
			
			@Override
			public void onSuccess(int statusCode,
					Header[] headers, JSONObject response) {
				super.onSuccess(statusCode, headers, response);
				Log.i("OUT","Upload successful");
			}
		});
						
		
		
	}
	
	/**
	 * Upload a new file to the server using the new API. 
	 * TODO: Need to patch this later since I don't know how to send Load commands in the multipart form
	 * 
	 * Right now it uses two requests, the first to upload the file and another one to load it in the printer.
	 * @param file
	 */
	public static void uploadFile(final Context context, final File file, final ModelPrinter p){
			
			RequestParams params = new RequestParams();
			
			//if it's a local file
			p.setJobPath(file.getAbsolutePath());
			
			try {
				//TODO fix
				//params.put("select", true);
				params.put("file", file);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} 

    		Toast.makeText(context, p.getDisplayName() + ": " + context.getString(R.string.devices_text_loading) + " " + file.getName() 
    				, Toast.LENGTH_LONG).show();
			
			p.setLoaded(false);
			
			HttpClientHandler.post(p.getAddress() + HttpUtils.URL_FILES + "/local", 
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
					p.setLoaded(true);
					fileCommand(context, p.getAddress(), file.getName(), "/local/");
					
					Toast.makeText(context, p.getDisplayName() + ": " + context.getString(R.string.devices_toast_upload_1) + file.getName(), Toast.LENGTH_LONG).show();
									
					
				}
				
				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
					// TODO Auto-generated method stub
					super.onFailure(statusCode, headers, responseString, throwable);
					p.setLoaded(true);
					Log.i("RESPONSEFAIL", responseString);
					
					Toast.makeText(context, p.getDisplayName() + ": " + context.getString(R.string.devices_toast_upload_2) + file.getName(), Toast.LENGTH_LONG).show();
					
				}
				
			});	
       
		
	}
	

}
