package android.app.printerapp.devices;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.StateUtils;
import android.app.printerapp.model.ModelJob;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DevicesListAdapter extends ArrayAdapter<ModelPrinter>{

	public DevicesListAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		final ModelPrinter m = getItem(position);
		
		
		//View not yet created
		if (v==null){
			
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_element, null, false);
			v.setOnClickListener(null);
			v.setOnDragListener(new DevicesDragListener(m));
						
		} else {
			//v = convertView;
		}
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.list_column_1_text);
		tag.setText(m.getName());
		
		ImageView icon = (ImageView) v.findViewById(R.id.list_column_1_icon);
		
		ModelJob job = m.getJob();
		
		
		
		
		
		//Job references, if there's no job, don't update anything
		//TODO: Remove null option
		try{
			
			
			TextView statusText = (TextView) v.findViewById(R.id.list_column_3);	
			
			int status = m.getStatus();
			
			
			

			if (status==StateUtils.STATE_PRINTING){
				statusText.setText(getProgress(job.getProgress()) + "% (" + job.getPrintTimeLeft() + " left)");
				
			} else //Witbox icon
			switch(status){
			
				case StateUtils.STATE_NONE:{
					icon.setImageResource(R.drawable.icon_printer);	
				}break;
				
				case StateUtils.STATE_NEW:
				case StateUtils.STATE_ADHOC: {
					tag.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
					statusText.setTextColor(getContext().getResources().getColor(android.R.color.darker_gray));
					
					statusText.setText(R.string.devices_add_dialog_title);
					icon.setImageResource(R.drawable.icon_detectedprinter);
				}break;
				
				default:{
					tag.setTextColor(getContext().getResources().getColor(android.R.color.black));
					statusText.setTextColor(getContext().getResources().getColor(android.R.color.black));
					
					statusText.setText(m.getMessage());
					icon.setImageResource(R.drawable.icon_selectedprinter);	
				}break;
			
			}
			
		}catch (NullPointerException e){
			Log.i("DEVICES", "No job");
		}
		
		
		return v;
	}
	
	public static String getProgress(String p){
		
		double value = 0;
				
		try {
			value = Double.valueOf(p) * 100;			
		}catch (NullPointerException e){
			e.printStackTrace();
		}
		
		return String.valueOf((int)value);
	}
	
	

}
