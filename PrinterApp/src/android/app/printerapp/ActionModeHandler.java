package android.app.printerapp;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * This class will handle ActionMode interaction for various interfaces
 * @author alberto-baeza
 *
 */
public class ActionModeHandler {
	
	private static boolean status = false;
	private static ActionMode mActionMode;
	
	
	//Start action mode with the desired callback
	//TODO only one right now
	public static void modeStart(View v){
		
		if (!status){
			mActionMode = v.startActionMode(mActionModeCallback);
			status = true;
		}
		
		
	}
	
	//Finish action mode
	public static void modeFinish(){
		
		if (status){
			
			mActionMode.finish();
			status = false;
		}
		
	}
	
	/**
	 * Callback for the  contextual menu as described @ Android Developers
	 */
	private static ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		
	    // Called when the action mode is created; startActionMode() was called
	    @Override
	    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
	        // Inflate a menu resource providing context menu items
	        MenuInflater inflater = mode.getMenuInflater();
	        inflater.inflate(R.menu.devices_cab_menu, menu);
	        return true;
	    }

	    // Called each time the action mode is shown. Always called after onCreateActionMode, but
	    // may be called multiple times if the mode is invalidated.
	    @Override
	    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
	        return false; // Return false if nothing is done
	    }

	    // Called when the user selects a contextual menu item
	    @Override
	    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
	        switch (item.getItemId()) {
	            case R.id.menu_cab_delete:
	                mode.finish(); // Action picked, so close the CAB
	                return true;
	                
	            case R.id.menu_cab_settings:
	            	mode.finish(); // Action picked, so close the CAB
	            	return true;
	            default:
	                return false;
	        }
	    }

	    // Called when the user exits the action mode
	    @Override
	    public void onDestroyActionMode(ActionMode mode) {
	       //mode.finish();
	       status = false;
	    }
	};
	
	

}
