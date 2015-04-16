package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.database.DeviceInfo;
import android.app.printerapp.devices.discovery.DiscoveryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.OctoprintProfiles;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * This class will create a dialog to edit and set printer type and settings.
 * It's called from the devices panel when you add a new printer or the settings option.
 * <p/>
 * Created by alberto-baeza on 12/4/14.
 */
public class EditPrinterDialog {

    //Default printer types
    private static final String[] PRINTER_TYPES = {"bq_witbox", "bq_hephestos", "custom"};

    //Context
    private Context mContext;

    //Adapters and arays
    private String[] mColorLabelsArray;
    private String[] mColorValuesArray;
    private ModelPrinter mPrinter;
    private ArrayList<String> profileArray;

    //UI references
    private View mRootView;
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

    private JSONObject mSettings;
    private Spinner spinner_port;

    //Constructor
    public EditPrinterDialog(Context context, ModelPrinter p, JSONObject object) {

        mPrinter = p;
        mContext = context;
        mSettings = object;
        createDialog();
    }

    //Initialize the UI elements
    private void initElements(View v) {

        mRootView = v;

//        mColorLabelsArray = new String[]{mContext.getResources().getString(R.string.settings_default_color),"default", "red", "orange", "yellow", "green", "blue", "violet", "black"};
        mColorLabelsArray = mContext.getResources().getStringArray(R.array.printer_color_labels);
        mColorValuesArray = mContext.getResources().getStringArray(R.array.printer_color_values);

        spinner_printer = (Spinner) v.findViewById(R.id.settings_edit_type_spinner);
        editText_name = (EditText) v.findViewById(R.id.settings_edit_name_edit);
        spinner_color = (Spinner) v.findViewById(R.id.settings_edit_color_spinner);

        //Add default types plus custom types from internal storage
        profileArray = new ArrayList<String>();
        for (String s : PRINTER_TYPES) {

            profileArray.add(s);
        }

        //Add internal storage types
        for (File file : mContext.getFilesDir().listFiles()) {

            //Only files with the .profile extension
            if (file.getAbsolutePath().contains(".profile")) {

                int pos = file.getName().lastIndexOf(".");
                String name = pos > 0 ? file.getName().substring(0, pos) : file.getName();

                //Add only the name
                profileArray.add(name);
            }

        }

        //Initialize adapters
        type_adapter = new ArrayAdapter<String>(mContext, R.layout.print_panel_spinner_item, profileArray);
        type_adapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
        color_adapter = new ArrayAdapter<String>(mContext, R.layout.print_panel_spinner_item, mColorLabelsArray);
        color_adapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);

        //Initial settings and spinners
        editText_name.setText(mPrinter.getDisplayName());

        //Select initial profile from the printer type
        spinner_printer.setAdapter(type_adapter);

        //If it's a custom profile
        if (mPrinter.getProfile() != null) {

            int pos = 0;

            for (String s : profileArray) {

                if (s.equals(mPrinter.getProfile())) {

                    spinner_printer.setSelection(pos);

                }
                pos++;

            }

        } else spinner_printer.setSelection(mPrinter.getType() - 1); //Default profile

        spinner_color.setAdapter(color_adapter);

        spinner_port = (Spinner) v.findViewById(R.id.settings_edit_port_spinner);

        //Ports
        try {
            JSONArray ports = mSettings.getJSONObject("options").getJSONArray("ports");
            ArrayList<String> ports_array = new ArrayList<String>();

            for (int i = 0; i < ports.length(); i++) {
                ports_array.add(ports.get(i).toString());
            }

            ArrayAdapter<String> ports_adapter = new ArrayAdapter<String>(mContext,
                    R.layout.print_panel_spinner_item, ports_array);
            ports_adapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
            spinner_port.setAdapter(ports_adapter);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        editText_nozzle = (EditText) v.findViewById(R.id.settings_edit_nozzle_edit);
        editText_extruders = (EditText) v.findViewById(R.id.settings_edit_extruders_edit);
        editText_width = (EditText) v.findViewById(R.id.settings_edit_bed_width);
        editText_height = (EditText) v.findViewById(R.id.settings_edit_bed_height);
        editText_depth = (EditText) v.findViewById(R.id.settings_edit_bed_depth);

        checkBox_circular = (CheckBox) v.findViewById(R.id.settings_edit_circular_check);
        checkBox_hot = (CheckBox) v.findViewById(R.id.settings_edit_hot_check);

        icon_printer = (ImageView) v.findViewById(R.id.settings_edit_icon);

        //Only enable edit name on button click
        button_edit = (ImageButton) v.findViewById(R.id.settings_edit_button);
        button_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                editText_name.setEnabled(true);
                editText_name.setText("");


                InputMethodManager imm = (InputMethodManager) mContext
                        .getSystemService(Context.INPUT_METHOD_SERVICE);

                if (imm != null) {
                    imm.showSoftInput(editText_name, 0);
                }

            }
        });

        //Delete custom profiles, only works on created profiles
        button_delete = (ImageButton) v.findViewById(R.id.settings_delete_button);
        button_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.i("OUT", "Delete " + spinner_printer.getSelectedItem());

                deleteProfile(spinner_printer.getSelectedItem().toString());
//                OctoprintProfiles.deleteProfile(mContext,mPrinter.getAddress(),spinner_printer.getSelectedItem().toString());

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
                switch (i) {

                    case 0: //witbox (locked)

                        profile = ModelProfile.retrieveProfile(mContext, ModelProfile.WITBOX_PROFILE, ModelProfile.TYPE_P);
                        icon_printer.setImageResource(R.drawable.printer_witbox_default);

                        break;
                    case 1: //prusa (locked)

                        profile = ModelProfile.retrieveProfile(mContext, ModelProfile.PRUSA_PROFILE, ModelProfile.TYPE_P);
                        icon_printer.setImageResource(R.drawable.printer_prusa_default);

                        break;

                    case 2: //custom (editable)

                        profile = ModelProfile.retrieveProfile(mContext, ModelProfile.DEFAULT_PROFILE, ModelProfile.TYPE_P);
                        icon_printer.setImageResource(R.drawable.printer_custom_default);
                        editable = true;
                        break;

                    default: //any other user-defined profile (locked)

                        profile = ModelProfile.retrieveProfile(mContext, profileArray.get(i), ModelProfile.TYPE_P);
                        icon_printer.setImageResource(R.drawable.printer_custom_default);
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

    }

    //Method to create the settings dialog
    public void createDialog() {


        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View editPrinterDialogView = inflater.inflate(R.layout.dialog_edit_printer_info, null);
        initElements(editPrinterDialogView);

        MaterialDialog.Builder dialogBuilder = new MaterialDialog.Builder(mContext)
                .title(R.string.settings_edit_name)
                .customView(editPrinterDialogView, false)
                .neutralText(R.string.cancel)
                .neutralColorRes(R.color.body_text_2)
                .negativeText(R.string.settings_change_network)
                .negativeColorRes(R.color.body_text_2)
                .positiveText(R.string.ok)
                .positiveColorRes(R.color.theme_accent_1)
                .cancelable(false)
                .callback(new MaterialDialog.ButtonCallback() {

                    @Override
                    public void onNeutral(MaterialDialog dialog) {
                        super.onNeutral(dialog);


                        dialog.dismiss();

                    }

                    @Override
                    public void onNegative(MaterialDialog dialog) {

                        new DiscoveryController(mContext).changePrinterNetwork(mPrinter);
                        dialog.dismiss();
                    }
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String newName = editText_name.getText().toString();
                        String newColor = null;

                        //only edit color if it's not the "keep color" option
                        if (spinner_color.getSelectedItemPosition() != 0)
                            newColor = mColorValuesArray[spinner_color.getSelectedItemPosition()];

                        //Only edit name if it's enabled
                        if ((newName != null) && (editText_name.isEnabled())) {

                            mPrinter.setDisplayName(newName);
                            DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_DISPLAY, mPrinter.getId(), newName);
                        } else newName = null;

                        //if (!editText_name.isEnabled()) newName = null;

                        //Set the new name on the server
                        OctoprintConnection.setSettings(mPrinter, newName, newColor, mContext);

                        String auxType = null;

                        //if it's not a custom editable profile
                        if (spinner_printer.getSelectedItemPosition() != 2) {

                            mPrinter.setType(spinner_printer.getSelectedItemPosition() + 1, spinner_printer.getSelectedItem().toString());
                            //OctoprintProfiles.selectProfile(mContext,mPrinter.getAddress(),spinner_printer.getSelectedItem().toString());

                            switch (spinner_printer.getSelectedItemPosition()) {

                                case 0:
                                    auxType = "bq_witbox";
                                    break;
                                case 1:
                                    auxType = "bq_hephestos";
                                    break;
                                default: {

                                    //Upload profile, connect if successful
                                    OctoprintProfiles.uploadProfile(mContext, mPrinter.getAddress(), ModelProfile.retrieveProfile(mContext, spinner_printer.getSelectedItem().toString(), ModelProfile.TYPE_P),
                                            spinner_port.getSelectedItem().toString());


                                    //auxType = spinner_printer.getSelectedItem().toString();
                                }
                                break;

                            }

                            //update new profile
                            if (auxType != null) {

                                OctoprintConnection.startConnection(mPrinter.getAddress(), mContext, spinner_port.getSelectedItem().toString(), auxType);
                                OctoprintProfiles.updateProfile(mContext, mPrinter.getAddress(), auxType);
                            }


                        } else { //CUSTOM selected

                            mPrinter.setType(3, null);
                            //Save new profile
                            saveProfile();

                        }

                        if (!DatabaseController.checkExisting(mPrinter)) {

                            mPrinter.setId(DatabaseController.writeDb(mPrinter.getName(), mPrinter.getAddress(), String.valueOf(mPrinter.getPosition()), String.valueOf(mPrinter.getType()),
                                    MainActivity.getCurrentNetwork(mContext)));
                            mPrinter.startUpdate(mContext);
                        } else {

                            DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_TYPE, mPrinter.getId(), String.valueOf(mPrinter.getType()));

                        }

                        notifyAdapters();
                        dialog.dismiss();
                    }
                });

        Dialog dialog = dialogBuilder.build();
        dialog.show();

//        Window window = dialog.getWindow();
//
//        //TODO RANDOM CRASH
//        try {
//
//            window.setLayout(500, LinearLayout.LayoutParams.MATCH_PARENT);
//
//        } catch (ArrayIndexOutOfBoundsException e) {
//
//            e.printStackTrace();
//        }


    }

    public void loadProfile(JSONObject profile, boolean editable) {

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

            if (volume.getString("formFactor").equals("circular"))
                checkBox_circular.setChecked(true);
            else checkBox_circular.setChecked(false);
            checkBox_circular.setEnabled(editable);

            checkBox_hot.setChecked(profile.getBoolean("heatedBed"));
            checkBox_hot.setEnabled(editable);

            //Enable/disable the tags
            mRootView.findViewById(R.id.settings_edit_nozzle_tag).setEnabled(editable);
            mRootView.findViewById(R.id.settings_edit_extruders_tag).setEnabled(editable);
            mRootView.findViewById(R.id.settings_edit_bed_tag).setEnabled(editable);

        } catch (JSONException e) {
            e.printStackTrace();
        }


    }

    public void saveProfile() {

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(R.string.settings_profile_add);

        final EditText name = new EditText(mContext);
        adb.setView(name);


        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                try {

                    String nameFormat = name.getText().toString().replace(" ", "_");

                    JSONObject json = ModelProfile.retrieveProfile(mContext, ModelProfile.DEFAULT_PROFILE, ModelProfile.TYPE_P);

                    json.put("name", name.getText().toString());
                    json.put("id", name.getText().toString().replace(" ", "_"));
                    json.put("model", "ModelPrinter");

                    JSONObject volume = new JSONObject();

                    if (checkBox_circular.isChecked()) volume.put("formFactor", "circular");
                    else volume.put("formFactor", "rectangular");

                    volume.put("depth", Float.parseFloat(editText_depth.getText().toString()));
                    volume.put("width", Float.parseFloat(editText_width.getText().toString()));
                    volume.put("height", Float.parseFloat(editText_height.getText().toString()));

                    json.put("volume", volume);

                    JSONObject extruder = new JSONObject();

                    extruder.put("nozzleDiameter", Double.parseDouble(editText_nozzle.getText().toString()));
                    extruder.put("count", Integer.parseInt(editText_extruders.getText().toString()));

                    ArrayList<Float> s = new ArrayList<Float>();
                    s.add(Float.parseFloat("0.0"));
                    s.add(Float.parseFloat("0.0"));

                    ArrayList<JSONArray> sa = new ArrayList<JSONArray>();
                    sa.add(new JSONArray(s));

                    extruder.put("offsets", new JSONArray(sa));

                    json.put("extruder", extruder);

                    if (checkBox_hot.isChecked()) json.put("heatedBed", true);
                    else json.put("heatedBed", false);

                    Log.i("OUT", json.toString());

                    if (ModelProfile.saveProfile(mContext, name.getText().toString(), json, ModelProfile.TYPE_P)) {

                        mPrinter.setType(3, name.getText().toString());
                        DatabaseController.updateDB(DeviceInfo.FeedEntry.DEVICES_PROFILE, mPrinter.getId(), name.getText().toString());

                        //Upload profile, connect if successful
                        OctoprintProfiles.uploadProfile(mContext, mPrinter.getAddress(), json, spinner_port.getSelectedItem().toString());

                        notifyAdapters();

                    }


                } catch (JSONException e) {

                    e.printStackTrace();

                }


            }
        });

        adb.show();


    }

    //this method will delete the profile from the system and also from any printer that has it
    public void deleteProfile(final String name) {

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setTitle(R.string.warning);
        adb.setMessage(R.string.settings_profile_delete);
        adb.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Delete profile first
                if (ModelProfile.deleteProfile(mContext, name, ModelProfile.TYPE_P)) {

                    profileArray.remove(spinner_printer.getSelectedItemPosition());
                    type_adapter.notifyDataSetChanged();

                }

                //Avoid ConcurrentModificationException
                ArrayList<ModelPrinter> aux = new ArrayList<ModelPrinter>();
                for (ModelPrinter p : DevicesListController.getList()) {

                    aux.add(p);

                }

                //Check for profile matches
                for (ModelPrinter p : aux) {

                    if (p.getProfile() != null)
                        if ((p != mPrinter) && (p.getProfile().equals(name))) {

                            //Remove from the configured printers list
                            DatabaseController.deleteFromDb(p.getId());
                            DevicesListController.getList().remove(p);

                            notifyAdapters();

                        }

                }

            }
        });

        adb.show();

    }


    //TODO intent to notify adapters asynchronously
    public void notifyAdapters() {

        Intent intent = new Intent("notify");
        intent.putExtra("message", "Devices");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

        Intent intent2 = new Intent("notify");
        intent2.putExtra("message", "Settings");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent2);

        Intent intent3 = new Intent("notify");
        intent.putExtra("message", "Profile");
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent3);

    }
}
