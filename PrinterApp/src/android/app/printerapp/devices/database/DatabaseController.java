package android.app.printerapp.devices.database;

import java.io.File;
import java.util.Map;
import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.app.printerapp.model.ModelPrinter;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * This class will handle Database interaction on a static way
 * Also will contain the SharedPreferences to handle Favorites
 * @author alberto-baeza
 *
 */
public class DatabaseController {
	
	private static final String PREFERENCES = "Favorites";
	
	static DatabaseHelper mDbHelper;
	static SQLiteDatabase mDb;
	static Context mContext;
	
	public DatabaseController(Context context){
	
		mContext = context;
		mDbHelper = new DatabaseHelper(mContext);
	
	}
	
	//Add a new element to the permanent database
	public static void writeDb(String name, String address, String position){
		
		// Gets the data repository in write mode
		mDb = mDbHelper.getWritableDatabase();
		
		Log.i("OUT","Adding: " + name);
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(FeedEntry.DEVICES_NAME, name);
		values.put(FeedEntry.DEVICES_ADDRESS, address);
		values.put(FeedEntry.DEVICES_POSITION, position);
		
		mDb.insert(FeedEntry.TABLE_NAME, null, values);		
		mDb.close();
		
	}
	
	public static void deleteFromDb(String name){
		
		mDb = mDbHelper.getWritableDatabase();
		
		mDb.delete(FeedEntry.TABLE_NAME, FeedEntry.DEVICES_NAME + " = '" + name + "'", null);
		mDb.close();
		
		//DevicesListController.loadList(mContext);
		
		
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
			
			Log.i("out", "Record exists: " + c.getString(0) + "  " + c.getString(1));
			
			exists = true;
			
		}else {	
			
			exists = false;
			
		}
		
		closeDb();
		
		return exists;
		
	}
	
	//update new position
	public static void updateDB(String tableName, String id, String newValue){
		
		mDb = mDbHelper.getReadableDatabase();
		
		// New value for one column
		ContentValues values = new ContentValues();
		values.put(tableName, newValue);
		
		int count = mDb.update(FeedEntry.TABLE_NAME, values,
				FeedEntry.DEVICES_NAME + " = '" + id + "'", null);
		
		Log.i("OUT", "Updated: " + count + " with " + tableName + " updated with " + newValue + " where " + id);
		
		mDb.close();
		
	}
	
	public static void deleteDB(){
		//TODO Database deletion for testing
		mContext.deleteDatabase("Devices.db");
	}
	
	
	
	
	
	
	/*****************************************************************************************
	 * 					SHARED PREFERENCES HANDLER
	 *****************************************************************************************/
	
	/**
	 * Check if a file is favorite
	 * @param name
	 * @return
	 */
	public static boolean isFavorite(String name){
		
		SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		
		if (prefs.contains(name)) return true;		
		return false;
		
	}
	
	/**
	 * Get the list of favorites to add to the file list
	 * @return
	 */
	public static Map<String,?> getFavorites(){
		SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		return prefs.getAll();
	}
	
	/**
	 * Set/remove as favorite using SharedPreferences, can't repeat names
	 * The type of operation is switched by a boolean
	 */
	public static void handleFavorite(File f, boolean add){
		

		SharedPreferences prefs = mContext.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		
		if (!add) {
			Log.i("OUT","Removing " + f.getName());
			editor.remove(f.getName());
		}
		else {
			Log.i("OUT","Putting favorite " + f.getName());
			editor.putString(f.getName(), f.getAbsolutePath());
		}
			
		editor.commit();
		
	}
	

}
