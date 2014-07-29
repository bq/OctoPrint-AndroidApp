package android.app.printerapp.viewer;

import java.io.File;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.printerapp.R;
import android.app.printerapp.library.StorageController;

public class ViewerMain extends Fragment {	
	private static int mState;
			
	private static File mFile;

	private static ViewerSurfaceView mSurface;
	private static FrameLayout mLayout;
		
	//Buttons
	private RadioGroup mGroupMovement;
	private static RadioGroup mGroupRotation;

	private Button mBackWitboxFaces;
	private Button mRightWitboxFaces;
	private Button mLeftWitboxFaces;
	private Button mDownWitboxFaces;
	private Button mLayers;
	private Button mXray;
	private Button mTransparent;
	private Button mNormal;
	private static SeekBar mSeekBar;
	
	private static DataStorage mDataStl;
	private static DataStorage mDataGcode;
	
	//TODO check
	private static boolean mDoSnapshot  = false;
	
	private static String mLastStlOpened = "";
	private static String mLastGcodeOpened = "";
	
	//Edition menu variables
	private static LinearLayout mMenu;
	private ImageButton mMove;
	private ImageButton mRotation;
	private ImageButton mMirror;	
	private ImageButton mScale;
	private ImageButton mExit;
	private ImageButton mDelete;
	
	private static ProgressBar mProgress;

	
	static Context mContext;
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
		View rootView = null;
		
		//If is not new
		if (savedInstanceState==null){
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			rootView = inflater.inflate(R.layout.viewer_main,
					container, false);
			
			mContext = getActivity();
											
			initUIElements (rootView);
			initEditButtons (rootView);
			
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);						
		}
		
		return rootView;	
		
	}
	
	/************************* UI ELEMENTS ********************************/
	
	private void initUIElements (View rootView) {
		mSeekBar = (SeekBar) rootView.findViewById (R.id.barLayer);		
		mSeekBar.setVisibility(View.INVISIBLE);
		
		mLayout = (FrameLayout) rootView.findViewById (R.id.frameLayout);

		mGroupMovement = (RadioGroup) rootView.findViewById (R.id.radioGroupMovement);		
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
				case R.id.lightRotation:
					mSurface.setMovementMode(ViewerSurfaceView.LIGHT_MODE);
					break;
				}		
			}			
		});

		mBackWitboxFaces = (Button) rootView.findViewById(R.id.back);
		mBackWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showBackWitboxFace();
			}
			
		});
		
		mRightWitboxFaces = (Button) rootView.findViewById(R.id.right);
		mRightWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showRightWitboxFace();
			}
			
		});
		
		mLeftWitboxFaces = (Button) rootView.findViewById(R.id.left);
		mLeftWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showLeftWitboxFace();
			}
			
		});
		
		mDownWitboxFaces = (Button) rootView.findViewById(R.id.down);
		mDownWitboxFaces.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.showDownWitboxFace();
			}
			
		});
		
		mNormal = (Button) rootView.findViewById (R.id.normal);		
		mNormal.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				changeStlViews(ViewerSurfaceView.NORMAL);	
			} 
		});
		
		mXray = (Button)  rootView.findViewById (R.id.xray);
		mXray.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				changeStlViews(ViewerSurfaceView.XRAY);	
			}			
		});
		
		mTransparent = (Button)  rootView.findViewById (R.id.transparent);
		mTransparent.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				changeStlViews(ViewerSurfaceView.TRANSPARENT);	
			}			
		});		
		
		mLayers = (Button) rootView.findViewById (R.id.layers);		
		mLayers.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				if (mFile!=null) {
					showGcodeFiles ();
				} else 	Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();
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
		    	mDataGcode.setActualLayer(progress);
		    	mSurface.requestRender();
		    }       
		}); 	
		
		mProgress = (ProgressBar) rootView.findViewById(R.id.progress_bar);
		mProgress.setVisibility(View.GONE);
	}
	
	private void initEditButtons (View rootView) {
		mMenu = (LinearLayout) rootView.findViewById(R.id.edition_menu);
		mMenu.setVisibility(View.INVISIBLE);
		
		mGroupRotation = (RadioGroup) rootView.findViewById (R.id.radio_group_rotation);	
		mGroupRotation.setVisibility(View.INVISIBLE);
		mGroupRotation.setOnCheckedChangeListener(new OnCheckedChangeListener () {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
				case R.id.rotation_x:
					mSurface.setRotationVector (ViewerSurfaceView.ROTATE_X);
					break;
				case R.id.rotation_y:
					mSurface.setRotationVector (ViewerSurfaceView.ROTATE_Y);
					break;
				case R.id.rotation_z:
					mSurface.setRotationVector (ViewerSurfaceView.ROTATE_Z);
					break;
				}
				
			}			
		});
		
		mMove = (ImageButton) rootView.findViewById (R.id.move);		
		mMove.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.setEditionMode(ViewerSurfaceView.MOVE_EDITION_MODE);
			} 
		});
		
		mRotation = (ImageButton) rootView.findViewById (R.id.rotate);		
		mRotation.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.setEditionMode(ViewerSurfaceView.ROTATION_EDITION_MODE);
			} 
		});
		
		mMirror = (ImageButton) rootView.findViewById(R.id.mirror);
		mMirror.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.setEditionMode(ViewerSurfaceView.MIRROR_EDITION_MODE);
				mSurface.doMirror();
			} 
		});
		
		mScale = (ImageButton) rootView.findViewById (R.id.scale);		
		mScale.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.setEditionMode(ViewerSurfaceView.SCALED_EDITION_MODE);
			} 
		});
		
		mDelete = (ImageButton) rootView.findViewById (R.id.delete);		
		mDelete.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.deleteObject();
			} 
		});
		
		mExit = (ImageButton) rootView.findViewById (R.id.exit);		
		mExit.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.exitEditionMode();
			} 
		});
	}
	
	public static void setEditionMenuVisibility (int visibility) {
		mMenu.setVisibility(visibility);
		mGroupRotation.setVisibility(visibility);

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
       		//Save current file
            return true;
            
    	case R.id.viewer_notes: 
    		//Add/View notes
            return true;
            
    	case R.id.viewer_autofit: 
    		//Autofit
            return true;
              
    	case R.id.viewer_clean: 
    		//Clean panel
            return true;
          
       default:
           return super.onOptionsItemSelected(item);
	   }
	}
	
   
   /************************* FILE MANAGEMENT ********************************/
   public static void openFile (String filePath) {
		mFile = new File(filePath);
		//Open the file
		if ((filePath.endsWith(".stl")|| filePath.endsWith(".STL")) && !mLastStlOpened.equals(mFile.getName())) {
			mDataStl = new DataStorage ();
			StlFile.openStlFile (mContext, mFile, mDataStl);
			mLastStlOpened = mFile.getName();
		} else if ((filePath.endsWith(".gcode")|| filePath.endsWith(".GCODE")) && !mLastGcodeOpened.equals(mFile.getName())) {
			mDataGcode = new DataStorage ();
			GcodeFile.openGcodeFile(mContext, mFile, mDataGcode);	
			mLastGcodeOpened = mFile.getName();
		}
		
		drawAndSnapshot(filePath);
	}
   
	private void changeStlViews (int state) {
		if (mFile!=null) {
			mState = state;
			if (!mFile.getPath().endsWith(".stl") && !mFile.getPath().endsWith(".STL")) openStlFile ();	
			else mSurface.configViewMode(state);	
		} else 	Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();		
	}
   
   private void openStlFile () {
		String name = mFile.getName().substring(0, mFile.getName().lastIndexOf('.'));
		String pathStl = StorageController.getParentFolder().getAbsolutePath() + "/Files/" + name + "/_stl";
		File f = new File (pathStl);

		//Only when it's a project
		if (f.isDirectory() && f.list().length>0){
			openFile (f.list()[0]);
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
									mState = ViewerSurfaceView.LAYERS;

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
	
	private static void drawAndSnapshot (String filePath) {	
		String pathSnapshot;
		setEditionMenuVisibility(View.INVISIBLE);

		if (filePath.endsWith(".stl") || filePath.endsWith(".STL") ) {
			mSeekBar.setVisibility(View.INVISIBLE);
			
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataStl.getPathFile() + ".jpeg";
			mDataStl.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (mContext, mDataStl, mState, mDoSnapshot, true);

		} else if (filePath.endsWith(".gcode") || filePath.endsWith(".GCODE")) {
			mSeekBar.setVisibility(View.VISIBLE);
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataGcode.getPathFile() + ".jpeg";
			mDataGcode.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (mContext, mDataGcode, mState, mDoSnapshot, false);
		}							
		//Add the view
		mLayout.removeAllViews();
		mLayout.addView(mSurface, 0);
		mLayout.addView(mMenu, 1);
		
				
		//TODO CHANGED: Edition menu does not appear on top if we set setZOrderOnTop (true).
		//Set the surface Z priority to top
		//mSurface.setZOrderOnTop(true);
		
		if (mDoSnapshot) mLayout.setVisibility(View.INVISIBLE);	

	}
		
	/*
	private boolean doSnapshot (String path) {
		boolean doSnapshot;
		File png = new File (path);
		if (png.exists()) doSnapshot =  false ;
		else doSnapshot = true;
		
		return doSnapshot;
	}
	
	
*/	
	
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
}
