package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
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

import java.io.File;
import java.util.ArrayList;

/**
 * This class will create a dialog to edit and set printer type and settings.
 * It's called from the devices panel when you add a new printer or the settings option.
 *
 * Created by alberto-baeza on 12/4/14.
 */
public class EditPrinterDialog {

    //Default printer types
    private static final String[] PRINTER_TYPES = {"WITBOX", "PRUSA", "CUSTOM"};

    //Adapters and arays
    private String[] colorArray;
    private ModelPrinter mPrinter;
    private ArrayAdapter<ModelPrinter> mAdapter;
    private ArrayList<String> profileArray;

    //UI references
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
    private ImageButton button_delete;

    private ArrayAdapter<String> type_adapter;
    private ArrayAdapter<String> color_adapter;

    //Constructor
    public EditPrinterDialog(ArrayAdapter<ModelPrinter> adapter, ModelPrinter p){

        mPrinter = p;
        mAdapter = adapter;
        createDialog();
    }

    //Initialize the UI elements
    private void initElements(View v){

        colorArray = new String[]{mAdapter.getContext().getResources().getString(R.string.settings_default_color),"default", "red", "orange", "yellow", "green", "blue", "violet", "black"};

        spinner_printer = (Spinner) v.findViewById(R.id.settings_edit_type_spinner);
        editText_name = (EditText) v.findViewById(R.id.settings_edit_name_edit);
        spinner_color = (Spinner) v.findViewById(R.id.settings_edit_color_spinner);

        //Add default types plus custom types from internal storage
        profileArray = new ArrayList<String>();
        for (String s : PRINTER_TYPES){

            profileArray.add(s);
        }

        //Add internal storage types
        for (File file : mAdapter.getContext().getFilesDir().listFiles()){

            //Only files with the .profile extension
            if (file.getAbsolutePath().contains(".profile")) {

                int pos = file.getName().lastIndexOf(".");
                String name = pos > 0 ? file.getName().substring(0, pos) : file.getName();

                //Add only the name
                profileArray.add(name);
            }

        }

        //Initialize adapters
        type_adapter = new ArrayAdapter<String>(mAdapter.getContext(),android.R.layout.simple_spinner_item, profileArray);
        color_adapter = new ArrayAdapter<String>(mAdapter.getContext(),android.R.layout.simple_spinner_item, colorArray);

        //Initial settings and spinners
        editText_name.setText(mPrinter.getDisplayName());

        //Select initial profile from the printer type
        spinner_printer.setAdapter(type_adapter);

        //If it's a custom profile
        if (mPrinter.getProfile()!=null){

            int pos = 0;

            for (String s : profileArray){

                if (s.equals(mPrinter.getProfile())) {

                    spinner_printer.setSelection(pos);

                }
                pos++;

            }

        } else spinner_printer.setSelection(mPrinter.getType() - 1); //Default profile

        spinner_color.setAdapter(color_adapter);

        editText_nozzle = (EditText) v.findViewById(R.id.settings_edit_nozzle_edit);
        editText_extruders = (EditText) v.findViewById(R.id.settings_edit_extruders_edit);
        editText_width = (EditText) v.findViewById(R.id.settings_edit_bed_width);
        editText_height = (EditText) v.findViewById(R.id.settings_edit_bed_height);
        editText_depth = (EditText) v.findViewById(R.id.settings_edit_bed_depth);

        checkBox_circular = (CheckBox) v.findViewById(R.id.settings_edit_circular_check);
        checkBox_hot= (CheckBox) v.findViewById(R.id.settings_edit_hot_check);

        icon_printer = (ImageView) v.findViewById(R.id.settings_edit_icon);

        //Only enable edit name on button click
        button_edit = (ImageButton) v.findViewById(R.id.settings_edit_button);
        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText_name.setEnabled(true);

            }
        });

        //Delete custom profiles, only works on created profiles
        button_delete = (ImageButton) v.findViewById(R.id.settings_delete_button);
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("OUT","Delete " + spinner_printer.getSelectedItem());

                deleteProfile(spinner_printer.getSelectedItem().toString());

            }
        });

        //Change type profile on item selected
        spinner_printer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                JSONObject profile = new JSONObject();

                boolean editable = false;
                button_delete.setVisibility(View.GONE);

                //Check for default types
                switch(i){

                    case 0: //witbox (locked)

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), ModelProfile.WITBOX_PROFILE);
                        icon_printer.setImageResource(R.drawable.icon_witbox);

                        break;
                    case 1: //prusa (locked)

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), ModelProfile.PRUSA_PROFILE);
                        icon_printer.setImageResource(R.drawable.icon_prusa);

                        break;

                    case 2: //custom (editable)

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), ModelProfile.DEFAULT_PROFILE);
                        icon_printer.setImageResource(R.drawable.icon_custom_generic);
                        editable = true;
                        break;

                    default: //any other user-defined profile (locked)

                        profile = ModelProfile.retrieveProfile(mAdapter.getContext(), profileArray.get(i));
                        icon_printer.setImageResource(R.drawable.icon_custom_generic);
                        editable = false;
                        button_delete.setVisibility(View.VISIBLE);

                        break;

                }

                //Load the selected profile
               loadProfile(profile, editable);


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

    //Method to create the settings dialog
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

                String newName = null;
                String newColor = null;

                //only edit color if it's not the "keep color" option
                if (spinner_color.getSelectedItemPosition()!=0) newColor = colorArray[spinner_color.getSelectedItemPosition()];

                //Only edit name if it's enabled
                if ((newName!=null )&& (editText_name.isEnabled())){

                    mPrinter.setDisplayName(newName);
                    DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, mPrinter.getId(), newName);
                }

                //if (!editText_name.isEnabled()) newName = null;

                //Set the new name on the server
                OctoprintConnection.setSettings(mPrinter, newName, newColor, mAdapter.getContext());

                //if it's not a custom editable profile
                if (spinner_printer.getSelectedItemPosition()!=2) {

                    mPrinter.setType(spinner_printer.getSelectedItemPosition() + 1, profileArray.get(spinner_printer.getSelectedItemPosition()));


                } else { //CUSTOM selected

                    mPrinter.setType(3, null);
                    //Save new profile
                    saveProfile();
                }

                //update new profile
                DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_TYPE, mPrinter.getId(), String.valueOf(mPrinter.getType()));

                mAdapter.notifyDataSetChanged();




            }
        });

        adb.setNegativeButton(R.string.cancel, null);

        Dialog dialog = adb.create();

        dialog.show();
        Window window = dialog.getWindow();
        window.setLayout(500, LinearLayout.LayoutParams.MATCH_PARENT);


    }

    public void loadProfile(JSONObject profile, boolean editable){

        try {

            JSONObject extruder = profile.getJSONObject("extruder");

            editText_nozzle.setText(String.valueOf(extruder.getDouble("nozzleDiameter")));
            editText_nozzle.setEnabled(editable);

            editText_extruders.setText(String.valueOf(extruder.getInt("count")));
            editText_extruders.setEnabled(editable);

            JSONObject volume = profile.getJSONObject("volume");

            editText_width.setText(String.valueOf(volume.getInt("width")));
            editText_width.setEnabled(editable);

            editText_depth.setText(String.valueOf(volume.getInt("depth")));
            editText_depth.setEnabled(editable);

            editText_height.setText(String.valueOf(volume.getInt("height")));
            editText_height.setEnabled(editable);

            if (volume.getString("formFactor").equals("circular")) checkBox_circular.setChecked(true);
            else checkBox_circular.setChecked(false);
            checkBox_circular.setEnabled(editable);

            checkBox_hot.setChecked(profile.getBoolean("heatedBed"));
            checkBox_hot.setEnabled(editable);



        } catch (JSONException e) {
            e.printStackTrace();
        }



    }

    public void saveProfile(){

        AlertDialog.Builder adb = new AlertDialog.Builder(mAdapter.getContext());
        adb.setTitle("New name");

        final EditText name = new EditText(mAdapter.getContext());
        adb.setView(name);


        adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try{

                    JSONObject json = new JSONObject();

                    JSONObject volume = new JSONObject();

                    if (checkBox_circular.isChecked()) volume.put("formFactor","circular");
                    else volume.put("formFactor","rectangular");

                    volume.put("depth",Integer.parseInt(editText_depth.getText().toString()));
                    volume.put("width",Integer.parseInt(editText_width.getText().toString()));
                    volume.put("height",Integer.parseInt(editText_height.getText().toString()));

                    json.put("volume",volume);

                    JSONObject extruder = new JSONObject();

                    extruder.put("nozzleDiameter",Double.parseDouble(editText_nozzle.getText().toString()));
                    extruder.put("count",Integer.parseInt(editText_extruders.getText().toString()));

                    json.put("extruder",extruder);

                    if (checkBox_hot.isChecked()) json.put("heatedBed",true);
                    else json.put("heatedBed",false);

                    Log.i("OUT", json.toString());

                    if (ModelProfile.saveProfile(mAdapter.getContext(), name.getText().toString(), json)){

                        mPrinter.setType(3, name.getText().toString());
                        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_PROFILE, mPrinter.getId(), name.getText().toString());

                    }
                } catch (JSONException e){

                    e.printStackTrace();

                }






            }
        });

        adb.show();



    }

    //this method will delete the profile from the system and also from any printer that has it
    public void deleteProfile(final String name){

        AlertDialog.Builder adb = new AlertDialog.Builder(mAdapter.getContext());
        adb.setTitle("Warning!");
        adb.setMessage("If more printers are using this profile, they will be unconfigured");
        adb.setPositiveButton("OK",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Delete profile first
                if (ModelProfile.deleteProfile(mAdapter.getContext(), name)){

                    profileArray.remove(spinner_printer.getSelectedItemPosition());
                    type_adapter.notifyDataSetChanged();

                }

                //Avoid ConcurrentModificationException
                ArrayList<ModelPrinter> aux = new ArrayList<ModelPrinter>();
                for (ModelPrinter p : DevicesListController.getList()){

                    aux.add(p);

                }

                //Check for profile matches
                for (ModelPrinter p : aux){

                    if (p.getProfile()!=null)
                    if ((p!=mPrinter)&&(p.getProfile().equals(name))){

                        //Remove from the configured printers list
                        DatabaseController.deleteFromDb(p.getId());
                        DevicesListController.getList().remove(p);
                        mAdapter.notifyDataSetChanged();

                    }

                }

            }
        });

        adb.show();

    }
}
