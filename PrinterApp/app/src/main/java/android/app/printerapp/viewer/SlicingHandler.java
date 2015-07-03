package android.app.printerapp.viewer;

import android.app.Activity;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.OctoprintSlicing;
import android.app.printerapp.octoprint.StateUtils;
import android.os.AsyncTask;
import android.os.Handler;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by alberto-baeza on 10/7/14.
 */
public class SlicingHandler {

    private static final int DELAY = 3000; //timer delay just in case

    //Data array to send to the server
    private byte[] mData = null;
    private List<DataStorage> mDataList = null;

    private Activity mActivity;
    //private String mProfile = null;

    private JSONObject mExtras = new JSONObject();


    //timer to upload files
    private Timer mTimer;

    //Check if there is a pending timer
    private boolean isRunning;

    //Last reference to the temp file
    private String mLastReference = null;
    private String mOriginalProject = null;

    //Default URL to slice models
    private ModelPrinter mPrinter;

    public SlicingHandler(Activity activity){

        mActivity = activity;
        isRunning = false;

       //TODO Clear temp folder?
        cleanTempFolder();
    }


    public void  setData(byte[] data){

        mData = data;

    }

    public void clearExtras(){

        mExtras = new JSONObject();
    }

    public void setExtras(String tag, Object value){

        //mProfile = profile;
        try {

            if (mExtras.has(tag))
            if (mExtras.get(tag).equals(value)){
                return;
            }
            mExtras.put(tag,value);



           // Log.i("Slicer","Added extra " + tag + ":" + value + " [" + mExtras.toString()+"]");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        ViewerMainFragment.slicingCallback();
    }

    //Set the printer dynamically to send the files
    public void setPrinter(ModelPrinter p){

        mPrinter = p;

    }



    //Creates a temporary file and save it into the parent folder
    //TODO create temp folder
    public File createTempFile(){

        File tempFile = null;


        //Create temporary folder
        File tempPath =  new File(LibraryController.getParentFolder().getAbsolutePath() + "/temp");

        if (tempPath.mkdir()){

            Log.i("Slicer", "Creating temporary file " + tempPath);

        } else Log.i("Slicer", "Directory exists " + tempPath);;

        try {

            //add an extra random id
            int randomInt = new Random().nextInt(100000);

            tempFile = File.createTempFile("tmp",randomInt+".stl", tempPath);
            tempFile.deleteOnExit();

            //delete previous file
            try{


                 File lastFile = null;
                if (mLastReference!=null){
                    lastFile= new File(mLastReference);
                    lastFile.delete();
                }


                Log.i("Slicer", "Deleted " + mLastReference);
            }
            catch (NullPointerException e){

                e.printStackTrace();
            }

            if (tempFile.exists()){

                mLastReference = tempFile.getAbsolutePath();

                DatabaseController.handlePreference(DatabaseController.TAG_RESTORE,"Last",mLastReference, true);

                DatabaseController.handlePreference(DatabaseController.TAG_SLICING, "Last", tempFile.getName(), true);


                StlFile.saveModel(mDataList,null,SlicingHandler.this);

                FileOutputStream fos = new FileOutputStream(tempFile);
                fos.write(mData);
                fos.getFD().sync();
                fos.close();

            } else {

            }




        } catch (Exception e) {

            e.printStackTrace();
        }

      /*  if (tempFile != null )Log.i("OUT", "FIle created nasdijalskdjldaj as fucking name " + tempFile.getName());
        else Log.i("OUT","ERROR CREATING TEMP FILASIDÑLAISDÑ  ");*/

        return  tempFile;

    }

    //TODO implementation with timers, should change to ScheduledThreadPoolExecutor maybe
    public void sendTimer(List<DataStorage> data){

        //Reset timer in case it was on progress
        if (isRunning) {

            Log.i("Slicer", "Cancelling previous timer");
            mTimer.cancel();
            mTimer.purge();
            isRunning = false;
        }

        //Reschedule task
        mTimer = new Timer();
        mDataList = data;
        mTimer.schedule(new SliceTask(),DELAY);
        isRunning = true;

    }

    //returns last .stl reference
    public String getLastReference(){
        return mLastReference;
    }
    public String getOriginalProject() { return mOriginalProject; }

    public void setOriginalProject(String path) {

        mOriginalProject = path;
        Log.i("OUT", "Workspace: " + path);

    }

    public void setLastReference(String path) { mLastReference = path; }

    /**
     * Task to start the uploading and slicing process from a timer
     */
    private class SliceTask extends TimerTask {

        @Override
        public void run() {


            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i("Slicer", "Timer ended, Starting task");

                    if (mPrinter!=null){

                        if (mPrinter.getStatus()== StateUtils.STATE_OPERATIONAL){

                            OctoprintFiles.deleteFile(mActivity, mPrinter.getAddress(), DatabaseController.getPreference(DatabaseController.TAG_SLICING, "Last"), "/local/");

                            Handler saveHandler = new Handler();
                            saveHandler.post(mSaveRunnable);

//                            new SaveTask().execute();

                        } else {

                            Log.i("Slicer", "No printer available");

                            Toast.makeText(mActivity, R.string.viewer_printer_selected,Toast.LENGTH_LONG).show();

                        }


                    } else {

                        if (DatabaseController.count() > 1){

                        }
                        Toast.makeText(mActivity,R.string.viewer_printer_unavailable,Toast.LENGTH_LONG).show();

                    }
                }
            });


            //Timer stopped
            isRunning = false;


        }
    }
    private Runnable mSaveRunnable = new Runnable() {
        @Override
        public void run() {

            File mFile = createTempFile();

            Log.i("Slicer", "Sending slice command");
            OctoprintSlicing.sliceCommand(mActivity,mPrinter.getAddress(),mFile,mExtras);
            //if (mExtras.has("print")) mExtras.remove("print");

            Log.i("Slicer", "Showing progress bar");
            ViewerMainFragment.showProgressBar(StateUtils.SLICER_UPLOAD, 0);


        }
    };

    /**
     * Task to save the actual file ia background process and then upload it to the server
     */
    private class SaveTask extends AsyncTask {

        File mFile;


        @Override
        protected Object doInBackground(Object[] objects) {

            mFile = createTempFile();

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {

            Log.i("Slicer", "Sending slice command");
            OctoprintSlicing.sliceCommand(mActivity,mPrinter.getAddress(),mFile,mExtras);
            //if (mExtras.has("print")) mExtras.remove("print");

            Log.i("Slicer", "Showing progress bar");
            ViewerMainFragment.showProgressBar(StateUtils.SLICER_UPLOAD, 0);

        }
    }


    //delete temp folder
    private void cleanTempFolder(){

        File file = new File(LibraryController.getParentFolder() + "/temp/");

        LibraryController.deleteFiles(file);
    }

}
