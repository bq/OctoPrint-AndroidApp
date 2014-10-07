package android.app.printerapp.devices.discovery;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.printerapp.ItemListActivity;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintNetwork;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;

/**
 * This class will connect to the server AP and select a wifi from the list to connect the server to
 * TODO As it is right now, the server part is unstable and doesn't always creates the AP
 * @author alberto-baeza
 *
 */
public class PrintNetworkManager {
	
		//This should contain the static generic password to the ad-hoc network
		//TODO hardcoded password
		private static final String PASS = "OctoPrint";
		
		//Reference to main Controller
		private static DevicesFragment mController;
		
		//Wifi network manager
		private static WifiManager mManager;
		
		//Dialog handler
		private static ProgressDialog mDialog;
		
		//Check if the network is currently being configured
		private boolean isOffline = false;
		
		private PrintNetworkReceiver mReceiver;
		
		//TODO TENTATIVE CHANGES!!!!!!1111!!ONEONE
		
		//position for the current printer being selected
		private int mPosition = -1;
				
		
		//Constructor
		public PrintNetworkManager(DevicesFragment context){
			
			mController = context;
			//Create a new Network Receiver
			mReceiver = new PrintNetworkReceiver(this);
			
			
			
		}
		
		/**
		 * Method to connect to the AP
		 * @param context
		 * @param ssid
		 * @param position
		 */
		public void setupNetwork(final DevicesFragment context, final String ssid, int position){
			
			//Get connection parameters
			WifiConfiguration conf = new WifiConfiguration();
			conf.SSID = "\"" + ssid + "\"";  
			conf.preSharedKey = "\""+ PASS +"\"";
			
			mPosition = position;
			
			//Add the new network
			mManager = (WifiManager)context.getActivity().getSystemService(Context.WIFI_SERVICE); 
			final int nId = mManager.addNetwork(conf);	
					
			//Configure network
			isOffline = true;
			
			
			//Disconnect to the current network and reconnect to the new
	         mManager.disconnect();
	         mManager.enableNetwork(nId, true);
	         mManager.reconnect();
	         
	         /****************************NOPENOPENOPENOPENOPE*****************************/
	         
	        /*
	         Log.i("OUT","UNREGISTER COJONES");
	     	mReceiver.unregister();
	       //Remove ad-hoc network
	     	 Log.i("OUT","REMOVE " + mPosition + " COJONES");
	     	DevicesListController.getList().remove(mPosition);
			mPosition = -1;
			ItemListActivity.notifyAdapters();
			
			 Log.i("OUT","CLEAN COJONES");
				clearNetwork("OctoPi-Dev");

				*/
				
			/******************************************************************************/
	         
	         //TODO hardcoded dialog
	         createNetworkDialog(getContext().getString(R.string.devices_discovery_connect));
    
		}
		
		
		/**
		 * Reverse an array of bytes to get the actual IP address
		 * @param array
		 */
		 public static void reverse(byte[] array) {
		      if (array == null) {
		          return;
		      }
		      int i = 0;
		      int j = array.length - 1;
		      byte tmp;
		      while (j > i) {
		          tmp = array[j];
		          array[j] = array[i];
		          array[i] = tmp;
		          j--;
		          i++;
		      }
		  }
		 
		 
		 
		 /*******************************************************************
		  * NETWORK HANDLING
		  *******************************************************************/
		 
		 /**
		  * Get the network list from the server and select one to connect
		  * @param response list with the networks
		  */
		 public void selectNetworkPrinter(JSONObject response, final String url){
			 
		 
			 try {
					JSONArray wifis = response.getJSONArray("wifis");
					
					final ArrayAdapter<String> networkList = new ArrayAdapter<String>(getContext(), android.R.layout.select_dialog_singlechoice);
					for (int i = 0 ; i < wifis.length(); i++){
						
						networkList.add(wifis.getJSONObject(i).getString("ssid"));
						
					}
										
					//Custom Dialog to insert network parameters.
			         AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
			         adb.setTitle("Configuring Network " + mManager.getConnectionInfo().getSSID());
			                 
			         //Get an adapter with the Network list retrieved from the main controller
			         adb.setAdapter(networkList, new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {    
							
							//Inflate network layout
							
							AlertDialog.Builder adb_net = new AlertDialog.Builder(getContext());
							LayoutInflater inflater = mController.getActivity().getLayoutInflater();
							View v = inflater.inflate(R.layout.alertdialog_network, null);
							
							final EditText et_ssid = (EditText)v.findViewById(R.id.adb_et1);
					        final EditText et_pass = (EditText)v.findViewById(R.id.adb_et2);
					        
					        et_ssid.setText(networkList.getItem(which));
					        
					        adb_net.setView(v);
					        adb_net.setPositiveButton(R.string.ok, new OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
									final String ssid = et_ssid.getText().toString();
									String psk = et_pass.getText().toString();
																								
									
									/**
									 * SAVE PARAMETERS
									 */
									
									//Target network for both the client and the device
									
									final WifiConfiguration target = new WifiConfiguration();
									//Set the parameters for the target network
									target.SSID = "\"" + ssid + "\"";
									target.preSharedKey = "\"" + psk + "\"";
									
									createNetworkDialog(getContext().getString(R.string.devices_discovery_config));
									
								
									//Send a network configuration request to the server
									OctoprintNetwork.configureNetwork(mReceiver, getContext(), ssid, psk, url);
									
									
									mReceiver.unregister();
									
									//From this point on we need a delay to the configuration to ensure a clear connection
								
									/**
									 * TODO Need a handler for this?
									 * Remove AP from the network list and connect to the Target network
									 */
									Handler handler = new Handler();
							        handler.postDelayed((new Runnable() {
										
										@Override
										public void run() {
											
											//Configuring network
											isOffline = true;
											
								         	mManager.disconnect();
											mManager.disableNetwork(mManager.getConnectionInfo().getNetworkId());
											mManager.removeNetwork(mManager.getConnectionInfo().getNetworkId());
											
											//Clear existing networks TODO: search for existing instead
											//clearNetwork(target.SSID);
											mManager.enableNetwork(searchNetwork(target), true);
											
											
											
											Handler postHandler = new Handler();
											postHandler.postDelayed(new Runnable() {
												
												@Override
												public void run() {
												
													Log.i("MANAGER","Registering again with " + target.SSID + "!");
																								

													DevicesListController.getList().remove(mPosition);
                                                    //Remove ad-hoc network
                                                    clearNetwork("OctoPi-Dev");
													mPosition = -1;

                                                    ItemListActivity.notifyAdapters();
                                                   // mReceiver.register();
													dismissNetworkDialog();
													
												}
											}, 10000);	
											
										}
									}), 5000);
								
								}
					        });
					        
					        adb_net.setNegativeButton(R.string.cancel, null);
					        adb_net.show();
						}
					});

			         adb.show(); 
			         
			 } catch (JSONException e) {
					e.printStackTrace();
				}
		 }
		 
		 
		 /*********************************************************************
		  * DIALOG HANDLING
		  ********************************************************************/
		 
		 /**
		  * Create Network Dialog while connecting to the Printer
		  * @param message
		  */
		 public void createNetworkDialog(String message){
				
				//Configure Progress Dialog
				mDialog = new ProgressDialog(mController.getActivity());
				mDialog.setIcon(android.R.drawable.ic_dialog_alert);
				mDialog.setTitle(getContext().getString(R.string.devices_discovery_title));
				mDialog.setMessage(message);
				mDialog.show();
			}
			
		 /**
		  * Called when the Network is established, should open a Dialog with the network list from the server
		  *	only will be called if there's a Network available (Dialog won't be null)
		  *
		  */
			public void dismissNetworkDialog(){
				
				if (isOffline)	{
					isOffline = false;
					mDialog.dismiss();
					byte[] ipAddress = BigInteger.valueOf(mManager.getDhcpInfo().gateway).toByteArray();
					reverse(ipAddress);
					InetAddress myaddr;
					try {
						myaddr = InetAddress.getByAddress(ipAddress);
						String hostaddr = myaddr.getHostAddress(); 
						Log.i("OUT","Numerito_ " +hostaddr);
					
					
					OctoprintNetwork.getNetworkList(this, "/" + hostaddr);
					
					} catch (UnknownHostException e) {
						e.printStackTrace();
					}
				}
				
				
				
			}
						
			/**
			 * This method will clear the existing Networks with the same name as the new inserted
			 * @param ssid
			 */
			private static void clearNetwork(String ssid){
					
					List<WifiConfiguration> configs = mManager.getConfiguredNetworks();
			        for (WifiConfiguration c: configs){
			        	Log.i("NETWORK", c.SSID + " is clearly not " + ssid);
			        	if (c.SSID.contains(ssid)){
			        		Log.i("out","Removed");
			        		mManager.removeNetwork(c.networkId);
		        		}
			        }
				}
			
			private static int searchNetwork(WifiConfiguration ssid){
				
				List<WifiConfiguration> configs = mManager.getConfiguredNetworks();
		        for (WifiConfiguration c: configs){
		        	if (c.SSID.equals(ssid.SSID)){
		        		
		        		Log.i("Network","No need to add a new network bitch");
		        		return c.networkId;
		        		
		    		}
		        }
		        
		        return mManager.addNetwork(ssid);
			}
			
			
		
	/**
	 * Add a new Printer calling to the Controller
	 * @param p
	 */
	public void addElementController(ModelPrinter p){
		
		mController.addElement(p);
		p.setNotConfigured();
		
	}
	
	/**
	 * Get Device context
	 * @return
	 */
	public Context getContext(){
		return mController.getActivity();
	}


}
