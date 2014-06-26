package android.app.printerapp.octoprint;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.util.Log;

import com.loopj.android.http.JsonHttpResponseHandler;

public class OctoprintFiles {
	
	//OCTOPRINT SERVER LISTENING PORT
	private static final String CUSTOM_PORT = ":5000";
	
	//Old api url
	private static final String GET_FILES = CUSTOM_PORT + "/ajax/gcodefiles";
		
	
	/**
	 * Get the whole filelist from the server.
	 */
	public static void getFiles(final ModelPrinter p){
				
		HttpClientHandler.get(p.getAddress() + GET_FILES, null, new JsonHttpResponseHandler(){
			
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
				     
				     
				     ModelFile m;
				     
				     //If it has an origin we need to set it for the printer
				     if (object.has("origin")){
				    	 //Set the storage to Witbox
				    	 m = new ModelFile(object.getString("name"), "sd");
				     }
				     else   {
				    	 m = new ModelFile(object.getString("name"), "Witbox");
				     }
				     m.setPathGcode(object.getString("name"));
				     //Add to storage file list
				     p.updateFiles(m);
				     
				     Log.i("FILES","Adding " + object.getString("name"));
				    } 
				 } 

				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
		
		
		});
		
		
	}
	

}
