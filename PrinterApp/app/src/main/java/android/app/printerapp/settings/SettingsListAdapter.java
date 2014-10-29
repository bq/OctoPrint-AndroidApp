package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

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



        } else {
            //v = convertView;
        }

        TextView tv = (TextView) v.findViewById(R.id.settings_text);
        tv.setText(m.getDisplayName() + " [" + m.getAddress().replace("/", "") +"]");

        if (DatabaseController.checkExisting(m)) {

            final ImageButton connectionButton = (ImageButton) v.findViewById(R.id.settings_connection);

            switch (m.getStatus()){

                case (StateUtils.STATE_CLOSED):
                case (StateUtils.STATE_ERROR):
                    connectionButton.setImageResource(R.drawable.ic_settings_disconnect);
                    break;
                default: connectionButton.setImageResource(R.drawable.ic_settings_connect);
                    break;

            }


            //TODO notify adapter instead of changing icons
            connectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (m.getStatus() == StateUtils.STATE_OPERATIONAL) {
                        OctoprintConnection.disconnect(getContext(), m.getAddress());
                        connectionButton.setImageResource(R.drawable.ic_settings_disconnect);
                    } else {
                        OctoprintConnection.getConnection(getContext(), m, true);
                        connectionButton.setImageResource(R.drawable.ic_settings_connect);
                    }



                }
            });

            v.findViewById(R.id.settings_delete).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Log.i("OUT", "Trying to delete " + m.getName());
                    DatabaseController.deleteFromDb(m.getId());

                    //TODO change to remove method
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

        } else {

            //v.setVisibility(View.GONE);

        }
		
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
				DatabaseController.updateDB(FeedEntry.DEVICES_DISPLAY, m.getId(), newName);
                notifyDataSetChanged();
				
			}
		});
		
		adb.setNegativeButton(R.string.cancel, null);
		
		adb.show();


		
	}
 
}
