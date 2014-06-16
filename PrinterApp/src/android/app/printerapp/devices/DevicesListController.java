package android.app.printerapp.devices;

import java.util.ArrayList;

import android.app.printerapp.model.ModelPrinter;


/**
 * This class will handle list events such as add, remove or update
 * @author alberto-baeza
 *
 */
public class DevicesListController {
	
	private ArrayList<ModelPrinter> mList;
	
	public DevicesListController(){
		
		mList = new ArrayList<ModelPrinter>();
		
	}
	
	//Add element to the list
	public void addToList(ModelPrinter m){
		mList.add(m);
		
	}
	
	//Return the list
	public ArrayList<ModelPrinter> getList(){
		return mList;
	}

}
