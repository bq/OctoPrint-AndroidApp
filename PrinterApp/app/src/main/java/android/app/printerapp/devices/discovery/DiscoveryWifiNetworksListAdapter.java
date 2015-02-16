package android.app.printerapp.devices.discovery;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter that creates the items of the wifi networks list view
 */
public class DiscoveryWifiNetworksListAdapter extends BaseAdapter {

    private final static String TAG = "DiscoveryWifiNetworksListAdapter";

    private Context mContext;
    private List<String> mWifiNetworksList;
    private List<String> mWifiSignalList;

    public DiscoveryWifiNetworksListAdapter(Context context, List<String> wifiNetworksList, List<String> wifiSignalList) {
        this.mContext = context;
        this.mWifiNetworksList = wifiNetworksList;
        this.mWifiSignalList = wifiSignalList;
    }

    @Override
    public int getCount() {
        return mWifiNetworksList.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView");

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_wifi_network, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.wifiNetworkSsid = (TextView) convertView.findViewById(R.id.wifi_ssid_textview);
            viewHolder.wifiNetworkSignalIcon = (ImageView) convertView.findViewById(R.id.wifi_signal_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.wifiNetworkSsid.setText(mWifiNetworksList.get(position));

        int signal = Integer.parseInt(mWifiSignalList.get(position));

        if ((signal <= 0) && (signal > -40)) {
            viewHolder.wifiNetworkSignalIcon.setImageResource(R.drawable.ic_signal_wifi_4);
        } else if ((signal <= -40) && (signal > -60)) {
            viewHolder.wifiNetworkSignalIcon.setImageResource(R.drawable.ic_signal_wifi_3);
        } else if ((signal <= -60) && (signal > -70)) {
            viewHolder.wifiNetworkSignalIcon.setImageResource(R.drawable.ic_signal_wifi_2);
        } else if ((signal <= -70) && (signal > -80)) {
            viewHolder.wifiNetworkSignalIcon.setImageResource(R.drawable.ic_signal_wifi_1);
        } else viewHolder.wifiNetworkSignalIcon.setImageResource(R.drawable.ic_signal_wifi_0);

        return convertView;
    }

    private static class ViewHolder {
        TextView wifiNetworkSsid;
        ImageView wifiNetworkSignalIcon;
    }
}
