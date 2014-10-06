package android.app.printerapp.settings;

import java.util.List;

import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;

/**
 * This is the adapter for the printer list on the settings fragment
 * It's going to hold the same device list as the Devices fragment
 * @author alberto-baeza
 *
 */
public class SettingsListAdapter extends ArrayAdapter<ModelPrinter>{

	public SettingsListAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		final ModelPrinter m = getItem(position);
		
		
					
			//View not yet created
			if (v==null){

				//Inflate the view
				LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				v = inflater.inflate(R.layout.settings_row, null, false);
				
				if (DatabaseController.checkExisting(m)) {
					
				
				
					v.findViewById(R.id.settings_delete).setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							DatabaseController.deleteFromDb(m.getName());
			                DevicesListController.getList().remove(m);
			                ItemListActivity.notifyAdapters();
			                notifyDataSetChanged();
						}
					});
					
					v.findViewById(R.id.settings_edit).setOnClickListener(new View.OnClickListener() {
						
						@Override
						public void onClick(View v) {
							optionEdit(m);
						}
					});
				
				}
				
			} else {
				//v = convertView;
			}
			
			TextView tv = (TextView) v.findViewById(R.id.settings_text);
			tv.setText(m.getDisplayName() + " [" + m.getAddress().replace("/", "") +"]");
		
		return v;
	}
	
	//Edit printer name by changing its display name and write it into the Database
	public void optionEdit(final ModelPrinter m){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
		adb.setTitle(R.string.settings_edit_name);
		
		final EditText et = new EditText(getContext());
		adb.setView(et);
		
		adb.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				String newName = et.getText().toString();
				
				m.setDisplayName(newName);
				DatabaseController.updateDB(FeedEntry.DEVICES_DISPLAY, m.getName(), newName);
				
			}
		});
		
		adb.setNegativeButton(R.string.cancel, null);
		
		adb.show();
		
	}
 
}
