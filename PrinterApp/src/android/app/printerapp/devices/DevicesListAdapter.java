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
import android.widget.TextView;

public class DevicesListAdapter extends ArrayAdapter<ModelPrinter>{

	public DevicesListAdapter(Context context, int resource,
			List<ModelPrinter> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v;
		ModelPrinter m = getItem(position);
		
		
		//View not yet created
		if (convertView==null){
			
			
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.list_element, null, false);
			
		} else {
			v = convertView;
		}
		
		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.list_column_1_text);
		tag.setText(m.getName());
		Log.i("OUT","EYEYY" + m.getName());
		
		
		return v;
	}

}
