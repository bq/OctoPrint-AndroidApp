package android.app.printerapp.devices;

import android.app.Activity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelFile;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;

/**
 * This will be the Controller to handle the taskbar storage engine.
 * We will use a LinearLayout instead of a ListView with an Adapter because we want to display it Horizontally and
 * tweaking a ListView or GridView for that is a lot of work.
 *
 * @author alberto-baeza
 */
public class DevicesQuickprint {

    private static String TAG = "DevicesQuickprint";

    //List to store every file
    private ArrayList<ModelFile> mFileList = new ArrayList<ModelFile>();
    private ArrayList<QuickPrintModel> mModelList = new ArrayList<QuickPrintModel>();

    //Horizontal list view that contains the models
    private HListView mHorizontalListView;
    private QuickPrintHorizontalListViewAdapter mQuickPrintAdapter;

    //Main thread reference
    private Context mContext;


    public DevicesQuickprint(Activity context, HListView quickprintHorizontalListView) {
        mContext = context;
        mHorizontalListView = quickprintHorizontalListView;
        mQuickPrintAdapter = new QuickPrintHorizontalListViewAdapter(mContext, mModelList);
        mHorizontalListView.setOnItemLongClickListener(onItemLongClickListener);
        mHorizontalListView.setSelector(mContext.getResources().getDrawable(R.drawable.list_selector));
        mHorizontalListView.setAdapter(mQuickPrintAdapter);

        //Show the models in the horizontal list view
        addFiles();
        displayFiles();
    }

   /**************************************************************************************
     * 		METHODS
     *****************************************************************************************/


    /**
     * TODO: This is the same method as LibraryController.retrieveFavorites but this will
     * contain History instead of favorites so it shouldn't matter
     */
    private void addFiles() {
        for (Map.Entry<String, ?> entry : DatabaseController.getPreferences(DatabaseController.TAG_FAVORITES).entrySet()) {

            ModelFile m = new ModelFile(entry.getValue().toString(), "Internal storage");

            mFileList.add(m);
        }
    }


    /**
     * Display the models on screen using the QuickPrintHorizontalListViewAdapter.
     */
    private void displayFiles() {


        //Fill the horizontal list view with the info of the models
        for (final ModelFile m : mFileList) {

            Drawable modelImage;
            String modelAbsolutePath;
            String modelName;
            String modelDescription;

            if (m.getGcodeList() != null) {
                File path = new File(m.getGcodeList());
                final File[] files = path.getParentFile().listFiles();

                for (int i = 0; i < files.length; i++) {

                    final int current = i;

                    if ((LibraryController.isProject(m))) {



                        //Set the model image
                        if (m.getStorage().equals("Internal storage")) {

                            modelImage = m.getSnapshot();

                            if (modelImage == null)
                                modelImage = mContext.getResources().getDrawable(R.drawable.ic_file_gray);

                        } else {
                            modelImage = mContext.getResources().getDrawable(R.drawable.ic_file_gray);
                        }

                        modelAbsolutePath = files[current].getAbsolutePath();
                        modelName = files[current].getName();
                        modelDescription = m.getInfo();

                        if (mHorizontalListView != null) {
                            QuickPrintModel quickPrintModel = new QuickPrintModel(m.getAbsolutePath(), m.getStorage(), modelImage, modelAbsolutePath, modelName, modelDescription);
                            Log.d(TAG, "Add quick print model to the horizontal list [" + quickPrintModel.toString() + "]");
                            mModelList.add(quickPrintModel);
                            mQuickPrintAdapter.notifyDataSetChanged();
                        } else {
                            Log.d(TAG, "NULL");
                        }
                    }
                }
            }
        }
    }

    AdapterView.OnItemLongClickListener onItemLongClickListener = new AdapterView.OnItemLongClickListener() {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            QuickPrintModel quickPrintModel = mModelList.get(position);
            if(quickPrintModel!=null) {
                Log.d(TAG, "On long click in quick print model [" + quickPrintModel.toString() + "]");
                String name = quickPrintModel.getModelAbsolutePath();

                //Check if there's a real gcode,
                if (name != null) {

                    ClipData data = null;

                    /*if (quickPrintModel.getStorage().equals("Witbox")) {
                        data = ClipData.newPlainText("internal", name);
                    } else if (quickPrintModel.getStorage().equals("sd")) {
                        data = ClipData.newPlainText("internalsd", quickPrintModel.getName());
                    } else*/
                    if (LibraryController.hasExtension(1, name)) {
                        data = ClipData.newPlainText("name", name);
                    }

                    QuickPrintObjectDragShadowBuilder shadowBuilder = new QuickPrintObjectDragShadowBuilder(mContext, view);
                    view.startDrag(data, shadowBuilder, view, 0);

                } else {
                    Toast.makeText(mContext, R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    };



}
