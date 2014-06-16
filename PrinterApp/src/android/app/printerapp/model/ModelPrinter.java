package android.app.printerapp.model;

import javax.jmdns.ServiceInfo;

public class ModelPrinter {
	
	//Service info
	private String mName;
	
	public ModelPrinter(ServiceInfo info){
		
		mName = info.getName();
		
	}
	
	/*********
	 * Gets
	 *********/
	
	public String getName(){
		return mName;
	}

}
