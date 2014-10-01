package android.app.printerapp.octoprint;

/**
 * Addresses and static fields for the OctoPrint API connection
 * @author alberto-baeza
 *
 */

public class HttpUtils {
	
	  public static final String CUSTOM_PORT = ":5000"; //Octoprint server listening port
	  public static final String API_KEY = "5A41D8EC149F406F9F222DCF93304B43"; //Hardcoded API Key
	  
	  /** OctoPrint URLs **/
	  
	  public static final String URL_FILES = CUSTOM_PORT + "/api/files"; //File operations
	  public static final String URL_CONTROL = CUSTOM_PORT + "/api/job"; //Job operations
	  public static final String URL_SOCKET = CUSTOM_PORT + "/sockjs/websocket"; //Socket handling
	  public static final String URL_CONNECTION = CUSTOM_PORT + "/api/connection"; //Connection handling
	  public static final String URL_PRINTHEAD = CUSTOM_PORT + "/api/printer/printhead"; //Send print head commands
	  public static final String URL_NETWORK = CUSTOM_PORT + "/api/plugin/netconnectd"; //Network config
	  public static final String URL_SLICING = CUSTOM_PORT + "/api/slicing/cura/profiles/testerino";
	  public static final String URL_SLICING_PROFILES = CUSTOM_PORT + "/api/slicing/cura/profiles";
	  public static final String URL_DOWNLOAD_FILES = CUSTOM_PORT + "/downloads/files/local/";
}
