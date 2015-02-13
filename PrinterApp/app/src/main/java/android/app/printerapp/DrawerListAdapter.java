package android.app.printerapp;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Adapter that creates the items of the drawer, with a name and an descriptive icon
 */
public class DrawerListAdapter extends BaseAdapter {

    private final static String TAG = "DrawerListAdapter";

    private Context mContext;
    private List<ListContent.DrawerListItem> mDrawerListItems;

    private int mActivatedPosition;

    public DrawerListAdapter(Context context, List<ListContent.DrawerListItem> drawerListItems) {
        this.mContext = context;
        this.mDrawerListItems = drawerListItems;
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


    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_drawer, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.drawerItemName = (TextView) convertView.findViewById(R.id.layout_navigation_drawer_item_textview);
            viewHolder.drawerItemIcon = (ImageView) convertView.findViewById(R.id.wifi_signal_icon);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        ListContent.DrawerListItem drawerListItem = mDrawerListItems.get(position);

        viewHolder.drawerItemName.setText(drawerListItem.content);

        //Set the text color and icon color based on whether the item is selected
        if (mActivatedPosition == position) {
            viewHolder.drawerItemName.setTextColor(mContext.getResources().getColor(R.color.navdrawer_text_color_selected));
            viewHolder.drawerItemIcon.setColorFilter(mContext.getResources().getColor(R.color.navdrawer_icon_tint_selected), PorterDuff.Mode.SRC_ATOP);
            viewHolder.drawerItemIcon.setImageDrawable(mContext.getResources().getDrawable(drawerListItem.iconId));
        } else {
            viewHolder.drawerItemName.setTextColor(mContext.getResources().getColor(R.color.navdrawer_text_color));
            viewHolder.drawerItemIcon.setColorFilter(mContext.getResources().getColor(R.color.navdrawer_icon_tint), PorterDuff.Mode.SRC_ATOP);
            viewHolder.drawerItemIcon.setImageDrawable(mContext.getResources().getDrawable(drawerListItem.iconId));
        }

        return convertView;
    }

    public void setActivatedPosition(int activatedPosition) {
        this.mActivatedPosition = activatedPosition;
    }

    private static class ViewHolder {
        TextView drawerItemName;
        ImageView drawerItemIcon;
    }
}
