package android.app.printerapp.viewer;

import java.io.File;

import android.app.Activity;
import android.content.SharedPreferences;
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
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.app.printerapp.R;


public class ViewerMain extends Fragment implements FileBrowser.OnFileListDialogListener {	
	private int mState;
	
	private static final int GCODE_EXTENSION = 6;
	private static final int STL_EXTENSION = 4;
		
	private String mPathFile;
	private File mFile;

	private ViewerSurfaceView mSurface;
	private FrameLayout mLayout;
		
	//Buttons
	private RadioGroup mGroupMovement;
	private RadioGroup mGroupRotation;

	private Button mBackWitboxFaces;
	private Button mRightWitboxFaces;
	private Button mLeftWitboxFaces;
	private Button mDownWitboxFaces;
	private Button mLayers;
	private Button mXray;
	private Button mTransparent;
	private Button mLoadFile;
	private Button mNormal;
	private SeekBar mSeekBar;
	
	private DataStorage mDataStl;
	private DataStorage mDataGcode;
	
	//TODO check
	private boolean mDoSnapshot  = false;
	
	private File [] mFilesList;
	private String mLastStlOpened = "";
	private String mLastGcodeOpened = "";
	
	
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
			
			//mPathFile = getActivity().getIntent().getExtras().getString("filename");
					
			mSeekBar = (SeekBar) rootView.findViewById (R.id.barLayer);		
			mSeekBar.setVisibility(View.INVISIBLE);
			
			mLayout = (FrameLayout) rootView.findViewById (R.id.frameLayout);
						
			if (mPathFile!= null) openFile (mPathFile);
					
			//Buttons and seekBar
			mGroupRotation = (RadioGroup) rootView.findViewById (R.id.radio_group_rotation);	
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
			
			mLoadFile = (Button) rootView.findViewById(R.id.load_file);		
			mLoadFile.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					FileBrowser fileListDialog = new FileBrowser(getActivity(), false, "Choose file...", ".stl", ".gcode");
					
					fileListDialog.setOnFileListDialogListener(ViewerMain.this);

					SharedPreferences config = getActivity().getSharedPreferences("PathSetting", Activity.MODE_PRIVATE);
					fileListDialog.show(config.getString("lastPath", Environment.getExternalStorageDirectory().getPath() + "/PrintManager"));
					
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
			
			getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);			
		
			
		}
		
		return rootView;
		
		
	}
	
	
	/*******************************************************************
	 * 
	 * 	MENU OPTIONS
	 * 
	 *******************************************************************/
	
	
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
		   //Open file to load
		   
		   //TODO test, encapsulate code into function
		   
		   	FileBrowser fileListDialog = new FileBrowser(getActivity(), false, "Choose file...", ".stl", ".gcode");
			
			fileListDialog.setOnFileListDialogListener(ViewerMain.this);

			SharedPreferences config = getActivity().getSharedPreferences("PathSetting", Activity.MODE_PRIVATE);
			fileListDialog.show(config.getString("lastPath", Environment.getExternalStorageDirectory().getPath() + "/PrintManager"));
			
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
	
	/*****************************************************************************/
   
   
   
	
	private void changeViewFrom (int state, String type) {
		if (mFile!= null) {
			mState = state;
			
			fillFilesList();

			String name = mFile.getName();
			String pathFile="";
			
			if (name.endsWith(type)) {
				if (type.equals(".gcode")) pathFile = getAssociatedStl (name);
				else if (type.equals(".stl")) pathFile = getAssociatedGcode (name);
							
				if (pathFile==null) Toast.makeText(getActivity(), "This view is not available.", Toast.LENGTH_SHORT).show();
				else openFile (pathFile);
			
			} else 	mSurface.configViewMode(mState);		
			
										
		} else Toast.makeText(getActivity(), "This view is not available. Open a file first", Toast.LENGTH_SHORT).show();	
		
	}
	
	private void openFile (String filePath) {
		mFile = new File(filePath);

		//Open the file
		if (filePath.endsWith(".stl") && !mLastStlOpened.equals(mFile.getName())) {
			mDataStl = new DataStorage ();
			new StlFile (getActivity(), mFile, mDataStl);
			mLastStlOpened = mFile.getName();
		} else if (filePath.endsWith(".gcode") && !mLastGcodeOpened.equals(mFile.getName())) {
			mDataGcode = new DataStorage ();
			new GcodeFile (getActivity(), mFile, mDataGcode, mSeekBar);	
			mLastGcodeOpened = mFile.getName();
		}
		
		drawAndSnapshot(filePath);
	}
	
	
	
	private void drawAndSnapshot (String filePath) {	
		String pathSnapshot;
		mLayout.removeAllViews();

		if (filePath.endsWith(".stl")) {
			mSeekBar.setVisibility(View.INVISIBLE);
			
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataStl.getPathFile() + ".jpeg";
			mDataStl.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (getActivity(), mDataStl, mState, mDoSnapshot, true);

		} else if (filePath.endsWith(".gcode")) {
			mSeekBar.setVisibility(View.VISIBLE);
			pathSnapshot = Environment.getExternalStorageDirectory().getPath() + "/PrintManager/Icons/" + mDataGcode.getPathFile() + ".jpeg";
			mDataGcode.setPathSnapshot(pathSnapshot);
			//mDoSnapshot = doSnapshot (pathSnapshot);

			mSurface = new ViewerSurfaceView (getActivity(), mDataGcode, mState, mDoSnapshot, false);
		}
					
		//Add the view
		mLayout.addView(mSurface);
		
		if (mDoSnapshot) mLayout.setVisibility(View.INVISIBLE);		
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
	
	/*
	private boolean doSnapshot (String path) {
		boolean doSnapshot;
		File png = new File (path);
		if (png.exists()) doSnapshot =  false ;
		else doSnapshot = true;
		
		return doSnapshot;
	}
	
	
	
	public void onPause() {
        super.onPause();
        mSurface.onPause();
	 }

	 @Override
	public void onResume() {
		 super.onResume();
		 mSurface.onResume();
	 }*/

	@Override
	public void onClickFileList(File file) {
		if (file == null) {
			return;
		}

		SharedPreferences config = getActivity().getSharedPreferences("PathSetting", Activity.MODE_PRIVATE);
		SharedPreferences.Editor configEditor = config.edit();
		configEditor.putString("lastPath", file.getParent());
		configEditor.commit();
		
		mFile = new File (file.getPath());
		
		openFile (file.getPath());
	}
}
