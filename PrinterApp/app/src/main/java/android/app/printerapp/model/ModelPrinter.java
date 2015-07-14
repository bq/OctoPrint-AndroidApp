package android.app.printerapp.model;

import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.camera.CameraHandler;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

public class ModelPrinter {

    //Id for database interaction
    private long mId;

    private int mPrinterType;
    private String mPrinterProfile = null;

	//Service info
	private String mName;
	private String mDisplayName;
    private int mDisplayColor = 0;
	private String mAddress;
	private int mStatus = StateUtils.STATE_NONE;
    private String mPort;
    private String mNetwork;
    private String mWebcamAddress;
	
	//TODO hardcoded string
	private String mMessage = "Offline";
	private String mTemperature;
	private String mTempTarget;
    private String mBedTemperature;
    private String mBedTempTarget;
	
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

    //TODO temporary profile handling
    private ArrayList<JSONObject> mProfiles;
	
	public ModelPrinter(String name, String address, int type){
		
		mName = name;
		mDisplayName = name;
		mAddress = address;
		mJob = new ModelJob();
		mFileList = new ArrayList<File>();
        mProfiles = new ArrayList<JSONObject>();
		mJobLoaded = true;
		
		//TODO: Load with db
		mJobPath = null;
		
		//Set new position according to the position in the DB, or the first available
		//if ((Integer.valueOf(position)==null)) mPosition = DevicesListController.searchAvailablePosition();
		//else mPosition = position;

        mPosition = DevicesListController.searchAvailablePosition();

        //TODO predefine network types

        switch (type){

            case StateUtils.STATE_ADHOC:
            case StateUtils.STATE_NEW: mStatus = type; break;

            default: mStatus = StateUtils.STATE_NONE; break;

        }

        mPrinterType = type;

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

    public String getWebcamAddress() { return mWebcamAddress;    }
	
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

    public String getBedTemperature() {
        return mBedTemperature;
    }

    public String getBedTempTarget() {
        return mBedTempTarget;
    }
	
	public ArrayList<File> getFiles(){
		return mFileList;
	}

	public int getPosition(){
		return mPosition;
	}
	
	public String getDisplayName(){
		return mDisplayName;
	}
    public int getDisplayColor() { return mDisplayColor; }
	
	public boolean getLoaded(){
		return mJobLoaded;
	}

	public String getJobPath(){
		return mJobPath;
	}

    public ArrayList<JSONObject> getProfiles() { return mProfiles; }

    public long getId() { return mId; }

    public String getPort() { return mPort; }

    public int getType() { return mPrinterType; }
    public String getProfile() { return mPrinterProfile; }
    public String getNetwork() { return mNetwork; }

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

                    mBedTemperature = temperature.getJSONObject(0).getJSONObject("bed").getString("actual");
                    mBedTempTarget = temperature.getJSONObject(0).getJSONObject("bed").getString("target");
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
		//OctoprintConnection.getNewConnection(context, this, false);
        mStatus = StateUtils.STATE_NONE;
		OctoprintConnection.openSocket(this, context);
	}
	
	public void setConnecting(){
		mStatus = StateUtils.STATE_NONE;
	}
	
	/*public void setNotConfigured(){
		mStatus = StateUtils.STATE_ADHOC;
		mMessage = "Not configured";
	}*/
	
	/*public void setNotLinked(){
		mStatus = StateUtils.STATE_NEW;
		mMessage = "New";
	}*/
	
	/*public void setLinked(Context context){
		//mStatus = StateUtils.STATE_NONE;
		//mMessage = "";
		startUpdate(context);
		mCam = new CameraHandler(context,mAddress);
		
	}*/
	
	//Set video stream from the camera
/*	public void setVideoStream(Context context){
		mCam = new CameraHandler(context,mAddress);
	}*/
	
	//change position
	public void setPosition(int pos){

        if ((Integer.valueOf(pos)==null)) mPosition = DevicesListController.searchAvailablePosition();
		else mPosition = pos;

        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_POSITION, getId(), String.valueOf(mPosition));
	}
	
	public void setDisplayName(String name){
		mDisplayName = name;
	}
    public void setDisplayColor(int color) { mDisplayColor = color; }
	
	public void setLoaded(boolean load){
		mJobLoaded = load;
	}
	
	public void setJobPath(String path){
		mJobPath = path;
	}

    public void setId(long id) { mId = id; }

    public void setPort(String port) { mPort = port; }

    public void setNetwork (String network) { mNetwork = network; }

    public void setType(int type, String profile) {
        mPrinterType = type;
        mPrinterProfile = profile;
    }

    public void setWebcamAddress(String address){
        mWebcamAddress = address;
    }

}
