package android.app.printerapp.devices;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * This class will handle the View adapter for the Devices fragment
 * @author alberto-baeza
 *
 */
public class DevicesGridAdapter extends ArrayAdapter<ModelPrinter>{

	//Constructor
	public DevicesGridAdapter(Context context, int resource, List<ModelPrinter> objects) {
		super(context, resource, objects);
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
		ProgressBar pb = (ProgressBar) v.findViewById(R.id.grid_element_progressbar);
		
		
		String status = m.getStatus();
		
		if (status!=null){
			
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

}
