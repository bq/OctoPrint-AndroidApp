package android.app.printerapp.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import android.app.Dialog;
import android.app.printerapp.R;
import android.app.printerapp.library.StorageController;

public class ViewerMain extends Fragment {	
	//Constants 
	public static final boolean DO_SNAPSHOT = true;
	public static final boolean DONT_SNAPSHOT = false;
	public static final boolean STL = true;
	public static final boolean GCODE = false;
	
	//Variables
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
	
	private static List<DataStorage> mDataStlList = new ArrayList<DataStorage>();	
	private static List<DataStorage> mDataGcodeList= new ArrayList<DataStorage>();
		
	//Edition menu variables
	private static LinearLayout mMenu;
	private ImageButton mMove;
	private ImageButton mRotation;
	private ImageButton mMirror;	
	private ImageButton mScale;
	private ImageButton mExit;
	private ImageButton mDelete;
	
	private static ProgressBar mProgress;

	private static Context mContext;
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
		    	mDataGcodeList.get(0).setActualLayer(progress);
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
	
   
   /************************* FILE MANAGEMENT ********************************/
   public static void openFile (String filePath) {
	   Log.i("viewer", " file path " + filePath);
		mFile = new File(filePath);
		//Open the file
		if ((filePath.endsWith(".stl")|| filePath.endsWith(".STL")) ) {
			DataStorage dataStl = new DataStorage();
			StlFile.openStlFile (mContext, mFile, dataStl, false);
			mDataStlList.add(dataStl);
		} else if ((filePath.endsWith(".gcode")|| filePath.endsWith(".GCODE"))) {
			mDataGcodeList.clear();
			mDataStlList.clear();
			DataStorage dataGcode = new DataStorage();
			GcodeFile.openGcodeFile(mContext, mFile, dataGcode,false);
			mDataGcodeList.add(dataGcode);
		}
		
		draw(filePath);
	}
   
	private void changeStlViews (int state) {
		if (mFile!=null) {
			mState = state;
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
	
	private static void draw (String filePath) {	
		//String pathSnapshot;
		setEditionMenuVisibility(View.INVISIBLE);

		if (filePath.endsWith(".stl") || filePath.endsWith(".STL") ) {
			mSeekBar.setVisibility(View.INVISIBLE);
			mSurface = new ViewerSurfaceView (mContext, mDataStlList, mState, DONT_SNAPSHOT, STL);

		} else if (filePath.endsWith(".gcode") || filePath.endsWith(".GCODE")) {
			mSeekBar.setVisibility(View.VISIBLE);
			mSurface = new ViewerSurfaceView (mContext, mDataGcodeList, mState, DONT_SNAPSHOT, GCODE);
		}							
		//Add the view
		mLayout.removeAllViews();
		mLayout.addView(mSurface, 0);
		mLayout.addView(mMenu, 1);
		
				
		//TODO CHANGED: Edition menu does not appear on top if we set setZOrderOnTop (true).
		//Set the surface Z priority to top
		//mSurface.setZOrderOnTop(true);
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

	    	if (StlFile.saveModel(mDataStlList, proyectNameText.getText().toString())) dialog.dismiss();
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
}
