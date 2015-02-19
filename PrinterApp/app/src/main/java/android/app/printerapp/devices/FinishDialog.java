package android.app.printerapp.devices;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.printview.GcodeCache;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.afollestad.materialdialogs.MaterialDialogCompat;

import java.io.File;

/**
 * Created by alberto-baeza on 2/12/15.
 */
public class FinishDialog {

    private static final String STRING_TEMP = "/_tmp";
    private ModelPrinter mPrinter;
    private Context mContext;

    public FinishDialog(Context context, ModelPrinter p){

       mPrinter = p;
        mContext = context;

        createDialog();

    }

    public void createDialog(){

        //Constructor
        MaterialDialogCompat.Builder adb = new MaterialDialogCompat.Builder(mContext);
        adb.setTitle(mContext.getString(R.string.finish_dialog_title) + " " + mPrinter.getJob().getFilename());

        //Inflate the view
        LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.dialog_finish_printing, null, false);

        adb.setView(v);
        adb.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                if (mPrinter.getJobPath()!=null){
                    File file = new File(mPrinter.getJobPath());

                    if (file.getParentFile().getAbsolutePath().contains(STRING_TEMP)) {


                        //Auto-save
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        if (sharedPref.getBoolean(mContext.getResources().getString(R.string.shared_preferences_save), true)){

                            //Select the same file again to reset progress
                            OctoprintFiles.fileCommand(mContext, mPrinter.getAddress(), mPrinter.getJob().getFilename(), "/local/", false, false);

                            File to = new File(file.getParentFile().getParentFile().getAbsolutePath() + "/_gcode/" + file.getName());

                            DatabaseController.updateHistoryPath(file.getAbsolutePath(), to.getAbsolutePath());

                            file.renameTo(to);

                            LibraryController.deleteFiles(file.getParentFile());



                        } else {
                            createFinishDialogSave(mPrinter,file);

                        }


                    } else {
                        OctoprintFiles.fileCommand(mContext, mPrinter.getAddress(), mPrinter.getJob().getFilename(), "/local/", true, false);
                    }

                    GcodeCache.removeGcodeFromCache(mPrinter.getJobPath());
                    mPrinter.setJobPath(null);
                    DatabaseController.handlePreference(DatabaseController.TAG_REFERENCES,mPrinter.getName(),null,false);
                }
                else {

                    OctoprintFiles.fileCommand(mContext, mPrinter.getAddress(), mPrinter.getJob().getFilename(), "/local/", true, false);


                }

            }
        });

        adb.show();

    }

    public void createFinishDialogSave(final ModelPrinter m, final File file) {

        //Constructor
        MaterialDialogCompat.Builder adb = new MaterialDialogCompat.Builder(mContext);
        adb.setTitle(m.getDisplayName() + " (100%) - " +file.getName());

        //Inflate the view
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.print_finished_dialog, null, false);

        final CheckBox cb_server = (CheckBox) v.findViewById(R.id.checkbox_keep_server);
        final CheckBox cb_local = (CheckBox) v.findViewById(R.id.checkbox_keep_local);
        final EditText et_name = (EditText) v.findViewById(R.id.et_name_model);

        et_name.setText(file.getName());

        adb.setView(v);

        adb.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                if (cb_server.isChecked()){

                    //Select the same file again to reset progress
                    OctoprintFiles.fileCommand(mContext, m.getAddress(), m.getJob().getFilename(), "/local/", false, false);

                } else {

                    //Remove file from server
                    OctoprintFiles.fileCommand(mContext, m.getAddress(), m.getJob().getFilename(), "/local/", true, false);


                }

                if (cb_local.isChecked()){

                    File to = new File(file.getParentFile().getParentFile().getAbsolutePath() + "/_gcode/" + et_name.getText().toString());


                    DatabaseController.updateHistoryPath(file.getAbsolutePath(), to.getAbsolutePath());
                    file.renameTo(to);

                    LibraryController.initializeHistoryList();


                } else {

                    try{
                        //Delete file locally
                        if (file.delete()){

                            Log.i("OUT", "File deleted!");

                        }

                    } catch (NullPointerException e){

                        Log.i("OUT", "Error deleting the file");

                    }



                }

                LibraryController.deleteFiles(file.getParentFile());


            }

        });

        adb.setNegativeButton(R.string.cancel, null);

        adb.show();
    }
}
