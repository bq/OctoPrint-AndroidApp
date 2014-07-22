package android.app.printerapp.viewer;

import java.io.File;

import android.content.Context;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.printerapp.R;


public class ViewerMain extends Fragment {	
	private static int mState;
	
	private static final int GCODE_EXTENSION = 6;
	private static final int STL_EXTENSION = 4;
		
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
	
	private File [] mFilesList;
	private static String mLastStlOpened = "";
	private static String mLastGcodeOpened = "";
	
	//Edition menu variables
	private static LinearLayout mMenu;
	private ImageButton mMove;
	private ImageButton mRotation;
	private ImageButton mScale;
	private ImageButton mExit;
	
	static Context mContext;

	
	//TODO 
	//private ImageButton mDelete;
	//private ImageButton mMirror;	
	
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
				int state = mState = ViewerSurfaceView.NORMAL;				
				changeViewFrom (state, ".gcode");	
			} 
		});
		
		mXray = (Button)  rootView.findViewById (R.id.xray);
		mXray.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				int state = ViewerSurfaceView.XRAY;
				changeViewFrom (state, ".gcode");			
			}			
		});
		
		mTransparent = (Button)  rootView.findViewById (R.id.transparent);
		mTransparent.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				int state = ViewerSurfaceView.TRANSPARENT;			
				changeViewFrom (state,".gcode");					
			}			
		});		
		
		mLayers = (Button) rootView.findViewById (R.id.layers);		
		mLayers.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				int state = ViewerSurfaceView.LAYERS;			
				changeViewFrom (state, ".stl");
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
		
		mScale = (ImageButton) rootView.findViewById (R.id.scale);		
		mScale.setOnClickListener(new OnClickListener () {
			@Override
			public void onClick(View v) {
				mSurface.setEditionMode(ViewerSurfaceView.SCALED_EDITION_MODE);
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
		if (filePath.endsWith(".stl") && !mLastStlOpened.equals(mFile.getName())) {
			mDataStl = new DataStorage ();
			StlFile.openStlFile (mContext, mFile, mDataStl);
			mLastStlOpened = mFile.getName();
		} else if (filePath.endsWith(".gcode") && !mLastGcodeOpened.equals(mFile.getName())) {
			mDataGcode = new DataStorage ();
			GcodeFile.openGcodeFile(mContext, mFile, mDataGcode);	
			mLastGcodeOpened = mFile.getName();
		}
		
		drawAndSnapshot(filePath);
	}
   
   private void changeViewFrom (int state, String type) {
		if (mFile!= null) {
			mState = state;
			
			fillFilesList();

			String name = mFile.getName();
			String pathFile="";
			
			if (name.endsWith(type)) {
				if (type.equals(".gcode")) pathFile = getAssociatedStl (name);
				else if (type.equals(".stl")) pathFile = getAssociatedGcode (name);
							
				if (pathFile==null) Toast.makeText(getActivity(), R.string.viewer_toast_not_available, Toast.LENGTH_SHORT).show();
				else openFile (pathFile);
			
			} else 	mSurface.configViewMode(mState);		
			
										
		} else Toast.makeText(getActivity(), R.string.viewer_toast_not_available_2, Toast.LENGTH_SHORT).show();		
	}
   
   private void fillFilesList() {
		if (mFilesList == null) {
			String d = Environment.getExternalStorageDirectory().getPath() + "/PrintManager";
			File dir = new File (d);
			
			if (dir.exists()) {
				mFilesList = dir.listFiles();
			}
		}
	}
	
	private String getAssociatedGcode (String name) {
		String splitedName;
		String pathFile;
		int indexStartName;
		int indexStartExt;
		
		name = name.substring(0, name.length()-STL_EXTENSION);

		for (int i=0; i<mFilesList.length; i++) {
			if (mFilesList[i].isFile()) {
				pathFile = mFilesList[i].getPath();
								
				indexStartName = pathFile.lastIndexOf("/");
				indexStartExt = pathFile.lastIndexOf(".");
	
				splitedName = pathFile.substring(indexStartName+1, indexStartExt);
				
				if (splitedName.equals(name) && pathFile.endsWith(".gcode")) {
					return mFilesList[i].getPath();
				}
			}
		}
		
		return null;
	}
	
	private String getAssociatedStl (String name) {
		String splitedName;
		String pathFile;
		int indexStartName;
		int indexStartExt;
		
		name = name.substring(0, name.length()-GCODE_EXTENSION);

		for (int i=0; i<mFilesList.length; i++) {
			if (mFilesList[i].isFile()) {
				pathFile = mFilesList[i].getPath();
								
				indexStartName = pathFile.lastIndexOf("/");
				indexStartExt = pathFile.lastIndexOf(".");
	
				splitedName = pathFile.substring(indexStartName+1, indexStartExt);
								
				if (splitedName.equals(name) && pathFile.endsWith(".stl")) return mFilesList[i].getPath();
				
			}
		}
		
		return null;
	}
	
	private static void drawAndSnapshot (String filePath) {	
		String pathSnapshot;
		mLayout.removeAllViews();

		if (filePath.endsWith(".stl")) {
			mSeekBar.setVisibility(View.INVISIBLE);
			
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataStl.getPathFile() + ".jpeg";
			mDataStl.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (mContext, mDataStl, mState, mDoSnapshot, true);

		} else if (filePath.endsWith(".gcode")) {
			mSeekBar.setVisibility(View.VISIBLE);
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataGcode.getPathFile() + ".jpeg";
			mDataGcode.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (mContext, mDataGcode, mState, mDoSnapshot, false);
		}
					
		//TODO Changed 
		//Set the surface Z priority to top
		mSurface.setZOrderOnTop(true);
		
		//Add the view
		mLayout.addView(mSurface);
		
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
	
	//TODO Bug: When the view is visible again, the model moves slightly
	public void setSurfaceVisibility(int i){
		
		if (mSurface!=null){
			switch (i){
			case 0: mSurface.setVisibility(View.GONE); break;
			case 1: mSurface.setVisibility(View.VISIBLE); break;
			}
		}		
	}
}
