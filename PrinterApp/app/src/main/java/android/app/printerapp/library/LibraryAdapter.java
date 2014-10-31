package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * This clas will handle the adapter for the library items
 *
 * @author alberto-baeza
 */
public class LibraryAdapter extends ArrayAdapter<File> implements Filterable {

    //Original list and current list to be filtered
    private ArrayList<File> mCurrent;
    private ArrayList<File> mOriginal;

    //Filter
    private ListFilter mFilter;

    private int mResource;

    public LibraryAdapter(Context context, int resource, List<File> objects) {
        super(context, resource, objects);
        mOriginal = (ArrayList<File>) objects;
        mCurrent = (ArrayList<File>) objects;
        mFilter = new ListFilter();

        mResource = resource;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;
        File m = getItem(position);

        //View not yet created
        if (v == null) {

            //Inflate the view
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = inflater.inflate(mResource, null, false);

        } else {
            //v = convertView;
        }

        TextView nameTextView = (TextView) v.findViewById(R.id.storage_name_textview);
        nameTextView.setText(m.getName());

        TextView pathTextView = (TextView) v.findViewById(R.id.storage_path_textview);
        pathTextView.setText(m.getAbsolutePath());

        ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);

        TextView gcodeTag = (TextView) v.findViewById(R.id.storage_gcode_tag);
        gcodeTag.setText("gcode");
        gcodeTag.setVisibility(View.GONE);

        if (m.isDirectory()) {

            if (LibraryController.isProject(m)) {
                Drawable d;
                d = ((ModelFile) m).getSnapshot();

                if (d != null) {
                    iv.setImageDrawable(d);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    iv.setImageResource(R.drawable.folder_normal_icon);
                    iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }

                if (((ModelFile) m).getStl() == null)
                    v.findViewById(R.id.storage_gcode_tag).setVisibility(View.VISIBLE);
            } else {
                iv.setImageResource(R.drawable.folder_normal_icon);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

        } else {

            //TODO Handle printer internal files
            if (m.getParent().equals("printer")) {
                iv.setImageResource(R.drawable.folder_internal_icon);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                ModelPrinter p = DevicesListController.getPrinter(Long.parseLong(m.getName()));
                nameTextView.setText(p.getDisplayName());
                pathTextView.setText(m.getAbsolutePath());

            } else {
                iv.setImageResource(R.drawable.file_icon);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                if (m.getParent().equals("sd")) {
                    gcodeTag.setText("sd");
                    gcodeTag.setVisibility(View.VISIBLE);
                } else if (m.getParent().equals("local")) {
                    gcodeTag.setText("internal");
                    gcodeTag.setVisibility(View.VISIBLE);
                }
            }
        }
        return v;
    }


    //Retrieve item from current list
    @Override
    public File getItem(int position) {
        return mCurrent.get(position);
    }

    //Retrieve count from current list
    @Override
    public int getCount() {
        return mCurrent.size();
    }

    //Get filter
    @Override
    public Filter getFilter() {

        if (mFilter == null)
            mFilter = new ListFilter();

        return mFilter;
    }

    public void removeFilter() {

        mCurrent = mOriginal;
        notifyDataSetChanged();

    }

    /**
     * This class is the custom filter for the Library
     *
     * @author alberto-baeza
     */
    private class ListFilter extends Filter {

        @SuppressLint("DefaultLocale")
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            //New filter result object
            FilterResults result = new FilterResults();

            if (constraint != null && constraint.toString().length() > 0) {
                //Temporal list
                ArrayList<File> filt = new ArrayList<File>();

                // if ((constraint.equals("gcode"))||(constraint.equals("stl"))){

                //Check if every item from the CURRENT list has the constraint
                for (File m : mCurrent) {

                    if (!m.isDirectory()) {
                        if (m.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            Log.i("OUT", "Added a lel " + m.getName());
                            filt.add(m);
                        }
                    } else {

                        if (!LibraryController.isProject(m)) {
                            filt.add(m);
                        } else {
                            if (m.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                Log.i("OUT", "Added a lel " + m.getName());
                                filt.add(m);
                            }
                        }
                    }
                }
               /* } else {
                     //Check if every item from the original list has the constraint
                    for (File m : mOriginal){
                    	
                    	if (m.isDirectory()){
                    		
                    		if (LibraryController.isProject(m)){
                    			if (((ModelFile)m).getStorage().contains(constraint)){
                            		filt.add(m);
                            	}
                    		}
                    		
                    	}
                    	
                    	
                    	
                    }*/
                //}


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
            mCurrent = (ArrayList<File>) results.values;
            notifyDataSetChanged();

        }

    }

}
