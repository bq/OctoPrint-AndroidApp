package android.app.printerapp.devices;

import java.io.File;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.content.ClipData;
import android.content.res.Resources;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/**
 * OnDragListener to handle printing and other possible events such as
 * printer positions on the grid
 * @author alberto-baeza
 *
 */
public class DevicesDragListener implements OnDragListener {
		
	//Reference to model
	private ModelPrinter mModel;
	
	/**
	 * Class constructor
	 * @param model The model currently being dragged on
	 */
	public DevicesDragListener(ModelPrinter model){
		mModel = model;
	}

	
	/**
	 * Possible drag event tags:
	 * 
	 * printer: Dragging a printer
	 * name: Dragging a file
	 * 
	 */
	@Override
	public boolean onDrag(View v, DragEvent event) {
		
		//Get the drop event
		int action = event.getAction();
	    switch (action) {
	    
	    //If it's a drop
	    case DragEvent.ACTION_DROP:{
	    	
	    	CharSequence tag = event.getClipData().getDescription().getLabel();
	    	

	    	//If it's a file (avoid draggable printers)
	    	if (tag.equals("name")){
 	
		    	//If it's not online, don't send to printer	
	    		//Now files can also be uploaded to printers with errors
		    	if ((mModel.getStatus() == StateUtils.STATE_OPERATIONAL) ||
		    	(mModel.getStatus() == StateUtils.STATE_ERROR)){
		    		
		    		// Gets the item containing the dragged data
		    		ClipData.Item item = event.getClipData().getItemAt(0);
		    		
		    		//Get parent folder and upload to device
		    		File file = new File(item.getText().toString());
		    		
		    		//Call to the static method to upload
		    		OctoprintFiles.uploadFile(v.getContext(), file, mModel, false, false);
		    		
		    	} else {
		    		
		    		//Error dialog
		    		ItemListActivity.showDialog(v.getContext().getString(R.string.devices_dialog_loading) + "\n" + mModel.getMessage());
		    	}
	    	}
	    	
	    	
	    	//Remove highlight
	    	v.setBackgroundColor(Resources.getSystem().getColor(android.R.color.transparent));
	    	
	    	
	    	
	    } break;
	    
	    case DragEvent.ACTION_DRAG_ENTERED:{
	    	
	    	//Highlight on hover
	    	v.setBackgroundColor(Resources.getSystem().getColor(android.R.color.holo_blue_light));
	    	
	    }break;
	    case DragEvent.ACTION_DRAG_EXITED:{
	    	
	    	//Remove highlight
	    	v.setBackgroundColor(Resources.getSystem().getColor(android.R.color.transparent));
	    	
	    }break;

	      default:
	      break;
	    }
		
		return true;
	}
	
}
