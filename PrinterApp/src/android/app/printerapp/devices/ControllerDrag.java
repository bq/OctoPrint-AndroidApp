package android.app.printerapp.devices;

import java.io.File;

import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintLoadAndPrint;
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
public class ControllerDrag implements OnDragListener {
		
	//Reference to model
	private ModelPrinter mModel;
	
	//Constructor
	public ControllerDrag(ModelPrinter model){

		mModel = model;
	}

	@Override
	public boolean onDrag(View v, DragEvent event) {
		
		//Get the drop event
		int action = event.getAction();
	    switch (action) {
	    
	    //If it's a drop
	    case DragEvent.ACTION_DROP:{
	    	
	    	//If it's not online, don't send to printer		    	
	    	if (mModel.getAddress().equals("Offline")){	 
	    		
	    		Log.i("OUT","Printer offline");
	    		
	    	} else {
	    		
	    		CharSequence tag = event.getClipData().getDescription().getLabel();
	    		ClipData.Item item = event.getClipData().getItemAt(0);
                // Gets the item containing the dragged data
	    		
	    		//If tag is name, we have a file to drop
	    		if (tag.equals("name")){
		    		
		    		//Get parent folder and upload to device
		    		Log.i("DRAG", item.getText().toString());
		    		File file = new File(item.getText().toString());
		    		OctoprintLoadAndPrint.uploadFile(mModel.getAddress(), file, false);

	    		//Check if it's on internal storage plus if it's sd or not, since we don't need to upload.	
	    		//TODO: Set the same method for both
	    		} else if (tag.equals("internal")){
	    			OctoprintLoadAndPrint.printInternalFile(mModel.getAddress(), item.getText().toString(), false, false);
		    		
	    		}else if (tag.equals("internalsd")){
	    			OctoprintLoadAndPrint.printInternalFile(mModel.getAddress(), item.getText().toString(), false, true);
			    	
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
