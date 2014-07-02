package android.app.printerapp.settings;

import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.os.Bundle;
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

public class SettingsFragment extends Fragment{
	
	private SettingsListAdapter mAdapter;
	
	public SettingsFragment(){}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
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
			rootView = inflater.inflate(R.layout.settings_layout,
					container, false);
			
			//Set tab host for the view
			setTabHost(rootView);
			
			/*********************************************************/
			
			
			
			mAdapter = new SettingsListAdapter(getActivity(), R.layout.settings_row, DevicesListController.getList());
			ListView l = (ListView) rootView.findViewById(R.id.lv_settings);
			l.setAdapter(mAdapter);
			
		}
		return rootView;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.settings_menu, menu);
	}
	
	//Option menu
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item) {
	   
	   switch (item.getItemId()) {
	   
	   case R.id.settings_menu_add: //Add a new printer
		  
		   return true;
    
       default:
           return super.onOptionsItemSelected(item);
	   }
	}
	
	/**
	 * Constructor for the tab host
	 * TODO: Should be moved to a View class since it only handles ui.
	 */
	public void setTabHost(View v){
				 
		TabHost tabs=(TabHost) v.findViewById(android.R.id.tabhost);
		tabs.setup();
		 
		TabHost.TabSpec spec=tabs.newTabSpec("Connection");
		spec.setIndicator("Connection");
		spec.setContent(R.id.tab1);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Devices");
		spec.setIndicator("Devices");
		spec.setContent(R.id.tab2);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Users");
		spec.setIndicator("Users");
		spec.setContent(R.id.tab3);
		tabs.addTab(spec);
		 
		tabs.setCurrentTab(0);
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {
		    	
		    	//TODO Notify adapters elsewhere
		    	mAdapter.notifyDataSetChanged();
		        Log.i("CONTROLLER", "Tab pressed: " + tabId);
		    }
		});
		
	}

}
