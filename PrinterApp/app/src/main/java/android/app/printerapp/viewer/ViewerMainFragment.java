package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.Fragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.util.ui.CustomPopupWindow;
import android.app.printerapp.util.ui.ListIconPopupWindowAdapter;
import android.app.printerapp.viewer.sidepanel.SidePanelHandler;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.devsmart.android.ui.HorizontalListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class ViewerMainFragment extends Fragment {
    //Tabs
    private static final int NORMAL = 0;
    private static final int OVERHANG = 1;
    private static final int TRANSPARENT = 2;
    private static final int XRAY = 3;
    private static final int LAYER = 4;

    //Constants
    public static final int DO_SNAPSHOT = 0;
    public static final int DONT_SNAPSHOT = 1;
    public static final int PRINT_PREVIEW = 3;
    public static final boolean STL = true;
    public static final boolean GCODE = false;

    private static final float POSITIVE_ANGLE = 15;
    private static final float NEGATIVE_ANGLE = -15;

    //Variables
    private static File mFile;

    private static ViewerSurfaceView mSurface;
    private static FrameLayout mLayout;

    //Advanced settings expandable panel
    private int mSettingsPanelMinHeight;

    //Buttons
    private RadioGroup mGroupMovement;

    private Button mBackWitboxFaces;
    private Button mRightWitboxFaces;
    private Button mLeftWitboxFaces;
    private Button mDownWitboxFaces;
    private static SeekBar mSeekBar;

    private static List<DataStorage> mDataList = new ArrayList<DataStorage>();

    //Undo button bar
    private static LinearLayout mUndoButtonBar;

    //Edition menu variables
    private static ProgressBar mProgress;

    private static Context mContext;
    private static View mRootView;

    private static LinearLayout mStatusBottomBar;
    private static LinearLayout mRotationLayout;
    private static SeekBar mRotationSeekbar;

    /**
     * ****************************************************************************
     */
    private static SlicingHandler mSlicingHandler;
    private SidePanelHandler mSidePanelHandler;

    private static int mCurrentType;
    private static int[] mCurrentPlate;

    private static TextView mRotationText;
    private static TextView mAxisText;
    private static int mCurrentAxis;

    //Empty constructor
    public ViewerMainFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retain instance to keep the Fragment from destroying itself
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Reference to View
        mRootView = null;

        //If is not new
        if (savedInstanceState == null) {

            //Show custom option menu
            setHasOptionsMenu(true);

            //Inflate the fragment
            mRootView = inflater.inflate(R.layout.print_panel_main,
                    container, false);

            mContext = getActivity();


            //Register receiver
            mContext.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

            initUIElements();

//            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

            //Init slicing elements
            mSlicingHandler = new SlicingHandler(getActivity());
            mSidePanelHandler = new SidePanelHandler(mSlicingHandler, getActivity(), mRootView);
            mCurrentType = WitboxFaces.TYPE_WITBOX;
            mCurrentPlate = new int[]{WitboxFaces.WITBOX_LONG, WitboxFaces.WITBOX_WITDH, WitboxFaces.WITBOX_HEIGHT};

            mSurface = new ViewerSurfaceView(mContext, mDataList, NORMAL, DONT_SNAPSHOT, mSlicingHandler);
            draw();


        }

        return mRootView;

    }

    public static void resetWhenCancel() {


        //Crashes on printview
        try {
            mDataList.remove(mDataList.size() - 1);
            mSurface.requestRender();

        } catch (Exception e) {

            e.printStackTrace();

        }


    }

    /**
     * ********************** UI ELEMENTS *******************************
     */

    private void initUIElements() {
        //Set tabs
        setTabHost(mRootView);

        //Set behavior of the expandable panel
        final FrameLayout expandablePanel = (FrameLayout) mRootView.findViewById(R.id.advanced_options_expandable_panel);
        expandablePanel.post(new Runnable() { //Get the initial height of the panel after onCreate is executed
            @Override
            public void run() {
                mSettingsPanelMinHeight = expandablePanel.getMeasuredHeight();
            }
        });
        /*final CheckBox expandPanelButton = (CheckBox) mRootView.findViewById(R.id.expand_button_checkbox);
        expandPanelButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Expand/collapse the expandable panel
                if (isChecked) ExpandCollapseAnimation.collapse(expandablePanel, mSettingsPanelMinHeight);
                else ExpandCollapseAnimation.expand(expandablePanel);
            }
        });*/

        //Set elements to handle the model
        mSeekBar = (SeekBar) mRootView.findViewById(R.id.barLayer);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            mSeekBar.getThumb().mutate().setAlpha(0);
        mSeekBar.setVisibility(View.INVISIBLE);

        //Undo button bar
        mUndoButtonBar = (LinearLayout) mRootView.findViewById(R.id.model_button_undo_bar_linearlayout);

        mLayout = (FrameLayout) mRootView.findViewById(R.id.viewer_container_framelayout);

        mGroupMovement = (RadioGroup) mRootView.findViewById(R.id.radioGroupMovement);
        mGroupMovement.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioRotation:
                        mSurface.setMovementMode(ViewerSurfaceView.ROTATION_MODE);
                        break;
                    case R.id.radioTranslation:
                        mSurface.setMovementMode(ViewerSurfaceView.TRANSLATION_MODE);
                        break;
                }
            }
        });

        mBackWitboxFaces = (Button) mRootView.findViewById(R.id.back);
        mBackWitboxFaces.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurface.showBackWitboxFace();
            }

        });

        mRightWitboxFaces = (Button) mRootView.findViewById(R.id.right);
        mRightWitboxFaces.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurface.showRightWitboxFace();
            }

        });

        mLeftWitboxFaces = (Button) mRootView.findViewById(R.id.left);
        mLeftWitboxFaces.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurface.showLeftWitboxFace();
            }

        });

        mDownWitboxFaces = (Button) mRootView.findViewById(R.id.down);
        mDownWitboxFaces.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSurface.showDownWitboxFace();
            }

        });

        mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mDataList.get(0).setActualLayer(progress);
                mSurface.requestRender();
            }
        });


        /*****************************
         * EXTRA
         *****************************/
        mProgress = (ProgressBar) mRootView.findViewById(R.id.progress_bar);
        mProgress.setVisibility(View.GONE);
        mRotationText = (TextView) mRootView.findViewById(R.id.text_rotation);
        mAxisText = (TextView) mRootView.findViewById(R.id.text_axis);
        mRotationSeekbar = (SeekBar) mRootView.findViewById(R.id.rotation_seek_bar);
        mRotationSeekbar.setProgress(12);
        mRotationText.setText("");
        mRotationSeekbar.setMax(24);
        mRotationSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            boolean lock = true;

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                //Calculation on a 12 point seekbar
                float newAngle = (i - 12) * POSITIVE_ANGLE;


                if (!lock) {

                    switch (mCurrentAxis) {

                        case 0:
                            mSurface.rotateAngleAxisX(newAngle);
                            break;
                        case 1:
                            mSurface.rotateAngleAxisY(newAngle);
                            break;
                        case 2:
                            mSurface.rotateAngleAxisZ(newAngle);
                            break;
                        default:
                            return;

                    }

                }


                mRotationText.setText((int) newAngle + "º");

                mSurface.requestRender();

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                lock = false;


            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                lock = true;
                mSurface.refreshRotatedObject();

            }
        });

        mRotationLayout = (LinearLayout) mRootView.findViewById(R.id.model_button_rotate_bar_linearlayout);
        mStatusBottomBar = (LinearLayout) mRootView.findViewById(R.id.model_status_bottom_bar);
        mRotationLayout.setVisibility(View.INVISIBLE);
        mStatusBottomBar.setVisibility(View.VISIBLE);

        mCurrentAxis = -1;

    }

    /**
     * Change the current rotation axis and update the text accordingly
     * <p/>
     * Alberto
     */
    public static void changeCurrentAxis(int currentAxis) {

        mCurrentAxis = currentAxis;

        float currentAngle = 12;

        switch (mCurrentAxis) {

            case 0:
                mAxisText.setText("Eje X");

                break;

            case 1:
                mAxisText.setText("Eje Y");

                break;
            case 2:
                mAxisText.setText("Eje Z");

                break;
            default:
                mAxisText.setText("Eje X");

                break;

        }

        mSurface.setRendererAxis(mCurrentAxis);

        mRotationSeekbar.setProgress((int) currentAngle);
        mRotationText.setText("");

    }


    /**
     * *************************************************************************
     */

    public static void initSeekBar(int max) {
        mSeekBar.setMax(max);
        mSeekBar.setProgress(max);
    }

    public static void configureProgressState(int v) {
        if (v == View.GONE) mSurface.requestRender();
        else if (v == View.VISIBLE) mProgress.bringToFront();

        mProgress.setVisibility(v);
    }


    /**
     * ********************** OPTIONS MENU *******************************
     */
    //Create option menu and inflate viewer menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.print_panel_menu, menu);

    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {

        switch (item.getItemId()) {

            case R.id.viewer_open:
                FileBrowser.openFileBrowser(getActivity(), FileBrowser.VIEWER, getString(R.string.choose_file), ".stl", ".gcode");
                return true;

            case R.id.viewer_save:
                saveNewProject();
                return true;

            case R.id.viewer_save_gcode:
                saveGcodeDialog();
                return true;

            case R.id.viewer_restore:
                optionRestoreView();
                return true;

            case R.id.viewer_autofit:
                //Autofit
                return true;

            case R.id.viewer_clean:

                optionClean();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Constructor for the tab host.
     */
    public void setTabHost(View v) {
        final TabHost tabs = (TabHost) v.findViewById(R.id.tabhost_views);
        tabs.setup();

        TabHost.TabSpec spec = tabs.newTabSpec("Normal");
        spec.setIndicator(getString(R.string.viewer_button_normal));
        spec.setContent(R.id.tab_normal);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Overhang");
        spec.setIndicator(getString(R.string.viewer_button_overhang));
        spec.setContent(R.id.tab_overhang);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Transparent");
        spec.setIndicator(getString(R.string.viewer_button_transparent));
        spec.setContent(R.id.tab_transparent);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Xray");
        spec.setIndicator(getString(R.string.viewer_button_xray));
        spec.setContent(R.id.tab_xray);
        tabs.addTab(spec);

        spec = tabs.newTabSpec("Gcode");
        spec.setIndicator(getString(R.string.viewer_button_layers));
        spec.setContent(R.id.tab_gcodes);
        tabs.addTab(spec);

        tabs.setCurrentTab(0);

        //Set style for the tab widget
        for (int i = 0; i < tabs.getTabWidget().getChildCount(); i++) {
            final View tab = tabs.getTabWidget().getChildTabViewAt(i);
            tab.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_indicator_ab_green));
            TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.body_text_2));
        }

        tabs.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {

                switch (tabs.getCurrentTab()) {
                    case NORMAL:
                        changeStlViews(ViewerSurfaceView.NORMAL);
                        break;
                    case OVERHANG:
                        changeStlViews(ViewerSurfaceView.OVERHANG);
                        break;
                    case TRANSPARENT:
                        changeStlViews(ViewerSurfaceView.TRANSPARENT);
                        break;

                    case XRAY:
                        changeStlViews(ViewerSurfaceView.XRAY);
                        break;
                    case LAYER:

                        File tempFile = new File(LibraryController.getParentFolder() + "/temp/temp.gco");
                        if (tempFile.exists()) {

                            //It's the last file
                            if (DatabaseController.getPreference("Slicing", "Last") == null) {

                                //Open desired file
                                openFile(tempFile.getAbsolutePath());

                            } else {
                                Toast.makeText(getActivity(), R.string.viewer_slice_wait, Toast.LENGTH_SHORT).show();
                            }

                        } else {

                            /*if (mFile != null) {
                                showGcodeFiles();
                            } else
                                Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();*/

                            Toast.makeText(getActivity(), R.string.viewer_slice_wait, Toast.LENGTH_SHORT).show();

                            //TODO hardcoded
                            tabs.setCurrentTab(0);
                        }
                        break;

                    default:
                        break;
                }
            }
        });

    }

    /**
     * ********************** FILE MANAGEMENT *******************************
     */


    /**
     * Restore the original view and discard the modifications by clearing the data list
     */
    public void optionRestoreView() {


        if (mDataList.size() > 0) {
            String pathStl = mDataList.get(0).getPathFile();
            mDataList.clear();

            openFile(pathStl);
        }


    }

    /**
     * Clean the print panel and delete all references
     */
    public static void optionClean() {

        //Delete slicing reference
        //DatabaseController.handlePreference("Slicing", "Last", null, false);

        mDataList.clear();
        mFile = null;
        mSlicingHandler.setOriginalProject(null);
        mSlicingHandler.setLastReference(null);
        mSeekBar.setVisibility(View.INVISIBLE);
        mSurface.requestRender();

    }

    public static void openFile(String filePath) {
        DataStorage data = null;

        //Open the file
        if (LibraryController.hasExtension(0, filePath)) {

            data = new DataStorage();
            mFile = new File(filePath);
            StlFile.openStlFile(mContext, mFile, data, DONT_SNAPSHOT);


        } else if (LibraryController.hasExtension(1, filePath)) {

            data = new DataStorage();
            if (!filePath.contains("/temp")) optionClean();
            mFile = new File(filePath);
            GcodeFile.openGcodeFile(mContext, mFile, data, DONT_SNAPSHOT);

        }
        mDataList.add(data);

        //Adding original project //TODO elsewhere?
        if (mSlicingHandler != null)
            if (mSlicingHandler.getOriginalProject() == null)
                mSlicingHandler.setOriginalProject(mFile.getParentFile().getParent());
    }

    private void changeStlViews(int state) {
        if (mFile != null) {
            if (!mFile.getPath().endsWith(".stl") && !mFile.getPath().endsWith(".STL"))
                openStlFile();
            else mSurface.configViewMode(state);
        } else
            Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();
    }

    private void openStlFile() {

        //Name didn't work with new gcode creation so new stuff!
        //String name = mFile.getName().substring(0, mFile.getName().lastIndexOf('.'));

        String pathStl;

        if (mSlicingHandler.getLastReference() != null) {

            pathStl = mSlicingHandler.getLastReference();
            openFile(pathStl);

        } else {

            //Here's the new stuff!
            pathStl = //LibraryController.getParentFolder().getAbsolutePath() + "/Files/" + name + "/_stl/";
                    mFile.getParentFile().getParent() + "/_stl/";
            File f = new File(pathStl);

            Log.i("OUT", "trying to open " + pathStl);

            //Only when it's a project
            if (f.isDirectory() && f.list().length > 0) {
                openFile(pathStl + f.list()[0]);
            } else {
                Toast.makeText(getActivity(), R.string.devices_toast_no_stl, Toast.LENGTH_SHORT).show();
            }
        }


    }


    //TODO remove
    private void showGcodeFiles() {
        //Logic for getting file type
        String name = mFile.getName().substring(0, mFile.getName().lastIndexOf('.'));
        String pathProject = LibraryController.getParentFolder().getAbsolutePath() + "/Files/" + name;
        File f = new File(pathProject);

        //Only when it's a project
        if (f.isDirectory()) {
            String path = pathProject + "/_gcode";

            if (LibraryController.isProject(f) && new File(path).list().length > 0) {
                AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
                adb.setTitle(mContext.getString(R.string.gcode_viewer));

                //We need the alertdialog instance to dismiss it
                final AlertDialog ad = adb.create();

                if (path != null) {
                    final File[] files = (new File(path)).listFiles();

                    //Create a string-only array for the adapter
                    if (files != null) {
                        String[] names = new String[files.length];

                        for (int i = 0; i < files.length; i++) {
                            names[i] = files[i].getName();
                        }

                        adb.setAdapter(new ArrayAdapter<String>(mContext, android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File m = files[which];

                                //Open desired file
                                openFile(m.getAbsolutePath());

                                ad.dismiss();
                            }
                        });

                    }
                }
                adb.show();
            } else {
                Toast.makeText(getActivity(), R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(), R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
        }
    }

    public static void draw() {
        //Once the file has been opened, we need to refresh the data list. If we are opening a .gcode file, we need to ic_action_delete the previous files (.stl and .gcode)
        //If we are opening a .stl file, we need to ic_action_delete the previous file only if it was a .gcode file.
        //We have to do this here because user can cancel the opening of the file and the Print Panel would appear empty if we clear the data list.

        String filePath = "";
        if (mFile != null) filePath = mFile.getAbsolutePath();

        if (LibraryController.hasExtension(0, filePath)) {
            if (mDataList.size() > 1) {
                if (LibraryController.hasExtension(1, mDataList.get(mDataList.size() - 2).getPathFile())) {
                    mDataList.remove(mDataList.size() - 2);
                }
            }
            Geometry.relocateIfOverlaps(mDataList);
            mSeekBar.setVisibility(View.INVISIBLE);

        } else if (LibraryController.hasExtension(1, filePath)) {
            if (mDataList.size() > 1)
                while (mDataList.size() > 1) {
                    mDataList.remove(0);
                }
            mSeekBar.setVisibility(View.VISIBLE);
        }

        //Add the view
        mLayout.removeAllViews();
        mLayout.addView(mSurface, 0);
        mLayout.addView(mSeekBar, 1);
//        mLayout.addView(mUndoButtonBar, 3);
//        mLayout.addView(mRotationLayout, 2);
    }

    /**
     * ********************** SAVE FILE *******************************
     */
    private void saveNewProject() {
        View dialogText = LayoutInflater.from(mContext).inflate(R.layout.set_project_name_dialog, null);
        final EditText proyectNameText = (EditText) dialogText.findViewById(R.id.proyect_name);

        proyectNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                proyectNameText.setError(null);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //do nothing
            }
        });


        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setView(dialogText)
                .setTitle(mContext.getString(R.string.project_name))
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .setPositiveButton(R.string.ok, null); //onclicklistener=null to avoid to dismiss the dialog

        //We need the alertdialog instance to dismiss it
        final AlertDialog ad = adb.create();
        ad.show();

        //We look for
        Button okButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
        okButton.setOnClickListener(new CustomListener(ad, proyectNameText));
    }

    private class CustomListener implements View.OnClickListener {
        private final Dialog dialog;
        private final EditText proyectNameText;

        public CustomListener(Dialog dialog, EditText proyectNameText) {
            this.dialog = dialog;
            this.proyectNameText = proyectNameText;
        }

        @Override
        public void onClick(View v) {

            /**
             * Added filename check for .stl files.
             *
             *  Alberto
             */

            if (LibraryController.hasExtension(0, mFile.getName())) {
                if (StlFile.checkIfNameExists(proyectNameText.getText().toString()))
                    proyectNameText.setError(mContext.getString(R.string.proyect_name_not_available));
                else {
                    if (StlFile.saveModel(mDataList, proyectNameText.getText().toString(), null))
                        dialog.dismiss();
                    else {
                        Toast.makeText(mContext, R.string.error_saving_invalid_model, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            } else {
                Toast.makeText(mContext, R.string.devices_toast_no_stl, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

        }
    }


    /**
     * ************************ SAVE GCODE ************************************ by Alberto
     */

    public void saveGcodeDialog() {

        //Original file to save
        final File fileFrom = new File(LibraryController.getParentFolder() + "/temp/temp.gco");


        //if there is a temporary sliced gcode
        if (fileFrom.exists()) {

            //Get original project
            final File actualFile = new File(mSlicingHandler.getOriginalProject());

            AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
            adb.setTitle(R.string.library_create_dialog_title);


            //Select new name for the gcode
            final EditText et = new EditText(mContext);
            et.setText(actualFile.getName());

            adb.setView(et);

            adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    //Save gcode
                    File fileTo = new File(actualFile + "/_gcode/" + et.getText().toString() + ".gcode");

                    //Delete file if success
                    if (!fileFrom.renameTo(fileTo)) {

                        openFile(fileTo.getAbsolutePath());

                        if (fileFrom.delete()) {
                            Log.i("OUT", "File deletedillo");
                        }
                    }

                    /**
                     * Use an intent because it's an asynchronous static method without any reference (yet)
                     */
                    Intent intent = new Intent("notify");
                    intent.putExtra("message", "Files");
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

                }
            });

            adb.show();

        } else {
            Toast.makeText(getActivity(), R.string.viewer_slice_wait, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ********************** SURFACE CONTROL *******************************
     */
    //This method will set the visibility of the surfaceview so it doesn't overlap
    //with the video grid view
    public void setSurfaceVisibility(int i) {

        if (mSurface != null) {
            switch (i) {
                case 0:
                    mSurface.setVisibility(View.GONE);
                    break;
                case 1:
                    mSurface.setVisibility(View.VISIBLE);
                    break;
            }
        }
    }

    private static PopupWindow mActionModePopupWindow;
    private static PopupWindow mCurrentActionPopupWindow;

    /**
     * ********************** ACTION MODE *******************************
     */

    /**
     * Show a pop up window with the available actions of the item
     */
    public static void showActionModePopUpWindow() {

        if (mActionModePopupWindow == null) {

            //Get the content view of the pop up window
            final LinearLayout popupLayout = (LinearLayout) ((Activity) mContext).getLayoutInflater()
                    .inflate(R.layout.item_edit_popup_menu, null);
            popupLayout.measure(0, 0);

            //Set the behavior of the action buttons
            int imageButtonHeight = 0;
            for (int i = 0; i < popupLayout.getChildCount(); i++) {
                View v = popupLayout.getChildAt(i);
                if (v instanceof ImageButton) {
                    ImageButton ib = (ImageButton) v;
                    imageButtonHeight = ib.getMeasuredHeight();
                    ib.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            onActionItemSelected((ImageButton) view);
                        }
                    });
                }
            }

            //Show the pop up window in the correct position
            int[] viewerContainerCoordinates = new int[2];
            mLayout.getLocationOnScreen(viewerContainerCoordinates);
            int popupLayoutPadding = (int) mContext.getResources().getDimensionPixelSize(R.dimen.content_padding_normal);
            int popupLayoutWidth = popupLayout.getMeasuredWidth();
            int popupLayoutHeight = popupLayout.getMeasuredHeight();
            final int popupLayoutX = viewerContainerCoordinates[0] + mLayout.getWidth() - popupLayoutWidth;
            final int popupLayoutY = viewerContainerCoordinates[1] + imageButtonHeight + popupLayoutPadding;

            mActionModePopupWindow = (new CustomPopupWindow(popupLayout, popupLayoutWidth,
                    popupLayoutHeight, R.style.SlideRightAnimation).getPopupWindow());

            mActionModePopupWindow.showAtLocation(mSurface, Gravity.NO_GRAVITY,
                    popupLayoutX, popupLayoutY);
        }
    }

    /**
     * Hide the action mode pop up window
     */
    public static void hideActionModePopUpWindow() {
        if (mActionModePopupWindow != null) {
            mActionModePopupWindow.dismiss();
            hideCurrentActionPopUpWindow();
            mSurface.exitEditionMode();
            mRotationLayout.setVisibility(View.INVISIBLE);
            mStatusBottomBar.setVisibility(View.VISIBLE);
            mActionModePopupWindow = null;
            mSurface.setRendererAxis(-1);
        }
    }

    /**
     * Hide the current action pop up window if it is showing
     */
    public static void hideCurrentActionPopUpWindow() {
        if (mCurrentActionPopupWindow != null) {
            mCurrentActionPopupWindow.dismiss();
            mCurrentActionPopupWindow = null;
        }
    }

    /**
     * Perform the required action depending on the pressed button
     *
     * @param item Action button that has been pressed
     */
    public static void onActionItemSelected(final ImageButton item) {

        mRotationLayout.setVisibility(View.INVISIBLE);
        mStatusBottomBar.setVisibility(View.VISIBLE);
        mSurface.setRendererAxis(-1);

        selectActionButton(item.getId());

        switch (item.getId()) {
            case R.id.move_item_button:
                mSurface.setEditionMode(ViewerSurfaceView.MOVE_EDITION_MODE);
                break;
            case R.id.rotate_item_button:
                final String[] actionButtonsValues = mContext.getResources().getStringArray(R.array.rotate_model_values);
                final TypedArray actionButtonsIcons = mContext.getResources().obtainTypedArray(R.array.rotate_model_icons);
                showHorizontalMenuPopUpWindow(item, actionButtonsValues, actionButtonsIcons, new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        changeCurrentAxis(Integer.parseInt(actionButtonsValues[position]));
                        mRotationLayout.setVisibility(View.VISIBLE);
                        mStatusBottomBar.setVisibility(View.INVISIBLE);
                        mSurface.setEditionMode(ViewerSurfaceView.ROTATION_EDITION_MODE);
                        hideCurrentActionPopUpWindow();
                        item.setImageResource(actionButtonsIcons.getResourceId(position, -1));
                    }
                });
                break;
            case R.id.scale_item_button:
                mSurface.setEditionMode(ViewerSurfaceView.SCALED_EDITION_MODE);
                break;
                /*case R.id.mirror:
                    mSurface.setEditionMode(ViewerSurfaceView.MIRROR_EDITION_MODE);
                    mSurface.doMirror();

                    slicingCallback();
                    break;*/
            case R.id.multiply_item_button:
                shoMultiplyDialog();
                break;
            case R.id.delete_item_button:
                mSurface.deleteObject();
                hideActionModePopUpWindow();
                break;
        }

    }


    /**
     * Set the state of the selected action button
     *
     * @param selectedId Id of the action button that has been pressed
     */
    public static void selectActionButton(int selectedId) {

        if (mActionModePopupWindow != null) {
            //Get the content view of the pop up window
            final LinearLayout popupLayout = (LinearLayout) mActionModePopupWindow.getContentView();

            //Set the behavior of the action buttons
            for (int i = 0; i < popupLayout.getChildCount(); i++) {
                View v = popupLayout.getChildAt(i);
                if (v instanceof ImageButton) {
                    ImageButton ib = (ImageButton) v;
                    if (ib.getId() == selectedId)
                        ib.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.oval_background_green));
                    else
                        ib.setBackgroundDrawable(mContext.getResources().getDrawable(R.drawable.action_button_selector_dark));
                }
            }
        }
    }

    /**
     * Show a pop up window with a horizontal list view as a content view
     */
    public static void showHorizontalMenuPopUpWindow(View currentView, String[] actionButtonsValues, TypedArray actionButtonsIcons, AdapterView.OnItemClickListener onItemClickListener) {

        HorizontalListView landscapeList = new HorizontalListView(mContext, null);
        ListIconPopupWindowAdapter listAdapter = new ListIconPopupWindowAdapter(mContext, actionButtonsValues, actionButtonsIcons, null);
        landscapeList.setOnItemClickListener(onItemClickListener);
        landscapeList.setAdapter(listAdapter);

        landscapeList.measure(0, 0);

        int popupLayoutHeight = 0;
        int popupLayoutWidth = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View mView = listAdapter.getView(i, null, landscapeList);
            mView.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            popupLayoutHeight = mView.getMeasuredHeight();
            popupLayoutWidth += mView.getMeasuredWidth();
        }

        //Show the pop up window in the correct position
        int[] actionButtonCoordinates = new int[2];
        currentView.getLocationOnScreen(actionButtonCoordinates);
        int popupLayoutPadding = (int) mContext.getResources().getDimensionPixelSize(R.dimen.content_padding_normal);
        final int popupLayoutX = actionButtonCoordinates[0] - popupLayoutWidth - popupLayoutPadding / 2;
        final int popupLayoutY = actionButtonCoordinates[1];

        mCurrentActionPopupWindow = (new CustomPopupWindow(landscapeList, popupLayoutWidth,
                popupLayoutHeight + popupLayoutPadding, R.style.SlideRightAnimation).getPopupWindow());

        mCurrentActionPopupWindow.showAtLocation(mSurface, Gravity.NO_GRAVITY, popupLayoutX, popupLayoutY);
    }

    /**
     * ********************** MULTIPLY ELEMENTS *******************************
     */

    public static void shoMultiplyDialog() {
        View dialogText = LayoutInflater.from(mContext).inflate(R.layout.set_copies_dialog, null);
        final NumberPicker numPicker = (NumberPicker) dialogText.findViewById(R.id.number_copies);
        numPicker.setMaxValue(10);
        numPicker.setMinValue(0);

        //Remove soft-input from number picker
        numPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
        adb.setView(dialogText)
                .setTitle(mContext.getString(R.string.viewer_menu_multiply_title))
                .setCancelable(false)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        drawCopies(numPicker.getValue());
                        slicingCallback();
                    }
                });

        adb.show();
    }

    private static void drawCopies(int numCopies) {
        int model = mSurface.getObjectPresed();
        int num = 0;

        while (num < numCopies) {
            final DataStorage newData = new DataStorage();
            newData.copyData(mDataList.get(model));
            mDataList.add(newData);

            /**
             * Check if the piece is out of the plate and stop multiplying
             */
            if (!Geometry.relocateIfOverlaps(mDataList)) {

                Toast.makeText(mContext, R.string.viewer_multiply_error, Toast.LENGTH_LONG).show();
                mDataList.remove(newData);
                break;

            }
            ;
            num++;
        }

        draw();
    }

/**
 * **************************** PROGRESS BAR FOR SLICING ******************************************
 */

    /**
     * Static method to show the progress bar by sending an integer when receiving data from the socket
     *
     * @param i either -1 to hide the progress bar, 0 to show an indefinite bar, or a normal integer
     */
    public static void showProgressBar(int status, int i) {


        if (mRootView != null) {

            LinearLayout ll = (LinearLayout) mRootView.findViewById(R.id.model_status_bottom_bar);

            ProgressBar pb = (ProgressBar) mRootView.findViewById(R.id.progress_slice);
            TextView tv = (TextView) mRootView.findViewById(R.id.viewer_text_progress_slice);

            ll.bringToFront();
            ll.setVisibility(View.VISIBLE);
            pb.setVisibility(View.VISIBLE);

            switch (status) {

                case StateUtils.SLICER_HIDE:

                    tv.setText("Downloaded");
                    pb.setVisibility(View.INVISIBLE);

                    break;

                case StateUtils.SLICER_UPLOAD:

                    tv.setText("Uploading...");
                    pb.setIndeterminate(true);

                    break;

                case StateUtils.SLICER_SLICE:

                    tv.setText("Slicing...");

                    if (i == 0) {
                        pb.setIndeterminate(true);

                    } else if (i == 100) {

                        pb.setIndeterminate(false);

                    } else {

                        pb.setProgress(i);
                        pb.setIndeterminate(false);

                    }

                    mRootView.invalidate();

                    break;

                case StateUtils.SLICER_DOWNLOAD:

                    tv.setText("Downloading...");
                    pb.setIndeterminate(true);

                    break;

                default:

                    break;


            }

        }


        Log.i("OUT", "Progress @" + i);


    }

    /**
     * Receives the "download complete" event asynchronously
     */
    public BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context ctxt, Intent intent) {

            if (DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last") != null)
                if ((DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last")).equals("temp.gco")) {

                    Log.i("Slicer", "Removing PREFERENCE [Last]");
                    DatabaseController.handlePreference(DatabaseController.TAG_SLICING, "Last", null, false);

                    showProgressBar(StateUtils.SLICER_HIDE, 0);
                } else {

                    Log.i("Slicer", "That ain't my file");
                }


        }
    };

    /**
     * Notify the side panel adapters, check for null if they're not available yet (rare case)
     */
    public void notifyAdapter() {

        try {
            mSidePanelHandler.profileAdapter.notifyDataSetChanged();
            if (mSidePanelHandler.printerAdapter != null)
                mSidePanelHandler.printerAdapter.notifyDataSetChanged();
        } catch (NullPointerException e) {

            e.printStackTrace();
        }


    }


    //TODO callback for a slicing request
    public static void slicingCallback() {

        Log.i("Slicer", "Starting thread");

        SliceTask task = new SliceTask();
        task.execute();


        Log.i("Slicer", "Ending thread");
    }

    static class SliceTask extends AsyncTask {


        @Override
        protected Object doInBackground(Object[] objects) {

            final List<DataStorage> newList = new ArrayList<DataStorage>(mDataList);

            //Code to update the UI
            //Check if the file is not yet loaded
            for (int i = 0; i < newList.size(); i++) {

                if (newList.get(i).getVertexArray() == null) {

                    Log.i("OUT", "HAHA!");
                    return null;
                }

            }

            Log.i("Slicer", "Sending callback");

            if ((mSlicingHandler != null) && (mFile != null)) {

                if (LibraryController.hasExtension(0, mFile.getName())) {
                    StlFile.saveModel(newList, null, mSlicingHandler);
                }

            }

            return null;
        }

    }


    /**
     * *********************************  SIDE PANEL *******************************************************
     */

    public static File getFile() {
        return mFile;
    }

    public static int[] getCurrentPlate() {

        return mCurrentPlate;
    }

    public static int getCurrentType() {
        return mCurrentType;
    }

    public static void changePlate(int type) {

        switch (type) {

            case WitboxFaces.TYPE_WITBOX:

                mCurrentPlate = new int[]{WitboxFaces.WITBOX_LONG, WitboxFaces.WITBOX_WITDH, WitboxFaces.WITBOX_HEIGHT};

                break;

            case WitboxFaces.TYPE_HEPHESTOS:

                mCurrentPlate = new int[]{WitboxFaces.HEPHESTOS_LONG, WitboxFaces.HEPHESTOS_WITDH, WitboxFaces.HEPHESTOS_HEIGHT};

                break;

        }

        mCurrentType = type;
        mSurface.changePlate(type);
        mSurface.requestRender();
    }

    public static void setSlicingPosition(float x, float y) {

        Log.i("Slicer", "MOG, new positiong to pront " + x + ":" + y);

        JSONObject position = new JSONObject();
        try {
            position.put("x", (int) x + mCurrentPlate[0]);
            position.put("y", (int) y + mCurrentPlate[1]);

            mSlicingHandler.setExtras("position", position);
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


}
