package android.app.printerapp.devices;

import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.StateUtils;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
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
	
	private static final int maxItems = 20;
	
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
			
			
		} else {
			//v = convertView;

		}
		
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.grid_element_tag);
		TextView tvl = (TextView) v.findViewById(R.id.grid_text_loading);
		ImageView icon = (ImageView) v.findViewById(R.id.grid_element_icon);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.grid_element_progressbar);
		ProgressBar pl = (ProgressBar) v.findViewById(R.id.grid_element_loading);
		ImageView iv = (ImageView) v.findViewById(R.id.grid_warning_icon);
		
		tvl.setVisibility(View.GONE);
		iv.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		pl.setVisibility(View.INVISIBLE);
		
		
		
		//Check if it's an actual printer or just an empty slot
		if (m==null){
			v.setOnDragListener(new DevicesEmptyDragListener(position));
			tag.setText("");
			icon.setVisibility(View.INVISIBLE);
			
		} else {
			v.setOnDragListener(new DevicesDragListener(m));
			tag.setText(m.getDisplayName());
			icon.setVisibility(View.VISIBLE);
			
			int status = m.getStatus();
			
			//Witbox icon
			switch(status){
			
				case StateUtils.STATE_NONE:{
					icon.setImageResource(R.drawable.icon_printer);	
				}break;
				
				case StateUtils.STATE_NEW:
				case StateUtils.STATE_ADHOC: {
					icon.setImageResource(R.drawable.icon_detectedprinter);
				}break;
				
				default:{
					icon.setImageResource(R.drawable.icon_selectedprinter);	
				}break;
			
			}
			
			//Status icon
			switch (status){
			
				case StateUtils.STATE_OPERATIONAL:{

					if (m.getJob()!=null){
						
						if (!m.getJob().getProgress().equals("null")){
							Double n = Double.parseDouble(m.getJob().getProgress() );
							if (n.intValue() == 100){
								pb.setVisibility(View.VISIBLE);
								pb.setProgress(n.intValue());
								tvl.setText(R.string.devices_text_completed);
								tvl.setVisibility(View.VISIBLE);
							}
						}
						
					}
					
					//Must put this second because loading has priority over completion
					
					if (!m.getLoaded()) {
						
						pl.setVisibility(View.VISIBLE);
						tvl.setText(R.string.devices_text_loading);
						tvl.setVisibility(View.VISIBLE);
					}
					
					//iv.setVisibility(View.GONE);
					//pb.setVisibility(View.GONE);
				} break;
				
				case StateUtils.STATE_PRINTING:{
					//iv.setVisibility(View.GONE);
					pb.setVisibility(View.VISIBLE);
					Double n = Double.valueOf(m.getJob().getProgress() );
					pb.setProgress(n.intValue());
					
					//iv.setImageResource(R.drawable.printer_icon);
				}break;
				case StateUtils.STATE_CLOSED:
				case StateUtils.STATE_ERROR:{
					iv.setImageResource(R.drawable.icon_error);
					iv.setVisibility(View.VISIBLE);
					//pb.setVisibility(View.GONE);
				}break;
				
				case StateUtils.STATE_CONNECTING: {
					tvl.setText(R.string.devices_text_connecting);
					tvl.setVisibility(View.VISIBLE);
					pl.setVisibility(View.VISIBLE);
				} break;
				
				default:{
					//iv.setVisibility(View.GONE);
					//pb.setVisibility(View.GONE);
					//Log.i("OUT","INVISIBLE!");
				}
				
			}
						
			
		}
			

		return v;
	}
	
	//Retrieve item from current list by its position on the grid
	@Override
	public ModelPrinter getItem(int position) {
					
		for (ModelPrinter p : mCurrent){
			if (p.getPosition()==position) return p;
		}

		return null;
	}
		
	//Retrieve count for MAX items to show empty slots
	@Override
	public int getCount() {
		return maxItems;
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
	                
	             
	                //TODO Should change filter logic to avoid redundancy
	                if (!constraint.equals(String.valueOf(StateUtils.STATE_NEW))){
	                	 //Check if every item from the original list has the constraint
	                    for (ModelPrinter m : mOriginal){
	                    	
	                    	if (m.getStatus() == (Integer.parseInt(constraint.toString()))){
	                    		filt.add(m);
	                    	}
	                    	
	                    }
	                } else {
	                	
	                	//Check if every item from the original list has the constraint
	                    for (ModelPrinter m : mOriginal){
	                    	
	                    	if (m.getStatus() != (Integer.parseInt(constraint.toString()))){
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
				mCurrent = (ArrayList<ModelPrinter>) results.values;
				notifyDataSetChanged();
				
			}
			
		}

}
