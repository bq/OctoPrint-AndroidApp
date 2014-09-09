package android.app.printerapp.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TabHost.OnTabChangeListener;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.library.StorageController;

public class ViewerMain extends Fragment {	
	//Tabs
	private static final int NORMAL = 0;
	private static final int OVERHANG = 1;
	private static final int TRANSPARENT = 2;
	private static final int XRAY = 3;
	private static final int LAYER = 4; 
	
	//Constants 
	public static final boolean DO_SNAPSHOT = true;
	public static final boolean DONT_SNAPSHOT = false;
	public static final boolean STL = true;
	public static final boolean GCODE = false;
	
	private static final float POSITIVE_ANGLE = 15;
	private static final float NEGATIVE_ANGLE = -15;
	
	//Variables			
	private static File mFile;

	private static ViewerSurfaceView mSurface;
	private static FrameLayout mLayout;
	
	private static RelativeLayout mRotateMenu;
		
	//Buttons
	private RadioGroup mGroupMovement;

	private Button mBackWitboxFaces;
	private Button mRightWitboxFaces;
	private Button mLeftWitboxFaces;
	private Button mDownWitboxFaces;
	private static SeekBar mSeekBar;
	
	private static List<DataStorage> mDataList = new ArrayList<DataStorage>();	
		
	//Edition menu variables	
	private static ProgressBar mProgress;

	private static Context mContext;
	private static View mRootView;
	
	//Empty constructor
	public ViewerMain(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Retain instance to keep the Fragment from destroying itself
				setRetainInstance(true);
		}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {	
		//Reference to View
		mRootView = null;
		
		//If is not new
		if (savedInstanceState==null){
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			mRootView = inflater.inflate(R.layout.viewer_main,
					container, false);
			
			mContext = getActivity();
											
			initUIElements ();
			initRotateButtons ();
			
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);	
			
			mSurface = new ViewerSurfaceView(mContext, mDataList, NORMAL, DONT_SNAPSHOT);
			draw();
		}
		
		return mRootView;	
		
	}
	
	public static void resetWhenCancel () {
		mDataList.remove(mDataList.size()-1);
		mSurface.requestRender();
	}
	
	/************************* UI ELEMENTS ********************************/
	
	private void initUIElements () {
		setTabHost(mRootView);
		
		mSeekBar = (SeekBar) mRootView.findViewById (R.id.barLayer);		
		mSeekBar.setVisibility(View.INVISIBLE);
		
		mLayout = (FrameLayout) mRootView.findViewById (R.id.frameLayout);

		mGroupMovement = (RadioGroup) mRootView.findViewById (R.id.radioGroupMovement);		
		mGroupMovement.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.radioRotation:
					mSurface.setMovementMode(ViewerSurfaceView.ROTATION_MODE);
					break;
				case R.id.radioTranslation:
					mSurface.setMovementMode(ViewerSurfaceView.TRANSLATION_MODE);
					break;
				}
			}			
		});

		mBackWitboxFaces = (Button) mRootView.findViewById(R.id.back);
		mBackWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showBackWitboxFace();
			}
			
		});
		
		mRightWitboxFaces = (Button) mRootView.findViewById(R.id.right);
		mRightWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showRightWitboxFace();
			}
			
		});
		
		mLeftWitboxFaces = (Button) mRootView.findViewById(R.id.left);
		mLeftWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showLeftWitboxFace();
			}
			
		});
		
		mDownWitboxFaces = (Button) mRootView.findViewById(R.id.down);
		mDownWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showDownWitboxFace();
			}
			
		});

		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {       
			
		    @Override       
		    public void onStopTrackingTouch(SeekBar seekBar) {      
		    }       
	
		    @Override       
		    public void onStartTrackingTouch(SeekBar seekBar) {     
		    }       
	
		    @Override       
		    public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { 
		    	mDataList.get(0).setActualLayer(progress);
		    	mSurface.requestRender();
		    }       
		}); 	
		
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progress_bar);
		mProgress.setVisibility(View.GONE);
	}
	
	private void initRotateButtons () {
		mRotateMenu = (RelativeLayout) mRootView.findViewById(R.id.angle_axis);
		mRotateMenu.setVisibility(View.INVISIBLE);
		
		Button positiveAngleX = (Button) mRootView.findViewById(R.id.positive_angle_x);
		positiveAngleX.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisX(POSITIVE_ANGLE);			
			}
		});
		
		Button positiveAngleY = (Button) mRootView.findViewById(R.id.positive_angle_y);		
		positiveAngleY.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisY(POSITIVE_ANGLE);				
			}
		});
		
		Button positiveAngleZ = (Button) mRootView.findViewById(R.id.positive_angle_z);
		positiveAngleZ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisZ(POSITIVE_ANGLE);	
			}
		});
		
		Button negativeAngleX = (Button) mRootView.findViewById(R.id.negative_angle_x);
		negativeAngleX.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisX(NEGATIVE_ANGLE);		
			}
		});
		
		Button negativeAngleY = (Button) mRootView.findViewById(R.id.negative_angle_y);		
		negativeAngleY.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisY(NEGATIVE_ANGLE);			
			}
		});
		
		Button negativeAngleZ = (Button) mRootView.findViewById(R.id.negative_angle_z);
		negativeAngleZ.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mSurface.rotateAngleAxisZ(NEGATIVE_ANGLE);			
			}
		});
	}
	
	public static void initSeekBar (int max) {
		mSeekBar.setMax(max);
		mSeekBar.setProgress(max);
	}
	
	public static void configureProgressState (int v) {
		if (v== View.GONE) mSurface.requestRender();
		else if (v==View.VISIBLE) 	mProgress.bringToFront();
		
		mProgress.setVisibility(v);
	}
		
	
	/************************* OPTIONS MENU ********************************/	
	//Create option menu and inflate viewer menu
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.viewer_menu, menu);
	}
	
   @Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	   
	   switch (item.getItemId()) {
	   
	   case R.id.viewer_open: 		   
		   	FileBrowser.openFileBrowser(getActivity(), FileBrowser.VIEWER, getString(R.string.choose_file), ".stl", ".gcode");
			return true;
			
       	case R.id.viewer_save: 
       		saveNewProyect ();		
            return true;
            
    	case R.id.viewer_notes: 
    		//Add/View notes
            return true;
            
    	case R.id.viewer_autofit: 
    		//Autofit
            return true;
              
    	case R.id.viewer_clean: 
            return true;
          
       default:
           return super.onOptionsItemSelected(item);
	   }
	}
   
   /**
	 * Constructor for the tab host
	 * TODO: Should be moved to a View class since it only handles ui.
	 */
	public void setTabHost(View v){			 
		final TabHost tabs=(TabHost) v.findViewById(R.id.tabhost_views);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("Normal");
		spec.setIndicator(getString(R.string.viewer_button_normal));
		spec.setContent(R.id.tab_normal);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Overhang");
		spec.setIndicator(getString(R.string.viewer_button_overhang));
		spec.setContent(R.id.tab_overhang);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Transparent");
		spec.setIndicator(getString(R.string.viewer_button_transparent));
		spec.setContent(R.id.tab_transparent);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Xray");
		spec.setIndicator(getString(R.string.viewer_button_xray));
		spec.setContent(R.id.tab_xray);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Gcode");
		spec.setIndicator(getString(R.string.viewer_button_layers));
		spec.setContent(R.id.tab_gcodes);
		tabs.addTab(spec);
		 
		tabs.setCurrentTab(0);
		
		tabs.getTabWidget().setBackgroundColor(Color.parseColor("#333333"));
		for(int i=0; i<tabs.getTabWidget().getChildCount(); i++) {
	        TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.parseColor("#ffffff"));
	    } 
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {

		    	switch (tabs.getCurrentTab()) {				
				case NORMAL:
					changeStlViews(ViewerSurfaceView.NORMAL);	
					break;
				case OVERHANG:
					//changeStlViews(ViewerSurfaceView.OVERHANG);						
					break; 
				case TRANSPARENT:
					changeStlViews(ViewerSurfaceView.TRANSPARENT);	
					break;
					
				case XRAY:		
					changeStlViews(ViewerSurfaceView.XRAY);	
					break;
				case LAYER:		
					if (mFile!=null) {
						showGcodeFiles ();
					} else 	Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();
				break;

				default:
					break;
				}		    	
		    }
		});
		
	}

   
   /************************* FILE MANAGEMENT ********************************/
   public static void openFile (String filePath) {
	   Log.i("viewer", " file path " + filePath);
	   DataStorage data = null;
		mFile = new File(filePath);
		//Open the file
		if ((filePath.endsWith(".stl")|| filePath.endsWith(".STL")) ) {
			data = new DataStorage();
			StlFile.openStlFile (mContext, mFile, data, false);
		} else if ((filePath.endsWith(".gcode")|| filePath.endsWith(".GCODE"))) {
			data = new DataStorage();
			GcodeFile.openGcodeFile(mContext, mFile, data,false);
		}
		mDataList.add(data);
   }
   
	private void changeStlViews (int state) {
		if (mFile!=null) {
			if (!mFile.getPath().endsWith(".stl") && !mFile.getPath().endsWith(".STL")) 
				openStlFile ();	
			else mSurface.configViewMode(state);	
		} else 	Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();		
	}
   
   private void openStlFile () {
		String name = mFile.getName().substring(0, mFile.getName().lastIndexOf('.'));
		String pathStl = StorageController.getParentFolder().getAbsolutePath() + "/Files/" + name + "/_stl/";
		
		File f = new File (pathStl);

		//Only when it's a project
		if (f.isDirectory() && f.list().length>0){
			openFile (pathStl+ f.list()[0]);
		} else {
			Toast.makeText(getActivity(), R.string.devices_toast_no_stl, Toast.LENGTH_SHORT).show();
		}	   
	}


	private void showGcodeFiles () {
		//Logic for getting file type
		String name = mFile.getName().substring(0, mFile.getName().lastIndexOf('.'));
		String pathProject = StorageController.getParentFolder().getAbsolutePath() + "/Files/" + name;
		File f = new File (pathProject);

		//Only when it's a project
		if (f.isDirectory()){
			String path = pathProject+"/_gcode";
			
			if (StorageController.isProject(f) && new File (path).list().length>0){				
				AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
				adb.setTitle(mContext.getString(R.string.gcode_viewer));
				
				//We need the alertdialog instance to dismiss it
				final AlertDialog ad = adb.create();
									
				if (path!=null) {					
					final File[] files = (new File(path)).listFiles();
					
					//Create a string-only array for the adapter
					if (files!=null){
						String[] names = new String[files.length];
						
						for (int i = 0 ; i< files.length ; i++){							
							names[i] = files[i].getName();
							
						}
							
						adb.setAdapter(new ArrayAdapter<String> (mContext, android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {					
							@Override
							public void onClick(DialogInterface dialog, int which) {
		
								    File m = files[which];
		
								    //Open desired file
								    openFile (m.getAbsolutePath());

								    ad.dismiss();
							}
						});
					
					} 
				}
				adb.show();
			} else {
				Toast.makeText(getActivity(), R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
			}
		} else {
			Toast.makeText(getActivity(), R.string.devices_toast_no_gcode, Toast.LENGTH_SHORT).show();
		}							
	}
	
	
	public static void draw () {
		//Once the file has been opened, we need to refresh the data list. If we are opening a .gcode file, we need to delete the previous files (.stl and .gcode)
		//If we are opening a .stl file, we need to delete the previous file only if it was a .gcode file.
		//We have to do this here because user can cancel the opening of the file and the Print Panel would appear empty if we clear the data list.
				
		String filePath = "";
		if (mFile!=null) filePath = mFile.getAbsolutePath();

		if (filePath.endsWith(".stl") || filePath.endsWith(".STL") ) {
			if (mDataList.size()>1) {
				if (mDataList.get(mDataList.size()-2).getPathFile().endsWith(".gcode") || mDataList.get(mDataList.size()-2).getPathFile().endsWith(".GCODE")) {
					mDataList.remove(mDataList.size()-2);
				}
			}
			
			mSeekBar.setVisibility(View.INVISIBLE);

		} else if (filePath.endsWith(".gcode") || filePath.endsWith(".GCODE")) {
			if (mDataList.size()>1) 
				while (mDataList.size()>1){
					mDataList.remove(0);
				}			
			mSeekBar.setVisibility(View.VISIBLE);
		}		
		
		//Add the view
		mLayout.removeAllViews();
		mLayout.addView(mSurface, 0);
		mLayout.addView(mSeekBar, 1);
		mLayout.addView(mRotateMenu, 2);
	}
		
	/************************* SAVE FILE ********************************/
	private void saveNewProyect () {
		View dialogText = LayoutInflater.from(mContext).inflate(R.layout.set_proyect_name_dialog, null);
		final EditText proyectNameText = (EditText) dialogText.findViewById(R.id.proyect_name);

		proyectNameText.addTextChangedListener(new TextWatcher() {
		    @Override
		    public void afterTextChanged(Editable s) {
		    	proyectNameText.setError(null);
		    }

		    @Override
		    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		    	//do nothing
		    }

		    @Override
		    public void onTextChanged(CharSequence s, int start, int before, int count) {
		    	//do nothing
		    }   		      
		});
			
	
   		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
   		adb.setView(dialogText)
			.setTitle(mContext.getString(R.string.project_name))
			.setCancelable(false)
   			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
   				public void onClick(DialogInterface dialog,int id) {
   					dialog.cancel();
   				}
   			})
   			.setPositiveButton(R.string.ok, null); //onclicklistener=null to avoid to dismiss the dialog
		
		//We need the alertdialog instance to dismiss it
		final AlertDialog ad = adb.create();
		ad.show();
		
		//We look for 
		Button okButton = ad.getButton(DialogInterface.BUTTON_POSITIVE);
		okButton.setOnClickListener(new CustomListener (ad, proyectNameText));
	}
	
	private class CustomListener implements View.OnClickListener {
	    private final Dialog dialog;
	    private final EditText proyectNameText;
	    public CustomListener(Dialog dialog, EditText proyectNameText) {
	        this.dialog = dialog;
	        this.proyectNameText = proyectNameText;
	    }
	    @Override
	    public void onClick(View v) {

	    	if (StlFile.saveModel(mDataList, proyectNameText.getText().toString())) dialog.dismiss();
			else {
				proyectNameText.setError(mContext.getString(R.string.proyect_name_not_available));
			}
	    }
	}
	
	/************************* SURFACE CONTROL ********************************/
	//This method will set the visibility of the surfaceview so it doesn't overlap
	//with the video grid view	
	public void setSurfaceVisibility(int i){
		
		if (mSurface!=null){
			switch (i){
			case 0: mSurface.setVisibility(View.GONE); break;
			case 1: mSurface.setVisibility(View.VISIBLE); break;
			}
		}		
	}
	
	private static ActionMode mActionMode;
	
	/************************* ACTION MODE ********************************/
	public static void showActionModeBar () {
		if (mActionMode== null) mActionMode = mRootView.startActionMode(mActionModeCallBack);
	}
	
	public static void hideActionModeBar() {
		if (mActionMode!=null) mActionMode.finish();
	}
		
	private static ActionMode.Callback mActionModeCallBack = new ActionMode.Callback() {		
		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			//Inflate a menu resource providing context menu items
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.edition_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false; //do nothing
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        	mRotateMenu.setVisibility(View.INVISIBLE);
			 switch (item.getItemId()) {
	            case R.id.move:
	            	mSurface.setEditionMode(ViewerSurfaceView.MOVE_EDITION_MODE);
	                return true;
	            case R.id.rotate: 
	            	mRotateMenu.setVisibility(View.VISIBLE);
	            	mSurface.setEditionMode(ViewerSurfaceView.ROTATION_EDITION_MODE);
	            	return true;
	            case R.id.scale:
	            	mSurface.setEditionMode(ViewerSurfaceView.SCALED_EDITION_MODE);
	            	return true;
	            case R.id.mirror:
	            	mSurface.setEditionMode(ViewerSurfaceView.MIRROR_EDITION_MODE);
					mSurface.doMirror();
	            	return true;	                
	            case R.id.delete:
					mSurface.deleteObject();
	            	mode.finish(); // Action picked, so close the CAB
	            	return true;
	            default:
	                return false;
	        }
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			mSurface.exitEditionMode();
        	mRotateMenu.setVisibility(View.INVISIBLE);

			mActionMode = null;
		}
	};
}
