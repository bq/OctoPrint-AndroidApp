package android.app.printerapp.viewer.sidepanel;

import android.app.Activity;
import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.util.ui.CustomPopupWindow;
import android.app.printerapp.util.ui.ViewHelper;
import android.app.printerapp.viewer.SlicingHandler;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.material.widget.PaperButton;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * Class to initialize and handle the side panel in the print panel
 * Created by alberto-baeza on 10/24/14.
 */
public class SidePanelHandler {

    //static parameters
    private static final String[] SUPPORT_OPTIONS = {"None", "Buildplate", "Everywhere"}; //support options
    private static final String[] ADHESION_OPTIONS = {"None", "Brim", "Raft"}; //adhesion options
    private static final String[] PRINTER_TYPE = {"Witbox", "Hephestos"};
    private static final String[] PREDEFINED_PROFILES = {"bq"}; //filter for profile deletion

    private static final int DEFAULT_INFILL = 20;
    private int mCurrentInfill = DEFAULT_INFILL;

    //Printer to send the files
    private ModelPrinter mPrinter;

    //Inherited elements
    private SlicingHandler mSlicingHandler;
    private View mRootView;
    private Activity mActivity;

    //UI elements
    private PaperButton printButton;
    private PaperButton sliceButton;
    private PaperButton saveButton;
    private PaperButton restoreButton;
    private PaperButton deleteButton;

    private Spinner s_profile;
    private Spinner s_type;
    private Spinner s_adhesion;
    private Spinner s_support;

    private RelativeLayout s_infill;
    private PopupWindow mInfillOptionsPopupWindow;
    private TextView infillText;

    public SidePanelPrinterAdapter printerAdapter;
    public SidePanelProfileAdapter profileAdapter;

    private EditText layerHeight;
    private EditText shellThickness;
    private CheckBox enableRetraction;
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
    private CheckBox enableCoolingFan;

    //Constructor
    public SidePanelHandler(SlicingHandler handler, Activity activity, View v) {

        mActivity = activity;
        mSlicingHandler = handler;
        mRootView = v;
        mPrinter = null;

        initUiElements();
        initSidePanel();

    }

    //Initialize UI references
    public void initUiElements() {

        s_type = (Spinner) mRootView.findViewById(R.id.type_spinner);
        s_profile = (Spinner) mRootView.findViewById(R.id.profile_spinner);
        s_adhesion = (Spinner) mRootView.findViewById(R.id.adhesion_spinner);
        s_support = (Spinner) mRootView.findViewById(R.id.support_spinner);

        s_infill = (RelativeLayout) mRootView.findViewById(R.id.infill_spinner);
        infillText = (TextView) mRootView.findViewById(R.id.infill_number_view);

        printButton = (PaperButton) mRootView.findViewById(R.id.print_model_button);
        sliceButton = (PaperButton) mRootView.findViewById(R.id.slice_model_button);
        saveButton = (PaperButton) mRootView.findViewById(R.id.save_settings_button);
        restoreButton = (PaperButton) mRootView.findViewById(R.id.restore_settings_button);
        deleteButton = (PaperButton) mRootView.findViewById(R.id.delete_settings_button);

        layerHeight = (EditText) mRootView.findViewById(R.id.layer_height_edittext);
        shellThickness = (EditText) mRootView.findViewById(R.id.shell_thickness_edittext);
        enableRetraction = (CheckBox) mRootView.findViewById(R.id.enable_retraction_checkbox);
        bottomTopThickness = (EditText) mRootView.findViewById(R.id.bottom_top_thickness_edittext);
        printSpeed = (EditText) mRootView.findViewById(R.id.print_speed_edittext);
        printTemperature = (EditText) mRootView.findViewById(R.id.print_temperature_edittext);
        filamentDiamenter = (EditText) mRootView.findViewById(R.id.diameter_edittext);
        filamentFlow = (EditText) mRootView.findViewById(R.id.flow_title_edittext);

        travelSpeed = (EditText) mRootView.findViewById(R.id.travel_speed_edittext);
        bottomLayerSpeed = (EditText) mRootView.findViewById(R.id.bottom_layer_speed_edittext);
        infillSpeed = (EditText) mRootView.findViewById(R.id.infill_speed_edittext);
        outerShellSpeed = (EditText) mRootView.findViewById(R.id.outher_shell_speed_edittext);
        innerShellSpeed = (EditText) mRootView.findViewById(R.id.inner_shell_speed_edittext);

        minimalLayerTime = (EditText) mRootView.findViewById(R.id.minimal_layer_time_edittext);
        enableCoolingFan = (CheckBox) mRootView.findViewById(R.id.enable_cooling_fan_checkbox);


        //profileText = (EditText) mRootView.findViewById(R.id.profile_edittext);

        // SCROLL VIEW HACK

        /**
         * Removes focus from the scrollview when notifying the adapter
         */
        ScrollView view = (ScrollView) mRootView.findViewById(R.id.advanced_options_scroll_view);
        view.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.requestFocusFromTouch();
                return false;
            }
        });

        mRootView.findViewById(R.id.connect_printer_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Open devices panel to connect a new printer
                MainActivity.performClick(2);

            }
        });

        initTextWatchers();

    }

    public void initTextWatchers(){

        layerHeight.addTextChangedListener(new GenericTextWatcher("profile.layer_height"));
        shellThickness.addTextChangedListener(new GenericTextWatcher("profile.wall_thickness"));
        enableRetraction.setOnCheckedChangeListener(new GenericTextWatcher("profile.retraction_enable"));
        bottomTopThickness.addTextChangedListener(new GenericTextWatcher("profile.solid_layer_thickness"));
        printSpeed.addTextChangedListener(new GenericTextWatcher("profile.print_speed"));
        printTemperature.addTextChangedListener(new GenericTextWatcher("profile.print_temperature"));
        filamentDiamenter.addTextChangedListener(new GenericTextWatcher("profile.filament_diameter"));
        filamentFlow.addTextChangedListener(new GenericTextWatcher("profile.filament_flow"));
        travelSpeed.addTextChangedListener(new GenericTextWatcher("profile.travel_speed"));
        bottomLayerSpeed.addTextChangedListener(new GenericTextWatcher("profile.bottom_layer_speed"));
        infillSpeed.addTextChangedListener(new GenericTextWatcher("profile.infill_speed"));
        outerShellSpeed.addTextChangedListener(new GenericTextWatcher("profile.outer_shell_speed"));
        innerShellSpeed.addTextChangedListener(new GenericTextWatcher("profile.inner_shell_speed"));
        minimalLayerTime.addTextChangedListener(new GenericTextWatcher("profile.cool_min_layer_time"));
        enableCoolingFan.setOnCheckedChangeListener(new GenericTextWatcher("profile.fan_enabled"));



    }

    //Enable/disable profile options depending on the model type
    public void enableProfileSelection(boolean enable) {

        s_profile.setEnabled(enable);
        s_support.setEnabled(enable);
        s_adhesion.setEnabled(enable);
        s_infill.setEnabled(enable);

    }


    //Initializes the side panel with the printer data
    public void initSidePanel() {

        Handler handler = new Handler();

        handler.post(new Runnable() {

            @Override
            public void run() {


                try {

                    //Initialize item listeners

                    /************************* INITIALIZE TYPE SPINNER ******************************/


                    s_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            switch (i) {

                                case 0:
                                    ViewerMainFragment.changePlate(ModelProfile.WITBOX_PROFILE);
                                    break;
                                case 1:
                                    ViewerMainFragment.changePlate(ModelProfile.PRUSA_PROFILE);
                                    break;
                                default:

                                    //TODO Profiles being removed automatically

                                    try {

                                        ViewerMainFragment.changePlate(s_type.getSelectedItem().toString());

                                    } catch (NullPointerException e) {


                                    }

                                    break;

                            }


                            mPrinter = DevicesListController.selectAvailablePrinter(i + 1, s_type.getSelectedItem().toString());
                            mSlicingHandler.setPrinter(mPrinter);

                            ViewerMainFragment.slicingCallback();


                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {


                        }
                    });

                    reloadProfileAdapter();





                    /******************** INITIALIZE SECONDARY PANEL ************************************/


                    //Set slicing parameters to send to the server

                    //The quality adapter is set by the printer spinner
                    s_profile.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                            parseJson(ModelProfile.retrieveProfile(mActivity, s_profile.getSelectedItem().toString(), ModelProfile.TYPE_Q));
                            //mSlicingHandler.setExtras("profile", s_profile.getSelectedItem().toString());

                            if (i > 2){

                                refreshProfileExtras();

                            } else {
                                reloadBasicExtras();
                                mSlicingHandler.setExtras("profile", s_profile.getSelectedItem().toString());


                            }

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            mSlicingHandler.setExtras("profile", null);
                        }
                    });

                    reloadQualityAdapter();


                    //Adhesion type
                    s_adhesion.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {


                            mSlicingHandler.setExtras("profile.platform_adhesion", s_adhesion.getItemAtPosition(i).toString().toLowerCase());

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
                            mSlicingHandler.setExtras("profile.support", s_support.getItemAtPosition(i).toString().toLowerCase());

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {
                            mSlicingHandler.setExtras("profile.support", null);
                        }
                    });


                    ArrayAdapter<String> adapter_adhesion = new ArrayAdapter<String>(mActivity,
                            R.layout.print_panel_spinner_item, ADHESION_OPTIONS);
                    adapter_adhesion.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
                    ArrayAdapter<String> adapter_support = new ArrayAdapter<String>(mActivity,
                            R.layout.print_panel_spinner_item, SUPPORT_OPTIONS);
                    adapter_support.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);

                    // s_profile.setAdapter(adapter_profile);
                    s_adhesion.setAdapter(adapter_adhesion);
                    s_support.setAdapter(adapter_support);

                    infillText.setText(DEFAULT_INFILL + "%");
                    s_infill.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openInfillPopupWindow();
                        }
                    });

                    /**************************************************************************/


                    /******************************** INITIALIZE BUTTONS *************************/

                    //Send a print command
                    printButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            refreshPrinters();
                            sendToPrint();
                        }
                    });

                    sliceButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ViewerMainFragment.slicingCallbackForced();
                            switchSlicingButton(false);

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
                            //parseJson(s_profile.getSelectedItemPosition());
                            parseJson(ModelProfile.retrieveProfile(mActivity, s_profile.getSelectedItem().toString(), ModelProfile.TYPE_Q));
                            if (s_profile.getSelectedItemPosition() <= 2) reloadBasicExtras();
                        }
                    });

                    deleteButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (s_profile.getSelectedItemPosition() > 2) deleteProfile(s_profile.getSelectedItem().toString());
                            else {
                                Toast.makeText(mActivity,"You can't delete this profile",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


                    /**************************************************************************/


                } catch (Exception e) {
                    e.printStackTrace();
                }


                /**
                 * Set preferred settings
                 */
                String prefType = DatabaseController.getPreference(DatabaseController.TAG_PROFILE, "type");
                String prefQuality = DatabaseController.getPreference(DatabaseController.TAG_PROFILE, "quality");
                //String prefPrinter = DatabaseController.getPreference(DatabaseController.TAG_PROFILE,"type");
                if (prefType != null) s_type.setSelection(Integer.parseInt(prefType));
                if (prefQuality != null) s_profile.setSelection(Integer.parseInt(prefQuality));
                //if (prefPrinter!=null) s_printer.setSelection(Integer.parseInt(prefPrinter));

                refreshPrinters();

            }
        });


    }

    /**
     * Send a gcode file to the selected printer
     */
    private void sendToPrint() {

        if (mPrinter != null) {

            //If printer is available
            if (mPrinter.getStatus() == StateUtils.STATE_OPERATIONAL) {

                //Retrieve the current file
                File mFile = ViewerMainFragment.getFile();

                if (mFile != null) {


                    Log.i("Slicer", "Current file: " + mFile.getAbsolutePath());

                    File actualFile = null;
                    if (mSlicingHandler.getOriginalProject() != null)
                        actualFile = new File(mSlicingHandler.getOriginalProject());

                    File finalFile = null;

                    Log.i("Slicer", "Current project: " + mSlicingHandler.getOriginalProject());

                    if (actualFile != null)
                        if (LibraryController.isProject(actualFile)) {

                            //It's the last file
                            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last") != null) {

                            /*mSlicingHandler.setExtras("print",true);
                            mPrinter.setJobPath(mSlicingHandler.getLastReference());
                           /mPrinter.setLoaded(false);
                            ItemListFragment.performClick(0);
                            ItemListActivity.showExtraFragment(1, mPrinter.getId());*/

                                //Add it to the reference list
                                DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES, mPrinter.getName(),
                                        mSlicingHandler.getOriginalProject() + "/_tmp/temp.gco", true);

                                mPrinter.setJobPath(null);

                                DevicesListController.selectPrinter(mActivity, actualFile, mSlicingHandler);


                            } else {

                                //Check for temporary gcode
                                File tempFile = new File(LibraryController.getParentFolder() + "/temp/temp.gco");


                                //If we have a gcode which is temporary it's either a stl or sliced gcode
                                if (tempFile.exists()) {

                                    //Get original project
                                    //final File actualFile = new File(mSlicingHandler.getOriginalProject());

                                    //File renameFile = new File(tempFile.getParentFile().getAbsolutePath() + "/" + (new File(mSlicingHandler.getOriginalProject()).getName() + ".gco"));

                                    File tempFolder =new File(mSlicingHandler.getOriginalProject() + "/_tmp/");
                                    if (!tempFolder.exists()){
                                        if (tempFolder.mkdir()){

                                            Log.i("Slicer", "Creating temp " + tempFolder.getAbsolutePath());

                                        }
                                        Log.i("Slicer", "Creating temp NOPE " + tempFolder.getAbsolutePath());
                                    }

                                    finalFile = new File(tempFolder + "/" + actualFile.getName().replace(" ", "_") + "_tmp.gcode");



                                    Log.i("Slicer", "Creating new file in " + finalFile.getAbsolutePath());

                                    Log.i("Slicer", "Final file is: STL or Sliced STL");


                                    tempFile.renameTo(finalFile);

                                    //if we don't have a temporary gcode, means we are currently watching an original gcode from a project
                                } else {

                                    if (LibraryController.hasExtension(1, mFile.getName())) {


                                        Log.i("Slicer", "Final file is: Project GCODE");

                                        finalFile = mFile;

                                    } else {

                                        Log.i("Slicer", "Mada mada");

                                    }

                                }

                            }


                            //Not a project
                        } else {

                            //Check for temporary gcode
                            File tempFile = new File(LibraryController.getParentFolder() + "/temp/temp.gco");


                            //If we have a gcode which is temporary it's a sliced gcode
                            if (tempFile.exists()) {

                                Log.i("Slicer", "Final file is: Random STL or Random Sliced STL");
                                finalFile = tempFile;


                                //It's a random gcode
                            } else {

                                Log.i("Slicer", "Final file is: Random GCODE");
                                finalFile = mFile;
                            }


                        }


                    if (finalFile != null)

                        //either case if the file exists, we send it to the printer
                        if (finalFile.exists()) {

                            DevicesListController.selectPrinter(mActivity, finalFile, null);
                            mPrinter.setJobPath(finalFile.getAbsolutePath());

                        } else {

                            Toast.makeText(mActivity, R.string.viewer_slice_error, Toast.LENGTH_LONG).show();

                        }


                } else {
                    Toast.makeText(mActivity, R.string.devices_toast_no_gcode, Toast.LENGTH_LONG).show();
                }
                ;

            } else
                Toast.makeText(mActivity, R.string.viewer_printer_unavailable, Toast.LENGTH_LONG).show();


        } else
            Toast.makeText(mActivity, R.string.viewer_printer_unavailable, Toast.LENGTH_LONG).show();


        /**
         * Save the printer profile settings
         */
        DatabaseController.handlePreference(DatabaseController.TAG_PROFILE, "type", String.valueOf(s_type.getSelectedItemPosition()), true);
        DatabaseController.handlePreference(DatabaseController.TAG_PROFILE, "quality", String.valueOf(s_profile.getSelectedItemPosition()), true);


    }


    /**
     * Parses a JSON profile to the side panel
     *
     * @i printer index in the list
     */
    public void parseJson(JSONObject profile) {

        //Parse the JSON element
        try {


            //JSONObject data = mPrinter.getProfiles().get(i).getJSONObject("data");
            JSONObject data = profile.getJSONObject("data");
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

            if (data.has("retraction_enable"))
                if (data.getString("retraction_enable").equals("true")) {
                    enableRetraction.setChecked(true);
                    Log.i("OUT", "Checked true");
                } else {
                    enableRetraction.setChecked(false);
                    Log.i("OUT", "Checked false");
                }

            if (data.getBoolean("fan_enabled")) {
                enableCoolingFan.setChecked(true);
                Log.i("OUT", "Checked true");
            } else {
                enableCoolingFan.setChecked(false);
                Log.i("OUT", "Checked false");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (NullPointerException e){ //If invalid values
            e.printStackTrace();
        }

    }

    /**
     * Parse float to a variable to avoid accuracy error
     *
     * @param s
     * @return
     */
    public Float getFloatValue(String s) throws NumberFormatException {

        Float f = Float.parseFloat(s);

        return f;
    }

    /**
     * Open a pop up window with the infill options
     */
    public void openInfillPopupWindow() {

//        if (mInfillOptionsPopupWindow == null) {
        //Get the content view of the pop up window
        final LinearLayout popupLayout = (LinearLayout) mActivity.getLayoutInflater()
                .inflate(R.layout.print_panel_infill_dropdown_menu, null);
        popupLayout.measure(0, 0);

        final Bitmap gridResource = BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.fill_grid);

        //Set the behavior of the infill seek bar
        final SeekBar infillSeekBar = (SeekBar) popupLayout.findViewById(R.id.seekBar_infill);
        final TextView infillPercent = (TextView) popupLayout.findViewById(R.id.infill_number_view);
        final ImageView infillGrid = (ImageView) popupLayout.findViewById(R.id.infill_grid_view);
        infillSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
                infillPercent.setText(progress + "%");
                infillText.setText(progress + "%");

                if(progress == 0) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_empty));
                }
                if (progress > 0 && progress <= 25) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_0));
                }
                if (progress > 26 && progress <= 50) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_25));
                }
                if (progress > 51 && progress <= 75) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_50));
                }
                if (progress > 76 && progress < 100) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_75));
                }
                if (progress == 100) {
                    infillGrid.setImageDrawable(mActivity.getResources().getDrawable(R.drawable.grid_full));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mCurrentInfill = infillSeekBar.getProgress();
                mSlicingHandler.setExtras("profile.fill_density", mCurrentInfill);

            }
        });

        infillSeekBar.setProgress(mCurrentInfill);
        infillPercent.setText(mCurrentInfill + " %");

        //Show the pop up window in the correct position
        int[] infillSpinnerCoordinates = new int[2];
        s_infill.getLocationOnScreen(infillSpinnerCoordinates);
        int popupLayoutPadding = (int) mActivity.getResources().getDimensionPixelSize(R.dimen.content_padding_normal);
        int popupLayoutWidth = 360; //FIXED WIDTH
        int popupLayoutHeight = popupLayout.getMeasuredHeight();
        final int popupLayoutX = infillSpinnerCoordinates[0] - 2; //Remove the background padding
        final int popupLayoutY = infillSpinnerCoordinates[1];

        mInfillOptionsPopupWindow = (new CustomPopupWindow(popupLayout, popupLayoutWidth,
                popupLayoutHeight, R.style.PopupMenuAnimation, true).getPopupWindow());

        mInfillOptionsPopupWindow.showAtLocation(s_infill, Gravity.NO_GRAVITY,
                popupLayoutX, popupLayoutY);
//        }
    }

    /**
     * Save a slicing profile by adding every individual element to a JSON
     */
    public void saveProfile() {

        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View getModelsDialogView = inflater.inflate(R.layout.dialog_create_profile, null);
        final MaterialEditText nameEditText = (MaterialEditText) getModelsDialogView.findViewById(R.id.new_profile_name_edittext);

        final MaterialDialog.Builder createFolderDialog = new MaterialDialog.Builder(mActivity);
        createFolderDialog.title(R.string.dialog_create_profile_title)
                .customView(getModelsDialogView, true)
                .positiveColorRes(R.color.theme_accent_1)
                .positiveText(R.string.create)
                .negativeColorRes(R.color.body_text_2)
                .negativeText(R.string.cancel)
                .autoDismiss(false)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        String name = nameEditText.getText().toString().trim();
                        if (name == null || name.equals("")) {
                            nameEditText.setError(mActivity.getString(R.string.library_create_folder_name_error));
                        }
                        else {
                            //Init UI elements

                            JSONObject profile = null;


                            //Parse the JSON element
                            try {

                                //Profile info
                                profile = new JSONObject();

                                profile.put("displayName", nameEditText.getText().toString());
                                profile.put("description", "Test profile created from App"); //TODO
                                profile.put("key", nameEditText.getText().toString().replace(" ", "_").toLowerCase());

                                //Data info
                                JSONObject data = new JSONObject();

                                data.put("layer_height", getFloatValue(layerHeight.getText().toString()));
                                data.put("wall_thickness", getFloatValue(shellThickness.getText().toString()));
                                data.put("solid_layer_thickness", getFloatValue(bottomTopThickness.getText().toString()));

                                data.put("print_speed", getFloatValue(printSpeed.getText().toString()));
                                data.put("print_temperature", new JSONArray().put(getFloatValue(printTemperature.getText().toString())));
                                data.put("filament_diameter", new JSONArray().put(getFloatValue(filamentDiamenter.getText().toString())));
                                data.put("filament_flow", getFloatValue(filamentFlow.getText().toString()));
                                data.put("retraction_enable", enableRetraction.isChecked());

                                data.put("travel_speed", getFloatValue(travelSpeed.getText().toString()));
                                data.put("bottom_layer_speed", getFloatValue(bottomLayerSpeed.getText().toString()));
                                data.put("infill_speed", getFloatValue(infillSpeed.getText().toString()));
                                data.put("outer_shell_speed", getFloatValue(outerShellSpeed.getText().toString()));
                                data.put("inner_shell_speed", getFloatValue(innerShellSpeed.getText().toString()));

                                data.put("cool_min_layer_time", getFloatValue(minimalLayerTime.getText().toString()));
                                data.put("fan_enabled", enableCoolingFan.isChecked());

                                profile.put("data", data);


                                Log.i("OUT", profile.toString());


                            } catch (JSONException e) {
                                e.printStackTrace();

                            } catch (NumberFormatException e) {

                                //Check if there was an invalid number
                                e.printStackTrace();
                                Toast.makeText(mActivity, e.getMessage(), Toast.LENGTH_LONG).show();
                                profile = null;

                            }

                            if (profile != null) {


                                //check if name already exists to avoid overwriting
                                for (String s : ModelProfile.getQualityList()) {

                                    try {
                                        if (profile.get("displayName").equals(s)) {

                                            Toast.makeText(mActivity, mActivity.getString(R.string.printview_profiles_overwrite) + ": " + s, Toast.LENGTH_LONG).show();

                                        }


                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                }

                                if(ModelProfile.saveProfile(mActivity, nameEditText.getText().toString(), profile, ModelProfile.TYPE_Q)) {

                                    reloadQualityAdapter();

                                    for (int i = 0 ; i < s_profile.getCount() ; i ++){

                                        if (s_profile.getItemAtPosition(i).toString().equals(nameEditText.getText().toString())){
                                            s_profile.setSelection(i);
                                            break;
                                        }


                                    }

                                }


                            }


                        }

                        dialog.dismiss();
                    }
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        dialog.dismiss();
                    }

                })
                .show();




    }

    public void deleteProfile(String name) {

        //Delete profile first
        if (ModelProfile.deleteProfile(mActivity, name, ModelProfile.TYPE_Q)) {

           reloadQualityAdapter();

        }
    }





    /*******************************************
     * ADAPTERS
     ******************************************/

    public void switchSlicingButton(boolean enable){

        if (mPrinter!=null) {
            sliceButton.setClickable(enable);
            sliceButton.refreshTextColor(enable);
        } else {
            sliceButton.setClickable(false);
            sliceButton.refreshTextColor(false);
        }

    }

    public void reloadProfileAdapter(){

        ModelProfile.reloadList(mActivity);

        ArrayAdapter mProfileAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.print_panel_spinner_item, ModelProfile.getProfileList());
        mProfileAdapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
        s_type.setAdapter(mProfileAdapter);

        if (mProfileAdapter!=null){
           mProfileAdapter.notifyDataSetChanged();
            s_type.postInvalidate();
        }



    }

    public void reloadQualityAdapter(){


        ModelProfile.reloadQualityList(mActivity);

        ArrayAdapter mProfileAdapter = new ArrayAdapter<String>(mActivity,
                R.layout.print_panel_spinner_item, ModelProfile.getQualityList());
        mProfileAdapter.setDropDownViewResource(R.layout.print_panel_spinner_dropdown_item);
        s_profile.setAdapter(mProfileAdapter);

        if (mProfileAdapter!=null){
            mProfileAdapter.notifyDataSetChanged();
            s_profile.postInvalidate();
        }

    }

    /**********************************************************************************************/


    /**
     * Only works for "extra" profiles, add a new value per field since we can't upload them yet
     */
    //TODO Temporary
    public void refreshProfileExtras(){

        if (s_profile.getSelectedItemPosition() > 2){


            mSlicingHandler.setExtras("profile.layer_height", getFloatValue(layerHeight.getText().toString()));
            mSlicingHandler.setExtras("profile.wall_thickness", getFloatValue(shellThickness.getText().toString()));
            mSlicingHandler.setExtras("profile.solid_layer_thickness", getFloatValue(bottomTopThickness.getText().toString()));

            mSlicingHandler.setExtras("profile.print_speed", getFloatValue(printSpeed.getText().toString()));
            mSlicingHandler.setExtras("profile.print_temperature", new JSONArray().put(getFloatValue(printTemperature.getText().toString())));
            mSlicingHandler.setExtras("profile.filament_diameter", new JSONArray().put(getFloatValue(filamentDiamenter.getText().toString())));
            mSlicingHandler.setExtras("profile.filament_flow", getFloatValue(filamentFlow.getText().toString()));
            mSlicingHandler.setExtras("profile.retraction_enable", enableRetraction.isChecked());

            mSlicingHandler.setExtras("profile.travel_speed", getFloatValue(travelSpeed.getText().toString()));
            mSlicingHandler.setExtras("profile.bottom_layer_speed", getFloatValue(bottomLayerSpeed.getText().toString()));
            mSlicingHandler.setExtras("profile.infill_speed", getFloatValue(infillSpeed.getText().toString()));
            mSlicingHandler.setExtras("profile.outer_shell_speed", getFloatValue(outerShellSpeed.getText().toString()));
            mSlicingHandler.setExtras("profile.inner_shell_speed", getFloatValue(innerShellSpeed.getText().toString()));

            mSlicingHandler.setExtras("profile.cool_min_layer_time", getFloatValue(minimalLayerTime.getText().toString()));
            mSlicingHandler.setExtras("profile.fan_enabled", enableCoolingFan.isChecked());


        }

    }


    public void refreshPrinters(){

        CardView advanced_layout = (CardView) mRootView.findViewById(R.id.advanced_options_card_view);
        LinearLayout simple_layout = (LinearLayout) mRootView.findViewById(R.id.simple_settings_layout);
        LinearLayout buttons_layout = (LinearLayout) mRootView.findViewById(R.id.advanced_settings_buttons_container);
        LinearLayout print_button = (LinearLayout) mRootView.findViewById(R.id.print_button_container);

        if (DatabaseController.count() < 1){
            mRootView.findViewById(R.id.viewer_select_printer_layout).setVisibility(View.GONE);
            mRootView.findViewById(R.id.viewer_no_printer_layout).setVisibility(View.VISIBLE);


            ViewHelper.disableEnableAllViews(false,advanced_layout);
            ViewHelper.disableEnableAllViews(false,simple_layout);
            ViewHelper.disableEnableAllViews(false,buttons_layout);
            ViewHelper.disableEnableAllViews(false,print_button);


        } else {
            mRootView.findViewById(R.id.viewer_select_printer_layout).setVisibility(View.VISIBLE);
            mRootView.findViewById(R.id.viewer_no_printer_layout).setVisibility(View.GONE);

            ViewHelper.disableEnableAllViews(true,advanced_layout);
            ViewHelper.disableEnableAllViews(true,simple_layout);
            ViewHelper.disableEnableAllViews(true,buttons_layout);
            ViewHelper.disableEnableAllViews(true,print_button);
            mPrinter = DevicesListController.selectAvailablePrinter(s_type.getSelectedItemPosition() + 1, s_type.getSelectedItem().toString());
            mSlicingHandler.setPrinter(mPrinter);
            if (mPrinter!=null)   ViewHelper.disableEnableAllViews(true,print_button);
            else ViewHelper.disableEnableAllViews(false,print_button);
        }

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mActivity);
        if (sharedPref.getBoolean(mActivity.getResources().getString(R.string.shared_preferences_autoslice), false)) sliceButton.setVisibility(View.INVISIBLE);
        else sliceButton.setVisibility(View.VISIBLE);

        mRootView.invalidate();




    }

    /**
     * Clear the extra parameter list and reload basic parameters
     */
    public void reloadBasicExtras(){

        mSlicingHandler.clearExtras();
        mSlicingHandler.setExtras("profile.fill_density", mCurrentInfill);
        mSlicingHandler.setExtras("profile.support", s_support.getSelectedItem());
        mSlicingHandler.setExtras("profile", s_profile.getSelectedItem().toString());

    }

    /**
     * Generic text watcher to add new printing parameters
     */
    private class GenericTextWatcher implements TextWatcher, CompoundButton.OnCheckedChangeListener {

        private String mValue;

        private GenericTextWatcher(String v) {

            mValue = v;

        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

        }

        @Override
        public void afterTextChanged(Editable editable) {

            try{
                mSlicingHandler.setExtras(mValue, getFloatValue(editable.toString()));

            } catch (NumberFormatException e){

                Log.i("Slicer", "Invalid value " + editable.toString());

            }

        }


        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

            mSlicingHandler.setExtras(mValue, b);

        }
    }



}
