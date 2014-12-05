package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by alberto-baeza on 12/4/14.
 */
public class EditPrinterDialog {

    private static final String[] PRINTER_TYPES = {"WITBOX", "PRUSA", "CUSTOM"};

    private String[] colorArray;
    private ModelPrinter mPrinter;
    private ArrayAdapter<ModelPrinter> mAdapter;

    private Spinner spinner_printer;
    private EditText editText_name;
    private Spinner spinner_color;
    private EditText editText_nozzle;
    private EditText editText_extruders;
    private EditText editText_width;
    private EditText editText_depth;
    private EditText editText_height;
    private CheckBox checkBox_circular;
    private CheckBox checkBox_hot;

    private ImageView icon_printer;
    private ImageButton button_edit;

    private ArrayAdapter<String> type_adapter;
    private ArrayAdapter<String> color_adapter;

    public EditPrinterDialog(ArrayAdapter<ModelPrinter> adapter, ModelPrinter p){

        mPrinter = p;
        mAdapter = adapter;
        createDialog();
    }

    private void initElements(View v){

        colorArray = new String[]{mAdapter.getContext().getResources().getString(R.string.settings_default_color),"default", "red", "orange", "yellow", "green", "blue", "violet", "black"};

        spinner_printer = (Spinner) v.findViewById(R.id.settings_edit_type_spinner);
        editText_name = (EditText) v.findViewById(R.id.settings_edit_name_edit);
        spinner_color = (Spinner) v.findViewById(R.id.settings_edit_color_spinner);

        type_adapter = new ArrayAdapter<String>(mAdapter.getContext(),android.R.layout.simple_spinner_item, PRINTER_TYPES);
        color_adapter = new ArrayAdapter<String>(mAdapter.getContext(),android.R.layout.simple_spinner_item, colorArray);

        editText_name.setText(mPrinter.getDisplayName());

        spinner_printer.setAdapter(type_adapter);
        spinner_printer.setSelection(mPrinter.getType() - 1);
        spinner_color.setAdapter(color_adapter);

        editText_nozzle = (EditText) v.findViewById(R.id.settings_edit_nozzle_edit);
        editText_extruders = (EditText) v.findViewById(R.id.settings_edit_extruders_edit);
        editText_width = (EditText) v.findViewById(R.id.settings_edit_bed_width);
        editText_height = (EditText) v.findViewById(R.id.settings_edit_bed_height);
        editText_depth = (EditText) v.findViewById(R.id.settings_edit_bed_depth);

        checkBox_circular = (CheckBox) v.findViewById(R.id.settings_edit_circular_check);
        checkBox_hot= (CheckBox) v.findViewById(R.id.settings_edit_hot_check);

        icon_printer = (ImageView) v.findViewById(R.id.settings_edit_icon);

        button_edit = (ImageButton) v.findViewById(R.id.settings_edit_button);
        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText_name.setEnabled(true);

            }
        });

        spinner_printer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                JSONObject profile = new JSONObject();

                switch(i){

                    case 0:

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), ModelProfile.WITBOX_PROFILE);
                        icon_printer.setImageResource(R.drawable.icon_witbox);

                        break;
                    case 1:

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), ModelProfile.PRUSA_PROFILE);
                        icon_printer.setImageResource(R.drawable.icon_prusa);

                        break;
                    default:
                        icon_printer.setImageResource(R.drawable.icon_custom_generic);
                        break;

                }

               loadProfile(profile);


            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        /*spinner_color.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                int color = OctoprintConnection.convertColor(colorArray[spinner_color.getSelectedItemPosition()]);

                if (i!=0)
                switch(spinner_printer.getSelectedItemPosition()){

                    case 0:

                        if (color!=0){

                            icon_printer.setImageResource(R.drawable.witbox_transparent);
                            icon_printer.setColorFilter(color, PorterDuff.Mode.DST_ATOP);

                        } else icon_printer.setImageResource(R.drawable.icon_witbox);

                        break;

                    case 1:

                        if (color!=0) {

                            icon_printer.setImageResource(R.drawable.prusa_transparent);
                            icon_printer.setColorFilter(color, PorterDuff.Mode.DST_ATOP);

                        } else icon_printer.setImageResource(R.drawable.icon_prusa);
                        break;

                    default:
                        icon_printer.setImageResource(R.drawable.icon_selectedprinter);
                        icon_printer.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
                        break;


                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });*/



    }

    public void createDialog(){

        AlertDialog.Builder adb = new AlertDialog.Builder(mAdapter.getContext());

        adb.setTitle(R.string.settings_edit_name);

        LayoutInflater inflater = (LayoutInflater) mAdapter.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.settings_edit_layout, null);

        initElements(v);

        adb.setView(v);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                String newName = editText_name.getText().toString();

                String newColor = null;

                if (spinner_color.getSelectedItemPosition()!=0) newColor = colorArray[spinner_color.getSelectedItemPosition()];

                if (!newName.equals("")) mPrinter.setDisplayName(newName);
                DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, mPrinter.getId(), newName);

                mPrinter.setType(spinner_printer.getSelectedItemPosition() + 1);
                DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_TYPE, mPrinter.getId(), String.valueOf(mPrinter.getType()));

                mAdapter.notifyDataSetChanged();

                if (!editText_name.isEnabled()) newName = null;

                //Set the new name on the server
                OctoprintConnection.setSettings(mPrinter, newName, newColor, mAdapter.getContext());

            }
        });

        adb.setNegativeButton(R.string.cancel, null);

        Dialog dialog = adb.create();

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(500, LinearLayout.LayoutParams.MATCH_PARENT);


    }

    public void loadProfile(JSONObject profile){

        try {

            editText_nozzle.setText(String.valueOf(profile.getDouble("extruder.nozzleDiameter")));
            editText_nozzle.setEnabled(false);

            editText_extruders.setText(String.valueOf(profile.getInt("extruder.count")));
            editText_extruders.setEnabled(false);

            editText_width.setText(String.valueOf(profile.getInt("volume.width")));
            editText_width.setEnabled(false);

            editText_depth.setText(String.valueOf(profile.getInt("volume.depth")));
            editText_depth.setEnabled(false);

            editText_height.setText(String.valueOf(profile.getInt("volume.height")));
            editText_height.setEnabled(false);

            if (profile.getString("volume.formFactor").equals("circular")) checkBox_circular.setChecked(true);
            else checkBox_circular.setChecked(false);
            checkBox_circular.setEnabled(false);

            checkBox_hot.setChecked(profile.getBoolean("heatedBed"));
            checkBox_hot.setEnabled(false);



        } catch (JSONException e) {
            e.printStackTrace();
        }



    }
}
