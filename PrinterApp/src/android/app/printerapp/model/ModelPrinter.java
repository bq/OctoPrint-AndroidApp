package android.app.printerapp.model;

import java.util.ArrayList;

import javax.jmdns.ServiceInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.printerapp.octoprint.OctoprintConnection;

public class ModelPrinter {
	
	//Service info
	private String mName;
	private String mAddress;
	private String mStatus = "Offline";
	private String mMessage;
	private String mTemperature;
	
	private ArrayList<ModelFile> mFileList;
	
	//Pending job
	private ModelJob mJob;
	
	public ModelPrinter(ServiceInfo info){
		
		mName = info.getName();
		mAddress = info.getInetAddresses()[0].toString();
		mJob = new ModelJob();
		mFileList = new ArrayList<ModelFile>();
		
		//Initialize web socket connection
		OctoprintConnection.getSettings(this);		
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
	
	public String getStatus(){
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
						
			if (state.getJSONObject("flags").getBoolean("closedOrError")) {
				mStatus = "Error";
				mMessage = state.getString("stateString");
			} else mStatus = state.getString("stateString");
			if (mStatus.equals("Printing from SD")) mStatus = "Printing";
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
}
