package android.app.printerapp.model;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * This class will define the profile type that will be used by the printers / quality types
 * Created by alberto-baeza on 12/4/14.
 */
public class ModelProfile {

    //Printer profiles
    public static final String WITBOX_PROFILE = "bq_witbox";
    public static final String PRUSA_PROFILE = "bq_hephestos";
    public static final String DEFAULT_PROFILE = "CUSTOM";

    public static final String TYPE_P = ".profile";
    public static final String TYPE_Q = ".quality";

    //Quality profiles
    public static final String LOW_PROFILE = "low_bq";
    public static final String MEDIUM_PROFILE = "medium_bq";
    public static final String HIGH_PROFILE = "high_bq";

    private static final String[] PRINTER_TYPE = {"Witbox", "Hephestos"};
    private static final String[] PROFILE_OPTIONS = {HIGH_PROFILE, MEDIUM_PROFILE, LOW_PROFILE};


    private static ArrayList<String> mProfileList;
    private static ArrayList<String> mQualityList;

    //Retrieve a profile in JSON format
    public static JSONObject retrieveProfile(Context context, String resource, String type){

        int id = 0;

        //Select a predefined profile
        if (resource.equals(WITBOX_PROFILE)) id = R.raw.witbox;
        if (resource.equals(PRUSA_PROFILE)) id = R.raw.prusa;
        if (resource.equals(DEFAULT_PROFILE)) id = R.raw.defaultprinter;
        if (resource.equals(LOW_PROFILE)) id = R.raw.low;
        if (resource.equals(MEDIUM_PROFILE)) id = R.raw.medium;
        if (resource.equals(HIGH_PROFILE)) id = R.raw.high;

        InputStream fis = null;

        if (id != 0)  fis = context.getResources().openRawResource(id);
        else { //Custom profile

            try {
                Log.i("PROFILE", "Looking for " + resource);
                fis = context.openFileInput(resource + type);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
       //fis = context.getResources().openRawResource(id);
        JSONObject json = null;
        if (fis!=null){

            BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
            StringBuilder sb = new StringBuilder();
            String line = null;



            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();


                json = new JSONObject(sb.toString());

                Log.i("json", json.toString());


            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }



        return json;

    }


    //Save a new custom profile
    public static boolean saveProfile(Context context, String name, JSONObject json, String type){

        String filename = name + type;
        FileOutputStream outputStream;

          try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.close();

              Log.i("OUT", "Written " + filename);

        } catch (Exception e) {
            e.printStackTrace();

              return false;
        }

        return true;

    }

    //Delete profile file from internal storage
    public static boolean deleteProfile(Context context, String name, String type) {

        File file = new File(context.getFilesDir(), name + type);
        if (file.delete()) return true;

        return false;

    }

    public static void reloadQualityList(Context context){

        //Add default types plus custom types from internal storage
        mQualityList = new ArrayList<String>();
        mQualityList.clear();
        for (String s : PROFILE_OPTIONS) {

            mQualityList.add(s);
        }

        //Add internal storage types
        for (File file : context.getApplicationContext().getFilesDir().listFiles()) {


            //Only files with the .profile extension
            if (file.getAbsolutePath().contains(TYPE_Q)) {


                int pos = file.getName().lastIndexOf(".");
                String name = pos > 0 ? file.getName().substring(0, pos) : file.getName();

                //Add only the name
                mQualityList.add(name);
            }

        }


    }

    public static void reloadList(Context context){

        //Add default types plus custom types from internal storage
        mProfileList = new ArrayList<String>();
        mProfileList.clear();
        for (String s : PRINTER_TYPE) {

            mProfileList.add(s);
        }

        //Add internal storage types
        for (File file : context.getApplicationContext().getFilesDir().listFiles()) {

            //Only files with the .profile extension
            if (file.getAbsolutePath().contains(TYPE_P)) {

                int pos = file.getName().lastIndexOf(".");
                String name = pos > 0 ? file.getName().substring(0, pos) : file.getName();

                //Add only the name
                mProfileList.add(name);
            }

        }

    }

    public static ArrayList<String> getProfileList(){
        return mProfileList;
    }
    public static ArrayList<String> getQualityList(){
        return mQualityList;
    }

}
