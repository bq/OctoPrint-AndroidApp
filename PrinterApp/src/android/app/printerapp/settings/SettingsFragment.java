package android.app.printerapp.settings;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

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
			
			getNetworkSsid(rootView);
			
			mAdapter = new SettingsListAdapter(getActivity(), R.layout.settings_row, DevicesListController.getList());
			ListView l = (ListView) rootView.findViewById(R.id.lv_settings);
			l.setAdapter(mAdapter);
			
			TextView tv = (TextView) rootView.findViewById(R.id.tv_version);
			tv.setText(setBuildVersion());
			
			
			
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
		spec.setIndicator(getString(R.string.settings_tabhost_tab_connection));
		spec.setContent(R.id.tab1);
		tabs.addTab(spec);
		 
		spec=tabs.newTabSpec("Devices");
		spec.setIndicator(getString(R.string.settings_tabhost_tab_devices));
		spec.setContent(R.id.tab2);
		tabs.addTab(spec);
		
		spec=tabs.newTabSpec("Users");
		spec.setIndicator(getString(R.string.settings_tabhost_tab_users));
		spec.setContent(R.id.tab3);
		tabs.addTab(spec);
		 
		tabs.setCurrentTab(0);
		
		tabs.getTabWidget().setBackgroundColor(Color.parseColor("#333333"));
		for(int i=0;i<tabs.getTabWidget().getChildCount();i++) 
	    {
	        TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
	        tv.setTextColor(Color.parseColor("#ffffff"));
	    } 
		
		tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {
		    	
		    	//TODO Notify adapters elsewhere
		    	mAdapter.notifyDataSetChanged();
		        Log.i("CONTROLLER", "Tab pressed: " + tabId);
		    }
		});
		
	}
	
	//Return network without quotes
	public void getNetworkSsid(View v){
		 
		WifiManager wifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();   		
		
		TextView tv = (TextView) v.findViewById(R.id.tv_network);
		tv.setText(wifiInfo.getSSID().replace("\"", ""));
		
		ImageView iv = (ImageView) v.findViewById(R.id.imageView_signal);
		
		int signal = wifiInfo.getRssi();
		
		if ((signal <= 0) && (signal > -40)){
			iv.setImageResource(R.drawable.stat_sys_wifi_signal_4);
		} else if ((signal <= -40) && (signal > -60)){
			iv.setImageResource(R.drawable.stat_sys_wifi_signal_3);
		} else if ((signal <= -60) && (signal > -70)){
			iv.setImageResource(R.drawable.stat_sys_wifi_signal_2);
		} else if ((signal <= -70) && (signal > -80)){
			iv.setImageResource(R.drawable.stat_sys_wifi_signal_1);
		} else iv.setImageResource(R.drawable.stat_sys_wifi_signal_0);

	}
	
	//TODO Remove build version
	public String setBuildVersion(){
		
		String s = "Version v.";

		 try{
		     ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0);
		     ZipFile zf = new ZipFile(ai.sourceDir);
		     ZipEntry ze = zf.getEntry("classes.dex");
		     long time = ze.getTime();
		     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm",new Locale("es", "ES"));
		     s = s + sdf.format(new java.util.Date(time));
		     zf.close();
		  }catch(Exception e){
			  
			  e.printStackTrace();
		  }
		 
		 return s;
	}
	
}
