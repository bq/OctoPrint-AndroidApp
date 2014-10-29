package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.ItemListFragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.OctoprintSlicing;
import android.app.printerapp.octoprint.StateUtils;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.material.widget.PaperButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;

/**
 * Class to initialize and handle the side panel in the print panel
 * Created by alberto-baeza on 10/24/14.
 */
public class SidePanelHandler {

    //static parameters
    private static final String[] INFILL_OPTIONS = {"Low","Medium","High","Full", "None"}; //quality options
    private static final String[] SUPPORT_OPTIONS = {"none", "buildplate", "everywhere"}; //support options
    private static final String[] PREDEFINED_PROFILES = {"bq"}; //filter for profile deletion

    //Printer to send the files
    private ModelPrinter mPrinter;

    //Inherited elements
    private SlicingHandler mSlicingHandler;
    private View mRootView;
    private Activity mActivity;

    //UI elements

    private Spinner s_printer;
    private PaperButton printButton;
    private PaperButton saveButton;
    private PaperButton restoreButton;
    private PaperButton deleteButton;

    private Spinner s_quality;
    private Spinner s_infill;
    private Spinner s_support;

    private EditText layerHeight;
    private EditText shellThickness;
    private com.material.widget.CheckBox enableRetraction;
    private EditText bottomTopThickness;
    private EditText printSpeed;
    private EditText printTemperature;
    private EditText filamentDiamenter;
    private EditText filamentFlow;

    private EditText travelSpeed;
    private EditText bottomLayerSpeed;
    private EditText infillSpeed;
    private EditText outerShellSpeed;
    private EditText innerShellSpeed;

    private EditText minimalLayerTime;
    private com.material.widget.CheckBox enableCoolingFan;

    private EditText profileText;


    //Constructor
    public SidePanelHandler(SlicingHandler handler, Activity activity, View v){

        mActivity = activity;
        mSlicingHandler = handler;
        mRootView = v;
        mPrinter = null;

        initUiElements();
        initSidePanel();

    }

    //Initialize UI references
    public void initUiElements(){

        s_printer = (Spinner) mRootView.findViewById(R.id.printer_spinner);
        s_quality = (Spinner)  mRootView.findViewById(R.id.quality_spinner);
        s_infill = (Spinner) mRootView.findViewById(R.id.infill_spinner);
        s_support = (Spinner) mRootView.findViewById(R.id.support_spinner);

        printButton = (PaperButton) mRootView.findViewById(R.id.print_model_button);
        saveButton = (PaperButton) mRootView.findViewById(R.id.save_settings_button);
        restoreButton = (PaperButton) mRootView.findViewById(R.id.restore_settings_button);
        deleteButton = (PaperButton) mRootView.findViewById(R.id.delete_settings_button);

        layerHeight = (EditText)mRootView.findViewById(R.id.layer_height_edittext);
        shellThickness = (EditText)mRootView.findViewById(R.id.shell_thickness_edittext);
        enableRetraction = (com.material.widget.CheckBox)mRootView.findViewById(R.id.enable_retraction_checkbox);
        bottomTopThickness = (EditText)mRootView.findViewById(R.id.bottom_top_thickness_edittext);
        printSpeed = (EditText)mRootView.findViewById(R.id.print_speed_edittext);
        printTemperature = (EditText)mRootView.findViewById(R.id.print_temperature_edittext);
        filamentDiamenter = (EditText)mRootView.findViewById(R.id.diameter_edittext);
        filamentFlow = (EditText)mRootView.findViewById(R.id.flow_title_edittext);

        travelSpeed = (EditText)mRootView.findViewById(R.id.travel_speed_edittext);
        bottomLayerSpeed = (EditText)mRootView.findViewById(R.id.bottom_layer_speed_edittext);
        infillSpeed = (EditText)mRootView.findViewById(R.id.infill_speed_edittext);
        outerShellSpeed = (EditText)mRootView.findViewById(R.id.outher_shell_speed_edittext);
        innerShellSpeed = (EditText)mRootView.findViewById(R.id.inner_shell_speed_edittext);

        minimalLayerTime = (EditText)mRootView.findViewById(R.id.minimal_layer_time_edittext);
        enableCoolingFan = (com.material.widget.CheckBox)mRootView.findViewById(R.id.enable_cooling_fan_checkbox);

        profileText = (EditText) mRootView.findViewById(R.id.profile_edittext);

    }

    //Initializes the side panel with the printer data
    public void initSidePanel(){

        Handler handler = new Handler();

        handler.post(new Runnable() {

            @Override
            public void run() {


                try {

                    //Initialize item listeners

                    /************************* INITIALIZE PRINTER SPINNER ********************************/

                    s_printer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                            //Select a printer from the spinner and add a no-printer option

                            if (i < DevicesListController.getList().size()){

                                mPrinter = DevicesListController.getList().get(i);

                                ArrayList<String> names = new ArrayList<String>();

                                for (JSONObject o : mPrinter.getProfiles()){

                                    try {

                                        names.add(o.getString("displayName"));
                                        //TODO set default value

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }



                                }

                                ArrayAdapter<String> adapter_quality = new ArrayAdapter<String>(mActivity,
                                        R.layout.print_panel_spinner_item, names);

                                s_quality.setAdapter(adapter_quality);

                                adapter_quality.notifyDataSetChanged();

                            } else mPrinter = null;


                            mSlicingHandler.setPrinter(mPrinter);

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                            mPrinter = null;
                            mSlicingHandler.setPrinter(mPrinter);

                        }
                    });



                    String[] nameList = new String[DevicesListController.getList().size() + 1];
                    int i = 0;

                    //New array with names only for the adapter
                    for (ModelPrinter p : DevicesListController.getList()){

                        if (p.getStatus() == StateUtils.STATE_OPERATIONAL){
                            nameList[i] = p.getDisplayName();

                        } else   nameList[i] = "***" + p.getDisplayName(); //TODO temporal

                        i++;

                    }

                    nameList[i] = mActivity.getString(R.string.viewer_printer_selected);

                    ArrayAdapter<String> adapter_printer = new ArrayAdapter<String>(mActivity,
                            R.layout.print_panel_spinner_item,nameList);

                    s_printer.setAdapter(adapter_printer);


                    /*************************************************************************************/



                    /******************** INITIALIZE SECONDARY PANEL ************************************/


                    //Set slicing parameters to send to the server

                    //The quality adapter is set by the printer spinner
                    s_quality.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            try {
                                String key = mPrinter.getProfiles().get(i).getString("key");
                                mSlicingHandler.setExtras("profile", key);

                                parseJson(i);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }




                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            mSlicingHandler.setExtras("profile", null);
                        }
                    });



                    //Infill
                    s_infill.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            float infill = 0;

                            switch(i){

                                case 0: infill = 20; break;
                                case 1: infill = 50; break;
                                case 2: infill = 80; break;
                                case 3: infill = 100; break;
                                case 4: infill = 0; break;
                                default: infill = 0; break;


                            }

                            Log.i("OUT","Infill: " + infill);

                            mSlicingHandler.setExtras("profile.fill_density", infill);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            mSlicingHandler.setExtras("profile.fill_density", null);
                        }
                    });


                    //Support
                    s_support.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                            mSlicingHandler.setExtras("profile.support",s_support.getItemAtPosition(i).toString());
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            mSlicingHandler.setExtras("profile.support",null);
                        }
                    });



                    ArrayAdapter<String> adapter_infill = new ArrayAdapter<String>(mActivity,
                            R.layout.print_panel_spinner_item, INFILL_OPTIONS);
                    ArrayAdapter<String> adapter_support = new ArrayAdapter<String>(mActivity,
                            R.layout.print_panel_spinner_item, SUPPORT_OPTIONS);

                    s_infill.setAdapter(adapter_infill);
                    s_support.setAdapter(adapter_support);

                    /**************************************************************************/


                    /******************************** INITIALIZE BUTTONS *************************/

                    //Send a print command
                    printButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            sendToPrint();
                        }
                    });



                    saveButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            saveProfile();
                        }
                    });

                    restoreButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            parseJson(s_quality.getSelectedItemPosition());
                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteProfile();
                        }
                    });




                    /**************************************************************************/


                }catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });


    }

    /**
     * Send a gcode file to the selected printer
     */
    private void sendToPrint(){

        if (mPrinter!=null){

            //If printer is available
            if (mPrinter.getStatus() == StateUtils.STATE_OPERATIONAL){

                //Retrieve the current file
                File mFile = ViewerMainFragment.getFile();

                if (mFile!=null) {

                    if (mRootView.findViewById(R.id.progress_slice).isShown()){

                        //TODO Check for slicing or what?
                        Toast.makeText(mActivity, R.string.viewer_slice_wait, Toast.LENGTH_LONG).show();

                    } else {

                        //Check for temporary gcode
                        File tempFile = new File(LibraryController.getParentFolder() + "/temp/temp.gco");
                        File finalFile = null;

                        //If we have a gcode which is temporary, we use that
                        if (tempFile.exists()){

                            //File renameFile = new File(tempFile.getParentFile().getAbsolutePath() + "/" + (new File(mSlicingHandler.getOriginalProject()).getName() + ".gco"));
                            finalFile = new File(mSlicingHandler.getOriginalProject() + "/_gcode/" + tempFile.getName());

                            Log.i("OUT", "Creating new file in " + finalFile.getAbsolutePath());

                            tempFile.renameTo(finalFile);
                            //renameFile = tempFile;

                            //if we don't have a temporary gcode, means we are currently watching an original gcode
                        } else {

                            if (LibraryController.hasExtension(1, mFile.getName())){

                                finalFile = mFile;

                            }

                        }

                        //either case if the file exists, we send it to the printer
                        if (finalFile.exists()) {

                            OctoprintFiles.uploadFile(mActivity, finalFile, mPrinter);
                            ItemListFragment.performClick(0);
                            ItemListActivity.showExtraFragment(1, mPrinter.getId());

                        } else {

                            Toast.makeText(mActivity,R.string.viewer_slice_error,Toast.LENGTH_LONG).show();

                        }



                    }
                }
                else {Toast.makeText(mActivity,R.string.devices_toast_no_gcode,Toast.LENGTH_LONG).show();};

            } else Toast.makeText(mActivity, R.string.viewer_printer_unavailable, Toast.LENGTH_LONG).show();



        } else Toast.makeText(mActivity,R.string.viewer_printer_selected, Toast.LENGTH_LONG).show();

    }

    /**
     * Parses a JSON profile to the side panel
     * @i printer index in the list
     */
    public void parseJson(int i){


         //Parse the JSON element
        try {

            profileText.setText(mPrinter.getProfiles().get(i).getString("displayName"));
            JSONObject data = mPrinter.getProfiles().get(i).getJSONObject("data");
            layerHeight.setText(data.getString("layer_height"));
            shellThickness.setText(data.getString("wall_thickness"));
            bottomTopThickness.setText(data.getString("solid_layer_thickness"));
            printSpeed.setText(data.getString("print_speed"));
            printTemperature.setText(data.getJSONArray("print_temperature").get(0).toString());
            filamentDiamenter.setText(data.getJSONArray("filament_diameter").get(0).toString());
            filamentFlow.setText(data.getString("filament_flow"));
            travelSpeed.setText(data.getString("travel_speed"));
            bottomLayerSpeed.setText(data.getString("bottom_layer_speed"));
            infillSpeed.setText(data.getString("infill_speed"));
            outerShellSpeed.setText(data.getString("outer_shell_speed"));
            innerShellSpeed.setText(data.getString("inner_shell_speed"));

            minimalLayerTime.setText(data.getString("cool_min_layer_time"));

            if (data.getBoolean("retraction_enable")){
                enableRetraction.setChecked(true);
                Log.i("OUT", "Checked true");
            }
            else {
                enableRetraction.setChecked(false);
                Log.i("OUT","Checked false" );
            }


            //TODO Not checked by default
            if (data.getBoolean("fan_enabled")){
                enableCoolingFan.setChecked(true);
                Log.i("OUT", "Checked true");
            }
            else {
                enableCoolingFan.setChecked(false);
                Log.i("OUT","Checked false" );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    /**
     * Parse float to a variable to avoid accuracy error
     * @param s
     * @return
     */
    public Float getFloatValue(String s){

        Float f = Float.parseFloat(s);
        return f;
    }

    /**
     * Save a slicing profile by adding every individual element to a JSON
     */
    public void saveProfile(){


        //Init UI elements

        JSONObject profile = null;

        //Parse the JSON element
        try {

            //Profile info
            profile = new JSONObject();

            profile.put("displayName", profileText.getText().toString());
            profile.put("description", "Test profile created from App"); //TODO
            profile.put("key", profileText.getText().toString().replace(" ","_").toLowerCase());


            //Data info
            JSONObject data = new JSONObject();
;
            data.put("layer_height", getFloatValue(layerHeight.getText().toString()));
            data.put("wall_thickness", getFloatValue(shellThickness.getText().toString()));
            data.put("solid_layer_thickness", getFloatValue(bottomTopThickness.getText().toString()));

            data.put("print_speed", getFloatValue(printSpeed.getText().toString()));
            data.put("print_temperature", new JSONArray().put(getFloatValue(printTemperature.getText().toString())));
            data.put("filament_diameter", new JSONArray().put(getFloatValue(filamentDiamenter.getText().toString())));
            data.put("filament_flow", getFloatValue(filamentFlow.getText().toString()));
            data.put("retraction_enabled", enableRetraction.isChecked());

            data.put("travel_speed", getFloatValue(travelSpeed.getText().toString()));
            data.put("bottom_layer_speed", getFloatValue(bottomLayerSpeed.getText().toString()));
            data.put("infill_speed", getFloatValue(infillSpeed.getText().toString()));
            data.put("outer_shell_speed", getFloatValue(outerShellSpeed.getText().toString()));
            data.put("inner_shell_speed", getFloatValue(innerShellSpeed.getText().toString()));

            data.put("cool_min_layer_time",getFloatValue( minimalLayerTime.getText().toString()));
            data.put("fan_enabled", enableCoolingFan.isChecked());

            profile.put("data",data);


            Log.i("OUT", profile.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (profile!=null){

            //check if name already exists to avoid overwriting
            for(JSONObject o : mPrinter.getProfiles()){

                try {
                    if (profile.get("displayName").equals(o.get("displayName"))){


                        //set error and focus
                        profileText.setError(mActivity.getString(R.string.viewer_button_save_error));
                        profileText.requestFocus();
                        return;

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }

            //Clear error and focus
            profileText.setError(null);
            profileText.clearFocus();

            //Send profile to server
            OctoprintSlicing.sendProfile(mActivity, mPrinter, profile);

        }



    }

    //Delete a profile that it's not restricted by a constant
    public void deleteProfile(){

        try {
            final String profile = mPrinter.getProfiles().get(s_quality.getSelectedItemPosition()).getString("key");


            AlertDialog.Builder adb = new AlertDialog.Builder(mActivity);
            adb.setTitle(R.string.viewer_profile_delete);
            adb.setMessage(s_quality.getSelectedItem().toString());
            adb.setPositiveButton(R.string.delete,new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                //Check if the profile is part of the predefined constants
                for (String s : PREDEFINED_PROFILES){

                    if (profile.contains(s)) {

                        Toast.makeText(mActivity,R.string.viewer_profile_delete_error,Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                OctoprintSlicing.deleteProfile(mActivity,mPrinter,profile);

            }
        });

        adb.setNegativeButton(R.string.cancel, null);
        adb.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}
