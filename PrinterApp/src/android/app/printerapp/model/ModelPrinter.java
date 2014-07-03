package android.app.printerapp.model;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.StateUtils;
import android.app.printerapp.octoprint.OctoprintConnection;

public class ModelPrinter {
	
	//Service info
	private String mName;
	private String mAddress;
	private int mStatus = StateUtils.STATE_NONE;
	private String mMessage;
	private String mTemperature;
	
	private ArrayList<ModelFile> mFileList;
	
	//Pending job
	private ModelJob mJob;
	
	public ModelPrinter(String name, String address){
		
		mName = name;
		mAddress = address;
		mJob = new ModelJob();
		mFileList = new ArrayList<ModelFile>();
			
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
	
	public ArrayList<ModelFile> getFiles(){
		return mFileList;
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
			mMessage = state.getString("stateString");
			mJob.updateJob(status);
			
			temperature = status.getJSONArray("temperatures");
			mTemperature = temperature.getJSONObject(0).getString("temp");
			
		
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public void updateFiles(ModelFile m){
		mFileList.add(m);
	}
	
	public void startUpdate(){
		//Initialize web socket connection
		OctoprintConnection.getSettings(this);
	}
	
	public void setNotLinked(){	
		mStatus = StateUtils.STATE_NEW;
		mMessage = "New";
	}
}
