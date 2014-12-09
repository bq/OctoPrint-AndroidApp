package android.app.printerapp.model;

import android.app.printerapp.R;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alberto-baeza on 12/4/14.
 */
public class ModelProfile {

    public static final String WITBOX_PROFILE = "WITBOX";
    public static final String PRUSA_PROFILE = "PRUSA";
    public static final String DEFAULT_PROFILE = "CUSTOM";

    public static JSONObject retrieveProfile(Context context, String resource){

        int id = 0;

        if (resource.equals(WITBOX_PROFILE)) id = R.raw.witbox;
        if (resource.equals(PRUSA_PROFILE)) id = R.raw.prusa;
        if (resource.equals(DEFAULT_PROFILE)) id = R.raw.defaultprinter;

        InputStream fis = null;

        if (id != 0)  fis = context.getResources().openRawResource(id);
        else {

            try {

                fis = context.openFileInput(resource + ".profile");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }


        }
       //fis = context.getResources().openRawResource(id);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        StringBuilder sb = new StringBuilder();
        String line = null;

        JSONObject json = null;

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


        return json;

    }

    public static boolean saveProfile(Context context, String name, JSONObject json){

        String filename = name + ".profile";
        FileOutputStream outputStream;

          try {
            outputStream = context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(json.toString().getBytes());
            outputStream.close();

              Log.i("OUT","Written " + filename);

        } catch (Exception e) {
            e.printStackTrace();

              return false;
        }

        return true;

    }

    //Delete profile file from internal storage
    public static boolean deleteProfile(Context context, String name) {

        File file = new File(context.getFilesDir(), name + ".profile");
        if (file.delete()) return true;

        return false;

    }

}
