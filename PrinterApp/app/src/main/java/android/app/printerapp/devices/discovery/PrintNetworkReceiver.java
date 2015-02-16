package android.app.printerapp.devices.discovery;

import android.app.printerapp.Log;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
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
    private static final int MAX_RETRIES = 5;

	private static WifiManager mWifiManager;
	private PrintNetworkManager mController;
	private Context mContext;
	private IntentFilter mFilter;
	private ConnectivityManager cm;
	private static  ArrayAdapter<String> mNetworkList;

    private int mRetries = MAX_RETRIES;



	
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
	 */
	@Override
	public void onReceive(Context context, Intent intent) {
		
		
		/**
		 * New method to check an established connection with a network (Reliable)
		 */
		if (intent.getAction() == ConnectivityManager.CONNECTIVITY_ACTION){
			NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
			
			 if (activeNetwork!=null){
				 
				 Log.i("NETWORK", "Network connectivity change " + activeNetwork.getState());
				 mController.dismissNetworkDialog();
				 
			 }
		}

        if (intent.getAction() == WifiManager.SUPPLICANT_STATE_CHANGED_ACTION){

            SupplicantState state = (SupplicantState) intent.getParcelableExtra(WifiManager.EXTRA_NEW_STATE);
            Log.i("NETWORK", state.toString());

            switch(state){

                case DISCONNECTED:

                    mRetries--;

                    Log.i("NETWORK", "Retries " + mRetries);

                    if (mRetries == 0) {
                        mController.errorNetworkDialog();
                        mRetries = MAX_RETRIES;
                    }

                break;

                case COMPLETED:

                    mRetries = MAX_RETRIES;

                break;

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
	        			
	        			Log.i("Network", "New printer found! " + s.SSID);
	        			//unregister();
	        			
	        			ModelPrinter m = new ModelPrinter(s.SSID,"/10.250.250.1", StateUtils.STATE_ADHOC);


                        //mController.checkNetworkId(s.SSID,false);
	        			mController.addElementController(m);

	       			 
	        			
	        		}
	        	}
	        	
	        }

	}

	//Start scanning for Networks.
	public void startScan(){

        mRetries = MAX_RETRIES;
		mWifiManager.startScan();
        mWifiManager.getScanResults();
		
	}
	
	public void register(){
		mContext.registerReceiver(this, mFilter);
		startScan();

	}
	
	public void unregister(){
        try{
            mContext.unregisterReceiver(this);

        } catch (IllegalArgumentException e){


        }

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
			} else {//Log.i("OUT","Duplicate network " + s.SSID);
			 }
		}

		
		for (String s : a){
			mNetworkList.add(s);
			
		}
		
		
	}
	
	public static ArrayAdapter<String> getNetworkList(){
		return mNetworkList;
	}


	
}
