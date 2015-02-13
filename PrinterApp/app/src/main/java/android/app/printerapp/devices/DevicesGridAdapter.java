package android.app.printerapp.devices;

import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.pnikosis.materialishprogress.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

/**
 * This class will handle the View adapter for the Devices fragment
 *
 * @author alberto-baeza
 */
public class DevicesGridAdapter extends ArrayAdapter<ModelPrinter> implements Filterable {

    private Context mContext;

    //Original list and current list to be filtered
    private ArrayList<ModelPrinter> mCurrent;
    private ArrayList<ModelPrinter> mOriginal;

    //Filter
    private GridFilter mFilter;


    //Constructor
    public DevicesGridAdapter(Context context, int resource, List<ModelPrinter> objects) {
        super(context, resource, objects);
        mContext = context;
        mOriginal = (ArrayList<ModelPrinter>) objects;
        mCurrent = (ArrayList<ModelPrinter>) objects;

    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return super.isEnabled(position);
    }

    //Overriding our view to show the grid on screen
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        //For every element on the list we create a model printer, but only use the
        //ones that are actually holding printers, else are empty spaces
        ModelPrinter m = getItem(position);

        //View not yet created
        if (convertView == null) {

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.grid_item_printer, null, false);

            holder = new ViewHolder();
            holder.textViewTag = (TextView) convertView.findViewById(R.id.discover_printer_name);
            holder.textViewLoading = (TextView) convertView.findViewById(R.id.grid_text_loading);
            holder.imageIcon = (ImageView) convertView.findViewById(R.id.discover_printer_icon);
            holder.progressBarPrinting = (ProgressBar) convertView.findViewById(R.id.grid_element_progressbar);
            holder.progressBarLoading = (ProgressWheel) convertView.findViewById(R.id.grid_element_loading);
            holder.imageWarning = (ImageView) convertView.findViewById(R.id.grid_warning_icon);
            holder.gridItem = (LinearLayout) convertView.findViewById(R.id.grid_item_printer_container);
            convertView.setTag(holder);

        } else {

            holder = (ViewHolder) convertView.getTag();
        }

        //Hide icons and progress bars
        holder.textViewLoading.setVisibility(View.GONE);
        holder.imageWarning.setVisibility(View.GONE);
        holder.progressBarPrinting.setVisibility(View.GONE);
        holder.progressBarLoading.setVisibility(View.INVISIBLE);

        //Check if it's an actual printer or just an empty slot
        if (m == null) {

            //Empty slot is an invisible printer on the current position
            convertView.setOnDragListener(new DevicesEmptyDragListener(position, this));
            holder.textViewTag.setText("");
            holder.imageIcon.setVisibility(View.INVISIBLE);
            holder.imageIcon.clearColorFilter();
            //holder.gridItem.setBackgroundResource(0);

        //It's a printer
        } else {

            if ((m.getStatus() == StateUtils.STATE_NEW) || (m.getStatus() == StateUtils.STATE_ADHOC)) {

                //Empty slot is an invisible printer on the current position
                convertView.setOnDragListener(null);
                holder.textViewTag.setText("");
                holder.imageIcon.setVisibility(View.GONE);
                holder.imageIcon.clearColorFilter();

            } else {


                //Intialize visual parameters
                convertView.setOnDragListener(new DevicesDragListener(mContext, m, this));
                holder.textViewTag.setText(m.getDisplayName());
                //holder.textViewTag.setTextColor(m.getDisplayColor());
                holder.imageIcon.setVisibility(View.VISIBLE);
                holder.imageIcon.setColorFilter(m.getDisplayColor(), Mode.SRC_ATOP);


                int status = m.getStatus();


                //LinearLayout gridItem = (LinearLayout) convertView.findViewById(R.id.grid_item_printer_container);
                holder.gridItem.setBackgroundResource(R.drawable.selectable_rect_background_green);

                //Printer icon
                switch (status) {

                /*case StateUtils.STATE_NONE: {
                    holder.imageIcon.setImageResource(R.drawable.icon_printer);
                }
                break;*/

                    case StateUtils.STATE_NEW:

                        holder.imageIcon.setImageResource(R.drawable.printer_signal_add);

                        break;
                    case StateUtils.STATE_ADHOC:
                        holder.imageIcon.setImageResource(R.drawable.signal_octopidev);

                        break;

                    default: {


                        switch (m.getType()) {

                            case StateUtils.TYPE_WITBOX:


                                if (m.getDisplayColor() != 0) {

                                    holder.imageIcon.setImageResource(R.drawable.printer_witbox_alpha);
                                    holder.imageIcon.setColorFilter(m.getDisplayColor(), Mode.DST_ATOP);

                                } else holder.imageIcon.setImageResource(R.drawable.printer_witbox_default);


                                break;

                            case StateUtils.TYPE_PRUSA:

                                if (m.getNetwork() != null)
                                    if (m.getNetwork().equals(MainActivity.getCurrentNetwork(getContext()))) {
                                        if (m.getDisplayColor() != 0) {

                                            holder.imageIcon.setImageResource(R.drawable.printer_prusa_alpha);
                                            holder.imageIcon.setColorFilter(m.getDisplayColor(), Mode.DST_ATOP);

                                        } else
                                            holder.imageIcon.setImageResource(R.drawable.printer_prusa_default);
                                    } else
                                        holder.imageIcon.setImageResource(R.drawable.printer_prusa_nowifi);

                                break;

                            case StateUtils.TYPE_CUSTOM:

                                if (m.getDisplayColor() != 0) {

                                    holder.imageIcon.setImageResource(R.drawable.printer_custom_alpha);
                                    holder.imageIcon.setColorFilter(m.getDisplayColor(), Mode.DST_ATOP);

                                } else
                                    holder.imageIcon.setImageResource(R.drawable.printer_custom_default);

                                break;


                            default:
                                holder.imageIcon.setImageResource(R.drawable.printer_custom_default);
                                break;


                        }

                    }
                    break;

                }

                //Status icon
                switch (status) {

                    case StateUtils.STATE_OPERATIONAL: {


                        //Check for printing completion
                        if (m.getJob() != null) {

                            //Currently finished means operational + file loaded with 100% progress
                            if (!m.getJob().getProgress().equals("null")) {

                                if (m.getJob().getFinished()) {

                                    holder.progressBarPrinting.setVisibility(View.VISIBLE);
                                    holder.progressBarPrinting.setProgress(100);
                                    //holder.progressBarPrinting.getProgressDrawable().setColorFilter(Color.parseColor("#ff009900"), Mode.SRC_IN);
                                    holder.textViewLoading.setText(R.string.devices_text_completed);
                                    holder.textViewLoading.setVisibility(View.VISIBLE);
                                }

							/*Double n = Double.parseDouble(m.getJob().getProgress() );

							if (n.intValue() == 100){


								pb.setVisibility(View.VISIBLE);
								pb.setProgress(n.intValue());
								pb.getProgressDrawable().setColorFilter(Color.GREEN, Mode.SRC_IN);
								tvl.setText(R.string.devices_text_completed);
								tvl.setVisibility(View.VISIBLE);


								//DevicesFragment.playMusic();
							}*/
                            }

                        }

                        //Must put this second because loading has priority over completion
                        if (!m.getLoaded()) {

                            //check if a file is loading
                            holder.progressBarLoading.setVisibility(View.VISIBLE);
                            holder.textViewLoading.setText(R.string.devices_text_loading);
                            holder.textViewLoading.setVisibility(View.VISIBLE);
                        }

                    }
                    break;


                    //When printing, show status bar and update progress
                    case StateUtils.STATE_PRINTING: {

                        holder.progressBarPrinting.setVisibility(View.VISIBLE);
                        if (!m.getJob().getProgress().equals("null")) {

                            Double n = Double.valueOf(m.getJob().getProgress());

                            holder.progressBarPrinting.setProgress(n.intValue());
                        }

                    }
                    break;

                    case StateUtils.STATE_PAUSED: {
                        holder.progressBarPrinting.setVisibility(View.VISIBLE);
                        Double n = Double.valueOf(m.getJob().getProgress());
                        holder.progressBarPrinting.setProgress(n.intValue());
                        holder.textViewLoading.setText(R.string.devices_text_paused);
                        holder.textViewLoading.setVisibility(View.VISIBLE);

                    }
                    break;

                    //when closed or error, show error icon
                    case StateUtils.STATE_CLOSED:
                    case StateUtils.STATE_ERROR: {
                        holder.imageWarning.setImageResource(R.drawable.icon_error);
                        holder.imageWarning.setVisibility(View.VISIBLE);
                    }
                    break;

                    //When connecting show status bar
                    case StateUtils.STATE_CONNECTING: {
                        holder.textViewLoading.setText(R.string.devices_text_connecting);
                        holder.textViewLoading.setVisibility(View.VISIBLE);
                        holder.progressBarLoading.setVisibility(View.VISIBLE);
                    }
                    break;

                    case StateUtils.STATE_NONE:
                        holder.textViewLoading.setText("Offline");
                        holder.textViewLoading.setVisibility(View.VISIBLE);
                        holder.progressBarLoading.setVisibility(View.VISIBLE);
                        break;

                    default: {
                    }

                }


                if (m.getNetwork() != null)
                    if (!m.getNetwork().equals(MainActivity.getCurrentNetwork(getContext()))) {
                        holder.imageIcon.clearColorFilter();
                        holder.imageIcon.setImageResource(R.drawable.printer_witbox_nowifi);
                    }

            }
        }

        return convertView;
    }

    //Retrieve item from current list by its position on the grid
    @Override
    public ModelPrinter getItem(int position) {

        for (ModelPrinter p : mCurrent) {
            if (p.getPosition() == position) return p;
        }
        return null;
    }

    //Retrieve count for MAX items to show empty slots
    @Override
    public int getCount() {
        return DevicesListController.GRID_MAX_ITEMS;
    }

    //Get filter
    @Override
    public Filter getFilter() {

        if (mFilter == null)
            mFilter = new GridFilter();
        return mFilter;
    }


    static class ViewHolder {

        TextView textViewTag;
        TextView textViewLoading;
        ImageView imageIcon;
        ProgressBar progressBarPrinting;
        ProgressWheel progressBarLoading;
        ImageView imageWarning;
        LinearLayout gridItem;


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
