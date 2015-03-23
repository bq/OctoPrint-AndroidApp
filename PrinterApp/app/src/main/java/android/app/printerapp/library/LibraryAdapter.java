package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
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

    //Flag to know if the list is being modified
    private boolean mListInSelectionMode;

    //List of selected items in selection mode
    private ArrayList<Boolean> mCheckedItems = new ArrayList<>();

    LibraryFragment mContext;

    private int mResource;

    public LibraryAdapter(Context context, LibraryFragment fragmentContext, int resource, List<File> objects) {
        super(context, resource, objects);
        mOriginal = (ArrayList<File>) objects;
        mCurrent = (ArrayList<File>) objects;
        mFilter = new ListFilter();
        mContext = fragmentContext;
        mResource = resource;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

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

        TextView nameTextView = (TextView) v.findViewById(R.id.model_name_textview);
        nameTextView.setText(m.getName());

        TextView dateTextView = (TextView) v.findViewById(R.id.model_mod_date_textview);

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        dateTextView.setText(sdf.format(m.lastModified()) + " " + m.getParentFile().getName() + "/");

        ImageView iv = (ImageView) v.findViewById(R.id.model_icon);

        //If selection mode is on, show the selection checkbox
        CheckBox selectModelCheckbox = (CheckBox) v.findViewById(R.id.select_model_checkbox);
        if (mListInSelectionMode) {

            try{
                selectModelCheckbox.setChecked(mCheckedItems.get(position));
                selectModelCheckbox.setVisibility(View.VISIBLE);
            } catch (IndexOutOfBoundsException e){

                e.printStackTrace();
            }

        } else {
            selectModelCheckbox.setChecked(false);
            selectModelCheckbox.setVisibility(View.INVISIBLE);
        }

        if (m.isDirectory()) {

            if (LibraryController.isProject(m)) {
                Drawable d;
                d = ((ModelFile) m).getSnapshot();

                if (d != null) {
                    iv.setImageDrawable(d);
                    iv.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    iv.setImageResource(R.drawable.ic_folder_grey600_36dp);
                    iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                }

            } else {
                iv.setImageResource(R.drawable.ic_folder_grey600_36dp);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

        } else {

            //TODO Handle printer internal files
            if (m.getParent().equals("printer")) {
                iv.setImageResource(R.drawable.ic_folder_grey600_36dp);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                ModelPrinter p = DevicesListController.getPrinter(Long.parseLong(m.getName()));
                nameTextView.setText(p.getDisplayName());


            } else {
                iv.setImageResource(R.drawable.ic_file_gray);
                iv.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            }

            dateTextView.setText(null);
        }

        ImageButton overflowButton = (ImageButton) v.findViewById(R.id.model_settings_imagebutton);
        if (overflowButton != null) {
            overflowButton.setColorFilter(getContext().getResources().getColor(R.color.body_text_3),
                    PorterDuff.Mode.MULTIPLY);
            overflowButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LibraryOnClickListener onClickListener = new LibraryOnClickListener(mContext, null);
                    onClickListener.onOverflowButtonClick(v, position);
                }
            });
        }


        //Hide overflow button in printer tab
        if ((mListInSelectionMode) || (mContext.getCurrentTab().equals(LibraryController.TAB_PRINTER))){

            overflowButton.setVisibility(View.GONE);

        } else overflowButton.setVisibility(View.VISIBLE);

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

    public void setSelectionMode(boolean isSelectionMode) {
        this.mListInSelectionMode = isSelectionMode;
        mCheckedItems = new ArrayList<>();
        for (int i = 0; i < mCurrent.size(); i++) {
            mCheckedItems.add(false);
        }
    }

    public void setItemChecked(int position, boolean checked) {
        mCheckedItems.set(position, checked);
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
                            filt.add(m);
                        }
                    } else {

                        if (!LibraryController.isProject(m)) {
                            filt.add(m);
                        } else {
                            if (m.getName().toLowerCase().contains(constraint.toString().toLowerCase())) {
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
