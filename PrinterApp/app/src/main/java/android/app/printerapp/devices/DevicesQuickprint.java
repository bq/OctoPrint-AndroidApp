package android.app.printerapp.devices;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelFile;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * This will be the Controller to handle the taskbar storage engine.
 * We will use a LinearLayout instead of a ListView with an Adapter because we want to display it Horizontally and
 * tweaking a ListView or GridView for that is a lot of work.
 *
 * @author alberto-baeza
 */
public class DevicesQuickprint {

    //List to store every file
    private ArrayList<ModelFile> mFileList = new ArrayList<ModelFile>();
    private ArrayList<QuickPrintModel> mModelList = new ArrayList<QuickPrintModel>();

    //Horizontal list view that contains the models
    private HorizontalListView mHorizontalListView;
    private QuickPrintHorizontalListViewAdapter mQuickPrintAdapter;

    //Main thread reference
    private Context mContext;


    public DevicesQuickprint(Activity context, HorizontalListView quickprintHorizontalListView) {
        mContext = context;
        mHorizontalListView = quickprintHorizontalListView;
        mQuickPrintAdapter = new QuickPrintHorizontalListViewAdapter(mContext, mModelList);
        mHorizontalListView.setAdapter(mQuickPrintAdapter);
        //Show the models in the horizontal list view
        addFiles();
        displayFiles();
    }


    /**************************************************************************************
     * 		METHODS
     *****************************************************************************************/


    /**
     * TODO: This is the same method as StorageController.retrieveFavorites but this will
     * contain History instead of favorites so it shouldn't matter
     */
    private void addFiles() {
        for (Map.Entry<String, ?> entry : DatabaseController.getPreferences("Favorites").entrySet()) {
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
            String modelName;
            String modelDescription;

            if (m.getGcodeList() != null) {
                File path = new File(m.getGcodeList());
                final File[] files = path.getParentFile().listFiles();

                for (int i = 0; i < files.length; i++) {

                    final int current = i;

                    if ((StorageController.isProject(m))) {

                        //Set the model image
                        if (m.getStorage().equals("Internal storage")) {

                            modelImage = m.getSnapshot();

                            if (modelImage == null)
                                modelImage = mContext.getResources().getDrawable(R.drawable.file_icon);

                        } else {
                            modelImage = mContext.getResources().getDrawable(R.drawable.file_icon);
                        }

                        modelName = files[current].getName();
                        modelDescription = m.getInfo();

                        if (mHorizontalListView != null) {
                            QuickPrintModel quickPrintModel = new QuickPrintModel(m.getName(), m.getStorage(), modelImage, modelName, modelDescription);
                            Log.i("out", "Add quick print model to the horizontal list [" + quickPrintModel.toString() + "]");
                            mModelList.add(quickPrintModel);
                            mQuickPrintAdapter.notifyDataSetChanged();
                        } else {
                            Log.i("out", "NULL");
                        }
                    /*
                     * TODO Adapt to the new horizontal list view
					 * On long click we start dragging the item, no need to make it invisible
					 */
//                        v.setOnLongClickListener(new OnLongClickListener() {
//
//                            @Override
//                            public boolean onLongClick(View v) {
//
//                                String name = files[current].getAbsolutePath();
//
//                                /**
//                                 * Check if there's a real gcode,
//                                 */
//                                if (name != null) {
//
//                                    ClipData data = null;
//
//                                    if (m.getStorage().equals("Witbox")) {
//                                        data = ClipData.newPlainText("internal", name);
//                                    } else if (m.getStorage().equals("sd")) {
//                                        data = ClipData.newPlainText("internalsd", m.getName());
//                                    } else if (StorageController.hasExtension(1, name)) {
//                                        data = ClipData.newPlainText("name", name);
//                                    }
//
//                                    DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
//                                    v.startDrag(data, shadowBuilder, v, 0);
//                                } else
//                                    Toast.makeText(mContext, R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
//
//                                return false;
//                            }
//                        });
                    }
                }
            }
        }
    }
}
