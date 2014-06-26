package android.app.printerapp.devices;

import java.util.ArrayList;

import android.app.printerapp.model.ModelPrinter;


/**
 * This class will handle list events such as add, remove or update
 * @author alberto-baeza
 *
 */
public class DevicesListController {
	
	//List for the printers found
	private static ArrayList<ModelPrinter> mList;
	
	public DevicesListController(){
		
		mList = new ArrayList<ModelPrinter>();
		
	}
	
	//Add element to the list
	public static void addToList(ModelPrinter m){
		mList.add(m);
		
	}
	
	//Return the list
	public static ArrayList<ModelPrinter> getList(){
		
		return mList;
	}
	
}
