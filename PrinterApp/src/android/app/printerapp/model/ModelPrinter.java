package android.app.printerapp.model;

import javax.jmdns.ServiceInfo;

import android.app.printerapp.octoprint.OctoprintConnection;

public class ModelPrinter {
	
	//Service info
	private String mName;
	private String mAddress;
	
	//Pending job
	private ModelJob mJob;
	
	public ModelPrinter(ServiceInfo info){
		
		mName = info.getName();
		mAddress = info.getInetAddresses()[0].toString();
		mJob = new ModelJob();
		
		//Initialize web socket connection
		OctoprintConnection.getSettings(this, mAddress);
		
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
	
}
