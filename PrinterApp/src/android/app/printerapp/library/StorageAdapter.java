package android.app.printerapp.library;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelFile;
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


/**
 * This clas will handle the adapter for the library items
 * @author alberto-baeza
 *
 */
public class StorageAdapter extends ArrayAdapter<File> implements Filterable {
	
	//Original list and current list to be filtered
	private ArrayList<File> mCurrent;
	private ArrayList<File> mOriginal;
	
	//Filter
	private ListFilter mFilter;

	public StorageAdapter(Context context, int resource, List<File> objects) {
		super(context, resource, objects);
		mOriginal = (ArrayList<File>) objects;
		mCurrent = (ArrayList<File>) objects;
		mFilter = new ListFilter();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		File m = getItem(position);
		
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
		
		
		if (m.isDirectory()){
			
			if (StorageController.isProject(m)){
				
				Drawable d;
				d =((ModelFile)m).getSnapshot();
			
				if (d!=null){
					iv.setImageDrawable(d);
				} else {
					iv.setImageResource(R.drawable.folder_icon);
				}
				
			} else{	
				
				if (!m.getParentFile().getAbsolutePath().equals(StorageController.getCurrentPath())) {
				
					tv.setText("[parent folder]");
					iv.setImageResource(R.drawable.arrow_back);
				
				} else iv.setImageResource(R.drawable.folder_empty);
					
			
				
			}
			

			
		} else {
			iv.setImageResource(R.drawable.file_icon);
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
	
	public void removeFilter(){
		
		mCurrent = mOriginal;
		notifyDataSetChanged();
		
	}
	
	/**
	 * This class is the custom filter for the Library
	 * @author alberto-baeza
	 *
	 */
	private class ListFilter extends Filter{

		@SuppressLint("DefaultLocale")
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			
			//New filter result object
            FilterResults result = new FilterResults();
            
            if(constraint != null && constraint.toString().length() > 0)
            {
            	//Temporal list
                ArrayList<File> filt = new ArrayList<File>();
                
               // if ((constraint.equals("gcode"))||(constraint.equals("stl"))){
                	
                	//Check if every item from the CURRENT list has the constraint
                    for (File m : mCurrent){
                    	
                    	if (!m.isDirectory()) {
                    		if (m.getName().toLowerCase().contains(constraint.toString().toLowerCase())){
	                    		Log.i("OUT","Added a lel " + m.getName());
	                    		filt.add(m);
                    		}
                    	} else {
                    		
                    		if (!StorageController.isProject(m)){
                    			filt.add(m);
                    		} else {
                    			if (m.getName().toLowerCase().contains(constraint.toString().toLowerCase())){
    	                    		Log.i("OUT","Added a lel " + m.getName());
    	                    		filt.add(m);
                        		}
                    		}
                    		
                    	}
                    		
                    	
                    }
               /* } else {
                	 //Check if every item from the original list has the constraint
                    for (File m : mOriginal){
                    	
                    	if (m.isDirectory()){
                    		
                    		if (StorageController.isProject(m)){
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
			mCurrent = (ArrayList<File>) results.values;
			notifyDataSetChanged();
			
		}
		
	}

}
