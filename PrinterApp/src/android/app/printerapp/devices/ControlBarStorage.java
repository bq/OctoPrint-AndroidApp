package android.app.printerapp.devices;

import java.util.ArrayList;


import android.app.Activity;
import android.app.printerapp.R;
import android.content.Context;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This will be the Controller to handle the taskbar storage engine.
 * We will use a LinearLayout instead of a ListView with an Adapter because we want to display it Horizontally and
 * tweaking a ListView or GridView for that is a lot of work.
 * @author alberto-baeza
 *
 */
public class ControlBarStorage {
	
	//Test demo file 
	//private static final String DEMO_FILE = "bq-keychain";
	
	//List to store every file
	private ArrayList<String> mFileList = new ArrayList<String>();
	
	//This will be the "custom" ListView
	private LinearLayout mLayout;
	
	//Main thread reference
	private Context mContext;
	
	private ActionBarDrawerToggle mDrawerToggle;
	private DrawerLayout mDrawerLayout;


	
	
	
	public ControlBarStorage(LinearLayout ll, Activity context){
		
		mLayout = ll;
				mContext = context;

		/*mDrawerLayout = ll;
		//mLayout = (LinearLayout)ll.findViewById(R.id.linearlayout_storage);
		mContext = context;
		
		
		// mDrawerLayout = (DrawerLayout) ll.findViewById(R.id.drawer_layout);

		 mDrawerToggle = new ActionBarDrawerToggle(context, mDrawerLayout, R.drawable.ic_launcher,  0, 0);

	        // Set the drawer toggle as the DrawerListener
	        mDrawerLayout.setDrawerListener(mDrawerToggle);*/

		
		retrieveFiles();
		displayFiles();
	}
	
	
	/**************************************************************************************
	 * 			METHODS
	 *****************************************************************************************/
	
	//Retrieve files from our system architecture.
	private void retrieveFiles(){
		
		//Test
		for (int i = 0; i<30; i++){
			
			mFileList.add("bq-keychain" + i);
			
		}
		
		//File mainFolder = GlobalMethods.getParentFolder();
		//File trashFolder = new File(Environment.getExternalStorageDirectory() + "/PrintManager");
		
		/**
		 * CHANGED FILE LOGIC, NOW RETRIEVES FOLDERS INSTEAD OF FILES, AND PARSES
		 * INDIVIDUAL ELEMENTS LATER ON.
		 */
		/*File[] files = mainFolder.listFiles();
		for (File file : files){
			if (file.isDirectory()){	
				
				ModelFile m = new ModelFile(file.getName(), "Internal storage");
				
				//TODO: Move this to the ModelFile code
				m.setPathStl(GlobalMethods.retrieveFile(m.getName(), "_stl"));	
				m.setPathGcode(GlobalMethods.retrieveFile(m.getName(), "_gcode"));	
				mFileList.add(m);
				
			}
		}
		
		//TODO: TEST trash storage
		
		File[] trashFiles = trashFolder.listFiles();
		for (File file : trashFiles){
			if (!file.isDirectory()){
				ModelFile m = new ModelFile(file.getName(), "Trash storage");
				m.setPathStl(file.getAbsolutePath());	
				m.setPathGcode(file.getAbsolutePath());	
				mFileList.add(m);
			}
		}*/
		
		
				
	}
	
	
	//Display them on screen, should be done on an Adapter but we don't have one.
	private void displayFiles(){ 
		
		/*
		 * Fill the Layout with the list views
		 */
		for (final String s : mFileList){
			
			LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View v = inflater.inflate(R.layout.storage_main, null);
			
			TextView tv = (TextView) v.findViewById(R.id.storage_label);
			tv.setText("Test");
			
			v.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					
					/**
					 * Call STL Viewer only if it's an .stl, put filename on Extra to load from the code
					 */

					/*String name = s.getStl();
					String nameGcode = s.getGcodeList();
					
					if (name!=null){
						if (name.substring(name.length() - 4, name.length()).equals(".stl")){
							Intent intent = new Intent(mController, STLMain.class);
							
							intent.putExtra("filename", s.getStl());

							mController.startActivity(intent);
						}
					} else Toast.makeText(mController, "No .stl found", Toast.LENGTH_SHORT).show();		 
					
					if (nameGcode!=null) {
						if (nameGcode.substring(nameGcode.length()-6, nameGcode.length()).equals(".gcode")) {
							Intent intent = new Intent (mController, GCodeMain.class);
							intent.putExtra("filename", nameGcode);
							
							mController.startActivity (intent);
						}
					} else Toast.makeText(mController, "No .gcode found", Toast.LENGTH_SHORT).show();			
				*/}
			});
			
			/*
			 * On long click we start dragging the item, no need to make it invisible
			 */
			v.setOnLongClickListener(new OnLongClickListener() {
				
				@Override
				public boolean onLongClick(View v) {
					

					//String name = s.getGcodeList();
					
					
					/**
					 * Check if there's a real gcode, 
					 */
					/*if (name!=null){
						
						ClipData data = null;
						
						if (s.getStorage().equals("Witbox")){
							 data= ClipData.newPlainText("internal", name);
						} else if (s.getStorage().equals("sd")){
							data = ClipData.newPlainText("internalsd", s.getName());
						}else if (name.substring(name.length() - 6, name.length()).equals(".gcode")){
							data = ClipData.newPlainText("name", name);	
						}
						
						DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
						v.startDrag(data, shadowBuilder, v, 0);
						
						
						
					} else 	Toast.makeText(mController, "No .gcode found", Toast.LENGTH_SHORT).show();

					*/
				
					
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
	public void addToList(String m){
		mFileList.add(m);
	}
		
}
