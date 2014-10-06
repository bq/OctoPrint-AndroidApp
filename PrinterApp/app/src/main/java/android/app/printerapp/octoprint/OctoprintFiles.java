package android.app.printerapp.octoprint;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.DownloadManager;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
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
				     if (object.has("origin")){
				    	 
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
		
		@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				
				super.onFailure(statusCode, headers, responseString, throwable);
				
				Log.i("OUT","PEDAZO DE FAIL " + responseString);
			}
		
		});
		
		
		
		
	}
	
	
	/**
	 * This method will send a select command to the server to load the file into the printer
	 * If a select command is sent when the file is 100% printed, the progress will reset
	 * If a delete command is also issued, the file will be unselected and then deleted from the server
	 * 
	 * @param context
	 * @param url
	 * @param filename
	 * @param target
	 * @param delete
	 */
	public static void fileCommand(final Context context, final String url, final String filename, final String target, final boolean delete){
		
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
				
				if (delete){
					
					deleteFile(context, url, filename, target);
					
				}
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
	public static void uploadFile(final Context context, final File file, final ModelPrinter p, final boolean slice){
			
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
			
			DatabaseController.handlePreference("References", p.getName(), p.getJobPath(), true);
			
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
					

					Log.i("SUCCESS", response.toString());
					
					if (slice){
						
						OctoprintSlicing.sliceCommand(context, p.getAddress(), file, "/local/");
						
						
					}else {
						
						//p.setLoaded(true);
						fileCommand(context, p.getAddress(), file.getName(), "/local/", false);
						
						Toast.makeText(context, p.getDisplayName() + ": " + context.getString(R.string.devices_toast_upload_1) + file.getName(), Toast.LENGTH_LONG).show();
							
						
					}
					
					
				
					
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
	
	/**
	 * Method to delete a file on the server remotely after it was printed. 
	 * @param context
	 * @param url
	 * @param filename
	 * @param target
	 */
	public static void deleteFile(Context context, String url, String filename, String target){
		
		HttpClientHandler.delete(context, url + HttpUtils.URL_FILES + "/local/" + filename, 
				new JsonHttpResponseHandler(){				

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

			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);
				
				Log.i("RESPONSEFAIL", responseString);

			}
			
		});	
   
		
	}
	
	/**
	 * TODO CHANGE TO BACKGROUND DOWNLOAD
	 * This method will create a Download Manager to retrieve gcode files from the server.
	 * Files will be saved in the gcode folder for the current project.
	 * 
	 * @param context
	 * @param url download reference
	 * @param path local folder to store the file
	 * @param filename
	 */
	public static void downloadFile(Context context, String url, String path, String filename){
		
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http:/" + url + filename));
	
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}
		
		
		
		request.setDestinationUri(Uri.parse("file://" + path + filename));

		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);
	}
	

}
