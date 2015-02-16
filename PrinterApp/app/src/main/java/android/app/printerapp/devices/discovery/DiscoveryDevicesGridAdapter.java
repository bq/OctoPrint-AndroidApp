package android.app.printerapp.devices.discovery;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter that creates the items of the discovery grid view
 */
public class DiscoveryDevicesGridAdapter extends BaseAdapter {

    private final static String TAG = "DrawerListAdapter";

    private Context mContext;
    private List<ModelPrinter> mDevicesGridItems;

    public DiscoveryDevicesGridAdapter(Context context, List<ModelPrinter> devicesGridItems) {
        this.mContext = context;
        this.mDevicesGridItems = devicesGridItems;
    }

    @Override
    public int getCount() {
        return mDevicesGridItems.size();
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
            convertView = inflater.inflate(R.layout.grid_item_discover_printer, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.printerName = (TextView) convertView.findViewById(R.id.discover_printer_name);
            viewHolder.printerIcon = (ImageView) convertView.findViewById(R.id.discover_printer_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ModelPrinter modelPrinter = mDevicesGridItems.get(position);

        if(modelPrinter.getStatus() == StateUtils.STATE_NEW) {
            viewHolder.printerName.setText(modelPrinter.getAddress().replaceAll("/", ""));
            viewHolder.printerIcon.setImageResource(R.drawable.printer_signal_add);
        }
        else {
            viewHolder.printerName.setText(modelPrinter.getName());
            viewHolder.printerIcon.setImageResource(R.drawable.signal_octopidev);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView printerName;
        ImageView printerIcon;
    }
}
