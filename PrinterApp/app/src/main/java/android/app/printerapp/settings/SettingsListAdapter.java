package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

/**
 * This is the adapter for the printer list on the settings fragment
 * It's going to hold the same device list as the Devices fragment
 *
 * @author alberto-baeza
 */
public class SettingsListAdapter extends ArrayAdapter<ModelPrinter> {

    public SettingsListAdapter(Context context, int resource,
                               List<ModelPrinter> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        final ModelPrinter m = getItem(position);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (!DatabaseController.checkExisting(m)) {

            v = inflater.inflate(R.layout.null_item, null, false);

        } else {

            if (v == null) {

                v = inflater.inflate(R.layout.list_item_settings_device, null, false);

            } else {
                if (!DatabaseController.checkExisting(m))
                    v = inflater.inflate(R.layout.null_item, null, false);
                else v = inflater.inflate(R.layout.list_item_settings_device, null, false);
                //v = convertView;
            }

            TextView deviceNameTextView = (TextView) v.findViewById(R.id.device_name_textview);
            deviceNameTextView.setText(m.getDisplayName() + " [" + m.getAddress().replace("/", "") + "]");

            try{
                TextView deviceNetworkTextView = (TextView) v.findViewById(R.id.device_network_textview);
                deviceNetworkTextView.setText("(" + m.getNetwork().replace("\"", "") + ")");
            } catch (NullPointerException e){

                e.printStackTrace(); //No network
            }


            ImageView iv = (ImageView) v.findViewById(R.id.device_icon_imageview);

            final ImageButton connectionButton = (ImageButton) v.findViewById(R.id.device_connection_button);

            switch (m.getStatus()) {

                case (StateUtils.STATE_CLOSED):
                case (StateUtils.STATE_ERROR):
                    connectionButton.setImageResource(R.drawable.ic_settings_disconnect);
                    break;
                default:
                    connectionButton.setImageResource(R.drawable.ic_settings_connect);
                    break;

            }

            switch (m.getType()) {

                case StateUtils.TYPE_WITBOX:
                    iv.setImageResource(R.drawable.printer_witbox_default);
                    break;
                case StateUtils.TYPE_PRUSA:
                    iv.setImageResource(R.drawable.printer_prusa_default);
                    break;
                case StateUtils.TYPE_CUSTOM:
                    iv.setImageResource(R.drawable.printer_custom_default);
                    break;

            }

            v.findViewById(R.id.device_delete_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    DatabaseController.deleteFromDb(m.getId());

                    //TODO change to remove method
                    DevicesListController.getList().remove(m);
                    //ItemListActivity.notifyAdapters();
                    notifyDataSetChanged();
                }
            });

            v.findViewById(R.id.device_edit_button).setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    appearanceEditDialog(m);
                }
            });


            //TODO notify adapter instead of changing icons
            connectionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (m.getStatus() == StateUtils.STATE_OPERATIONAL) {
                        OctoprintConnection.disconnect(getContext(), m.getAddress());
                        connectionButton.setImageResource(R.drawable.ic_settings_disconnect);
                    } else {
                        OctoprintConnection.getNewConnection(getContext(), m);
                        connectionButton.setImageResource(R.drawable.ic_settings_connect);
                    }


                }
            });

        }

        return v;
    }

    //Edit printer name by changing its display name and write it into the Database
    public void appearanceEditDialog(final ModelPrinter m) {

        final String[] colorArray = new String[]{getContext().getResources().getString(R.string.settings_default_color), "default", "red", "orange", "yellow", "green", "blue", "violet", "black"};

        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
        adb.setTitle(R.string.settings_edit_name);

        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.settings_edit_mini_layout, null);

        final EditText et = (EditText) v.findViewById(R.id.settings_edit_name_edit);
        et.setText(m.getDisplayName());

        final Spinner spinner = (Spinner) v.findViewById(R.id.settings_edit_color_spinner);
        spinner.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, colorArray));

        adb.setView(v);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newName = et.getText().toString();
                String newColor = null;
                if (spinner.getSelectedItemPosition() != 0)
                    newColor = colorArray[spinner.getSelectedItemPosition()];

                if (!newName.equals("")) m.setDisplayName(newName);
                DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, m.getId(), newName);

                notifyDataSetChanged();

                //Set the new name on the server
                OctoprintConnection.setSettings(m, newName, newColor, getContext());

            }
        });

        adb.setNegativeButton(R.string.cancel, null);

        adb.show();


    }


}
