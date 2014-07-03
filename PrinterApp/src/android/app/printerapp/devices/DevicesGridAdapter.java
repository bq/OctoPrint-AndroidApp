package android.app.printerapp.devices;

import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This class will handle the View adapter for the Devices fragment
 * @author alberto-baeza
 *
 */
public class DevicesGridAdapter extends ArrayAdapter<ModelPrinter> implements Filterable{
	
	//Original list and current list to be filtered
	private ArrayList<ModelPrinter> mCurrent;
	private ArrayList<ModelPrinter> mOriginal;
	//Filter
	private GridFilter mFilter;

	//Constructor
	public DevicesGridAdapter(Context context, int resource, List<ModelPrinter> objects) {
		super(context, resource, objects);
		
		mOriginal = (ArrayList<ModelPrinter>) objects;
		mCurrent = (ArrayList<ModelPrinter>) objects;
	}
	
	//Overriding our view to show the grid on screen
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		ModelPrinter m = getItem(position);
		
		
		//View not yet created
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.grid_element, null, false);
			
			v.setOnDragListener(new DevicesDragListener(m));
			
			
		} else {
			//v = convertView;
		}
		
		
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.grid_element_tag);
		tag.setText(m.getName());
				
		ImageView iv = (ImageView) v.findViewById(R.id.grid_warning_icon);
		ImageView icon = (ImageView) v.findViewById(R.id.grid_element_icon);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.grid_element_progressbar);
		
		
		String status = m.getStatus();
		
		if (status!=null){
			
			if (status.equals("Offline")){
				icon.setImageResource(R.drawable.witbox_offline_icon);	
			} else if (status.equals("New")){
				icon.setImageResource(R.drawable.witbox_offline_icon_ghost);
			} else icon.setImageResource(R.drawable.witbox_icon);	
			
			if (status.equals("Operational")){
				iv.setImageResource(R.drawable.tick_icon_small);
				iv.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			} else if (status.equals("Printing")){
				iv.setVisibility(View.VISIBLE);
				pb.setVisibility(View.VISIBLE);
				Double n = Double.valueOf(m.getJob().getProgress() ) * 100;
				pb.setProgress(n.intValue());
				
				iv.setImageResource(R.drawable.printer_icon);
			} else if (status.equals("Error")){
				iv.setImageResource(R.drawable.warning_icon);
				iv.setVisibility(View.VISIBLE);
				pb.setVisibility(View.GONE);
			} else{
				iv.setVisibility(View.GONE);
				pb.setVisibility(View.GONE);
				Log.i("OUT","INVISIBLE!");
			}
		} else Log.i("OUT","NULL STATUS");
		
		
		
		return v;
	}
	
	//Retrieve item from current list
	@Override
	public ModelPrinter getItem(int position) {
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
			mFilter = new GridFilter();
		
		return mFilter;
	}
		
		/**
		 * This class is the custom filter for the Library
		 * @author alberto-baeza
		 *
		 */
		private class GridFilter extends Filter{

			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				
				//New filter result object
	            FilterResults result = new FilterResults();
	            
	            if(constraint != null && constraint.toString().length() > 0)
	            {
	            	//Temporal list
	                ArrayList<ModelPrinter> filt = new ArrayList<ModelPrinter>();
	                
	             
                	 //Check if every item from the original list has the constraint
                    for (ModelPrinter m : mOriginal){
                    	
                    	if (m.getStatus().contains(constraint)){
                    		filt.add(m);
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
				mCurrent = (ArrayList<ModelPrinter>) results.values;
				notifyDataSetChanged();
				
			}
			
		}

}
