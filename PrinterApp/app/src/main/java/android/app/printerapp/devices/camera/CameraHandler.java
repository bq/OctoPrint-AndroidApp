package android.app.printerapp.devices.camera;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.IOException;
import java.net.URI;

/**
 * This class will handle the Camera connection.
 * The device camera will stream on MJPEG.
 * @author alberto-baeza
 *
 */
public class CameraHandler {

   private static final String STREAM_PORT = ":8080/?action=stream";
	
   //UI reference to the video view
	private MjpegView mv = null;
	
	//Boolean to check if the stream was already started
	public boolean isRunning = false;
	
   //sample public cam
   private String URL;
	
	public CameraHandler(final Context context, String address){

		
        mv = new MjpegView(context);  
        
        /**
         * This method handles the stream connection / reconnection.
         * We need to check if the stream alredy started to reconnect in case
         * of a bad initial conection (when we upgrade a service).
         * If it was running, we only need to restart the stream by setting
         * the source again.
         */
        mv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				if (!mv.isRunning()){
					
					if (!isRunning){
						new DoRead().execute(URL);
						isRunning = true;						
					}else mv.restartPlayback(context);
					
					
					
				} else Log.i("CAMERA","Is running already!");
				
				
			}
		});
        
        //Create URL
        URL = "http:/" + address.substring(0,address.lastIndexOf(':')) + STREAM_PORT;

       //Read stream
        Log.i("CAMERA","Executing " + URL);
	   
	}

    public void startVideo(){

        if (!mv.isRunning()){

            if (!isRunning) {
                new DoRead().execute(URL);
                isRunning = true;
            }

        } else Log.i("CAMERA","Is running already!");
    }

	/**
	 * This class will send a http get request to the server's stream
	 * @author alberto-baeza
	 *
	 */
	    class DoRead extends AsyncTask<String, Void, MjpegInputStream> {
	    	
	        protected MjpegInputStream doInBackground(String... url) {
	        	

	            HttpResponse res = null;
	            DefaultHttpClient httpclient = new DefaultHttpClient();     
	            
	            try {      	
	                res = httpclient.execute(new HttpGet(URI.create(url[0])));
	                
	                if(res.getStatusLine().getStatusCode()==401){
	                    return null;
	                }
	                return new MjpegInputStream(res.getEntity().getContent());  
	            } catch (ClientProtocolException e) {
	                e.printStackTrace();
	                //Error connecting to camera
	            } catch (IOException e) {
	                e.printStackTrace();
	                //Error connecting to camera
	            }

	            return null;
	        }

	        protected void onPostExecute(MjpegInputStream result) {
	        	
	        	//Returns an input stream
	            mv.setSource(result);
	            
	            
	            //Display options
	            mv.setDisplayMode(MjpegView.SIZE_BEST_FIT);
          
	            mv.showFps(true);

	        }
	    }
	    
	    public MjpegView getView(){
	    	return mv;
	    }
	}
