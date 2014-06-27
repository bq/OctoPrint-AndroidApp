package android.app.printerapp.library;

import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelFile;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * This clas will handle the adapter for the library items
 * @author alberto-baeza
 *
 */
public class StorageAdapter extends ArrayAdapter<ModelFile> implements Filterable {
	
	//Original list and current list to be filtered
	private ArrayList<ModelFile> mCurrent;
	private ArrayList<ModelFile> mOriginal;
	
	//Filter
	private ListFilter mFilter;

	public StorageAdapter(Context context, int resource, List<ModelFile> objects) {
		super(context, resource, objects);
		mOriginal = (ArrayList<ModelFile>) objects;
		mCurrent = (ArrayList<ModelFile>) objects;
		mFilter = new ListFilter();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		ModelFile m = getItem(position);
		
		//View not yet created
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.storage_main, null, false);			
			
		} else {
			//v = convertView;
		}
		
		TextView tv = (TextView) v.findViewById(R.id.storage_label);
		tv.setText(m.getName());
		
		ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);
		
		if (m.getStorage().equals("Internal storage")){
			Drawable d;
			d = m.getSnapshot();
		
			if (d!=null){
				iv.setImageDrawable(d);
			} else {
				iv.setImageResource(R.drawable.file_icon);
			}
		} else iv.setImageResource(R.drawable.file_icon);
		
		
			
		
		
		return v;
	}
	

	//Retrieve item from current list
	@Override
	public ModelFile getItem(int position) {
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
	
	/**
	 * This class is the custom filter for the Library
	 * @author alberto-baeza
	 *
	 */
	private class ListFilter extends Filter{

		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			
			//New filter result object
            FilterResults result = new FilterResults();
            
            if(constraint != null && constraint.toString().length() > 0)
            {
            	//Temporal list
                ArrayList<ModelFile> filt = new ArrayList<ModelFile>();
                
                if ((constraint.equals("gcode"))||(constraint.equals("stl"))){
                	
                	//Check if every item from the CURRENT list has the constraint
                    for (ModelFile m : mCurrent){
                    	
                    	if (m.getName().contains(constraint)){
                    		filt.add(m);
                    	}
                    	
                    }
                } else {
                	 //Check if every item from the original list has the constraint
                    for (ModelFile m : mOriginal){
                    	
                    	if (m.getStorage().contains(constraint)){
                    		filt.add(m);
                    	}
                    	
                    }
                }
                
               

                //New list is filtered list
                result.count = filt.size();
                result.values = filt;
            }
            else
            {
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
			mCurrent = (ArrayList<ModelFile>) results.values;
			notifyDataSetChanged();
			
		}
		
	}

}
