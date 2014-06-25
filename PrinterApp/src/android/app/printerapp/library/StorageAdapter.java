package android.app.printerapp.library;

import java.util.List;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelFile;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class StorageAdapter extends ArrayAdapter<ModelFile> {

	public StorageAdapter(Context context, int resource, List<ModelFile> objects) {
		super(context, resource, objects);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		ModelFile m = getItem(position);
		
		//View not yet created
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.storage_main, null, false);			
			
		} else {
			//v = convertView;
		}
		
		TextView tv = (TextView) v.findViewById(R.id.storage_label);
		tv.setText(m.getName());
		
		ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);
		
		Drawable d;
			d = Drawable.createFromPath(StorageController.getParentFolder() + "/" + m.getName() + "/" + m.getName() + ".jpg");
		
			if (d!=null){
				iv.setImageDrawable(d);
			} else {
				iv.setImageResource(R.drawable.file_icon);
			}
			
		
		
		return v;
	}

}
