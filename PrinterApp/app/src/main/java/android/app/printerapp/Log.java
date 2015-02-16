package android.app.printerapp;


/**
 * Created by alberto-baeza on 2/13/15.
 */
public class Log {

    private static boolean isLogsEnabled = true;

        public static void i(String logTag, String logString) {

            if (isLogsEnabled) {
                android.util.Log.i(logTag, logString);
            }
        }

        public static void v(String logTag, String logString) {

            if (isLogsEnabled) {
                android.util.Log.v(logTag, logString);
            }
        }

    public static void e(String logTag, String logString) {

        if (isLogsEnabled) {
            android.util.Log.e(logTag, logString);
        }
    }

    public static void d(String logTag, String logString) {

        if (isLogsEnabled) {
            android.util.Log.d(logTag, logString);
        }
    }

    public static void w(String logTag, String logString) {

        if (isLogsEnabled) {
            android.util.Log.w(logTag, logString);
        }
    }


}
