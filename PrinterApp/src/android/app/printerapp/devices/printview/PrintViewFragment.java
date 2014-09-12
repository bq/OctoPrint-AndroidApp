package android.app.printerapp.devices.printview;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import android.app.printerapp.R;
import android.app.printerapp.StateUtils;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintControl;
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
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * This class will show the PrintView detailed view for every printer
 * Should be able to control printer commands and show video feed.
 * @author alberto-baeza
 *
 */
public class PrintViewFragment extends Fragment{
	
	private ModelPrinter mPrinter;
	private boolean isPrinting = false;
	
	private TextView tv_printer;
	private TextView tv_file;
	private TextView tv_temp;
	private TextView tv_prog;	
	
	private static DataStorage mDataGcode;
	private static ViewerSurfaceView mSurface;
	private static FrameLayout mLayout; 

	private static Context mContext;
	private static int mActualProgress =30;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		
		//Reference to View
		View rootView = null;

		//If is not new
		if (savedInstanceState==null){
			
			Bundle args = getArguments();
			mPrinter = DevicesListController.getPrinter(args.getString("printer"));
			
			if (mPrinter.getStatus() == StateUtils.STATE_PRINTING) isPrinting = true;
			else isPrinting = false;
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			rootView = inflater.inflate(R.layout.printview_layout,
					container, false);	
			
			RelativeLayout ll = (RelativeLayout) rootView.findViewById(R.id.printview_camera);
			RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
	                 RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);    
			if (mPrinter.getVideo().getParent() != null)	{
				mPrinter.getVideo().stopPlayback();
				((ViewGroup)mPrinter.getVideo().getParent()).removeAllViews();
			} 
			
			ll.addView(mPrinter.getVideo(),layoutParams);
			
			tv_printer = (TextView) rootView.findViewById(R.id.printview_printer);
			tv_file = (TextView) rootView.findViewById(R.id.printview_file);
			tv_temp = (TextView) rootView.findViewById(R.id.printview_temp);
			tv_prog = (TextView) rootView.findViewById(R.id.printview_time);
			mContext = getActivity();

			refreshData();
			
			openGcodePrintView ("/storage/emulated/0/PrintManager/prueba.gcode", rootView, R.id.view_gcode);
			
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.printview_menu, menu);
		
		
	}
	
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
	
	public String getProgress(String p){
		
		double value = 0;
				
		try {
			value = Double.valueOf(p);			
		}catch (Exception e){
			//e.printStackTrace();
		}
		
		return String.valueOf((int)value);
	}
	
	public void refreshData(){
		
		tv_printer.setText(mPrinter.getDisplayName());
		tv_file.setText(mPrinter.getJob().getFilename());
		tv_temp.setText(mPrinter.getTemperature() + "ºC / " + mPrinter.getTempTarget() + "ºC");
			
		if (mPrinter.getStatus()== StateUtils.STATE_PRINTING){
			
			isPrinting = true;
			tv_prog.setText(getProgress(mPrinter.getJob().getProgress()) + "% (" + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTimeLeft()) + 
					" left / " + ConvertSecondToHHMMString(mPrinter.getJob().getPrintTime()) + " elapsed)");
			
			
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
	
	@Override
	public void onDestroy() {
		mPrinter.getVideo().stopPlayback();
		((ViewGroup)mPrinter.getVideo().getParent()).removeAllViews();
		super.onDestroy();
	}
	
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
		int maxLines =  mDataGcode.getMaxLinesFile();			
		int progress = (int) percentage*maxLines/100;
		mDataGcode.setActualLayer(progress);
    	if (mSurface!= null) mSurface.requestRender();	
	}
}
