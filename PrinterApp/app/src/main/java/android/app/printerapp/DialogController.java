package android.app.printerapp;

import android.app.AlertDialog;
import android.content.Context;


/**
 * Temporary class to show dialogs from static classes
 * @author alberto-baeza
 *
 */
public class DialogController {
	
	private Context mContext;
	
	public DialogController(Context context){
		
		mContext = context;
		
	}
	
	/**
	 * Display dialog
	 * @param msg the message shown
	 */
	public void displayDialog(String msg){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setTitle(R.string.error);
		adb.setIcon(android.R.drawable.stat_sys_warning);
		adb.setMessage(msg);
		adb.show();

	}

}
