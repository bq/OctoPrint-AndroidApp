package android.app.printerapp.devices.database;

import android.app.printerapp.Log;
import android.app.printerapp.MainActivity;
import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.app.printerapp.model.ModelPrinter;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Map;


/**
 * This class will handle Database interaction on a static way
 * Also will contain the SharedPreferences to handle Favorites
 * @author alberto-baeza
 *
 */
public class DatabaseController {

    public static final String TAG_NETWORK = "Network";
    public static final String TAG_REFERENCES = "References";
    public static final String TAG_FAVORITES = "Favorites";
    public static final String TAG_KEYS = "Keys";
    public static final String TAG_SLICING = "Slicing";
    public static final String TAG_PROFILE = "ProfilePreferences";
    public static final String TAG_RESTORE = "Restore";
	
	static DatabaseHelper mDbHelper;
	static SQLiteDatabase mDb;
	static Context mContext;
	
	public DatabaseController(Context context){
	
		mContext = context;
		mDbHelper = new DatabaseHelper(mContext);
	
	}
	
	//Add a new element to the permanent database
	public static long writeDb(String name, String address, String position, String type, String network){
		
		// Gets the data repository in write mode
		mDb = mDbHelper.getWritableDatabase();
		
		Log.i("OUT", "Adding: " + name);
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(FeedEntry.DEVICES_NAME, name);
		values.put(FeedEntry.DEVICES_ADDRESS, address);
		values.put(FeedEntry.DEVICES_POSITION, position);
		values.put(FeedEntry.DEVICES_DISPLAY, name);
        values.put(FeedEntry.DEVICES_TYPE, type);
        values.put(FeedEntry.DEVICES_NETWORK, network);
		
		long id = mDb.insert(FeedEntry.TABLE_NAME, null, values);
		mDb.close();

        MainActivity.refreshDevicesCount();

        return id;
		
	}
	
	public static void deleteFromDb(long id){
		
		mDb = mDbHelper.getWritableDatabase();
		
		mDb.delete(FeedEntry.TABLE_NAME, FeedEntry._ID + " = '" + id + "'", null);
		mDb.close();
		
		
	}
	
	//Retrieve the cursor with the elements from the database
	public static Cursor retrieveDeviceList(){
		
		// Gets the data repository in read mode
		mDb = mDbHelper.getReadableDatabase();
		
		String selectQuery ="SELECT * FROM " + FeedEntry.TABLE_NAME;
		
		Cursor c = mDb.rawQuery(selectQuery, null);
	
		return c;			
	}
	
	//Close database statically
	public static void closeDb(){
		
		
		if (mDb.isOpen())mDb.close();
		
	}
	
	//Check if a printer exists to avoid multiple insertions
	public static boolean checkExisting(ModelPrinter m){
		
		boolean exists = true;
		
		// Gets the data repository in read mode
		mDb = mDbHelper.getReadableDatabase();
		
		String selectQuery ="SELECT * FROM " + FeedEntry.TABLE_NAME + " WHERE " + FeedEntry.DEVICES_NAME + " = '" + m.getName() + "' AND " +
				FeedEntry.DEVICES_ADDRESS + " = '" + m.getAddress() + "'";
		
		Cursor c = mDb.rawQuery(selectQuery, null);

		if (c.moveToFirst()){
			
			exists = true;
			
		}else {	
			
			exists = false;
			
		}
		
		closeDb();
		
		return exists;
		
	}
	
	//update new position
	public static void updateDB(String tableName, long id, String newValue){
		
		mDb = mDbHelper.getReadableDatabase();
		
		// New value for one column
		ContentValues values = new ContentValues();
		values.put(tableName, newValue);
		
		int count = mDb.update(FeedEntry.TABLE_NAME, values,
				FeedEntry._ID + " = '" + id + "'", null);
		
		Log.i("OUT", "Updated: " + count + " with " + tableName + " updated with " + newValue + " where " + id);
		
		mDb.close();
		
	}
	
	public static void deleteDB(){
		//TODO Database deletion for testing
		mContext.deleteDatabase("Devices.db");
	}

    public static int count(){

        Cursor c = retrieveDeviceList();
        int count = c.getCount();
        closeDb();
        return count;
    }






	/*****************************************************************************************
	 * 					SHARED PREFERENCES HANDLER
	 *****************************************************************************************/

	/**
	 * Check if a file is favorite
	 * @return
	 */
	public static boolean isPreference(String where, String key){

		SharedPreferences prefs = mContext.getSharedPreferences(where, Context.MODE_PRIVATE);

        /*
        Map<String,?> keys = prefs.getAll();

        for(Map.Entry<String,?> entry : keys.entrySet()){
            Log.i("map values",entry.getKey() + ": " +
                    entry.getValue().toString());
        }*/
		
		if (prefs.contains(key)) return true;		
		return false;
		
	}
	
	/**
	 * Get the list of favorites to add to the file list
	 * @return
	 */
	public static Map<String,?> getPreferences(String where){
		SharedPreferences prefs = mContext.getSharedPreferences(where, Context.MODE_PRIVATE);
		return prefs.getAll();
	}
	
	/**
	 * Get a single item from the list
	 * @param where 
	 * @param key
	 * @return
	 */
	public static String getPreference(String where, String key){
		
		SharedPreferences prefs = mContext.getSharedPreferences(where, Context.MODE_PRIVATE);
		return prefs.getString(key, null);
	}
	
	/**
	 * Set/remove as favorite using SharedPreferences, can't repeat names
	 * The type of operation is switched by a boolean
	 */
	public static void handlePreference(String where, String key, String value, boolean add){
		

		SharedPreferences prefs = mContext.getSharedPreferences(where, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		
		if (!add) {
			Log.i("OUT", "Removing " + key);
			editor.remove(key);
		}
		else {
			Log.i("OUT", "Putting favorite " + key);
			editor.putString(key, value);
		}
			
		editor.commit();
		
	}
	
	
	/***********************************************  HISTORY TABLE  ******************************************************/

	public static void writeDBHistory(String name, String path, String time, String type, String date){

        // Gets the data repository in write mode
        mDb = mDbHelper.getWritableDatabase();

        Log.i("OUT", "Adding: " + name);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.HISTORY_MODEL, name);
        values.put(FeedEntry.HISTORY_PATH, path);
        values.put(FeedEntry.HISTORY_TIME, time);
        values.put(FeedEntry.HISTORY_PRINTER, type);
        values.put(FeedEntry.HISTORY_DATE, date);

        mDb.insert(FeedEntry.TABLE_HISTORY_NAME, null, values);
        mDb.close();

    }

    //Retrieve the cursor with the elements from the database
    public static Cursor retrieveHistory(){

        // Gets the data repository in read mode
        mDb = mDbHelper.getReadableDatabase();

        String selectQuery ="SELECT * FROM " + FeedEntry.TABLE_HISTORY_NAME;

        Cursor c = mDb.rawQuery(selectQuery, null);

        return c;
    }

    public static void updateHistoryPath(String oldPath, String newPath){

        mDb = mDbHelper.getReadableDatabase();

        // New value for one column
        ContentValues values = new ContentValues();
        values.put(FeedEntry.HISTORY_PATH, newPath);

        mDb.update(FeedEntry.TABLE_HISTORY_NAME, values,
                FeedEntry.HISTORY_PATH + " = '" + oldPath + "'", null);


        mDb.close();

    }

    public static void removeFromHistory(String path){
        mDb = mDbHelper.getWritableDatabase();
        mDb.delete(FeedEntry.TABLE_HISTORY_NAME, FeedEntry.HISTORY_PATH + " = '" + path + "'", null);
        mDb.close();

    }
		

}
