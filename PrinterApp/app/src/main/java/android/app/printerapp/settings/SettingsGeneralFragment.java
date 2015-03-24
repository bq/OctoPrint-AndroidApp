package android.app.printerapp.settings;

import android.app.Fragment;
import android.app.printerapp.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by sara on 5/02/15.
 */
public class SettingsGeneralFragment extends Fragment {

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
            rootView = inflater.inflate(R.layout.settings_general_fragment, container, false);

            final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            CheckBox checkBox_slice = (CheckBox) rootView.findViewById(R.id.settings_slicing_checkbox);
            checkBox_slice.setChecked(sharedPref.getBoolean(getString(R.string.shared_preferences_slice), false));
            checkBox_slice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    sharedPref.edit().putBoolean(getString(R.string.shared_preferences_slice), b).apply();


                }
            });
            CheckBox checkBox_print = (CheckBox) rootView.findViewById(R.id.settings_printing_checkbox);
            checkBox_print.setChecked(sharedPref.getBoolean(getString(R.string.shared_preferences_print), false));
            checkBox_print.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    sharedPref.edit().putBoolean(getString(R.string.shared_preferences_print), b).apply();


                }
            });
            CheckBox checkBox_save = (CheckBox) rootView.findViewById(R.id.settings_save_files_checkbox);
            checkBox_save.setChecked(sharedPref.getBoolean(getString(R.string.shared_preferences_save), false));
            checkBox_save.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    sharedPref.edit().putBoolean(getString(R.string.shared_preferences_save), b).apply();


                }
            });

            CheckBox checkBox_autoslice = (CheckBox) rootView.findViewById(R.id.settings_automatic_checkbox);
            checkBox_autoslice.setChecked(sharedPref.getBoolean(getString(R.string.shared_preferences_autoslice), false));
            checkBox_autoslice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    sharedPref.edit().putBoolean(getString(R.string.shared_preferences_autoslice), b).apply();


                }
            });


            /*********************************************************/

            getNetworkSsid(rootView);

        }
        return rootView;
    }


    public void notifyAdapter(){
        mAdapter.notifyDataSetChanged();
    }

    //Return network without quotes
    public void getNetworkSsid(View v){

        WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();

        TextView tv = (TextView) v.findViewById(R.id.network_name_textview);
        tv.setText(wifiInfo.getSSID().replace("\"", ""));

        ImageView iv = (ImageView) v.findViewById(R.id.wifi_signal_imageview);

        int signal = wifiInfo.getRssi();

        if ((signal <= 0) && (signal > -40)){
            iv.setImageResource(R.drawable.ic_signal_wifi_4);
        } else if ((signal <= -40) && (signal > -60)){
            iv.setImageResource(R.drawable.ic_signal_wifi_3);
        } else if ((signal <= -60) && (signal > -70)){
            iv.setImageResource(R.drawable.ic_signal_wifi_2);
        } else if ((signal <= -70) && (signal > -80)){
            iv.setImageResource(R.drawable.ic_signal_wifi_1);
        } else iv.setImageResource(R.drawable.ic_signal_wifi_0);

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
