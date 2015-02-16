package android.app.printerapp.viewer;

import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Adapter that creates the items of the file browser
 */
public class FileBrowserAdapter extends BaseAdapter {

    private final static String TAG = "FileBrowserAdapter";

    private Context mContext;
    private List<File> mBrowserFilesList;
    private List<String> mBrowserFileNamesList;

    private int mActivatedPosition;

    public FileBrowserAdapter(Context context, List<String> browserFileNamesList, List<File> browserFilesList) {
        this.mContext = context;
        this.mBrowserFileNamesList = browserFileNamesList;
        this.mBrowserFilesList = browserFilesList;
    }

    @Override
    public int getCount() {
        return mBrowserFilesList.size();
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
            convertView = inflater.inflate(R.layout.list_item_file_browser, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.fileItemName = (TextView) convertView.findViewById(R.id.wifi_ssid_textview);
            viewHolder.fileItemIcon = (ImageView) convertView.findViewById(R.id.wifi_signal_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        viewHolder.fileItemName.setText(mBrowserFileNamesList.get(position));

        if(mBrowserFilesList.get(position).isDirectory())
            viewHolder.fileItemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_folder_grey600_36dp));
        else
            viewHolder.fileItemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file_gray));

        return convertView;
    }

    public void setActivatedPosition(int activatedPosition) {
        Log.d(TAG, "New activated position [" + activatedPosition + "]");
        this.mActivatedPosition = activatedPosition;
    }

    private static class ViewHolder {
        TextView fileItemName;
        ImageView fileItemIcon;
    }
}
