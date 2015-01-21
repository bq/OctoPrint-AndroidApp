package android.app.printerapp.devices;

import android.app.printerapp.MainActivity;
import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintFiles;
import android.app.printerapp.octoprint.StateUtils;
import android.content.ClipData;
import android.content.Context;
import android.content.res.Resources;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ArrayAdapter;

import java.io.File;

/**
 * OnDragListener to handle printing and other possible events such as
 * printer positions on the grid
 * @author alberto-baeza
 *
 */
public class DevicesDragListener implements OnDragListener {

    private Context mContext;

	//Reference to model
    private ModelPrinter mModel;
    private ArrayAdapter<ModelPrinter> mAdapter;
	
	/**
	 * Class constructor
	 * @param model The model currently being dragged on
	 */
	public DevicesDragListener(Context context, ModelPrinter model, ArrayAdapter<ModelPrinter> adapter){
		mContext = context;
        mModel = model;
        mAdapter = adapter;
	}

	
	/**
	 * Possible drag event tags:
	 * 
	 * printer: Dragging a printer
	 * name: Dragging a file
	 */
	@Override
	public boolean onDrag(View v, DragEvent event) {
		
		//Get the drop event
		int action = event.getAction();
	    switch (action) {
	    
	    //If it's a drop
	    case DragEvent.ACTION_DROP:{

            CharSequence tag = event.getClipDescription().getLabel();
	    	

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
		    		OctoprintFiles.uploadFile(v.getContext(), file, mModel);

                    mAdapter.notifyDataSetChanged();
		    		
		    	} else {
		    		//Error dialog
		    		MainActivity.showDialog(v.getContext().getString(R.string.devices_dialog_loading) + "\n" + v.getContext().getString(R.string.viewer_printer_unavailable));
		    	}
	    	}

	    	//Remove highlight
	    	v.setBackgroundColor(Resources.getSystem().getColor(android.R.color.transparent));

	    } break;
	    
	    case DragEvent.ACTION_DRAG_ENTERED:{

            CharSequence tag = event.getClipDescription().getLabel();
            //If it's a file (avoid draggable printers)
            if (tag.equals("name")) {

                if ((mModel.getStatus() == StateUtils.STATE_OPERATIONAL) ||
                        (mModel.getStatus() == StateUtils.STATE_ERROR)) {

                    //Highlight on hover
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.drag_and_drop_hover_background));
                }
            }

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
