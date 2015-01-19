package android.app.printerapp.library;

import android.app.AlertDialog;
import android.app.printerapp.R;
import android.content.Context;
import android.content.DialogInterface;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.util.ArrayList;

/**
 *
 * This class will search for files inside the External directory to add them as projects.
 *
 * Created by alberto-baeza on 1/15/15.
 */
public class FileScanner {

    private ArrayList<File> mFileList = new ArrayList<>();

    public FileScanner(String path, Context context){

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
    private void startScan(String path){

        File pathFile = new File(path);

        File[] files = pathFile.listFiles();

        if (files !=null)
            for (File file : files){

                //If folder
                if (file.isDirectory()) {



                    //exclude files from the application folder
                    if (!file.getAbsolutePath().contains(LibraryController.getParentFolder().getAbsolutePath())) {

                        startScan(file.getAbsolutePath());

                    }
                } else {

                    //Add stl/gcodes to the search list
                    if (LibraryController.hasExtension(0, file.getName()) || (LibraryController.hasExtension(1, file.getName()))){

                        Log.i("Scanner", "File found! " + file.getName());

                        if (!LibraryController.fileExists(file.getName())){

                            mFileList.add(file);
                        }



                    }


                }


            }

    }

    //Creates a dialog to add the files
    private void addDialog(final Context context){

        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle(R.string.library_scan_dialog_title);

        String[] fileNames = new String[mFileList.size()];
        final boolean[] checkedItems = new boolean[mFileList.size()];

        int i = 0;

        for (File f : mFileList){

            fileNames[i] = f.getName();
            checkedItems[i] = false;
            i++;

        }

        adb.setMultiChoiceItems(fileNames,checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {

                if (b) checkedItems[i] = true;
                else checkedItems[i] = false;
            }
        });

        adb.setPositiveButton(R.string.ok,new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                int c = 0;
                ArrayList<File> mCheckedFiles = new ArrayList<File>();

                for (File f : mFileList){

                    if (checkedItems[c]){


                        mCheckedFiles.add(f);
                        Log.i("Scanner","Adding: " + f.getName());


                    }

                    c++;
                }

                if (mCheckedFiles.size()>0)
                LibraryModelCreation.enqueueJobs(context, mCheckedFiles); //enqueue checked files

            }
        });

        adb.setNegativeButton(R.string.cancel,null);

        adb.show();

    }


}
