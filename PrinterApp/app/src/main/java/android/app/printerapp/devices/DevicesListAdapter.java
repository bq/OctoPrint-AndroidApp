package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelJob;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the listview mode, has reduced functionality and status text added
 *
 * @author alberto-baeza
 */
public class DevicesListAdapter extends ArrayAdapter<ModelPrinter> {

    private Context mContext;

    //Original list and current list to be filtered
    private ArrayList<ModelPrinter> mCurrent;
    private ArrayList<ModelPrinter> mOriginal;

    //Filter
    private GridFilter mFilter;

    public DevicesListAdapter(Context context, int resource,
                              List<ModelPrinter> objects) {
        super(context, resource, objects);
        mContext = context;
        mOriginal = (ArrayList<ModelPrinter>) objects;
        mCurrent = (ArrayList<ModelPrinter>) objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        final ModelPrinter m = getItem(position);


        //TODO Holder for the list adapter
        //View not yet created
        if (v == null) {


            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(R.layout.list_item_printer, null, false);
            v.setOnDragListener(new DevicesDragListener(mContext, m, this));

        } else {
            //v = convertView;
        }

        //Printer tag reference
        TextView tag = (TextView) v.findViewById(R.id.printer_name_text_view);
        tag.setText(m.getDisplayName());

        ImageView icon = (ImageView) v.findViewById(R.id.printer_icon_imageview);

        ModelJob job = m.getJob();


        //Job references, if there's no job, don't update anything
        //TODO: Remove null option
        try {

            TextView statusText = (TextView) v.findViewById(R.id.printer_state_textview);
            int status = m.getStatus();

            if (status == StateUtils.STATE_PRINTING) {
                statusText.setText(getProgress(job.getProgress()) + "% (" + job.getPrintTimeLeft() + " left)");

            } else //Witbox icon
                switch (status) {

                    case StateUtils.STATE_NONE: {
                        icon.setImageResource(R.drawable.icon_printer);
                    }
                    break;

                    case StateUtils.STATE_NEW:
                    case StateUtils.STATE_ADHOC: {
                        tag.setTextColor(getContext().getResources().getColor(R.color.body_text_3));
                        statusText.setTextColor(getContext().getResources().getColor(R.color.body_text_3));

                        statusText.setText(R.string.devices_add_dialog_title);
                        icon.setImageResource(R.drawable.icon_detectedprinter);
                    }
                    break;

                    case StateUtils.STATE_ERROR: {
                        statusText.setTextColor(getContext().getResources().getColor(R.color.error));
                        statusText.setText(m.getMessage());
                    }
                    break;

                    default: {
                        tag.setTextColor(getContext().getResources().getColor(R.color.body_text_1));
                        statusText.setTextColor(getContext().getResources().getColor(R.color.body_text_2));

                        statusText.setText(m.getMessage());
                        icon.setImageResource(R.drawable.icon_selectedprinter);
                    }
                    break;

                }

        } catch (NullPointerException e) {

        }


        return v;
    }

    @Override
    public ModelPrinter getItem(int position) {
        return mCurrent.get(position);
    }

    //Retrieve count from current list
    @Override
    public int getCount() {
        return mCurrent.size();
    }

    public static String getProgress(String p) {

        double value = 0;

        try {
            value = Double.valueOf(p);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

        return String.valueOf((int) value);
    }


    //TODO: REDUNDANT FILTER
    //Get filter
    @Override
    public Filter getFilter() {

        if (mFilter == null)
            mFilter = new GridFilter();

        return mFilter;
    }

    /**
     * This class is the custom filter for the Library
     *
     * @author alberto-baeza
     */
    private class GridFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            //New filter result object
            FilterResults result = new FilterResults();

            if (constraint != null && constraint.toString().length() > 0) {
                //Temporal list
                ArrayList<ModelPrinter> filt = new ArrayList<ModelPrinter>();


                //TODO Should change filter logic to avoid redundancy
                if (!constraint.equals(String.valueOf(StateUtils.STATE_NEW))) {
                    //Check if every item from the original list has the constraint
                    for (ModelPrinter m : mOriginal) {

                        if (m.getStatus() == (Integer.parseInt(constraint.toString()))) {
                            filt.add(m);
                        }

                    }
                } else {

                    //Check if every item from the original list has the constraint
                    for (ModelPrinter m : mOriginal) {

                        if (m.getStatus() != (Integer.parseInt(constraint.toString()))) {
                            filt.add(m);
                        }

                    }
                }


                //New list is filtered list
                result.count = filt.size();
                result.values = filt;
            } else {
                //New list is original list (no filter, default)
                result.count = mOriginal.size();
                result.values = mOriginal;

            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {


            //If there are results, update list
            mCurrent = (ArrayList<ModelPrinter>) results.values;
            notifyDataSetChanged();

        }

    }


}
