package android.app.printerapp;

import android.app.AlertDialog;
import android.content.Context;

public class DialogController {
	
	private Context mContext;
	
	public DialogController(Context context){
		
		mContext = context;
		
	}
	
	public void displayDialog(String msg){
		
		AlertDialog.Builder adb = new AlertDialog.Builder(mContext);
		adb.setTitle("Error");
		adb.setIcon(android.R.drawable.stat_sys_warning);
		adb.setMessage(msg);
		adb.show();

	}

}
