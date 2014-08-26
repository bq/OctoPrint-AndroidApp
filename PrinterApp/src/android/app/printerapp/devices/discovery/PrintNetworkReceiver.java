package android.app.printerapp.devices.discovery;

import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelPrinter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.ArrayAdapter;

/**
 * This class will handle new ad-hoc printers, searching for them on the local network and connecting to them to configure.
 * @author alberto-baeza
 *
 */
public class PrintNetworkReceiver extends BroadcastReceiver{
	
	//TODO: Hardcoded Network name for testing
	//Filter to search for when scanning networks
	private static final String NETWORK_NAME = "bq_";
	
	private WifiManager mWifiManager;
	private DevicesFragment mController;
	private IntentFilter mFilter;
	private ConnectivityManager cm;
	private static  ArrayAdapter<String> mNetworkList;
	
	
	//Constructor
	public PrintNetworkReceiver(DevicesFragment controller){
		
		this.mWifiManager = (WifiManager)controller.getActivity().getSystemService(Context.WIFI_SERVICE);
		this.mController = controller;
		
		//Network list
		//TODO: Don't make it permanent
		mNetworkList = new ArrayAdapter<String>(mController.getActivity(), android.R.layout.select_dialog_singlechoice);

		
		
		cm = (ConnectivityManager)controller.getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
		
		mFilter = new IntentFilter();
		mFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		mFilter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
		mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		
		register();
	}

	/**
	 * Search for a pre-defined network name and treat them as a new offline printer.
	 * TODO: REMOVE MULTIPLE INSERTIONS
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		/**
		 * New method to check an established connection with a network (Reliable)
		 */
		if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION){
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

			 if (activeNetwork!=null){
				 Log.d("app","Network connectivity change " + activeNetwork.getState());
				 PrintNetworkManager.dismissNetworkDialog();	
			 }
		}

		 //Search for the Network with the desired name    
		 if (intent.getAction() == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
	            
	        	List<ScanResult> result =  mWifiManager.getScanResults();
	        	
	        	setNetworkList(result);
	        	
	        	//Put the results on a list and search for the one(s) we want
	        	for (ScanResult s : result){
	        		
	        		//Log.i("out",s.SSID);
	        		
	        		//TODO: This should search for multiple Networks son we can't unregister the receiver.
	        		//Log.i("Network",s.toString());
	        		if ((s.SSID.contains(NETWORK_NAME))&&(s.SSID.length()<=7)){
	        			
	        			Log.i("Network","New printer found! " + s.SSID);
	        			//unregister();
	        			
	        			ModelPrinter m = new ModelPrinter(s.SSID,"/10.0.0.1",DevicesListController.searchAvailablePosition());
	        			        			
	        			//Check if network is already on the list
	        			if (!DevicesListController.checkExisting(m)){
	        				
	        				mController.addElement(m);
	        				m.setNotConfigured();
	        			}
	        			else Log.i("OUT","QUe existo ya coño");
	       			 
	        			
	        		}
	        	}
	        	
	        }

	}

	//Start scanning for Networks.
	public void startScan(){
		mWifiManager.startScan();
		
	}
	
	public void register(){
		mController.getActivity().registerReceiver(this, mFilter);
		startScan();
	}
	
	public void unregister(){
		mController.getActivity().unregisterReceiver(this);
	}
	
	/**
	 * Fill the list with every available Network, it's updated "automatically" with every
	 * scan request which can also be triggered with "Refresh"
	 * @param networks
	 */
	public void setNetworkList(List<ScanResult> networks){
		
		mNetworkList.clear();
		ArrayList <String >a = new ArrayList<String>();
		
		for (ScanResult s : networks){
			if (!a.contains(s.SSID)){
				a.add(s.SSID);
				Log.i("OUT",s.SSID);
			} else Log.i("OUT","Duplicate network " + s.SSID);
		}

		
		for (String s : a){
			mNetworkList.add(s);
			
		}
		
		
	}
	
	public static ArrayAdapter<String> getNetworkList(){
		return mNetworkList;
	}
	
}
