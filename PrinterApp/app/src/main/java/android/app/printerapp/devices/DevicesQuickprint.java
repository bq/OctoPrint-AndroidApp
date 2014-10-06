package android.app.printerapp.devices;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import android.app.Activity;
import android.app.printerapp.R;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelFile;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * This will be the Controller to handle the taskbar storage engine.
 * We will use a LinearLayout instead of a ListView with an Adapter because we want to display it Horizontally and
 * tweaking a ListView or GridView for that is a lot of work.
 * @author alberto-baeza
 *
 */
public class DevicesQuickprint {
	
	//List to store every file
	private ArrayList<ModelFile> mFileList = new ArrayList<ModelFile>();
	
	//This will be the "custom" ListView
	private LinearLayout mLayout;
	
	//Main thread reference
	private Context mContext;
	
	
	public DevicesQuickprint(LinearLayout ll, Activity context){
		
		mLayout = ll;
		mContext = context;

		addFiles();
		displayFiles();
	}
	
	
	/**************************************************************************************
	 * 		METHODS
	 *****************************************************************************************/
	
	
	/**
	 * TODO: This is the same method as StorageController.retrieveFavorites but this will
	 * contain History instead of favorites so it shouldn't matter
	 */
	private void addFiles(){
		
		for (Map.Entry<String, ?> entry : DatabaseController.getPreferences("Favorites").entrySet()){
			
			ModelFile m = new ModelFile(entry.getValue().toString(), "Internal storage");
			mFileList.add(m);
			
		}
		
	}
	
	
	//Display them on screen, should be done on an Adapter but we don't have one.
	private void displayFiles(){ 
		
		/*
		 * Fill the Layout with the list views
		 */
		for (final ModelFile m : mFileList){
			
			//LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			//View v = inflater.inflate(R.layout.storage_main, null);
			
			if (m.getGcodeList()!=null){
				File path = new File(m.getGcodeList());
				final File[] files = path.getParentFile().listFiles();
				
				for (int i=0 ; i<files.length; i++){
					
				final int current = i;
								
				ImageView v;
				
				if ((StorageController.isProject(m))){
					
					//TextView tv = (TextView) v.findViewById(R.id.storage_label);
					//tv.setText(m.getName());
					
					//ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);
					
					v = new ImageView(mContext);
					v.setLayoutParams(new LayoutParams(120,120,Gravity.CENTER));
					v.setPadding(5, 0, 5, 0);
					
					if (m.getStorage().equals("Internal storage")){
						Drawable d;
						d =m.getSnapshot();
					
						if (d!=null){
							v.setImageDrawable(d);
						} else {
							v.setImageResource(R.drawable.file_icon);
						}
					} else v.setImageResource(R.drawable.file_icon);
				
						 	
					/*
					 * On long click we start dragging the item, no need to make it invisible
					 */
					v.setOnLongClickListener(new OnLongClickListener() {
						
						@Override
						public boolean onLongClick(View v) {
							
		
							String name = files[current].getAbsolutePath();
							
							
							/**
							 * Check if there's a real gcode, 
							 */
							if (name!=null){
								
								ClipData data = null;
								
								if (m.getStorage().equals("Witbox")){
									 data= ClipData.newPlainText("internal", name);
								} else if (m.getStorage().equals("sd")){
									data = ClipData.newPlainText("internalsd", m.getName());
								}else if (StorageController.hasExtension(1, name)){
									data = ClipData.newPlainText("name", name);	
								}
								
								DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
								v.startDrag(data, shadowBuilder, v, 0);
								
								
								
							} else 	Toast.makeText(mContext, R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
		
							
						
							
							return false;
						}
					});
							
					if (mLayout!=null){
						mLayout.addView(v);
					} else Log.i("out","NULL");
				}
				
				}
			}
			
			
		}
		
	}	
		
}
