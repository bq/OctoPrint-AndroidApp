package android.app.printerapp.devices.discovery;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 *  TODO: This class will hold the custom adapter to the discovery printer list, but since it doesn't allow
 * multichoice natively, it's on hold until an implementation comes around.
 *
 * @author alberto-baeza
 *
 */
public class DiscoveryOptionAdapter extends ArrayAdapter<ModelPrinter>{

	public DiscoveryOptionAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		final ModelPrinter m = getItem(position);
		
	
		
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.discovery_element, null, false);	
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					DatabaseController.writeDb(m.getName(),m.getAddress());
					
				}
			});
						
		} else {
			
		}
		
		TextView tv1 = (TextView) v.findViewById(R.id.discovery_tv1);
		TextView tv2 = (TextView) v.findViewById(R.id.discovery_tv2);
		
		tv1.setText(m.getName());
		tv2.setText(m.getAddress());
		
		return v;
	}

}
