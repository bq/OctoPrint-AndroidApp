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
	  
	  public static final String URL_FILES = "/api/files"; //File operations
	  public static final String URL_CONTROL = "/api/job"; //Job operations
	  public static final String URL_SOCKET = "/sockjs/websocket"; //Socket handling
	  public static final String URL_CONNECTION = "/api/connection"; //Connection handling
	  public static final String URL_PRINTHEAD = "/api/printer/printhead"; //Send print head commands
	  public static final String URL_NETWORK = "/api/plugin/netconnectd"; //Network config
	  public static final String URL_SLICING = "/api/slicing/cura/profiles";
	  public static final String URL_DOWNLOAD_FILES = "/downloads/files/local/";

    /** External links **/

      public static final String URL_THINGIVERSE = "http://www.thingiverse.com/newest";
}
