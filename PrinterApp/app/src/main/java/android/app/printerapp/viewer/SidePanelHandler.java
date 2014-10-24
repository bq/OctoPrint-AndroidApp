package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.ItemListFragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.OctoprintSlicing;
import android.app.printerapp.octoprint.StateUtils;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.material.widget.PaperButton;

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
    private static final String[] INFILL_OPTIONS = {"20","50","100"};
    private static final String[] SUPPORT_OPTIONS = {"none", "buildplate", "everywhere"};

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

    private Spinner s_quality;
    private Spinner s_infill;
    private Spinner s_support;

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
        restoreButton = (PaperButton) mRootView.findViewById(R.id.retore_settings_button);

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

                          mSlicingHandler.setExtras("profile", s_quality.getItemAtPosition(i).toString());

                          parseJson(i);


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
                            mSlicingHandler.setExtras("profile.fill_density", Float.parseFloat(s_infill.getItemAtPosition(i).toString()));
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
            travelSpeed.setText(data.getString("travel_speed"));
            bottomLayerSpeed.setText(data.getString("bottom_layer_speed"));
            infillSpeed.setText(data.getString("infill_speed"));
            outerShellSpeed.setText(data.getString("outer_shell_speed"));
            innerShellSpeed.setText(data.getString("inner_shell_speed"));

            minimalLayerTime.setText(data.getString("cool_min_layer_time"));



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
            profile.put("description", "Test profile created from App");
            profile.put("key", profileText.getText().toString());


            //Data info
            JSONObject data = new JSONObject();

            data.put("travel_speed", travelSpeed.getText().toString());
            data.put("bottom_layer_speed", bottomLayerSpeed.getText().toString());
            data.put("infill_speed", infillSpeed.getText().toString());
            data.put("outer_shell_speed", outerShellSpeed.getText().toString());
            data.put("inner_shell_speed", innerShellSpeed.getText().toString());

            data.put("cool_min_layer_time", minimalLayerTime.getText().toString());
            data.put("fan_enabled", enableCoolingFan.isChecked());

            profile.put("data",data);


            Log.i("OUT", profile.toString());


        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (profile!=null){

            //Send profile to server
            OctoprintSlicing.sendProfile(mActivity, mPrinter, profile);

        }



    }


}
