package android.app.printerapp.devices.database;

import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
	
	 // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Devices.db";
    
    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedEntry.DEVICES_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_ADDRESS + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_POSITION + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_DISPLAY + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_TYPE + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_PROFILE + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.DEVICES_NETWORK + TEXT_TYPE + ")";


    private static final String SQL_CREATE_ENTRIES_HISTORY  =
            "CREATE TABLE " + FeedEntry.TABLE_HISTORY_NAME + " (" +
                    FeedEntry.HISTORY_MODEL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.HISTORY_PATH + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.HISTORY_TIME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.HISTORY_PRINTER + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.HISTORY_DATE + TEXT_TYPE + ")";




    private static final String SQL_DELETE_ENTRIES =
        "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME + COMMA_SEP + FeedEntry.TABLE_HISTORY_NAME;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES_HISTORY);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
	}
	
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
