package android.app.printerapp.history;

import android.app.printerapp.ListContent;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.library.LibraryController;
import android.app.printerapp.model.ModelFile;
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
 * Created by alberto-baeza on 2/18/15.
 */
public class HistoryDrawerAdapter extends BaseAdapter{

    private final static String TAG = "DrawerListAdapter";

    private Context mContext;
    private List<ListContent.DrawerListItem> mDrawerListItems;

    public HistoryDrawerAdapter(Context context, List<ListContent.DrawerListItem> drawerListItems) {
        this.mContext = context;
        this.mDrawerListItems = drawerListItems;
    }


    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mDrawerListItems.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    public void removeItem(int i) {
        mDrawerListItems.remove(i);
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Log.d(TAG, "getView");

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.history_drawer_layout, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.drawerItemName = (TextView) convertView.findViewById(R.id.history_drawer_model);
            viewHolder.drawerItemIcon = (ImageView) convertView.findViewById(R.id.history_icon_imageview);
            viewHolder.drawerItemType = (TextView) convertView.findViewById(R.id.history_drawer_type);
            viewHolder.drawerItemTime = (TextView) convertView.findViewById(R.id.history_drawer_printtime);
            viewHolder.drawerItemDate = (TextView) convertView.findViewById(R.id.history_drawer_date);


            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ListContent.DrawerListItem drawerListItem = mDrawerListItems.get(position);

        viewHolder.drawerItemName.setText(drawerListItem.model);
        viewHolder.drawerItemType.setText(drawerListItem.type);
        viewHolder.drawerItemTime.setText(drawerListItem.time);
        viewHolder.drawerItemDate.setText(drawerListItem.date);

        if (drawerListItem.path != null){

            File path = new File(drawerListItem.path);
            String root = (path.getParentFile().getParentFile().getAbsolutePath());

            for (File m : LibraryController.getFileList()){

                if (LibraryController.isProject(m)){

                    if (m.getAbsolutePath().equals(root)){

                        viewHolder.drawerItemIcon.setImageDrawable(((ModelFile)m).getSnapshot());
                        viewHolder.drawerItemName.setText(path.getName());

                        break;

                    }

                }

            }

        } else viewHolder.drawerItemIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_file_gray));




        return convertView;
    }

    private static class ViewHolder {
        TextView drawerItemName;
        TextView drawerItemType;
        TextView drawerItemTime;
        TextView drawerItemDate;
        ImageView drawerItemIcon;
    }

}
