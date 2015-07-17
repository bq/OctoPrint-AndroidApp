package android.app.printerapp.octoprint;

import android.app.DownloadManager;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.apache.http.entity.StringEntity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

public class OctoprintFiles {
	
	/**
	 * Get the whole filelist from the server.
	 */
	public static void getFiles(final Context context , final ModelPrinter p, final File fileUpload){

        if (fileUpload!=null){ //TODO emulating fileUpload

            p.setLoaded(false);

            //if it's a local file
            p.setJobPath(fileUpload.getAbsolutePath());
            DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES, p.getName(), p.getJobPath(), true);


        }
				
		HttpClientHandler.get(p.getAddress() + HttpUtils.URL_FILES, null, new JsonHttpResponseHandler(){
			
		//Override onProgress because it's faulty
		@Override
		public void onProgress(int bytesWritten, int totalSize) {						
		}
			
		@Override
		public void onSuccess(int statusCode, Header[] headers,
				JSONObject response) {
			super.onSuccess(statusCode, headers, response);

            p.getFiles().clear();
					
			try {
				
				JSONArray json = response.getJSONArray("files");				
  
				 if (json != null) {

                     if (fileUpload == null){

                         for (int i=0;i<json.length();i++){

                             //Retrieve every file
                             JSONObject object = json.getJSONObject(i);

                             //TODO check pending files


                             File m = null;


                             //If it has an origin we need to set it for the printer
                             if (object.getString("origin").equals("sdcard")){

                                 //Set the storage to sd
                                 m = new File("sd/" +object.getString("name"));
                                 if (!m.getParent().equals("sd")) m = null;
                             }
                             else   {

                                 if (LibraryController.hasExtension(0,object.getString("name"))){

                                     if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last")!=null){

                                         if (DatabaseController.getPreference(DatabaseController.TAG_SLICING,"Last").equals(object.getString("name"))){

                                             if (object.has("links")){
                                                 DatabaseController.handlePreference(DatabaseController.TAG_SLICING,"Last","temp.gco", true);

                                                 OctoprintFiles.downloadFile(context, p.getAddress() + HttpUtils.URL_DOWNLOAD_FILES,
                                                         LibraryController.getParentFolder() + "/temp/", "temp.gco");
                                                 OctoprintFiles.deleteFile(context,p.getAddress(),object.getString("name"), "/local/");

                                             } else {

                                             }

                                         }

                                     }


                                 }else if (LibraryController.hasExtension(1,object.getString("name"))) {

                                     //Set the storage to Witbox
                                     m = new File("local/" +object.getString("name"));
                                 }



                             }

                             //Add to storage file list
                             if (m!=null) p.updateFiles(m);



                         }


                     } else {

                         String hash = LibraryController.calculateHash(fileUpload);
                         int found = -1;

                         for (int i=0;i<json.length();i++) {

                             //Retrieve every file
                             JSONObject object = json.getJSONObject(i);

                             if (object.getString("origin").equals("local"))
                             if (object.getString("hash").equals(hash)) {

                                 Log.i("Slicer", "File found with hash " + object.getString("hash"));
                                 found = i;

                             }

                         }

                         if (found!= -1) fileCommand(context, p.getAddress(), json.getJSONObject(found).getString("name"), "/local/", false, true);
                         else uploadFile(context, fileUpload, p);
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
	public static void fileCommand(final Context context, final String url, final String filename, final String target, final boolean delete, final boolean print){
		
		JSONObject object = new JSONObject();
		StringEntity entity = null;
		
		try {
			object.put("command", "select");
            if (print)object.put("print", "true");
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
				Log.i("OUT", "Command successful");
				
				if (delete){
					
					deleteFile(context, url, filename, target);
					
				}
			}


                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);

                        Log.i("OUT", "Not found on local, trying sd");

                        JSONObject object = new JSONObject();
                        StringEntity entity = null;

                        try {
                            object.put("command", "select");
                            if (print)object.put("print", "true");
                            entity = new StringEntity(object.toString(), "UTF-8");

                        } catch (JSONException e) {		e.printStackTrace();
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }


                        HttpClientHandler.post(context, url + HttpUtils.URL_FILES + "/sdcard/" + filename,
                                entity, "application/json", new JsonHttpResponseHandler() {

                                    @Override
                                    public void onProgress(int bytesWritten,
                                                           int totalSize) {
                                    }

                                    @Override
                                    public void onSuccess(int statusCode,
                                                          Header[] headers, JSONObject response) {
                                        super.onSuccess(statusCode, headers, response);
                                        Log.i("OUT", "Command successful");

                                    }

                                });
                    }
                });
						
		
		
	}
	
	/**
	 * Upload a new file to the server using the new API.
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

            DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES, p.getName(), p.getJobPath(), true);

			

			
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


                    try{
                        //p.setLoaded(true);
                        fileCommand(context, p.getAddress(), file.getName(), "/local/", false, true);

                        Toast.makeText(context, p.getDisplayName() + ": " + context.getString(R.string.devices_toast_upload_1) + file.getName(), Toast.LENGTH_LONG).show();

                    } catch (IllegalArgumentException e){

                       e.printStackTrace();
                       p.setLoaded(true);
                    }

						


				}

				@Override
				public void onFailure(int statusCode, Header[] headers,
						String responseString, Throwable throwable) {
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
				

			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers,
					String responseString, Throwable throwable) {
				// TODO Auto-generated method stub
				super.onFailure(statusCode, headers, responseString, throwable);

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

        Log.i("Slicer", "Downloading " + filename);
		
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse("http:/" + url + filename));

        //hide notifications
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
	
		// in order for this if to run, you must use the android 3.2 to compile your app
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		    request.allowScanningByMediaScanner();
		    //request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		}


        //Delete duplicated files
		File extra = new File( path + filename);
        if (extra.exists()){
            extra.delete();
        }
		
		request.setDestinationUri(Uri.parse("file://" + path + filename));



		// get download service and enqueue file
		DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		manager.enqueue(request);

	}



	

}
