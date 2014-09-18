package android.app.printerapp.devices.discovery;

import java.util.List;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.ActionBar.LayoutParams;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesFragment;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.HttpClientHandler;
import android.app.printerapp.octoprint.StateUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * This class will handle ad-hoc network connection and post-configuration
 * @author alberto-baeza
 *
 */
public class PrintNetworkManager {
	

	
	
	//This should contain the static generic password to the ad-hoc network
	private static final String PASS = "raspberry";
	
	//Parent Network to connect if errors are found
	//private static int parent;

	
	//Target network for both the client and the device
	private static WifiConfiguration target;
	
	private static DevicesFragment mController;
	
	//Wifi network manager
	private static WifiManager mManager;
	
	private static ProgressDialog mDialog;
	
	private static PrintNetworkReceiver mReceiver;
	
	public PrintNetworkManager(DevicesFragment context){
		
		mController = context;
		mReceiver = new PrintNetworkReceiver(context);
		
	}
		
	public void setupNetwork(final DevicesFragment context, final String ssid, final ModelPrinter p){

		
		
		//Get connection parameters
		WifiConfiguration conf = new WifiConfiguration();
		conf.SSID = "\"" + ssid + "\"";  
		conf.preSharedKey = "\""+ PASS +"\"";
		
		//Add the new network
		mManager = (WifiManager)context.getActivity().getSystemService(Context.WIFI_SERVICE); 
		final int nId = mManager.addNetwork(conf);
		
		//Store the parent for future reference
		//parent = mManager.getConnectionInfo().getNetworkId();
		
		
		//Create new wifi configuration for target network
		target = new WifiConfiguration();

		    	
    	//When found, disconnect to the current network and reconnect to the new
         mManager.disconnect();
         mManager.enableNetwork(nId, true);
         mManager.reconnect();
         
         //Custom Dialog to insert network parameters.
         AlertDialog.Builder adb = new AlertDialog.Builder(context.getActivity());
         adb.setTitle("Configure " + ssid);
         
         //Get an adapter with the Network list retrieved from the main controller
         adb.setAdapter(PrintNetworkReceiver.getNetworkList(), new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				
				AlertDialog.Builder adb_net = new AlertDialog.Builder(context.getActivity());
				
				 LinearLayout ll_dialog = new LinearLayout(context.getActivity());
		         
		         ll_dialog.setOrientation(LinearLayout.VERTICAL);
		         
		         final EditText et_ssid = new EditText(context.getActivity());
		         final EditText et_pass = new EditText(context.getActivity());
		         
		         et_ssid.setWidth(LayoutParams.MATCH_PARENT);
		         et_pass.setWidth(LayoutParams.MATCH_PARENT);
		         et_pass.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
		         et_pass.setTransformationMethod(PasswordTransformationMethod.getInstance());
		         
		         TextView tv_ssid = new TextView(context.getActivity());
		         TextView tv_pass = new TextView(context.getActivity());
		         
		         tv_ssid.setText("SSID:");
		         tv_pass.setText("Password:");

		         //TODO: Hardcoded parameters for testing
		         et_ssid.setText(PrintNetworkReceiver.getNetworkList().getItem(which));
		         //et_pass.setText("P3dr0y3ll0b0!");
		         
		         ll_dialog.addView(tv_ssid);
		         ll_dialog.addView(et_ssid);
		         ll_dialog.addView(tv_pass);
		         ll_dialog.addView(et_pass);
		         
		         adb_net.setView(ll_dialog);
		         adb_net.setPositiveButton("Ok", new OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
													
							//Post request to call the network configuration script on the ad-hoc Network.
							RequestParams params = new RequestParams();
							params.put("ssid", et_ssid.getText().toString());
							params.put("pass", et_pass.getText().toString());
							
							
							/**
							 * SAVE PARAMETERS
							 */
							
							//Set the parameters for the target network
							target.SSID = "\"" + et_ssid.getText().toString() + "\"";
							target.preSharedKey = "\"" + et_pass.getText().toString() + "\"";
							
							createNetworkDialog("Configuring " + ssid + " network, this may take a while. \n\n" +
									"New device discovery is currently down until the configuration finishes.");
							
							
							
							/**
							 * SEND NETWORK CONFIGURATION REQUEST
							 */
							//TODO: Hardcoded IP but should work on most cases.
							HttpClientHandler.post("/10.0.0.1/network.php", params, new JsonHttpResponseHandler(){
								
								@Override
								public void onFinish() {
									super.onFinish();
									
									//Disconnect from the current ad-hoc network and reconnect to previous one.
									Log.i("OUT","TIMEOUTEDDDDDD");
								}
							});
						
							//We're not removing anymore but updating the same service sharing an ID.
							//p.setLinked();
						
							
							/**
							 * RECONNECT TO TARGET NETWORK
							 * TODO: Wait 15s to ensure a clear connection.
							 */
							
							mReceiver.unregister();
																
							Handler handler = new Handler();
					        handler.postDelayed((new Runnable() {
								
								@Override
								public void run() {
									

									Log.i("NETWORK","DISCONNECTING");
						         	mManager.disconnect();
									mManager.disableNetwork(nId);
									mManager.removeNetwork(nId);
									
									//Clear existing networks TODO: search for existing instead
									//clearNetwork(target.SSID);
									mManager.enableNetwork(searchNetwork(target), true);
									
									
									
									Handler postHandler = new Handler();
									postHandler.postDelayed(new Runnable() {
										
										@Override
										public void run() {
										
											Log.i("MANAGER","Registering again with " + target.SSID + "!");
																						
											//Remove ad-hoc network
											clearNetwork(ssid);							        
											
											postCheck(p);		
											dismissNetworkDialog();
											
										}
									}, 10000);	
									
								}
							}), 5000);
													
						}
			         });
			         adb_net.setNegativeButton("Cancel", null);
			         
			         adb_net.show();
			       
			         
			         
				
			}
		});
        
//if (!mController.getNetworkType())
         adb.show();        
         createNetworkDialog("WARNING!!! Connecting to printer network.");
	}
	
	
	private static void postCheck(ModelPrinter p){
		
		if (p.getStatus()==StateUtils.STATE_NEW){
			Log.i("out","Service found");
		} else {

			mReceiver.register();
			//mController.jmdnsreset();
			Log.i("out","Next try");
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
        
       Log.i("Network","Added a new network " + ssid);
        
        return mManager.addNetwork(ssid);
	}	
	
	public void destroy(){
		mReceiver.unregister();
	}
	
	
	/***********************************************
	 * 		DIALOG HANDLER
	 **********************************************/
	
	public static void createNetworkDialog(String message){
		
		//Configure Progress Dialog
		mDialog = new ProgressDialog(mController.getActivity());
		mDialog.setIcon(R.drawable.error_icon);
		mDialog.setTitle("Wait...");
		mDialog.setMessage(message);
		mDialog.show();
	}
	
	public static void dismissNetworkDialog(){
		
		if (mDialog!=null)
		mDialog.dismiss();
		}
	
	public DevicesFragment getController(){
		return mController;
	}

}
