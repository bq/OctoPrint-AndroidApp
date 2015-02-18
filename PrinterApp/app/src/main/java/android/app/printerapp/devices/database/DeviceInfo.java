package android.app.printerapp.devices.database;

import android.provider.BaseColumns;

public final class DeviceInfo {
	
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public DeviceInfo() {}

    /* Inner class that defines the table contents */
    public static abstract class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "Devices";
        public static final String DEVICES_NAME = "name";
        public static final String DEVICES_ADDRESS = "address";
        public static final String DEVICES_POSITION = "position";
        public static final String DEVICES_DISPLAY = "display";
        public static final String DEVICES_TYPE = "type";
        public static final String DEVICES_PROFILE = "profile";
        public static final String DEVICES_NETWORK = "network";

        public static final String TABLE_HISTORY_NAME = "History";
        public static final String HISTORY_MODEL = "model";
        public static final String HISTORY_PATH = "path";
        public static final String HISTORY_TIME = "printTime";
        public static final String HISTORY_PRINTER = "printer";
        public static final String HISTORY_DATE = "date";





    }

}
