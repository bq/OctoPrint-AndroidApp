package android.app.printerapp.devices.discovery;

import java.util.ArrayList;

import javax.jmdns.ServiceInfo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.printerapp.R;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * This class will handle the discovery option to link configured printers and 
 * add them to the database.
 */
public class DiscoveryServiceOption {
	
	//List handling
	private ArrayList<String> mServiceList = new ArrayList<String>();
	private ArrayAdapter<String> mServiceAdapter;
	
	//Reference to context
	private Activity mContext;
	
	
	public DiscoveryServiceOption(Activity context){
		
		//UI recerence
		mContext = context;
		
		//Alert dialog buiilder
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setTitle(R.string.devices_add_dialog_title);
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.menu_add_dialog, null, false);
		
		final ListView lv = (ListView) v.findViewById(R.id.add_dialog_list);
				
		mServiceAdapter = new ArrayAdapter<String>(mContext,android.R.layout.simple_list_item_multiple_choice,mServiceList);
		
		lv.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
   
		lv.setAdapter(mServiceAdapter);
		adb.setView(v);
		
		adb.setPositiveButton(R.string.add, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				//Get list elements
				SparseBooleanArray s = lv.getCheckedItemPositions();
				
				for (int i = 0; i < lv.getCount(); i++) {
					
					if (s.get(i)){
						
						Log.i("OUT","Selected: " + mServiceList.get(i));
						
						
					}
					
					
				}
				
			}
		});
		adb.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				JmdnsServiceListener.stopListening();
				
			}
		});
		
		adb.show();
		
		startListening();
	}
	
	//Start service
	public void startListening(){
		
		mServiceList.clear();
		new JmdnsServiceListener(this);
		
	}
	
	//Add to list
	public void addToServiceList(final ServiceInfo info){
		mContext.runOnUiThread(new Runnable() {
			
			@Override
			public void run() {

				mServiceList.add(info.getName());
				mServiceAdapter.notifyDataSetChanged();
				

			}
		});
		
	}
	
	public Context getContext(){
		return mContext;
	}
	
}
