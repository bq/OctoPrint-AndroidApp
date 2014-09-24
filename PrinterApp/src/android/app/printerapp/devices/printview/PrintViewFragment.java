package android.app.printerapp.devices.printview;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.SlidingUpPanelLayout;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintControl;
import android.app.printerapp.octoprint.StateUtils;
import android.app.printerapp.viewer.DataStorage;
import android.app.printerapp.viewer.GcodeFile;
import android.app.printerapp.viewer.ViewerMain;
import android.app.printerapp.viewer.ViewerSurfaceView;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

/**
 * This class will show the PrintView detailed view for every printer
 * Should be able to control printer commands and show video feed.
 * @author alberto-baeza
 *
 */
public class PrintViewFragment extends Fragment{
		
	//Current Printer and status
	private static ModelPrinter mPrinter;
	private boolean isPrinting = false;
	
	//View references
	private TextView tv_printer;
	private TextView tv_file;
	private TextView tv_temp;
	private TextView tv_prog;	
	
	//File references
	private static DataStorage mDataGcode;
	private static ViewerSurfaceView mSurface;
	private static FrameLayout mLayout; 
	private static FrameLayout mLayoutVideo;
	
	//Context needed for file loading
	private static Context mContext;
	
	//TODO: temp variable for initial progress
	private static int mActualProgress = 0;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		//Reference to View
		View rootView = null;

		//If is not new
		if (savedInstanceState==null){
			
			//Necessary for gcode tracking
			mContext = getActivity();
			
			//Get the printer from the list
			Bundle args = getArguments();
			mPrinter = DevicesListController.getPrinter(args.getString("printer"));
			
			//Check printing status
			if (mPrinter.getStatus() == StateUtils.STATE_PRINTING) isPrinting = true;
			else {
				
				//TODO Set print status as 100% if it's not printing
				mActualProgress = 100;
				isPrinting = false;
			}
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			rootView = inflater.inflate(R.layout.printview_layout,
					container, false);	
				
			/************************************************************************/
			
			//Show gcode tracking if there's a current path in the printer/preferences
			
			if (mPrinter.getJobPath()!=null) {
				
				//getJobPath is lost after app closing
				//TODO: File doesn't change if switched while in PrintView
				openGcodePrintView (mPrinter.getJobPath(), rootView, R.id.view_gcode);
				
				
			//No job path
			} else {
				
				//If we have a stored path
				if (DatabaseController.isPreference("References", mPrinter.getName())){
					
					String path = DatabaseController.getPreference("References", mPrinter.getName());
					
					File file = new File(path);
					
					//If the path is not the same as we thought
					if (!mPrinter.getJob().getFilename().equals(file.getName())){
						
						DatabaseController.handlePreference("References", mPrinter.getName(), null, false);
						mPrinter.setJobPath(null);
					
						//If it's the same, update jobpath
					} else {
						
						openGcodePrintView (path, rootView, R.id.view_gcode);	
						mPrinter.setJobPath(path);
					}
				
					
				}

			}

			
			//Get video
			mLayoutVideo = (FrameLayout) rootView.findViewById(R.id.printview_video);
			
			if (mPrinter.getVideo().getParent() != null)	{
				mPrinter.getVideo().stopPlayback();
				((ViewGroup)mPrinter.getVideo().getParent()).removeAllViews();
			} 
			mLayoutVideo.addView(mPrinter.getVideo());		
			
			
			/***************************************************************************/
			
			
			//UI references
			tv_printer = (TextView) rootView.findViewById(R.id.printview_printer);
			tv_file = (TextView) rootView.findViewById(R.id.printview_file);
			tv_temp = (TextView) rootView.findViewById(R.id.printview_temp);
			tv_prog = (TextView) rootView.findViewById(R.id.printview_time);
			
			final EditText et_am = (EditText) rootView.findViewById(R.id.et_amount);
			
			rootView.findViewById(R.id.button_xy_down).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_xy_up).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "y", - Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_xy_left).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x", - Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_xy_right).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "x", Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_z_down).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", - Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_z_up).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "jog", "z", Integer.parseInt(et_am.getText().toString()));
					
				}
			});
			
			rootView.findViewById(R.id.button_z_home).setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					
					OctoprintControl.sendHeadCommand(getActivity(), mPrinter.getAddress(), "home", null, 0);
					
				}
			});
			
			
			/***************** SLIDE PANEL ************************************/
			
			//Slide panel setup
			
			SlidingUpPanelLayout slidePanel = (SlidingUpPanelLayout) rootView.findViewById(R.id.sliding_panel);
			TextView textView = (TextView) rootView.findViewById(R.id.drag_text);
			slidePanel.setDragView(textView);

			refreshData();
			
			
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.printview_menu, menu);
		
		
	}
	
	//Switch menu options if it's printing/paused
	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		
		if (!isPrinting){
			menu.findItem(R.id.printview_pause).setIcon(android.R.drawable.ic_media_play);
		} else menu.findItem(R.id.printview_pause).setIcon(android.R.drawable.ic_media_pause);
		
		super.onPrepareOptionsMenu(menu);
	}
	
	//Option menu
	   @Override
		public boolean onOptionsItemSelected(android.view.MenuItem item) {
		   
		   switch (item.getItemId()) {
		   				
	       	case R.id.printview_pause:
	       		
	       		if (!isPrinting){
	       			OctoprintControl.sendCommand(getActivity(),mPrinter.getAddress(), "start");
	       		} else {
	       			OctoprintControl.sendCommand(getActivity(),mPrinter.getAddress(), "pause");
	       		}
	
	       		
	            return true;
	            
	       	case R.id.printview_stop:
	       		
	       		OctoprintControl.sendCommand(getActivity(), mPrinter.getAddress(), "cancel");
	       		
	       		return true;
	              
	          
	       default:
	           return super.onOptionsItemSelected(item);
		   }
		}
	
	   /**
	    * Convert progress string to percentage
	    * @param p progress string
	    * @return converted value
	    */
	public String getProgress(String p){
		
		double value = 0;
				
		try {
			value = Double.valueOf(p);			
		}catch (Exception e){
			//e.printStackTrace();
		}
		
		return String.valueOf((int)value);
	}
	
	/**
	 * Dinamically update progress bar and text from the main activity
	 */
	public void refreshData(){
		
		//Check around here if files were changed
		tv_printer.setText(mPrinter.getDisplayName() + " : "+mPrinter.getMessage());
		tv_file.setText(mPrinter.getJob().getFilename());
		tv_temp.setText(mPrinter.getTemperature() + "ºC / " + mPrinter.getTempTarget() + "ºC");
			
		if ((mPrinter.getStatus()== StateUtils.STATE_PRINTING)||
				(mPrinter.getStatus()== StateUtils.STATE_PAUSED)){
			
			isPrinting = true;
			tv_prog.setText(getProgress(mPrinter.getJob().getProgress()) + "% (" + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTimeLeft()) + 
					" left / " + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTime()) + " elapsed)");
			
		if (mPrinter.getJobPath()!=null) changeProgress(Double.valueOf(mPrinter.getJob().getProgress()));	
		
		} else {
			
			if (!mPrinter.getLoaded()) tv_file.setText(R.string.devices_upload_waiting);
			tv_prog.setText(mPrinter.getMessage());
			isPrinting = false;
		}
		
		getActivity().invalidateOptionsMenu();
		
	}
	
	
	//External method to convert seconds to HHmmss
	private String ConvertSecondToHHMMString(String secondtTime)
	{
		String time = "--:--:--";
		
		if (!secondtTime.equals("null")){
			
			 TimeZone tz = TimeZone.getTimeZone("UTC");
			  SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss",Locale.US);
			  df.setTimeZone(tz);
			  time = df.format(new Date(Integer.parseInt(secondtTime)*1000L));
		}
	 

	  return time;

	}
	
	//TODO Properly close the video when destroying the view
	@Override
	public void onDestroy() {
		mPrinter.getVideo().stopPlayback();
		((ViewGroup)mPrinter.getVideo().getParent()).removeAllViews();
		super.onDestroy();
	}
	
	/*******************************************************************************************
	 * 
	 * 					PRINT VIEW PROGRESS HANDLER
	 * 
	 * @param filePath
	 * @param rootView
	 * @param frameLayoutId
	 *******************************************************************************************/
	
	public void openGcodePrintView (String filePath, View rootView, int frameLayoutId) {
		Context context = getActivity();
		mLayout = (FrameLayout) rootView.findViewById (frameLayoutId);
		File file = new File (filePath);
		
		mDataGcode = new DataStorage();
		GcodeFile.openGcodeFile(context, file, mDataGcode,ViewerMain.PRINT_PREVIEW);
		mDataGcode.setActualLayer(0);
		
	}
	
	public static boolean drawPrintView () {
		List <DataStorage> gcodeList = new ArrayList <DataStorage> ();
		gcodeList.add(mDataGcode);	

		mSurface = new ViewerSurfaceView (mContext, gcodeList, ViewerSurfaceView.LAYERS, ViewerMain.PRINT_PREVIEW);
		mLayout.removeAllViews();
		mLayout.addView(mSurface, 0);	
		
		changeProgress(mActualProgress);
	

		return true;
	}
	
	public static void changeProgress (double percentage) {
		int maxLines =  mDataGcode.getMaxLayer();	
		int progress = (int) percentage*maxLines/100;
		mDataGcode.setActualLayer(progress);
    	if (mSurface!= null) mSurface.requestRender();	
	}
}
