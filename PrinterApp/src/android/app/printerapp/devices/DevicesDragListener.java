package android.app.printerapp.devices;

import java.io.File;

import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.StateUtils;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.content.ClipData;
import android.content.res.Resources;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

/**
 * OnDragListener to handle printing and other possible events
 * @author alberto-baeza
 *
 */
public class DevicesDragListener implements OnDragListener {
		
	//Reference to model
	private ModelPrinter mModel;
	
	//Constructor
	public DevicesDragListener(ModelPrinter model){

		mModel = model;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		
		//Get the drop event
		int action = event.getAction();
	    switch (action) {
	    
	    //If it's a drop
	    case DragEvent.ACTION_DROP:{
	    	
	    	CharSequence tag = event.getClipData().getDescription().getLabel();
	    	
	    	if (!tag.equals("printer")){
	    		
	    	
	    		    	
		    	//If it's not online, don't send to printer		    	
		    	if (mModel.getAddress().equals("Offline")){	 
		    		
		    		//ItemListActivity.showDialog("Printer offline");
		    		
		    	} else if ((mModel.getStatus() == StateUtils.STATE_OPERATIONAL) ||
		    	(mModel.getStatus() == StateUtils.STATE_ERROR)){
		    		
		    		
		    		ClipData.Item item = event.getClipData().getItemAt(0);
	                // Gets the item containing the dragged data
		    		
		    		//If tag is name, we have a file to drop
		    		if (tag.equals("name")){
			    		
			    		//Get parent folder and upload to device
			    		Log.i("DRAG", item.getText().toString());
			    		File file = new File(item.getText().toString());
			    		OctoprintFiles.uploadFile(v.getContext(), file, mModel);
			    			
		    		//Check if it's on internal storage plus if it's sd or not, since we don't need to upload.	
		    		//TODO: Set the same method for both
		    		} 
		    		
		    		
		    	} else {
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
