package android.app.printerapp.devices;

import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
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
	
	
	//max items in the grid
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
	
	//TODO implement view holder
	//Overriding our view to show the grid on screen
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		View v = convertView;
		
		//For every element on the list we create a model printer, but only use the
		//ones that are actually holding printers, else are empty spaces
		ModelPrinter m = getItem(position);
					
		//View not yet created
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.devices_grid_element, null, false);
			
			
		} else {
			//v = convertView;

		}
		
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.grid_element_tag);
		TextView ip = (TextView) v.findViewById(R.id.grid_element_ip);
		TextView tvl = (TextView) v.findViewById(R.id.grid_text_loading);
		ImageView icon = (ImageView) v.findViewById(R.id.grid_element_icon);
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.grid_element_progressbar);
		ProgressBar pl = (ProgressBar) v.findViewById(R.id.grid_element_loading);
		ImageView iv = (ImageView) v.findViewById(R.id.grid_warning_icon);
		
		
		//Hide icons and progress bars
		tvl.setVisibility(View.GONE);
		iv.setVisibility(View.GONE);
		pb.setVisibility(View.GONE);
		pl.setVisibility(View.INVISIBLE);

		//Check if it's an actual printer or just an empty slot
		if (m==null){
			
			//Empty slot is an invisible printer on the current position
			v.setOnDragListener(new DevicesEmptyDragListener(position));
			tag.setText("");
			ip.setText("");
			icon.setVisibility(View.INVISIBLE);
			
			//it's a printer
		} else {
			
			//intialize visual parameters
			v.setOnDragListener(new DevicesDragListener(m));
			tag.setText(m.getDisplayName());
			ip.setText(m.getAddress().replace("/", ""));
			icon.setVisibility(View.VISIBLE);
			
			int status = m.getStatus();
			
			//Printer icon
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

					
					//Check for printing completion
					if (m.getJob()!=null){
						
						//Currently finished means operational + file loaded with 100% progress
						if (!m.getJob().getProgress().equals("null")){
							
							if (m.getJob().getFinished()){
								pb.setVisibility(View.VISIBLE);
								pb.setProgress(100);
								pb.getProgressDrawable().setColorFilter(Color.GREEN, Mode.SRC_IN);
								tvl.setText(R.string.devices_text_completed);
								tvl.setVisibility(View.VISIBLE);
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
						pl.setVisibility(View.VISIBLE);
						tvl.setText(R.string.devices_text_loading);
						tvl.setVisibility(View.VISIBLE);
					}

				} break;
				
				//When printing, show status bar and update progress
				case StateUtils.STATE_PRINTING:{

					pb.setVisibility(View.VISIBLE);
					Double n = Double.valueOf(m.getJob().getProgress() );
					pb.setProgress(n.intValue());
					
				}break;
				
				case StateUtils.STATE_PAUSED:{
					pb.setVisibility(View.VISIBLE);
					Double n = Double.valueOf(m.getJob().getProgress() );
					pb.setProgress(n.intValue());
					tvl.setText(R.string.devices_text_paused);
					tvl.setVisibility(View.VISIBLE);
					
				}break;
				
				//when closed or error, show error icon
				case StateUtils.STATE_CLOSED:
				case StateUtils.STATE_ERROR:{
					iv.setImageResource(R.drawable.icon_error);
					iv.setVisibility(View.VISIBLE);
				}break;
				
				//When connecting show status bar
				case StateUtils.STATE_CONNECTING: {
					tvl.setText(R.string.devices_text_connecting);
					tvl.setVisibility(View.VISIBLE);
					pl.setVisibility(View.VISIBLE);
				} break;
				
				default:{
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
