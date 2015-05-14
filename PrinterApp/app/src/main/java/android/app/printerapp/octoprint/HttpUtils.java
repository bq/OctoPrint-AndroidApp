package android.app.printerapp.octoprint;

import android.app.printerapp.Log;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.devices.discovery.PrintNetworkManager;
import android.app.printerapp.model.ModelPrinter;

/**
 * Addresses and static fields for the OctoPrint API connection
 *
 * @author alberto-baeza
 */

public class HttpUtils {

    public static final String CUSTOM_PORT = ":5000"; //Octoprint server listening port

    /**
     * OctoPrint URLs *
     */

    public static final String URL_FILES = "/api/files"; //File operations
    public static final String URL_CONTROL = "/api/job"; //Job operations
    public static final String URL_SOCKET = "/sockjs/websocket"; //Socket handling
    public static final String URL_CONNECTION = "/api/connection"; //Connection handling
    public static final String URL_PRINTHEAD = "/api/printer/printhead"; //Send print head commands
    public static final String URL_TOOL = "/api/printer/tool"; //Send tool commands
    public static final String URL_BED = "/api/printer/bed"; //Send bed commands
    public static final String URL_NETWORK = "/api/plugin/netconnectd"; //Network config
    public static final String URL_SLICING = "/api/slicing/cura/profiles";
    public static final String URL_DOWNLOAD_FILES = "/downloads/files/local/";
    public static final String URL_SETTINGS = "/api/settings";
    public static final String URL_AUTHENTICATION = "/apps/auth";
    public static final String URL_PROFILES = "/api/printerprofiles";

    /**
     * External links *
     */

    public static final String URL_THINGIVERSE = "http://www.thingiverse.com/newest";
    public static final String URL_YOUMAGINE = "https://www.youmagine.com/designs";

    //Retrieve current API Key from database
    public static String getApiKey(String url) {
        String parsedUrl = url.substring(0, url.indexOf("/", 1));



        String id = null;

        for (ModelPrinter p : DevicesListController.getList()) {


            switch (p.getStatus()){

                case StateUtils.STATE_ADHOC:

                    if (p.getName().equals(PrintNetworkManager.getCurrentNetwork().replace("\"","")))
                        id = PrintNetworkManager.getNetworkId(p.getName());

                    break;

                default:

                    if (p.getAddress().equals(parsedUrl)){
                        id = PrintNetworkManager.getNetworkId(p.getName());

                        if (!DatabaseController.isPreference(DatabaseController.TAG_KEYS, id))
                            id = PrintNetworkManager.getNetworkId(p.getAddress());
                    }



                    break;

            }


        }

        if (DatabaseController.isPreference(DatabaseController.TAG_KEYS, id)) {

            return DatabaseController.getPreference(DatabaseController.TAG_KEYS, id);

        } else {

            Log.i("Connection", id + " is not preference");
            return "";
        }

    }
}
