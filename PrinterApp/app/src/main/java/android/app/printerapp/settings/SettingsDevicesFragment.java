package android.app.printerapp.settings;

import android.app.Fragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Created by sara on 5/02/15.
 */
public class SettingsDevicesFragment  extends Fragment {

    private SettingsListAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Retain instance to keep the Fragment from destroying itself
        setRetainInstance(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //Reference to View
        View rootView = null;

        //If is not new
        if (savedInstanceState==null){

            //Inflate the fragment
            rootView = inflater.inflate(R.layout.settings_devices_fragment, container, false);

            /*********************************************************/

            mAdapter = new SettingsListAdapter(getActivity(), R.layout.list_item_settings_device, DevicesListController.getList());
            ListView l = (ListView) rootView.findViewById(R.id.lv_settings);
            l.setAdapter(mAdapter);

            notifyAdapter();
        }
        return rootView;
    }


    public void notifyAdapter(){
        mAdapter.notifyDataSetChanged();
    }

}
