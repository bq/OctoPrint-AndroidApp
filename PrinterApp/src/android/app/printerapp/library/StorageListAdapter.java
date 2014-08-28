package android.app.printerapp.library;

import java.io.File;
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

public class StorageListAdapter extends ArrayAdapter<File>{

	public StorageListAdapter(Context context, int resource, List<File> objects) {
		super(context, resource, objects);
		
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View v = convertView;
		File m = getItem(position);
		
		//View not yet created
		if (v==null){
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.storage_list_element, null, false);			
			
		} else {
			//v = convertView;
		}
		
		TextView tv = (TextView) v.findViewById(R.id.storage_label);
		tv.setText(m.getName());
		
		ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);
		if (m.isDirectory()){
			
			if (StorageController.isProject(m)){
				
				Drawable d;
				d =((ModelFile)m).getSnapshot();
			
				if (d!=null){
					iv.setImageDrawable(d);
				} else {
					iv.setImageResource(R.drawable.browser_carpeta);
				}
				
			} else{	
				
				if (m.getAbsolutePath().equals(StorageController.getCurrentPath().getParentFile().getAbsolutePath())) {
				
					tv.setText("[parent folder]");
					iv.setImageResource(R.drawable.arrow_back);
				
				} else {
					iv.setImageResource(R.drawable.browser_carpeta);
				}
					
			
				
			}
			

			
		} else {
			if (m.getParent().equals("printer")) iv.setImageResource(R.drawable.browser_carpeta);
			iv.setImageResource(R.drawable.file_icon);
		}
		
		return v;
	}

}


