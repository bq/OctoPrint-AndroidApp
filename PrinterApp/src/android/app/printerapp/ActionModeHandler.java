package android.app.printerapp;

import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
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
	private static ModelPrinter mCurrentModel;
	
	
	//Start action mode with the desired callback
	//TODO only one right now
	public static void modeStart(View v, ModelPrinter m){
		
		if (!status){
			mActionMode = v.startActionMode(mActionModeCallback);
			status = true;
			mCurrentModel = m ;
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
	                DatabaseController.deleteFromDb(mCurrentModel.getName());
	                mode.finish();
	                return true;
	                
	            case R.id.menu_cab_settings:
	            	//ItemListFragment.performClick(4);
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
