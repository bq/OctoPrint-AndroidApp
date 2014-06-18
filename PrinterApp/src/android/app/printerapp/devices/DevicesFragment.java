package android.app.printerapp.devices;

import android.app.Activity;
import android.app.printerapp.R;
import android.app.printerapp.devices.discovery.JmdnsServiceListener;
import android.app.printerapp.model.ModelPrinter;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

/**
 * This is the fragment that will contain the Device VIEW logic
 * @author alberto-baeza
 *
 */
public class DevicesFragment extends Fragment{
	
	
	//Controllers and adapters
	private DevicesListController mListController;
	//private DevicesGridAdapter mGridAdapter;
	private static DevicesListAdapter mListAdapter;
	
	private DevicesLayoutAdapter mLayoutAdapter;
	
	//Empty constructor
	public DevicesFragment(){}
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		/**
		 * Since API level 11, thread policy has changed and now does not allow network operation to
		 * be executed on UI thread (NetworkOnMainThreadException), so we have to add these lines to 
		 * permit it.
		 */
		if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
		
		
		
		super.onCreate(savedInstanceState);
		
		//Retain instance to keep the Fragment from destroying itself
		setRetainInstance(true);
		
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		//Reference to View
		View rootView = null;
		
		//If is not new
		if (savedInstanceState==null){
			
			//Show custom option menu
			setHasOptionsMenu(true);
			
			//Inflate the fragment
			rootView = inflater.inflate(R.layout.devices_layout,
					container, false);
			

			
			/**
			 * CUSTOM VIEW METHODS
			 */
			
			//Set tab host for the view
			setTabHost(rootView);
			
			//List controller
			mListController = new DevicesListController();
			
			
			//------------------------------- View references -----------------//
			
						
			/*GridView g = (GridView) rootView.findViewById(R.id.devices_grid);

			mGridAdapter = new DevicesGridAdapter(getActivity(),
					R.layout.grid_element, mListController.getList());
	 
			g.setAdapter(mGridAdapter);*/
			
			
			
			
			
			/*******************************************************************/
			
			//Reference to the first tab
			ViewGroup mViewGroup = (ViewGroup) rootView.findViewById(R.id.devices_grid);
			mLayoutAdapter = new DevicesLayoutAdapter(getActivity(), mViewGroup);
			
			
			//Reference to the second tab, handled by an adapter
			mListAdapter = new DevicesListAdapter(getActivity(), 
					R.layout.list_element, mListController.getList());
			
			ListView l = (ListView) rootView.findViewById(R.id.devices_list);
			l.setAdapter(mListAdapter);
			
			
			/*******************************************************************/
			
			
			//Custom service listener
			new JmdnsServiceListener(this);
		
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.devices_menu, menu);
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	}

	/**
	 * Constructor for the tab host
	 * TODO: Should be moved to a View class since it only handles ui.
	 */
	public void setTabHost(View v){
				 
		TabHost tabs=(TabHost) v.findViewById(android.R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("Status");
		spec.setIndicator("Status");
		spec.setContent(R.id.tab1);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Videowall");
		spec.setIndicator("Videowall");
		spec.setContent(R.id.tab2);
		tabs.addTab(spec);
		
		 
		tabs.setCurrentTab(0);
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {
		        Log.i("CONTROLLER", "Tab pressed: " + tabId);
		    }
		});
		
	}
	
	/**
	 * LIST HANDLER
	 * TODO: Eventually this will add elements to a Database
	 */
	
	public void listHandler(final ModelPrinter m){
		
		getActivity().runOnUiThread(new Runnable() {
			
			@Override
			public void run() {
				
				mListController.addToList(m);
				
				/*************************************************************
				 * VIEW HANDLER
				 *************************************************************/

				//mGridAdapter.notifyDataSetChanged();
				mLayoutAdapter.addToLayout(m);
				mListAdapter.notifyDataSetChanged();
				
			}
		});

		
	}
	
	public static void notifyAdapter(){
		mListAdapter.notifyDataSetChanged();
	}
	
	
	
	
}
