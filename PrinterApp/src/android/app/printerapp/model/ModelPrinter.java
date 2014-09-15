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

public class ModelPrinter {
	
	//Service info
	private String mName;
	private String mDisplayName;
	private String mAddress;
	private int mStatus = StateUtils.STATE_NONE;
	
	//TODO hardcoded string
	private String mMessage = "Offline";
	private String mTemperature;
	private String mTempTarget;
	
	private ArrayList<File> mFileList;
	
	//Pending job
	private ModelJob mJob;
	private boolean mJobLoaded;
	
	//Job path in case it's a local file, else null
	private String mJobPath;
	
	//Camera
	private CameraHandler mCam;
	
	//Position in grid
	private int mPosition;
	
	public ModelPrinter(String name, String address, int position){
		
		mName = name;
		mDisplayName = name;
		mAddress = address;
		mJob = new ModelJob();
		mFileList = new ArrayList<File>();
		mJobLoaded = true;
		
		//TODO: Load with db
		mJobPath = null;
		
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
	
	public String getTempTarget(){
		return mTempTarget;
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
	
	public String getDisplayName(){
		return mDisplayName;
	}
	
	public boolean getLoaded(){
		return mJobLoaded;
	}

	public String getJobPath(){
		return mJobPath;
	}
	
	/**********
	 *  Sets
	 **********/
	
	public void updatePrinter(String message, int stateCode, JSONObject status){
						
		mStatus = stateCode;
		mMessage = message;
		
		
		if (status!=null){
			
			mJob.updateJob(status);
			
			try {
				//Avoid having empty temperatures
				JSONArray temperature = status.getJSONArray("temps");
				if (temperature.length()>0) {
					mTemperature = temperature.getJSONObject(0).getJSONObject("tool0").getString("actual");
					mTempTarget = temperature.getJSONObject(0).getJSONObject("tool0").getString("target");
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
			
		}
		
		

	}
	
	public void updateFiles(File m){
		mFileList.add(m);
	}
	
	public void startUpdate(Context context){
		//Initialize web socket connection
		OctoprintConnection.getConnection(context, this);
		OctoprintConnection.getSettings(this,context);
	}
	
	public void setConnecting(){
		mStatus = StateUtils.STATE_CONNECTING;
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
		startUpdate(context);
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
	
	public void setDisplayName(String name){
		mDisplayName = name;
	}
	
	public void setLoaded(boolean load){
		mJobLoaded = load;
	}
	
	public void setJobPath(String path){
		mJobPath = path;
	}

}
