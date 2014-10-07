package android.app.printerapp.viewer;

import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.OctoprintSlicing;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by alberto-baeza on 10/7/14.
 */
public class SlicingHandler {

    //Data array to send to the server
    private byte[] mData = null;


    private Context mContext;

    //Default URL to slice models
    private String mUrl;

    public SlicingHandler(Context context){

        mContext = context;

    }


    public void  setData(byte[] data){

        mData = data;

    }

    //Creates a temporary file and save it into the parent folder
    //TODO create temp folder
    public File createTempFile(){

        File tempFile = null;

        try {

            tempFile = File.createTempFile("tmp",".stl", StorageController.getParentFolder());
            tempFile.deleteOnExit();

            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(mData);
            fos.close();



        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("OUT", "FIle created nasdijalskdjldaj");

        return  tempFile;

    }

    public void sendTimer(){

        //countdown

        //if new file refresh countdown

        //countdown ends
        //OctoprintSlicing.sliceCommand(mContext, selectAvailablePrinter(), createTempFile(), "/local/");
        OctoprintFiles.uploadFile(mContext, createTempFile(), selectAvailablePrinter(), true);
    }


    private ModelPrinter selectAvailablePrinter(){

    //search for operational printers

        for (ModelPrinter p : DevicesListController.getList()){

            if (p.getStatus() == StateUtils.STATE_OPERATIONAL)
                return p;

        }
        return null;

    }




}
