package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class QuickPrintHorizontalListViewAdapter extends BaseAdapter {

    private static String TAG = "QuickPrintHorizontalListViewAdapter";

    private Context mContext;
    private LayoutInflater mInflater;

    private ArrayList<QuickPrintModel> mModelList = new ArrayList<QuickPrintModel>();

    public QuickPrintHorizontalListViewAdapter(Context context, ArrayList<QuickPrintModel> modelList) {
        this.mContext = context;
        this.mModelList = modelList;
        this.mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getCount() {
        return mModelList.size();
    }

    @Override
    public Object getItem(int i) {
        return mModelList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        Log.d(TAG, "Notify data set changed [" + mModelList.toString() + "]");
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (convertView == null) {
            //Create a custom card view with the info of the model
            convertView = mInflater.inflate(R.layout.list_item_card_view_model, null);
            holder = new ViewHolder();
            holder.modelImage = (ImageView) convertView.findViewById(R.id.model_image_view);
            holder.modelName = (TextView) convertView.findViewById(R.id.model_name_text_view);
            holder.modelDescription = (TextView) convertView.findViewById(R.id.model_description_text_view);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final QuickPrintModel quickPrintModel = mModelList.get(position);

        //Set the info of the model
        holder.modelImage.setImageDrawable(quickPrintModel.getModelImageDrawable());
        holder.modelName.setText(quickPrintModel.getModelName());
        holder.modelDescription.setText(quickPrintModel.getModelDescription());

        return convertView;
    }

    static class ViewHolder {
        ImageView modelImage;
        TextView modelName;
        TextView modelDescription;
    }
}
