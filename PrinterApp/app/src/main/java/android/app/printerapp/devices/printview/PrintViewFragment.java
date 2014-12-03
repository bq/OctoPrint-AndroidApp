package android.app.printerapp.devices.printview;


import android.app.DownloadManager;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.SlidingUpPanelLayout;
import android.app.printerapp.devices.camera.CameraHandler;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.HttpUtils;
import android.app.printerapp.octoprint.OctoprintControl;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.viewer.DataStorage;
import android.app.printerapp.viewer.GcodeFile;
import android.app.printerapp.viewer.ViewerMainFragment;
import android.app.printerapp.viewer.ViewerSurfaceView;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.material.widget.PaperButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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

    //View references
    private TextView tv_printer;
    private TextView tv_file;
    private TextView tv_temp;
    private TextView tv_prog;

    private ProgressBar pb_prog;

    private PaperButton button_pause;
    private PaperButton button_stop;

    //File references
    private static DataStorage mDataGcode;
    private static ViewerSurfaceView mSurface;
    private static FrameLayout mLayout;
    private static FrameLayout mLayoutVideo;

    private View mRootView;

    //Context needed for file loading
    private static Context mContext;

    //TODO: temp variable for initial progress
    private static int mActualProgress = 0;

    private ProgressDialog mDownloadDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //Reference to View
        mRootView = null;

        //If is not new
        if (savedInstanceState == null) {

            //Necessary for gcode tracking
            mContext = getActivity();

            //Get the printer from the list
            Bundle args = getArguments();
            mPrinter = DevicesListController.getPrinter(args.getLong("id"));
            //getActivity().getActionBar().setTitle(mPrinter.getAddress().replace("/", ""));

            //Check printing status
            if (mPrinter.getStatus() == StateUtils.STATE_PRINTING) isPrinting = true;
            else {

                //TODO Set print status as 100% if it's not printing
                mActualProgress = 100;
                isPrinting = false;
            }

            //Show custom option menu
            setHasOptionsMenu(true);

            //Update the actionbar to show the up carat/affordance
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);

            //Inflate the fragment
            mRootView = inflater.inflate(R.layout.printview_layout,
                    container, false);

            /************************************************************************/

            //Show gcode tracking if there's a current path in the printer/preferences

            retrieveGcode();

            mCamera = new CameraHandler(mContext, mPrinter.getAddress());
            //Get video
            mLayoutVideo = (FrameLayout) mRootView.findViewById(R.id.printview_video);

            /* if (mPrinter.getVideo().getParent() != null) {
                mPrinter.getVideo().stopPlayback();
                ((ViewGroup) mPrinter.getVideo().getParent()).removeAllViews();
            }*/
            mLayoutVideo.addView(mCamera.getView());

            mCamera.startVideo();


            /***************************************************************************/


            //UI references
            tv_printer = (TextView) mRootView.findViewById(R.id.printview_printer_tag);
            tv_file = (TextView) mRootView.findViewById(R.id.printview_printer_file);
            tv_temp = (TextView) mRootView.findViewById(R.id.printview_temp);
            tv_prog = (TextView) mRootView.findViewById(R.id.printview_printer_progress);
            pb_prog = (ProgressBar) mRootView.findViewById(R.id.printview_progress_bar);

            button_pause = (PaperButton) mRootView.findViewById(R.id.printview_pause_button);
            button_stop = (PaperButton) mRootView.findViewById(R.id.printview_stop_button);

            button_pause.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isPrinting) {
                        OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "start");
                    } else {
                        OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "pause");
                    }
                }
            });

            button_stop.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "cancel");
                }
            });

            final EditText et_am = (EditText) mRootView.findViewById(R.id.et_amount);

            mRootView.findViewById(R.id.button_xy_down).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_xy_up).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", -Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_xy_left).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x", -Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_xy_right).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x", Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_z_down).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", -Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_z_up).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", Integer.parseInt(et_am.getText().toString()));
                }
            });

            mRootView.findViewById(R.id.button_z_home).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "home", null, 0);
                }
            });


            /***************** SLIDE PANEL ************************************/

            //Slide panel setup

            SlidingUpPanelLayout slidePanel = (SlidingUpPanelLayout) mRootView.findViewById(R.id.sliding_panel);
            CheckBox imageButton = (CheckBox) mRootView.findViewById(R.id.expand_button_checkbox);
            slidePanel.setDragView(imageButton);

            refreshData();

        }
        return mRootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
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
                getActivity().onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
        tv_printer.setText(mPrinter.getDisplayName() + " : " + mPrinter.getMessage() + " [" + mPrinter.getPort() + "]");
        tv_file.setText(mPrinter.getJob().getFilename());
        tv_temp.setText(mPrinter.getTemperature() + "ºC / " + mPrinter.getTempTarget() + "ºC");

        if ((mPrinter.getStatus() == StateUtils.STATE_PRINTING) ||
                (mPrinter.getStatus() == StateUtils.STATE_PAUSED)) {

            isPrinting = true;
            tv_prog.setText(getProgress(mPrinter.getJob().getProgress()) + "% (" + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTimeLeft()) +
                    " left / " + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTime()) + " elapsed) - ");

            Double n = Double.valueOf(mPrinter.getJob().getProgress());
            pb_prog.setProgress(n.intValue());

            if (mDataGcode!=null)
                changeProgress(Double.valueOf(mPrinter.getJob().getProgress()));

        } else {

            if (!mPrinter.getLoaded()) tv_file.setText(R.string.devices_upload_waiting);
            tv_prog.setText(mPrinter.getMessage() + " - ");
            isPrinting = false;
        }

        getActivity().invalidateOptionsMenu();

    }


    //External method to convert seconds to HHmmss
    private String ConvertSecondToHHMMString(String secondtTime) {
        String time = "--:--:--";

        if (!secondtTime.equals("null")) {

            TimeZone tz = TimeZone.getTimeZone("UTC");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.US);
            df.setTimeZone(tz);
            time = df.format(new Date(Integer.parseInt(secondtTime) * 1000L));
        }


        return time;

    }

    public void stopCameraPlayback() {

        mCamera.getView().stopPlayback();
        mCamera.getView().setVisibility(View.GONE);


    }

    //TODO Properly close the video when destroying the view
    @Override
    public void onDestroy() {
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
    public void retrieveGcode(){

        if ((mPrinter.getJobPath()!=null) && (!isPrinting)){

            Log.i(TAG,"Sigh, loading " + mPrinter.getJobPath());

            openGcodePrintView(getActivity(), mPrinter.getJobPath(), mRootView, R.id.view_gcode);

        }else {
            //if (mPrinter.getJobPath()==null) mPrinter.setJobPath("null");


            //File tempFile = new File(mPrinter.getJobPath());

            //Log.i(TAG,"My path is " + mPrinter.getJobPath());

            if (!mPrinter.getJob().getFilename().equals("null"))
                /*if ((mPrinter.getJob().getFilename().equals(tempFile.getName())) && (tempFile.exists())){

                    Log.i(TAG,"It's the same and I have it, DO OPEN");

                    //openGcode();
                    openGcodePrintView(getActivity(), mPrinter.getJobPath(), mRootView, R.id.view_gcode);


                } else*/ {

                    Log.i(TAG,"Either it's not the same or I don't have it, download: " + mPrinter.getJob().getFilename());


                    File downloadPath = new File(LibraryController.getParentFolder() + "/temp/", mPrinter.getJob().getFilename());

                    if (downloadPath.exists()){

                        Log.i(TAG,"Wait, I downloaded it once!");
                        openGcodePrintView(getActivity(), downloadPath.getAbsolutePath(), mRootView, R.id.view_gcode);


                    } else {

                        if (!downloadPath.getParentFile().exists()) downloadPath.getParentFile().mkdirs();

                        OctoprintFiles.downloadFile(mContext, mPrinter.getAddress() + HttpUtils.URL_DOWNLOAD_FILES,
                                LibraryController.getParentFolder() + "/temp/", mPrinter.getJob().getFilename());

                        DatabaseController.handlePreference("References", mPrinter.getName(),
                                LibraryController.getParentFolder() + "/temp/" + mPrinter.getJob().getFilename(), true);

                        Log.i(TAG, "Downloading and adding to preferences");

                        //Progress dialog to notify command events
                        mDownloadDialog = new ProgressDialog(getActivity());
                        mDownloadDialog.setMessage(getActivity().getString(R.string.printview_download_dialog));
                        mDownloadDialog.show();

                        //Register receiver
                        mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));


                    }
                    //downloadGcode();
                    mPrinter.setJobPath(null);

            }



        }

    }


    public void openGcodePrintView(Context context, String filePath, View rootView, int frameLayoutId) {
        //Context context = getActivity();
        mLayout = (FrameLayout) rootView.findViewById(frameLayoutId);
        File file = new File(filePath);

        mDataGcode = new DataStorage();
        GcodeFile.openGcodeFile(context, file, mDataGcode, ViewerMainFragment.PRINT_PREVIEW);
        mDataGcode.setActualLayer(0);

    }

    public static boolean drawPrintView() {
        List<DataStorage> gcodeList = new ArrayList<DataStorage>();
        gcodeList.add(mDataGcode);

        mSurface = new ViewerSurfaceView(mContext, gcodeList, ViewerSurfaceView.LAYERS, ViewerMainFragment.PRINT_PREVIEW, null);
        mLayout.removeAllViews();
        mLayout.addView(mSurface, 0);

        changeProgress(mActualProgress);


        return true;
    }

    public static void changeProgress(double percentage) {
        int maxLines = mDataGcode.getMaxLayer();
        int progress = (int) percentage * maxLines / 100;
        mDataGcode.setActualLayer(progress);
        if (mSurface != null) mSurface.requestRender();
    }

    /**
     * Receives the "download complete" event asynchronously
     */
    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            Log.i(TAG, "Received download completed");


            //If we have a stored path
            if (DatabaseController.isPreference("References", mPrinter.getName())) {

                String path = DatabaseController.getPreference("References", mPrinter.getName());

                Log.i(TAG, "Hey we had a reference for " + path);

                File file = new File(path);

                if (file.exists()) {

                    DatabaseController.handlePreference("References", mPrinter.getName(), null, false);

                    Log.i(TAG, "Cool, let's show it");

                    openGcodePrintView(mRootView.getContext(), path, mRootView, R.id.view_gcode);
                    //mPrinter.setJobPath(path);

                    //Register receiver
                    mContext.unregisterReceiver(onComplete);

                } else {
                    Log.i(TAG,"But file didn't download ok");

                    //Register receiver
                    mContext.unregisterReceiver(onComplete);

                }


            }

            if (mDownloadDialog!=null)mDownloadDialog.dismiss();
        }
    };
}

