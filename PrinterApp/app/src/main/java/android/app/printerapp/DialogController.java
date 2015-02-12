package android.app.printerapp;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;


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

        MaterialDialog.Builder madb = new MaterialDialog.Builder(mContext);
		madb.title(R.string.error);
		madb.icon(mContext.getResources().getDrawable(R.drawable.ic_warning_grey600_24dp));
		madb.content(msg);
        madb.positiveColor(R.color.theme_primary);
        madb.positiveText(R.string.ok);
		madb.show();

	}

}
