package android.app.printerapp.model;

import java.io.File;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.StateUtils;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.camera.CameraHandler;
import android.app.printerapp.devices.camera.MjpegView;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

public class ModelPrinter {
	
	//Service info
	private String mName;
	private String mAddress;
	private int mStatus = StateUtils.STATE_NONE;
	
	//TODO hardcoded string
	private String mMessage = "Offline";
	private String mTemperature;
	
	private ArrayList<File> mFileList;
	
	//Pending job
	private ModelJob mJob;
	
	//Camera
	private CameraHandler mCam;
	
	//Position in grid
	private int mPosition;
	
	public ModelPrinter(String name, String address, int position){
		
		mName = name;
		mAddress = address;
		mJob = new ModelJob();
		mFileList = new ArrayList<File>();
		
		//Set new position according to the position in the DB, or the first available
		if ((position<0) || (Integer.valueOf(position)==null)) mPosition = DevicesListController.searchAvailablePosition();
		else mPosition = position;
		
		Log.i("OUT","Creating service @"+position);
			
	}
	
	/*********
	 * Gets
	 *********/
	
	public String getName(){
		return mName;
	}
	
	public ModelJob getJob(){
		return mJob;
	}
	
	public String getAddress(){
		return mAddress;
	}
	
	public int getStatus(){
		return mStatus;
	}
	
	public String getMessage(){
		return mMessage;
	}
	
	public String getTemperature(){
		return mTemperature;
	}
	
	public ArrayList<File> getFiles(){
		return mFileList;
	}
	
	public MjpegView getVideo(){
			
		return mCam.getView();
	}
	
	public int getPosition(){
		return mPosition;
	}
	
	/**********
	 *  Sets
	 **********/
	
	public void updatePrinter(JSONObject status){
		
		JSONObject state;
		JSONArray temperature;
		try {
			state = status.getJSONObject("state");
						
			mStatus = state.getInt("state");
			
			if ((Integer.valueOf(mStatus)) == null){
				Log.i("OUT","STATE IS NULL");
				mStatus = StateUtils.STATE_NONE;
			} else Log.i("OUT","STATE IS " + mStatus);
			
			mMessage = state.getString("stateString");
			mJob.updateJob(status);
			
			temperature = status.getJSONArray("temperatures");
			mTemperature = temperature.getJSONObject(0).getString("temp");
			
		
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void updateFiles(File m){
		mFileList.add(m);
	}
	
	public void startUpdate(){
		//Initialize web socket connection
		OctoprintConnection.getSettings(this);
	}
	
	public void setNotConfigured(){
		mStatus = StateUtils.STATE_ADHOC;
		mMessage = "Not configured";
	}
	
	public void setNotLinked(){	
		mStatus = StateUtils.STATE_NEW;
		mMessage = "New";
	}
	
	public void setLinked(Context context){
		mStatus = StateUtils.STATE_NONE;
		mMessage = "";
		startUpdate();
		mCam = new CameraHandler(context,mAddress);
		
	}
	
	//Set video stream from the camera
/*	public void setVideoStream(Context context){
		mCam = new CameraHandler(context,mAddress);
	}*/
	
	//change position
	public void setPosition(int pos){	
		mPosition = pos;
	}

}
