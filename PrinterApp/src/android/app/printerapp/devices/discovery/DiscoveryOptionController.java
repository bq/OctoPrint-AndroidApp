package android.app.printerapp.devices.discovery;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This class will handle the discovery option to link configured printers and 
 * add them to the database.
 */
public class DiscoveryOptionController {
	
	//List handling
	private ArrayList<ModelPrinter> mServiceList = new ArrayList<ModelPrinter>();
	private ArrayList<String> checkedItems = new ArrayList<String>();
	
	
	
	//private DiscoveryOptionAdapter mServiceAdapter;
	private ArrayAdapter<String> mServiceAdapter;
	
	
	
	
	//Reference to context
	private Activity mContext;
	
	
	public DiscoveryOptionController(Activity context){
		
		//UI reference
		mContext = context;
		
		//Alert dialog builder
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setTitle(R.string.devices_add_dialog_title);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.menu_add_dialog, null, false);
		
		final ListView lv = (ListView) v.findViewById(R.id.add_dialog_list);
				
		
		/********************************************************************************************************/
		
		//Using a normal adapter instead of a custom one since it doesn't allow multichoice
		mServiceAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_multiple_choice, checkedItems);
				//new DiscoveryOptionAdapter(context, R.layout.discovery_element, mServiceList);
		
		
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		   
		lv.setAdapter(mServiceAdapter);
						
		adb.setView(v);
		
		/**************************************************************************************************************/
		
		adb.setPositiveButton(R.string.add, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Get list elements
				SparseBooleanArray s = lv.getCheckedItemPositions();
				
				for (int i = 0; i < lv.getCount(); i++) {
					
					if (s.get(i)){
						
						//Write every new printer on the db
						DatabaseController.writeDb(mServiceList.get(i).getName(), mServiceList.get(i).getAddress(), String.valueOf(DevicesListController.searchAvailablePosition()));	
						
						//Add them to the list separately
						//DevicesFragment.addElement(new ModelPrinter(mServiceList.get(i).getName(), mServiceList.get(i).getAddress()));
						
					}
				}
				
				//Stop service listener
				JmdnsServiceListener.stopListening();
				
			}
		});
		
		
		
		adb.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Stop service listener
				JmdnsServiceListener.stopListening();
				
			}
		});
		
		adb.show();
		
		startListening();
	}
	
	//Start service
	public void startListening(){
		checkedItems.clear();
		mServiceList.clear();
		//new JmdnsServiceListener(this);
		
	}
	
	//Add to list from service listener
	public void addToServiceList(final ModelPrinter m){
		mContext.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {

				
				if (!DatabaseController.checkExisting(m)){
					mServiceList.add(m);
					checkedItems.add(m.getName());
					mServiceAdapter.notifyDataSetChanged();
				}

			}
		});
		
	}
	
	public Context getContext(){
		return mContext;
	}
	
}
