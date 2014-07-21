package android.app.printerapp.devices;

import java.util.ArrayList;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.StateUtils;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;


/**
 * This class will handle list events such as add, remove or update
 * @author alberto-baeza
 *
 */
public class DevicesListController {
	
	//List for the printers found
	private static ArrayList<ModelPrinter> mList = new ArrayList<ModelPrinter>();
			
	//Add element to the list
	public static void addToList(ModelPrinter m){
		mList.add(m);
		
	}
	
	//Return the list
	public static ArrayList<ModelPrinter> getList(){
		
		return mList;
	}
	
	//Load device list from the Database
	public static void loadList(Context context){
		
		mList.clear();
		
		Cursor c = DatabaseController.retrieveDeviceList();
		
		c.moveToFirst();
		
		while (!c.isAfterLast()){
			
			Log.i("OUT","Entry: " + c.getString(1) + ";" + c.getString(2) + ";" + c.getString(3));
			
			ModelPrinter m = new ModelPrinter(c.getString(1),c.getString(2) , Integer.parseInt(c.getString(3)));
			
			addToList(m);
			m.startUpdate();
			if (m.getStatus()!=StateUtils.STATE_NEW) m.setVideoStream(context);
			
			c.moveToNext();
		}
	   
	   DatabaseController.closeDb();
	   ItemListActivity.notifyAdapters();

	}
	
	public static int searchAvailablePosition(){
		
		int max = 12;
		boolean[] mFree = new boolean[max];
		
		for (ModelPrinter p : mList){
			
			mFree[p.getPosition()] = true;
			
		}
		for (int i = 0; i<max; i++){
			
			if (!mFree[i]) return i;
			
		}
		
		
		return -1;
		
	}
		
}
