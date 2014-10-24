package android.app.printerapp.devices.discovery;

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

import java.util.ArrayList;
import java.util.List;

/**
 * This class will handle new ad-hoc printers, searching for them on the local network and connecting to them to configure.
 * @author alberto-baeza
 *
 */
public class PrintNetworkReceiver extends BroadcastReceiver{
	
	//TODO: Hardcoded Network name for testing
	//Filter to search for when scanning networks
	private static final String NETWORK_NAME = "OctoPi";
	
	private WifiManager mWifiManager;
	private PrintNetworkManager mController;
	private Context mContext;
	private IntentFilter mFilter;
	private ConnectivityManager cm;
	private static  ArrayAdapter<String> mNetworkList;
	
	
	//Constructor
	public PrintNetworkReceiver(PrintNetworkManager controller){

		this.mContext = controller.getContext();
		this.mWifiManager = (WifiManager)mContext.getSystemService(Context.WIFI_SERVICE);
		this.mController = controller;
		
		//Network list
		//TODO: Don't make it permanent
		mNetworkList = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_singlechoice);

		
		
		cm = (ConnectivityManager)mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		
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
				 
				 Log.i("NETWORK","Network connectivity change " + activeNetwork.getState());
				 mController.dismissNetworkDialog();	
				 
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
	        		if (s.SSID.contains(NETWORK_NAME)){
	        			
	        			Log.i("Network","New printer found! " + s.SSID);
	        			//unregister();
	        			
	        			ModelPrinter m = new ModelPrinter(s.SSID,"/octopi-dev.local",DevicesListController.searchAvailablePosition());
	        			
	        			//Check if network is already on the list
	        			if (!DevicesListController.checkExisting(m)){
	        				mController.addElementController(m);
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
		mContext.registerReceiver(this, mFilter);
		startScan();
	}
	
	public void unregister(){
		mContext.unregisterReceiver(this);
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
