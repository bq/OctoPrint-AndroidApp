package android.app.printerapp.devices;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelJob;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintControl;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
			
			v.findViewById(R.id.list_column_5_icon_1).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendCommand(m.getAddress(), "start");
					
				}
			});
			
			v.findViewById(R.id.list_column_5_icon_2).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					OctoprintControl.sendCommand(m.getAddress(), "cancel");
					
				}
			});
			
		} else {
			//v = convertView;
		}
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.list_column_1_text);
		tag.setText(m.getName());
		
		ModelJob job = m.getJob();
		
		
		//Job references, if there's no job, don't update anything
		//TODO: Remove null option
		try{
			

			TextView file = (TextView) v.findViewById(R.id.list_column_2);
			file.setText(job.getFilename());
			
			TextView status = (TextView) v.findViewById(R.id.list_column_3);	
			
			String state = m.getStatus();
			
			if (state.equals("Printing")){
				status.setText(getProgress(job.getProgress()) + "% (" + job.getPrintTimeLeft() + " left)");
				
			} else status.setText(state);
			
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
