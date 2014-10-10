package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.util.Log;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alberto-baeza on 10/7/14.
 */
public class SlicingHandler {

    //Data array to send to the server
    private byte[] mData = null;


    private Activity mActivity;


    //timer to upload files
    private Timer mTimer;

    //Check if there is a pending timer
    private boolean isRunning;

    //Last reference to the temp file
    private String mLastReference = null;
    private String mOriginalProject = null;

    //Default URL to slice models
    private String mUrl;

    public SlicingHandler(Activity activity){

        mActivity = activity;
        isRunning = false;
        cleanTempFolder();
    }


    public void  setData(byte[] data){

        mData = data;

    }

    //Creates a temporary file and save it into the parent folder
    //TODO create temp folder
    public File createTempFile(){

        File tempFile = null;

        try {

            File tempPath =  new File(StorageController.getParentFolder().getAbsolutePath() + "/temp");

            tempPath.mkdir();

            tempFile = File.createTempFile("tmp",".stl", tempPath);
            tempFile.deleteOnExit();

            //delete previous file
            try{
                File lastFile = new File(mLastReference);
                lastFile.delete();
            } catch (NullPointerException e){

                e.printStackTrace();
                Log.i("OUT","FUCKING FILE DIDNT EXIST FUKLASJDLKASJD");
            }


            mLastReference = tempFile.getAbsolutePath();


            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(mData);
            fos.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (tempFile != null )Log.i("OUT", "FIle created nasdijalskdjldaj as fucking name " + tempFile.getName());
        else Log.i("OUT","ERROR CREATING TEMP FILASIDÑLAISDÑ  ");

        return  tempFile;

    }

    //TODO implementation with timers, should change to ScheduledThreadPoolExecutor maybe
    public void sendTimer(){

        //Reset timer in case it was on progress
        if (isRunning) {
            mTimer.cancel();
            mTimer.purge();

            Log.i("OUT","TIMER RESETING HIJO DE PUTA" );
            isRunning = false;
        }

        Log.i("OUT","Creating EL TIMER" );
        //Reschedule task
        mTimer = new Timer();
        mTimer.schedule(new SliceTask(),5000);
        isRunning = true;

    }


    private ModelPrinter selectAvailablePrinter(){

    //search for operational printers

        for (ModelPrinter p : DevicesListController.getList()){

            if (p.getStatus() == StateUtils.STATE_OPERATIONAL)
                return p;

        }
        return null;

    }

    //returns last .stl reference
    public String getLastReference(){
        return mLastReference;
    }
    public String getOriginalProject() { return mOriginalProject; }

    public void setOriginalProject(String path) {

        mOriginalProject = path;
        Log.i("OUT","Workspace: " + path);
    }

    private class SliceTask extends TimerTask {

        @Override
        public void run() {


            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("OUT","TASKEANDO" );

                    ModelPrinter p = selectAvailablePrinter();

                    if (p!=null){
                        OctoprintFiles.uploadFile(mActivity,createTempFile(),selectAvailablePrinter(),true);
                    } else {

                        Log.i("OUT", "No available printers for slicing");

                    }
                }
            });


            //Timer stopped
            isRunning = false;


        }
    }

    //delete temp folder
    private void cleanTempFolder(){

        File file = new File(StorageController.getParentFolder() + "/temp/");

        StorageController.deleteFiles(file);
    }

}
