package android.app.printerapp.library.detail;

import java.io.File;
import java.util.List;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.StorageController;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * This is the adapter for the detail view
 * @author alberto-baeza
 *
 */
public class DetailViewAdapter extends ArrayAdapter<File> {
	
	private Drawable mDrawable;

	public DetailViewAdapter(Context context, int resource, List<File> objects, Drawable d) {
		super(context, resource, objects);
		
		mDrawable = d;
		
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		

		View v = convertView;	
		
		final File f = getItem(position);
		
		//View not yet created
		if (v==null){
			
			
			//Inflate the view
			LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			v = inflater.inflate(R.layout.detailview_list_element, null, false);
			
			
		} else {
			//v = convertView;
		}
		
		//UI references
		TextView tv1 = (TextView) v.findViewById(R.id.detailview_list_tv1);
		tv1.setText(f.getName());
		
		ImageView iv = (ImageView) v.findViewById(R.id.detailview_list_iv);
		iv.setImageDrawable(mDrawable);
						
		ImageButton ib = (ImageButton) v.findViewById(R.id.detailview_list_iv1);
		ImageButton ibe = (ImageButton) v.findViewById(R.id.detailview_list_iv2);
		
		if ((StorageController.hasExtension(1, f.getName()))){
			
			v.findViewById(R.id.detailview_gcode).setVisibility(View.VISIBLE);

			//it's an stl
		}else {
			
			ib.setVisibility(View.GONE);
		}
		
		
		//Print button
		ib.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

                if ((StorageController.hasExtension(0, f.getName()))) DevicesListController.selectPrinter(v.getContext(), f , 0);
                else DevicesListController.selectPrinter(v.getContext(), f , 1);

				//OctoprintFiles.downloadFile(v.getContext(), "", f.getParentFile().getParentFile().getName(), "shit.gco");
				
			}
		});
		
		//Edit button
		ibe.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {

                DevicesListController.selectPrinter(v.getContext(), f , 0);

				
			}
		});
	

		return v;
	}
	

}
