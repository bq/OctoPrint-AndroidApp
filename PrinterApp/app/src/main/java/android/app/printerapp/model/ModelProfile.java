package android.app.printerapp.model;

import android.app.printerapp.R;
import android.content.Context;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by alberto-baeza on 12/4/14.
 */
public class ModelProfile {

    public static final int WITBOX_PROFILE = R.raw.witbox;
    public static final int PRUSA_PROFILE = R.raw.prusa;



    public static JSONObject retrieveProfile(Context context, int resource){

        InputStream fis = context.getResources().openRawResource(resource);
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

}
