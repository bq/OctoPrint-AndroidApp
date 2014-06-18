package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.RelativeLayout;
import android.widget.TextView;


/**
 * This class is not actually an adapter but a way to keep runtime control for the
 * main layout, similar to a normal grid adapter
 * @author alberto-baeza
 *
 */
public class DevicesLayoutAdapter {
	
	private ViewGroup mViewGroup;
	private Context mContext;
	private boolean mActionMode;
	
	//Need reference to the view (to update) and context (inflate)
	public DevicesLayoutAdapter(Context context, ViewGroup v){
		
		mViewGroup = v;
		mContext = context;
		mActionMode = false;
		
		
		//Inflate the view
				LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View vw = i.inflate(R.layout.grid_element, null, false);
				
				//Printer tag reference
				TextView tag = (TextView) vw.findViewById(R.id.grid_element_tag);
				tag.setText("BASTARDO D:<");
				MyCustomTouchListener listener = new MyCustomTouchListener(mViewGroup);
				
				vw.setOnTouchListener(listener);
				vw.setOnLongClickListener(listener);
				
				
				mViewGroup.addView(vw);
				
				mViewGroup.invalidate();
		
	}
	
	
	//This will add a new element to the view and initialize the listeners
	public void addToLayout(ModelPrinter m){
		
		//Inflate the view
		LayoutInflater i = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = i.inflate(R.layout.grid_element, null, false);

		//Printer tag reference
		TextView tag = (TextView) v.findViewById(R.id.grid_element_tag);
		tag.setText(m.getName());
		
		MyCustomTouchListener listener = new MyCustomTouchListener(mViewGroup);
		
		v.setOnTouchListener(listener);
		v.setOnLongClickListener(listener);
		
		
		mViewGroup.addView(v);
		
		mViewGroup.invalidate();
	}
	
	/**
	 * Callback for the  contextual menu as described @ Android Developers
	 */
	private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
		
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
	       mActionMode = false;
	    }
	};
	
	
/*******************************************************************************/
	
	/**
	 * This is the drag method obtained from an external source
	 * @author alberto-baeza
	 *
	 */
	public class MyCustomTouchListener implements OnTouchListener, OnLongClickListener{
		
		 //X-Y variables
		 private int xDelta;
		 private int yDelta;
		 
		 private ViewGroup marco;
		 
		 //Long click flag to avoid overlapping functionalities
		 private boolean onLong = false;
		
		public MyCustomTouchListener(ViewGroup m){
			marco = m;
		}
		
		@Override
		 public boolean onTouch(View view, MotionEvent event) {
			
				//Finger coordinates
				  final int X = (int) event.getRawX();
				  final int Y = (int) event.getRawY();
				  
				  switch (event.getAction() & MotionEvent.ACTION_MASK) {
				  
				  	//On image touch
				     case MotionEvent.ACTION_DOWN:
				    	 
				    	 //Open contextual action bar
				    	 if (!mActionMode){
				    		 view.startActionMode(mActionModeCallback);
				    		 mActionMode = true;
				    	 }
				    	 	
				    	 //Image coordinates
				         RelativeLayout.LayoutParams Params = 
				            (RelativeLayout.LayoutParams) view.getLayoutParams();
				         xDelta = X - Params.leftMargin;
				         yDelta = Y - Params.topMargin;
				         view.setSelected(true);
				         break;
				         
				      //When we raise the finger we go back to the initial state   
				     case MotionEvent.ACTION_UP:
				    	 view.setSelected(false);
				    	 onLong = false;
				         break;

				         //On move
				      case MotionEvent.ACTION_MOVE:
				    	  
				    	  //Only when onlongclick was triggered
				  		if (onLong){
				       
				  			//move the object 
				          RelativeLayout.LayoutParams layoutParams = 
				             (RelativeLayout.LayoutParams) view.getLayoutParams();
				          layoutParams.leftMargin = X - xDelta;
				          layoutParams.topMargin = Y - yDelta;
				         
				          //Avoid deformation
				          layoutParams.rightMargin = -50;
				          layoutParams.bottomMargin = -50;
				          
				         //Set new parameters to finish movement
				          view.setLayoutParams(layoutParams);
				          
				  		}
				          break;
				      }
				  
				  	//draw new view
				      marco.invalidate();
			
		  
		      return false;
		  }

		
		//Trigger the long click flag to permit movement accross the screen
		@Override
		public boolean onLongClick(View v) {
			
			onLong = true;
			return false;
		}
		

	}

}
