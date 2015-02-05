package android.app.printerapp.settings;

import android.app.Fragment;
import android.app.printerapp.R;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by sara on 5/02/15.
 */
public class SettingsAboutFragment extends Fragment {

    private SettingsListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retain instance to keep the Fragment from destroying itself
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Reference to View
        View rootView = null;

        //If is not new
        if (savedInstanceState==null){

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.settings_about_fragment, container, false);

            /*********************************************************/

            TextView tv = (TextView) rootView.findViewById(R.id.app_version_textview);
            tv.setText(setBuildVersion());

        }
        return rootView;
    }

    public String setBuildVersion(){

        String s = "Version v.";

        try{

            //Get version name from package
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            String fString = pInfo.versionName;

            //Parse version and date
            String hash = fString.substring(0,fString.indexOf(" "));
            String date = fString.substring(fString.indexOf(" "), fString.length());

            //Format hash
            String [] fHash = hash.split(";");

            //Format date
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm",new Locale("es", "ES"));
            String fDate = sdf.format(new java.util.Date(date));

            //Get version code / Jenkins build
            String code;
            if (pInfo.versionCode == 0) code = "IDE";
            else code = "#"+ pInfo.versionCode;

            //Build string
            s = s + fHash[0] + " " + fHash[1] + " " + fDate + " " + code;

        }catch(Exception e){

            e.printStackTrace();
        }

        return s;
    }
}
