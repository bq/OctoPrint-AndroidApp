package android.app.printerapp.devices;

import android.app.printerapp.R;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
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
	
	//Need reference to the view (to update) and context (inflate)
	public DevicesLayoutAdapter(Context context, ViewGroup v){
		
		mViewGroup = v;
		mContext = context;
		
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
				        
				    	 //Image coordinates
				         RelativeLayout.LayoutParams Params = 
				            (RelativeLayout.LayoutParams) view.getLayoutParams();
				         xDelta = X - Params.leftMargin;
				         yDelta = Y - Params.topMargin;
				         break;
				         
				      //When we raise the finger we go back to the initial state   
				     case MotionEvent.ACTION_UP:
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
