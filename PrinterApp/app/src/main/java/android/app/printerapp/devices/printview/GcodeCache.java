package android.app.printerapp.devices.printview;

import android.app.printerapp.Log;
import android.app.printerapp.viewer.DataStorage;

import java.util.ArrayList;

/**
 * This class will hold a reference to every gcode already loaded in memory in the app
 * to avoid having to open it every single time
 * Created by alberto-baeza on 12/19/14.
 */
public class GcodeCache {

    private static ArrayList<DataStorage> mGcodeCacheList;

    //Generic constructor
    public GcodeCache(){

        mGcodeCacheList = new ArrayList<DataStorage>();

    }

    //Add a new gcode to the list
    public static void addGcodeToCache(DataStorage data ){

        mGcodeCacheList.add(data);

    }

    //Retrieve a gcode from the list by its path file
    public static DataStorage retrieveGcodeFromCache(String path){


        for(DataStorage data : mGcodeCacheList){

            if (data.getPathFile().equals(path)) return data;

        }

        return null;

    }

    //Remove a gcode from the list
    public static void removeGcodeFromCache(String path){

        int index = -1;

        for (int i = 0; i < mGcodeCacheList.size() ; i++){

            if (mGcodeCacheList.get(i).getPathFile().equals(path)) {

                Log.i("PrintView", mGcodeCacheList.size() + " Removed " + path + " from cache");
                index = i;

            }

        }

        if (index>=0) mGcodeCacheList.remove(index);

    }


}
