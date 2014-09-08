package android.app.printerapp.devices;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.StateUtils;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class DevicesCameraAdapter extends ArrayAdapter<ModelPrinter>{

	//TODO: REDO ADAPTER EVERYTIME
	public DevicesCameraAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;		
		
		ModelPrinter m = getItem(position);
		
		//View not yet created
		if (v==null){

			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.video_view, null, false);
			
			if (m.getStatus()!=StateUtils.STATE_NEW)
			if (m.getVideo().getParent() == null)	{
				
				
				//Create a new linear layout to store the view
				LinearLayout ll = new LinearLayout(getContext());
				LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
		                 LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);    
				
				
				ll.addView(m.getVideo(), layoutParams);
				
				LinearLayout layout = (LinearLayout) v.findViewById(R.id.video_layout);
				//Remove all previous views (refresh icon)
				layout.removeAllViews();
				layout.addView(ll);
			}
			
	
			
		}else{
			
		}
		
		TextView tv = (TextView) v.findViewById(R.id.video_label);
		tv.setText(m.getDisplayName());
		
		

		return v;
	}
	
}
