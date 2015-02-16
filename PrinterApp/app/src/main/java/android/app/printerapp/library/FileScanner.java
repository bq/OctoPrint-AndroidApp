package android.app.printerapp.library;

import android.app.AlertDialog;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import java.io.File;
import java.util.ArrayList;

/**
 * This class will search for files inside the External directory to add them as projects.
 * <p/>
 * Created by alberto-baeza on 1/15/15.
 */
public class FileScanner {

    private ArrayList<File> mFileList = new ArrayList<File>();

    public FileScanner(String path, Context context) {

        Log.i("Scanner", "Starting scanner!");


        //Create search dialog
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        AlertDialog alert;

        adb.setTitle(R.string.search);

        //TODO create a proper view
        ProgressBar pb = new ProgressBar(context);
        pb.setIndeterminate(true);
        adb.setView(pb);

        alert = adb.create();
        alert.show();

        startScan(path);

        alert.dismiss(); //Dismiss dialog

        Log.i("Scanner", "Found " + mFileList.size() + " elements!");

        addDialog(context);

    }

    //Scan recursively for files in the external directory
    private void startScan(String path) {

        File pathFile = new File(path);

        File[] files = pathFile.listFiles();

        if (files != null)
            for (File file : files) {

                //If folder
                if (file.isDirectory()) {


                    //exclude files from the application folder
                    if (!file.getAbsolutePath().contains(LibraryController.getParentFolder().getAbsolutePath())) {

                        startScan(file.getAbsolutePath());

                    }
                } else {

                    //Add stl/gcodes to the search list
                    if (LibraryController.hasExtension(0, file.getName()) || (LibraryController.hasExtension(1, file.getName()))) {

                        Log.i("Scanner", "File found! " + file.getName());

                        if (!LibraryController.fileExists(file.getName())) {

                            mFileList.add(file);
                        }


                    }


                }


            }

    }

    //Creates a dialog to add the files
    private void addDialog(final Context context) {

        String[] fileNames = new String[mFileList.size()];
        final boolean[] checkedItems = new boolean[mFileList.size()];

        int i = 0;

        for (File f : mFileList) {

            fileNames[i] = f.getName();
            checkedItems[i] = false;
            i++;

        }

        LayoutInflater li = LayoutInflater.from(context);
        View view = li.inflate(R.layout.dialog_list, null);

        final uk.co.androidalliance.edgeeffectoverride.ListView listView =
                (uk.co.androidalliance.edgeeffectoverride.ListView) view.findViewById(R.id.dialog_list_listview);
        listView.setSelector(context.getResources().getDrawable(R.drawable.list_selector));
        TextView emptyText = (TextView) view.findViewById(R.id.dialog_list_emptyview);
        listView.setEmptyView(emptyText);

        ArrayAdapter<String> ad = new ArrayAdapter<String>(context, R.layout.list_item_add_models_dialog, R.id.text1, fileNames);
        listView.setAdapter(ad);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        listView.setDivider(null);

        new MaterialDialog.Builder(context)
                .title(R.string.library_scan_dialog_title)
                .customView(view, false)
                .negativeText(R.string.cancel)
                .negativeColorRes(R.color.body_text_2)
                .positiveText(R.string.dialog_continue)
                .positiveColorRes(R.color.theme_accent_1)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        SparseBooleanArray ids = listView.getCheckedItemPositions();

                        ArrayList<File> mCheckedFiles = new ArrayList<File>();
                        for (int i = 0; i < ids.size(); i++) {

                            if (ids.valueAt(i)) {

                                File file = mFileList.get(ids.keyAt(i));
                                mCheckedFiles.add(file);
                                Log.i("Scanner", "Adding: " + file.getName());

                            }
                        }

                        if (mCheckedFiles.size() > 0)
                            LibraryModelCreation.enqueueJobs(context, mCheckedFiles); //enqueue checked files

                    }
                })
                .build()
                .show();

    }


}
