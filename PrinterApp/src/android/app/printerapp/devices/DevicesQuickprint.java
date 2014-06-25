package android.app.printerapp.devices;

import java.util.ArrayList;

import android.app.Activity;
import android.app.printerapp.R;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelFile;
import android.content.ClipData;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnLongClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This will be the Controller to handle the taskbar storage engine.
 * We will use a LinearLayout instead of a ListView with an Adapter because we want to display it Horizontally and
 * tweaking a ListView or GridView for that is a lot of work.
 * @author alberto-baeza
 *
 */
public class DevicesQuickprint {
	
	//Test demo file 
	//private static final String DEMO_FILE = "bq-keychain";
	
	//List to store every file
	private ArrayList<ModelFile> mFileList = new ArrayList<ModelFile>();
	
	//This will be the "custom" ListView
	private LinearLayout mLayout;
	
	//Main thread reference
	private Context mContext;
	

	
	
	
	public DevicesQuickprint(LinearLayout ll, Activity context){
		
		mLayout = ll;
		mContext = context;
		
		mFileList = StorageController.retrieveFiles();
		displayFiles();
	}
	
	
	/**************************************************************************************
	 * 			METHODS
	 *****************************************************************************************/
	
	
	
	
	//Display them on screen, should be done on an Adapter but we don't have one.
	private void displayFiles(){ 
		
		/*
		 * Fill the Layout with the list views
		 */
		for (final ModelFile m : mFileList){
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.storage_main, null);
			
			TextView tv = (TextView) v.findViewById(R.id.storage_label);
			tv.setText(m.getName());
			
			ImageView iv = (ImageView) v.findViewById(R.id.storage_icon);
			
			Drawable d;
				d = Drawable.createFromPath(StorageController.getParentFolder() + "/" + m.getName() + "/" + m.getName() + ".jpg");
			
				if (d!=null){
					iv.setImageDrawable(d);
				} else {
					//d = mContext.getResources().getDrawable(R.drawable.file_icon);
					iv.setImageResource(R.drawable.file_icon);
				}
				
			 	
			/*
			 * On long click we start dragging the item, no need to make it invisible
			 */
			v.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					

					String name = m.getGcodeList();
					
					
					/**
					 * Check if there's a real gcode, 
					 */
					if (name!=null){
						
						ClipData data = null;
						
						if (m.getStorage().equals("Witbox")){
							 data= ClipData.newPlainText("internal", name);
						} else if (m.getStorage().equals("sd")){
							data = ClipData.newPlainText("internalsd", m.getName());
						}else if (name.substring(name.length() - 6, name.length()).equals(".gcode")){
							data = ClipData.newPlainText("name", name);	
						}
						
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						v.startDrag(data, shadowBuilder, v, 0);
						
						
						
					} else 	Toast.makeText(mContext, "No .gcode found", Toast.LENGTH_SHORT).show();

					
				
					
					return false;
				}
			});
					
			if (mLayout!=null){
				mLayout.addView(v);
			} else Log.i("out","NULL");
			
			
		}
		
	}

	
	//Refresh file list
	public void refreshList(){
		mLayout.removeAllViews();
		displayFiles();
	}
	
	//Add a new file to the list (OctoprintFiles)
	public void addToList(ModelFile m){
		mFileList.add(m);
	}
	
		
}
