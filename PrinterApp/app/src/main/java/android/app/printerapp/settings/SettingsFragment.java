package android.app.printerapp.settings;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

/**
 * Class to manage the application and printer settings
 */
public class SettingsFragment extends Fragment {
	
	private SettingsListAdapter mAdapter;
    private SettingsHiddenAdapter mHiddenAdapter;

    //Blacklist created by the new printers
    private ArrayList<String> mBlackList;

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

            mBlackList = new ArrayList<String>();



            mHiddenAdapter = new SettingsHiddenAdapter(getActivity(), R.layout.settings_row, mBlackList);
            ListView lh = (ListView) rootView.findViewById(R.id.lv_hidden);
            lh.setAdapter(mHiddenAdapter);

			TextView tv = (TextView) rootView.findViewById(R.id.tv_version);
			tv.setText(setBuildVersion());

            notifyAdapter();
			
			
			
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


           optionAddPrinter();

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

        //Set style for the tab widget
        for (int i = 0; i < tabs.getTabWidget().getChildCount(); i++) {
            final View tab = tabs.getTabWidget().getChildTabViewAt(i);
            tab.setBackgroundDrawable(getResources().getDrawable(R.drawable.tab_indicator_ab_green));
            TextView tv = (TextView) tabs.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(getResources().getColor(R.color.body_text_2));
        }


        tabs.setOnTabChangedListener(new OnTabChangeListener() {
		    @Override
		    public void onTabChanged(String tabId) {
		    	
		    	//TODO Notify adapters elsewhere
		    	mAdapter.notifyDataSetChanged();
                mHiddenAdapter.notifyDataSetChanged();
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
		     /*ApplicationInfo ai = getActivity().getPackageManager().getApplicationInfo(getActivity().getPackageName(), 0);
		     ZipFile zf = new ZipFile(ai.sourceDir);
		     ZipEntry ze = zf.getEntry("classes.dex");
		     long time = ze.getTime();
		     SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm",new Locale("es", "ES"));
		     s = s + sdf.format(new java.util.Date(time));
		     zf.close();*/

             PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
             String fString = pInfo.versionName;

             String hash = fString.substring(0,fString.indexOf(" "));
             String date = fString.substring(fString.indexOf(" "), fString.length());

             String [] fHash = hash.split(";");

             SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmm",new Locale("es", "ES"));
             String fDate = sdf.format(new java.util.Date(date));

             String code;
             if (pInfo.versionCode == 0) code = "IDE";
             else code = "#"+ pInfo.versionCode;

             s = s + fHash[0] + " " + fHash[1] + " " + fDate + " " + code;

		  }catch(Exception e){
			  
			  e.printStackTrace();
		  }
		 
		 return s;
	}

    public void notifyAdapter(){
        mAdapter.notifyDataSetChanged();
        loadBlacklist();
        mHiddenAdapter.notifyDataSetChanged();
    }

    //Load/reload blacklist
    public void loadBlacklist(){

        //Update blacklist
        mBlackList.clear();

        for (Map.Entry<String, ?> entry : DatabaseController.getPreferences("Blacklist").entrySet()) {

            mBlackList.add(entry.getKey());
        }

    }


    /**
     * Add a new printer to the database by IP instead of service discovery
     */
    private void optionAddPrinter(){

        AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());


        adb.setTitle(R.string.settings_add_title);

        //Inflate the view
        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.settings_add_printer_dialog, null, false);

        final EditText et_name = (EditText) v.findViewById(R.id.et_name);
        final EditText et_address = (EditText) v.findViewById(R.id.et_address);

        adb.setView(v);

        //On insertion write the printer onto the database and start updating the socket
        adb.setPositiveButton(R.string.add, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                ModelPrinter m = new ModelPrinter(et_name.getText().toString(), "/" + et_address.getText().toString(), DevicesListController.searchAvailablePosition());

                if (!DevicesListController.checkExisting(m)) {

                    DevicesListController.addToList(m);
                    m.setId(DatabaseController.writeDb(m.getName(), m.getAddress(), String.valueOf(m.getPosition())));
                    m.setLinked(getActivity());
                    notifyAdapter();

                }

            }
        });

        adb.setNegativeButton(R.string.cancel, null);

        adb.setView(v);

        adb.show();


    }
	
}
