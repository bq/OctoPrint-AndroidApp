package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.ClipData;
import android.content.res.Resources;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;
import android.widget.ArrayAdapter;


/**
 * this class will handle drag listeners for empty cells on the GridView, 
 * it will only listen to printers, and will update the position on the DB also
 * @author alberto-baeza
 *
 */
public class DevicesEmptyDragListener implements OnDragListener{
	
	//current position on the grid
	private int mPosition;
    private ArrayAdapter<ModelPrinter> mAdapter;
	
	/**
	 * Constructor to the listener
	 * @param pos the position on the grid
	 */
	public DevicesEmptyDragListener(int pos, ArrayAdapter<ModelPrinter> adapter){

		mPosition = pos;
        mAdapter = adapter;
	}


    /**
     * We have to parse the event tag separately per event because there is a bug with ACTION_DRAG_ENDED
     * that has a null getClipDescription().
     */
	@Override
	public boolean onDrag(View v, DragEvent event) {
		
        //Get the drop event
        int action = event.getAction();

        switch (action) {

        //If it's a drop
        case DragEvent.ACTION_DROP:{

            CharSequence tag = event.getClipDescription().getLabel();
            ClipData.Item item = event.getClipData().getItemAt(0);

            //If it's a printer
            if (tag.equals("printer")){


                int id = Integer.parseInt(item.getText().toString());
                //Find a printer from it's name
                ModelPrinter p = null;

                if (id>=0){

                   p = DevicesListController.getPrinter(id);

                } else {

                   p = DevicesListController.getPrinterByPosition(-(id + 1));

                }

                if (p!=null){
                    //update position
                    p.setPosition(mPosition);
                    //update database
                    //DatabaseController.updateDB(FeedEntry.DEVICES_POSITION, p.getId(), String.valueOf(mPosition));

                    //static notification of the adapters
;
                    mAdapter.notifyDataSetChanged();



                }

            }


            //Remove highlight
            v.setBackgroundColor(Resources.getSystem().getColor(android.R.color.transparent));

        } break;

        case DragEvent.ACTION_DRAG_ENTERED:{

            //TODO NullPointerException
            try{

                CharSequence tag = event.getClipDescription().getLabel();
                //If it's a printer
                if (tag.equals("printer")){

                    //Highlight on hover
                    v.setBackgroundColor(v.getContext().getResources().getColor(R.color.drag_and_drop_hover_background));
                }

            } catch (NullPointerException e){

                e.printStackTrace();

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
