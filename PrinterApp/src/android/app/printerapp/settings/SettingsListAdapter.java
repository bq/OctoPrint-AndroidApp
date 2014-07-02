package android.app.printerapp.settings;

import java.util.List;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class SettingsListAdapter extends ArrayAdapter<ModelPrinter>{

	public SettingsListAdapter(Context context, int resource,
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
			v = inflater.inflate(R.layout.settings_row, null, false);
			
		} else {
			//v = convertView;
		}
		
		TextView tv = (TextView) v.findViewById(R.id.settings_text);
		tv.setText(m.getName());
		
		return v;
	}
 
}
