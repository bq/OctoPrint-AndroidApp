package android.app.printerapp.devices.printview;


import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.FinishDialog;
import android.app.printerapp.devices.camera.CameraHandler;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.DiscoveryController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.model.ModelProfile;
import android.app.printerapp.octoprint.HttpUtils;
import android.app.printerapp.octoprint.OctoprintConnection;
import android.app.printerapp.octoprint.OctoprintControl;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.util.ui.ViewHelper;
import android.app.printerapp.viewer.DataStorage;
import android.app.printerapp.viewer.GcodeFile;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.app.printerapp.viewer.ViewerSurfaceView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.material.widget.PaperButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will show the PrintView detailed view for every printer
 * Should be able to control printer commands and show video feed.
 *
 * @author alberto-baeza
 */
public class PrintViewFragment extends Fragment {

    private static final String TAG = "PrintView";

    //Current Printer and status
    private static ModelPrinter mPrinter;
    private CameraHandler mCamera;
    private boolean isPrinting = false;
    private boolean isGcodeLoaded = false;

    //View references
    private TextView tv_printer;
    private TextView tv_file;
    private TextView tv_temp;
    private TextView tv_temp_bed;
    private TextView tv_prog;
    private TextView tv_profile;

    private ProgressBar pb_prog;
    private SeekBar sb_head;

    private PaperButton button_pause;
    private PaperButton button_stop;
    private ImageView icon_pause;

    //File references
    private static DataStorage mDataGcode;
    private static ViewerSurfaceView mSurface;
    private SurfaceView mVideoSurface;
    private static FrameLayout mLayout;
    private static FrameLayout mLayoutVideo;

    private View mRootView;

    //Context needed for file loading
    private static Context mContext;
    private static int mActualProgress = 0;

    private Dialog mDownloadDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Reference to View
        mRootView = null;

        //If is not new
        if (savedInstanceState == null) {

            //Necessary for gcode tracking
            mContext = getActivity();

            //Show custom option menu
            setHasOptionsMenu(true);

            //Get the printer from the list
            Bundle args = getArguments();
            mPrinter = DevicesListController.getPrinter(args.getLong("id"));
            //getActivity().getActionBar().setTitle(mPrinter.getAddress().replace("/", ""));

            if (mPrinter==null) {
                getActivity().onBackPressed();
            }else {

                try { //TODO CRASH
                    //Check printing status
                    if ((mPrinter.getStatus() == StateUtils.STATE_PRINTING)||
                            (mPrinter.getStatus() == StateUtils.STATE_PAUSED)) isPrinting = true;
                    else {

                        mActualProgress = 100;
                        isPrinting = false;
                    }
                } catch (NullPointerException e){

                    getActivity().onBackPressed();
                }




                //Update the actionbar to show the up carat/affordance
                if (DatabaseController.count()>1){

                    ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

                }


                //Inflate the fragment
                mRootView = inflater.inflate(R.layout.printview_layout,
                        container, false);

                /************************************************************************/




                //Get video
                mLayoutVideo = (FrameLayout) mRootView.findViewById(R.id.printview_video);

                //TODO CAMERA DISABLED
                mCamera = new CameraHandler(mContext, mPrinter.getWebcamAddress(), mLayoutVideo);




                mVideoSurface = mCamera.getView();
                mLayoutVideo.addView(mVideoSurface);

                mCamera.startVideo();

                //Get tabHost from the xml
                final TabHost tabHost = (TabHost) mRootView.findViewById(R.id.printviews_tabhost);
                tabHost.setup();

                //Create VIDEO tab
                TabHost.TabSpec settingsTab = tabHost.newTabSpec("Video");
                settingsTab.setIndicator(getTabIndicator(mContext.getResources().getString(R.string.printview_video_text), R.drawable.ic_videocam));
                settingsTab.setContent(R.id.printview_video);
                tabHost.addTab(settingsTab);

                //Create 3D RENDER tab
                TabHost.TabSpec featuresTab = tabHost.newTabSpec("3D Render");
                featuresTab.setIndicator(getTabIndicator(mContext.getResources().getString(R.string.printview_3d_text), R.drawable.visual_normal_24dp));
                featuresTab.setContent(R.id.view_gcode);
                tabHost.addTab(featuresTab);

                tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
                    @Override
                    public void onTabChanged(String s) {


                        if (s.equals("Video")){

                            if (mSurface!=null)
                                mLayout.removeAllViews();

                        } else {    //Redraw the gcode

                            if (!isGcodeLoaded){

                                //Show gcode tracking if there's a current path in the printer/preferences
                                if (mPrinter.getJob().getFilename()!=null){
                                    retrieveGcode();
                                }

                            } else {
                                if (mSurface!=null){
                                    drawPrintView();
                                }


                            }

                        }

                        //TODO CAMERA DISABLED
                        mLayoutVideo.invalidate();
                    }
                });

                /***************************************************************************/


                initUiElements();



                refreshData();

                //Register receiver
                mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            }



        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.printview_menu, menu);
    }

    //Switch menu options if it's printing/paused
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    //Option menu
    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                if (DatabaseController.count()>1) getActivity().onBackPressed();
                return true;

            case R.id.printview_add:
                new DiscoveryController(getActivity()).scanDelayDialog();
                return true;

            case R.id.printview_settings:
                //getActivity().onBackPressed();
                MainActivity.showExtraFragment(0, 0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //Initialize all UI elements
    private void initUiElements(){

        //UI references
        tv_printer = (TextView) mRootView.findViewById(R.id.printview_printer_tag);
        tv_file = (TextView) mRootView.findViewById(R.id.printview_printer_file);
        tv_temp = (TextView) mRootView.findViewById(R.id.printview_extruder_temp);
        tv_temp_bed = (TextView) mRootView.findViewById(R.id.printview_bed_temp);
        tv_prog = (TextView) mRootView.findViewById(R.id.printview_printer_progress);
        tv_profile = (TextView) mRootView.findViewById(R.id.printview_text_profile_text);
        pb_prog = (ProgressBar) mRootView.findViewById(R.id.printview_progress_bar);

        button_pause = (PaperButton) mRootView.findViewById(R.id.printview_pause_button);
        icon_pause = (ImageView) mRootView.findViewById(R.id.printview_pause_image);



        button_stop = (PaperButton) mRootView.findViewById(R.id.printview_stop_button);



        icon_pause.setColorFilter(mContext.getResources().getColor(R.color.body_text_2),
                PorterDuff.Mode.MULTIPLY);




        button_pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (!isPrinting) {

                    if ((!mPrinter.getJob().getProgress().equals("null")) && (mPrinter.getJob().getFinished()))
                    {

                            new FinishDialog(mContext,mPrinter);

                        } else {
                            OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "start");
                        }
                }else OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "pause");

            }
        });

//        ((ImageView) mRootView.findViewById(R.id.printview_stop_image)).
//                setColorFilter(mContext.getResources().getColor(android.R.color.holo_red_dark),
//                        PorterDuff.Mode.MULTIPLY);
        button_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "cancel");
            }
        });



        sb_head = (SeekBar) mRootView.findViewById(R.id.seekbar_head_movement_amount);
        sb_head.setProgress(2);


        mRootView.findViewById(R.id.button_xy_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_xy_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", -convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_xy_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x", - convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_xy_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x",  convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_z_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", - convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_z_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", convertProgress(sb_head.getProgress()));
            }
        });

        mRootView.findViewById(R.id.button_z_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "home", "z", 0);
            }
        });

        mRootView.findViewById(R.id.button_xy_home).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "home", "xy", 0);
            }
        });

        /**
         * Temperatures
         */

        final SeekBar extruder1SeekBar = (SeekBar)  mRootView.findViewById(R.id.printview_extruder_temp_slider);
        final PaperButton extruder1Button = (PaperButton) mRootView.findViewById(R.id.printview_extruder_temp_button);
        extruder1Button.setText(String.format(getResources().getString(R.string.printview_change_temp_button), 0));

        extruder1SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                extruder1Button.setText(String.format(getResources().getString(R.string.printview_change_temp_button), i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        extruder1Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendToolCommand(getActivity(), mPrinter.getAddress(), "target", "tool0", extruder1SeekBar.getProgress());
            }
        });

        final SeekBar bedSeekBar = (SeekBar)  mRootView.findViewById(R.id.printview_bed_temp_slider);
        final PaperButton bedButton = (PaperButton) mRootView.findViewById(R.id.printview_bed_temp_button);
        bedButton.setText(String.format(getResources().getString(R.string.printview_change_temp_button), 0));

        bedSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                bedButton.setText(String.format(getResources().getString(R.string.printview_change_temp_button), i));

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OctoprintControl.sendToolCommand(getActivity(), mPrinter.getAddress(), "target", "bed", bedSeekBar.getProgress());
            }
        });

        /*
        Extruder

         */

        final EditText et_am = (EditText) mRootView.findViewById(R.id.et_amount);

        mRootView.findViewById(R.id.printview_retract_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OctoprintControl.sendToolCommand(getActivity(), mPrinter.getAddress(), "extrude",  null, Double.parseDouble(et_am.getText().toString()));

            }
        });

        mRootView.findViewById(R.id.printview_etrude_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                OctoprintControl.sendToolCommand(getActivity(), mPrinter.getAddress(), "extrude",  null, - Double.parseDouble(et_am.getText().toString()));

            }
        });

    }

    private double convertProgress(int amount){

        double finalAmount = 0.1 * Math.pow(10,Math.abs(amount));

        return finalAmount;

    }

    /**
     * Return the custom view of the print view tab
     *
     * @param title Title of the tab
     * @param icon  Icon of the tab
     * @return Custom view of a tab layout
     */
    private View getTabIndicator(String title, int icon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.printview_tab_layout, null);
        ImageView iv = (ImageView) view.findViewById(R.id.tab_icon_imageview);
        iv.setImageResource(icon);
        iv.setColorFilter(mContext.getResources().getColor(R.color.body_text_1),
                PorterDuff.Mode.MULTIPLY);
        TextView tv = (TextView) view.findViewById(R.id.tab_title_textview);
        tv.setText(title);
        return view;
    }

    /**
     * Convert progress string to percentage
     *
     * @param p progress string
     * @return converted value
     */
    public String getProgress(String p) {

        double value = 0;

        try {
            value = Double.valueOf(p);
        } catch (Exception e) {
            //e.printStackTrace();
        }

        return String.valueOf((int) value);
    }

    /**
     * Dinamically update progress bar and text from the main activity
     */
    public void refreshData() {

        //Check around here if files were changed
        tv_printer.setText(mPrinter.getDisplayName() + ": " + mPrinter.getMessage() + " [" + mPrinter.getPort() + "]");
        tv_file.setText(mPrinter.getJob().getFilename());
        tv_temp.setText(mPrinter.getTemperature() + "ºC / " + mPrinter.getTempTarget() + "ºC");
        tv_temp_bed.setText(mPrinter.getBedTemperature() + "ºC / " + mPrinter.getBedTempTarget() + "ºC");

        tv_profile.setText(" " + mPrinter.getProfile());
        CardView printer_select_layout = (CardView) mRootView.findViewById(R.id.printer_select_card_view);

        if ((mPrinter.getStatus() == StateUtils.STATE_PRINTING) ||
                (mPrinter.getStatus() == StateUtils.STATE_PAUSED)) {

            isPrinting = true;

            if (mPrinter.getStatus() == StateUtils.STATE_PRINTING){

                button_pause.setText(getString(R.string.printview_pause_button));
                icon_pause.setImageDrawable(getResources().getDrawable(R.drawable.ic_pause));

            } else {

                button_pause.setText(getString(R.string.printview_start_button));
                icon_pause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));

            }


            tv_prog.setText(getProgress(mPrinter.getJob().getProgress()) + "% (" + OctoprintConnection.ConvertSecondToHHMMString(mPrinter.getJob().getPrintTimeLeft()) +
                    " left / " + OctoprintConnection.ConvertSecondToHHMMString(mPrinter.getJob().getPrintTime()) + " elapsed) - ");

            if (!mPrinter.getJob().getProgress().equals("null")) {
                Double n = Double.valueOf(mPrinter.getJob().getProgress());
                pb_prog.setProgress(n.intValue());



            }

            if (mDataGcode != null)
                changeProgress(Double.valueOf(mPrinter.getJob().getProgress()));

        } else{


            if (!mPrinter.getLoaded()) tv_file.setText(R.string.devices_upload_waiting);

            if ((!mPrinter.getJob().getProgress().equals("null")) && (mPrinter.getJob().getFinished()))
            {

                pb_prog.setProgress(100);
                tv_file.setText(R.string.devices_text_completed);
                button_pause.setText(getString(R.string.printview_finish_button));
                icon_pause.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_done));

                mRootView.findViewById(R.id.stop_button_container).setVisibility(View.INVISIBLE);

            } else {

                tv_prog.setText(mPrinter.getMessage() + " - ");
                button_pause.setText(getString(R.string.printview_start_button));
                icon_pause.setImageDrawable(getResources().getDrawable(R.drawable.ic_play));

                mRootView.findViewById(R.id.stop_button_container).setVisibility(View.VISIBLE);
            }

            isPrinting = false;

        }

        if ((mPrinter.getStatus() == StateUtils.STATE_NONE) ||(mPrinter.getStatus() == StateUtils.STATE_CLOSED)) {

            tv_file.setVisibility(View.INVISIBLE);
            tv_prog.setVisibility(View.INVISIBLE);
            tv_profile.setVisibility(View.INVISIBLE);
            sb_head.setProgress(0);
            mRootView.findViewById(R.id.printview_text_profile_tag).setVisibility(View.INVISIBLE);

            ViewHelper.disableEnableAllViews(false, printer_select_layout);

        } else {
            //mRootView.findViewById(R.id.disabled_gray_tint).setVisibility(View.VISIBLE);
            tv_file.setVisibility(View.VISIBLE);
            tv_prog.setVisibility(View.VISIBLE);
            tv_profile.setVisibility(View.VISIBLE);
            sb_head.setProgress(2);
            mRootView.findViewById(R.id.printview_text_profile_tag).setVisibility(View.VISIBLE);

            ViewHelper.disableEnableAllViews(true, printer_select_layout);
        }

        getActivity().invalidateOptionsMenu();

    }




    public void stopCameraPlayback() {

        //TODO CAMERA DEISABLE
        mCamera.getView().stopPlayback();
        mCamera.getView().setVisibility(View.GONE);


    }

    @Override
    public void onDestroy() {

        //mContext.unregisterReceiver(onComplete);
        super.onDestroy();
    }

    /**
     * ****************************************************************************************
     * <p/>
     * PRINT VIEW PROGRESS HANDLER
     *
     *****************************************************************************************
     */

    /**
     * Method to check if we own the gcode loaded in the printer to display it or we have to download it.
     */
    public void retrieveGcode() {


        //If we have a jobpath, we've uploaded the file ourselves
        if (mPrinter.getJobPath() != null) {

            Log.i(TAG, "PATH IS " + mPrinter.getJobPath());

            //Get filename
            File currentFile = new File(mPrinter.getJobPath());

            if (currentFile.exists())
                //if it's the same as the server or it's in process of being uploaded
                if ((mPrinter.getJob().getFilename().equals(currentFile.getName()))
                        || (!mPrinter.getLoaded())) {

                    Log.i(TAG, "Sigh, loading " + mPrinter.getJobPath());

                    if (LibraryController.hasExtension(1, currentFile.getName()))
                        openGcodePrintView(getActivity(), mPrinter.getJobPath(), mRootView, R.id.view_gcode);
                    else Log.i(TAG, "Das not gcode");

                    isGcodeLoaded = true;

                    //end process
                    return;

                    //Not the same file
                } else Log.i(TAG, "FAIL ;D " + mPrinter.getJobPath());

        }

        if (mPrinter.getLoaded())

            if (mPrinter.getJob().getFilename()!=null)
            //The server actually has a job
            if (!mPrinter.getJob().getFilename().equals("null")) {

                Log.i(TAG, "Either it's not the same or I don't have it, download: " + mPrinter.getJob().getFilename());

                String download = "";
                if (DatabaseController.getPreference(DatabaseController.TAG_REFERENCES,mPrinter.getName())!=null){

                    Log.i(TAG, "NOT NULLO");

                    download = DatabaseController.getPreference(DatabaseController.TAG_REFERENCES, mPrinter.getName());

                }else {

                    Log.i(TAG, "Çyesp NULLO");
                    download = LibraryController.getParentFolder() + "/temp/" + mPrinter.getJob().getFilename();

                    //Add it to the reference list
                    DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES, mPrinter.getName(),
                            LibraryController.getParentFolder() + "/temp/" + mPrinter.getJob().getFilename(), true);
                }



                //Check if we've downloaded the same file before
                //File downloadPath = new File(LibraryController.getParentFolder() + "/temp/", mPrinter.getJob().getFilename());
                File downloadPath = new File(download);

                if (downloadPath.exists()) {

                    Log.i(TAG, "Wait, I downloaded it once!");
                    openGcodePrintView(getActivity(), downloadPath.getAbsolutePath(), mRootView, R.id.view_gcode);

                    //File changed, remove jobpath
                    mPrinter.setJobPath(downloadPath.getAbsolutePath());

                    //We have to download it again
                } else {

                    //Remake temp folder if it's not available
                    if (!downloadPath.getParentFile().exists())
                        downloadPath.getParentFile().mkdirs();

                    Log.i(TAG, "Downloadinag " + downloadPath.getParentFile().getAbsolutePath() + " PLUS " + mPrinter.getJob().getFilename());

                    //Download file
                    OctoprintFiles.downloadFile(mContext, mPrinter.getAddress() + HttpUtils.URL_DOWNLOAD_FILES,
                            downloadPath.getParentFile().getAbsolutePath() + "/", mPrinter.getJob().getFilename());



                    Log.i(TAG, "Downloading and adding to preferences");

                    //Get progress dialog UI
                    View waitingForServiceDialogView = LayoutInflater.from(mContext).inflate(R.layout.dialog_progress_content_horizontal, null);
                    ((TextView) waitingForServiceDialogView.findViewById(R.id.progress_dialog_text)).setText(R.string.printview_download_dialog);

                    //Show progress dialog
                    MaterialDialog.Builder connectionDialogBuilder = new MaterialDialog.Builder(mContext);
                    connectionDialogBuilder.customView(waitingForServiceDialogView, true)
                            .autoDismiss(false);

                    //Progress dialog to notify command events
                    mDownloadDialog = new MaterialDialog.Builder(mContext)
                    .customView(waitingForServiceDialogView, true)
                    .autoDismiss(false)
                    .build();
                    mDownloadDialog.show();

                    //File changed, remove jobpath
                    mPrinter.setJobPath(null);
                }


            }

        isGcodeLoaded = true;
    }


    public void openGcodePrintView(Context context, String filePath, View rootView, int frameLayoutId) {
        //Context context = getActivity();
        mLayout = (FrameLayout) rootView.findViewById(frameLayoutId);
        File file = new File(filePath);

        DataStorage tempData = GcodeCache.retrieveGcodeFromCache(file.getAbsolutePath());

        if (tempData != null) {

            mDataGcode = tempData;
            drawPrintView();


        } else {

            mDataGcode = new DataStorage();
            GcodeFile.openGcodeFile(context, file, mDataGcode, ViewerMainFragment.PRINT_PREVIEW);

            GcodeCache.addGcodeToCache(mDataGcode);
        }


        mDataGcode.setActualLayer(mActualProgress);

    }

    public static boolean drawPrintView() {
        List<DataStorage> gcodeList = new ArrayList<DataStorage>();
        gcodeList.add(mDataGcode);

        mSurface = new ViewerSurfaceView(mContext, gcodeList, ViewerSurfaceView.LAYERS, ViewerMainFragment.PRINT_PREVIEW, null);

        mLayout.removeAllViews();
        mLayout.addView(mSurface, 0);

        changeProgress(mActualProgress);

        mSurface.setZOrderOnTop(true);

        JSONObject profile = ModelProfile.retrieveProfile(mContext, mPrinter.getProfile(), ModelProfile.TYPE_P);
        try {
            JSONObject volume = profile.getJSONObject("volume");

            mSurface.changePlate(new int[]{volume.getInt("width") / 2, volume.getInt("depth") / 2, volume.getInt("height")});
            mSurface.requestRender();

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return true;
    }

    public static void changeProgress(double percentage) {
        int maxLines = mDataGcode.getMaxLayer();
        int progress = (int) percentage * maxLines / 100;
        mDataGcode.setActualLayer(progress);
        if (mSurface != null) mSurface.requestRender();
    }

    @Override
    public void onDestroyView() {

        try {
            mContext.unregisterReceiver(onComplete);
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }

        super.onDestroyView();
    }

    /**
     * Receives the "download complete" event asynchronously
     */
    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            DownloadManager manager = (DownloadManager) ctxt.getSystemService(Context.DOWNLOAD_SERVICE);

            String filename = null;

            //Get the downloaded file name
            Bundle extras = intent.getExtras();
            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(extras.getLong(DownloadManager.EXTRA_DOWNLOAD_ID));
            Cursor c = manager.query(q);

            if (c.moveToFirst()) {
                int status = c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS));
                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    filename = c.getString(c.getColumnIndex(DownloadManager.COLUMN_TITLE));
                }
            }


            //If we have a stored path
            if (DatabaseController.isPreference(DatabaseController.TAG_REFERENCES, mPrinter.getName())) {

                String path = DatabaseController.getPreference(DatabaseController.TAG_REFERENCES, mPrinter.getName());

                File file = new File(path);


                if ((file.getName().equals(filename))){

                    //In case there was a previous cached file with the same path
                    GcodeCache.removeGcodeFromCache(path);



                    if (file.exists()) {

                        openGcodePrintView(mRootView.getContext(), path, mRootView, R.id.view_gcode);
                        mPrinter.setJobPath(path);

                        //Register receiver
                        mContext.unregisterReceiver(onComplete);

                        //DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES, mPrinter.getName(), null, false);

                    } else {

                        Toast.makeText(getActivity(), R.string.printview_download_toast_error, Toast.LENGTH_LONG).show();

                        //Register receiver
                        mContext.unregisterReceiver(onComplete);
                    }
                } else {

                }




            }


            if (mDownloadDialog != null) mDownloadDialog.dismiss();
        }
    };
}

