package android.app.printerapp.devices.discovery;

import android.app.Dialog;
import android.app.printerapp.Log;
import android.app.printerapp.R;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.OctoprintNetwork;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

/**
 * This class will connect to the server AP and select a wifi from the list to connect the server to
 * TODO As it is right now, the server part is unstable and doesn't always creates the AP
 *
 * @author alberto-baeza
 */
public class PrintNetworkManager {

    //This should contain the static generic password to the ad-hoc network
    //TODO hardcoded password
    private static final String PASS = "OctoPrint";

    //Reference to main Controller
    private static DiscoveryController mController;

    //Wifi network manager
    private static WifiManager mManager;

    //Dialog handler
    private static Dialog mDialog;

    //Check if the network is currently being configured
    private boolean isOffline = false;

    private PrintNetworkReceiver mReceiver;

    //position for the current printer being selected
    private int mPosition = -1;
    private ModelPrinter mPrinter = null;

    //original network to configure if an error happens
    private int mOriginalNetwork;


    //Constructor
    public PrintNetworkManager(DiscoveryController context) {

        mController = context;

        //Create a new Network Receiver
        mReceiver = new PrintNetworkReceiver(this);

    }

    /**
     * Method to connect to the AP
     *
     * @param position
     */
    public void setupNetwork(ModelPrinter p, int position) {


        //Get connection parameters
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + p.getName() + "\"";
        conf.preSharedKey = "\"" + PASS + "\"";

        mPrinter = p;
        mPosition = position;

        //Add the new network
        mManager = (WifiManager) mController.getActivity().getSystemService(Context.WIFI_SERVICE);

        mOriginalNetwork = mManager.getConnectionInfo().getNetworkId();

        final int nId = mManager.addNetwork(conf);

        //Configure network
        isOffline = true;


        connectSpecificNetwork(nId);

        createNetworkDialog(getContext().getString(R.string.devices_discovery_connect));

    }


    /**
     * Reverse an array of bytes to get the actual IP address
     *
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
     *
     * @param response list with the networks
     */
    public void selectNetworkPrinter(JSONObject response, final String url) {


        try {

            mReceiver.unregister();

            final JSONArray wifis = response.getJSONArray("wifis");
            final ArrayList<String> wifiList = new ArrayList<String>();
            final ArrayList<String> wifiQualityList = new ArrayList<String>();
            for (int i = 0; i < wifis.length(); i++) {
                wifiList.add(wifis.getJSONObject(i).getString("ssid"));
                wifiQualityList.add(wifis.getJSONObject(i).getString("quality"));

            }

            final DiscoveryWifiNetworksListAdapter networkListDialogAdapter
                    = new DiscoveryWifiNetworksListAdapter(getContext(), wifiList, wifiQualityList);

            //Get the dialog UI
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View selectWifiNetworkDialogView = inflater.inflate(R.layout.dialog_select_wifi_network, null);

            MaterialDialog.Builder adb;
            final Dialog selectNetworkDialog;

            adb = new MaterialDialog.Builder(getContext())
                    .title(R.string.devices_configure_wifi_title)
                    .customView(selectWifiNetworkDialogView, false)
                    .negativeText(R.string.cancel)
                    .negativeColorRes(R.color.body_text_2);

            selectNetworkDialog = adb.build();

            ListView wifiNetworksListView = (ListView) selectWifiNetworkDialogView.findViewById(R.id.wifi_networks_listview);
            wifiNetworksListView.setAdapter(networkListDialogAdapter);
            wifiNetworksListView.setEmptyView(selectWifiNetworkDialogView.findViewById(R.id.wifi_networks_noresults_container));
            wifiNetworksListView.setSelector(R.drawable.selectable_rect_background_green);
            wifiNetworksListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {

                    //Inflate network layout
                    LayoutInflater inflater = (LayoutInflater) mController.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View wifiPasswordDialogView = inflater.inflate(R.layout.dialog_wifi_network_info, null);

                    final EditText wifiPasswordEditText = (EditText) wifiPasswordDialogView.findViewById(R.id.wifi_password_edittext);


                    //Add check box to show/hide the password
                    final CheckBox showPasswordCheckbox = (CheckBox) wifiPasswordDialogView.findViewById(R.id.show_password_checkbox);
                    showPasswordCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            // Use this for store the current cursor mPosition of the edit text
                            int start = wifiPasswordEditText.getSelectionStart();
                            int end = wifiPasswordEditText.getSelectionEnd();

                            if (isChecked) wifiPasswordEditText.setTransformationMethod(null);
                            else
                                wifiPasswordEditText.setTransformationMethod(new PasswordTransformationMethod());

                            wifiPasswordEditText.setSelection(start, end);
                        }
                    });


                    wifiPasswordEditText.requestFocus();

                    try {
                        if (wifis.getJSONObject(position).getBoolean("encrypted")){

                            new MaterialDialog.Builder(getContext())
                                    .title(wifiList.get(position))
                                    .customView(wifiPasswordDialogView, false)
                                    .positiveText(R.string.ok)
                                    .positiveColorRes(R.color.theme_accent_1)
                                    .negativeText(R.string.cancel)
                                    .negativeColorRes(R.color.body_text_2)
                                    .autoDismiss(false)
                                    .callback(new MaterialDialog.ButtonCallback() {
                                        @Override
                                        public void onPositive(MaterialDialog dialog) {
                                            final String ssid = wifiList.get(position).toString();
                                            String psk = wifiPasswordEditText.getText().toString().trim();

                                            if (psk.equals("")) {
                                                wifiPasswordEditText.setError(getContext().getString(R.string.empty_password_error));
                                            } else {
                                                configureSelectedNetwork(ssid, psk, url);
                                                mReceiver.unregister();
                                                dialog.dismiss();
                                                selectNetworkDialog.dismiss();
                                                wifiPasswordEditText.clearFocus();
                                            }
                                        }

                                        @Override
                                        public void onNegative(MaterialDialog dialog) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        } else {

                            configureSelectedNetwork(wifiList.get(position), null, url);
                            mReceiver.unregister();
                            selectNetworkDialog.dismiss();

                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }


                }
            });

            selectNetworkDialog.show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void configureSelectedNetwork(String ssid, String pass, String url) {

        /**
         * SAVE PARAMETERS
         */

        //Target network for both the client and the device

        final WifiConfiguration target = new WifiConfiguration();
        //Set the parameters for the target network
        target.SSID = "\"" + ssid + "\"";

        if (pass!=null) target.preSharedKey = "\"" + pass + "\"";
        else target.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);


        createNetworkDialog(getContext().getString(R.string.devices_discovery_config));

        //Send a network configuration request to the server
        OctoprintNetwork.configureNetwork(mReceiver, getContext(), ssid, pass, url);


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

                if (mManager == null ) mManager = (WifiManager) mController.getActivity().getSystemService(Context.WIFI_SERVICE);
                mManager.disconnect();

                final String origin = getNetworkId(mManager.getConnectionInfo().getSSID());

                mManager.disableNetwork(mManager.getConnectionInfo().getNetworkId());
                mManager.removeNetwork(mManager.getConnectionInfo().getNetworkId());

                //Clear existing networks
                //clearNetwork(target.SSID);
                connectSpecificNetwork(searchNetwork(target));


                DevicesListController.removeElement(mPosition);

                Handler postHandler = new Handler();
                postHandler.postDelayed(new Runnable() {

                    @Override
                    public void run() {

                        Log.i("MANAGER", "Registering again with " + target.SSID + "!");

                        DevicesListController.removeElement(mPosition);


                        DatabaseController.handlePreference(DatabaseController.TAG_NETWORK, "Last", origin, true);

                        //Remove ad-hoc network
                        clearNetwork("OctoPi-Dev");
                        mPosition = -1;
                        mPrinter = null;

                        //mController.notifyAdapter();
                        // mReceiver.register();
                        dismissNetworkDialog();


                    }
                }, 10000);

            }
        }), 5000);


    }

    public void  connectSpecificNetwork(int nId) {

        Log.i("Manager","Enabling " + nId);
        //Disconnect to the current network and reconnect to the new
        mManager.disconnect();
        mManager.enableNetwork(nId, true);
        mManager.reconnect();

    }


    /*********************************************************************
     * DIALOG HANDLING
     ********************************************************************/

    /**
     * Create Network Dialog while connecting to the Printer
     *
     * @param message
     */
    public void createNetworkDialog(String message) {

        //Get progress dialog UI
        View configurePrinterDialogView = LayoutInflater.from(mController.getActivity()).inflate(R.layout.dialog_progress_content_horizontal, null);
        ((TextView) configurePrinterDialogView.findViewById(R.id.progress_dialog_text)).setText(message);

        //Show progress dialog
        final MaterialDialog.Builder configurePrinterDialogBuilder = new MaterialDialog.Builder(mController.getActivity());
        configurePrinterDialogBuilder.title(R.string.devices_discovery_title)
                .customView(configurePrinterDialogView, true)
                .cancelable(false)
                .negativeText(R.string.cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onNegative(MaterialDialog dialog) {
                        super.onNegative(dialog);
                        dialog.setOnDismissListener(null);
                        dialog.dismiss();

                    }
                })
                .autoDismiss(false);
        //Progress dialog to notify command events
        mDialog = configurePrinterDialogBuilder.build();
        mDialog.show();
    }

    /**
     * Called when the Network is established, should open a Dialog with the network list from the server
     * only will be called if there's a Network available (Dialog won't be null)
     */
    public void dismissNetworkDialog() {


        if (isOffline) {
            isOffline = false;
            mDialog.dismiss();
            if (mManager!=null)  {
                byte[] ipAddress = BigInteger.valueOf(mManager.getDhcpInfo().gateway).toByteArray();
                reverse(ipAddress);
            }

            InetAddress myaddr;

               // myaddr = InetAddress.getByAddress(ipAddress);


                //TODO HARDCODED ACCESS POINT
                //String hostaddr = "10.250.250.1";//myaddr.getHostAddress();

                if (mPrinter != null) {
                    OctoprintNetwork.getNetworkList(this, mPrinter);
                } else {

                    mController.waitServiceDialog();

                }

        }


    }

    public void errorNetworkDialog() {

        if (mDialog != null) {

            mDialog.dismiss();
            connectSpecificNetwork(mOriginalNetwork);
            createNetworkDialog(getContext().getString(R.string.dialog_add_printer_timeout));

        }


    }


    /**
     * This method will clear the existing Networks with the same name as the new inserted
     *
     * @param ssid
     */
    private static void clearNetwork(String ssid) {

        List<WifiConfiguration> configs = mManager.getConfiguredNetworks();
        for (WifiConfiguration c : configs) {
            if (c.SSID.contains(ssid)) {
                mManager.removeNetwork(c.networkId);
            }
        }
    }

    private static int searchNetwork(WifiConfiguration ssid) {

        List<WifiConfiguration> configs = mManager.getConfiguredNetworks();
        for (WifiConfiguration c : configs) {
            if (c.SSID.equals(ssid.SSID)) {
                return c.networkId;
            }
        }

        return mManager.addNetwork(ssid);
    }


    /**
     * Add a new Printer calling to the Controller
     *
     * @param p
     */
    public void addElementController(ModelPrinter p) {

        mController.addElement(p);
        //p.setNotConfigured();

    }

    /**
     * Get Device context
     *
     * @return
     */
    public Context getContext() {
        return mController.getActivity();
    }


    /**
     * EXCLUSIVE TO THIS IMPLEMENTATION
     * <p/>
     * Parse the network ID to search in the preference list
     *
     * @param s ssid to get the number
     * @return the parsed number
     */
    public static String getNetworkId(String s) {

        String ssid = s.replaceAll("[^A-Za-z0-9]", "");

        if (ssid.length() >= 4) return ssid.substring(ssid.length() - 4, (ssid.length()));
        else return "0000";

    }


    /**
     * Check if the network was on the preference list to link it to the service
     *
     * @param ssid
     * @param result
     */
    public static boolean checkNetworkId(String ssid, boolean result) {

        Log.i("Discovery", "Checking ID");

        final int message;
        boolean exists = false;

        if (result) message = R.string.devices_discovery_toast_success;
        else message = R.string.devices_discovery_toast_error;

        /**
         * Check for pending networks in the preference list
         */
        if (DatabaseController.isPreference(DatabaseController.TAG_NETWORK, "Last")) {

            if (DatabaseController.getPreference(DatabaseController.TAG_NETWORK, "Last").equals(getNetworkId(ssid))) {

                exists = true;

                DatabaseController.handlePreference(DatabaseController.TAG_NETWORK, "Last", null, false);

                //Toast.makeText(mController.getActivity(), message, Toast.LENGTH_LONG).show();


            }
        }

        return exists;
    }

    public static String getCurrentNetwork() {

        WifiManager manager = (WifiManager) mController.getActivity().getSystemService(Context.WIFI_SERVICE);

        return manager.getConnectionInfo().getSSID();

    }


    public void reloadNetworks() {

        mReceiver.unregister();
        mReceiver.register();

    }
}
