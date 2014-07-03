package android.app.printerapp.devices.database;

import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.app.printerapp.model.ModelPrinter;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


/**
 * This class will handle Database interaction on a static way
 * @author alberto-baeza
 *
 */
public class DatabaseController {
	
	static DatabaseHelper mDbHelper;
	static SQLiteDatabase mDb;
	static Context mContext;
	
	public DatabaseController(Context context){
	
		mContext = context;
		mDbHelper = new DatabaseHelper(mContext);
	
	}
	
	//Add a new element to the permanent database
	public static void writeDb(String name, String address){
		
		// Gets the data repository in write mode
		mDb = mDbHelper.getWritableDatabase();
		
		Log.i("OUT","Adding: " + name);
		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(FeedEntry.DEVICES_NAME, name);
		values.put(FeedEntry.DEVICES_ADDRESS, address);
		
		mDb.insert(FeedEntry.TABLE_NAME, null, values);		
		mDb.close();
		
	}
	
	public static void deleteFromDb(String name, String address){
		
		mDb = mDbHelper.getWritableDatabase();
		
		mDb.delete(FeedEntry.TABLE_NAME, FeedEntry.DEVICES_NAME + " = '" + name + "'", null);
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
			
			Log.i("out", "Record exists: " + c.getString(0) + "  " + c.getString(1));
			
			exists = true;
			
		}else {	
			
			exists = false;
			
		}
		
		closeDb();
		
		return exists;
		
	}
	
	public static void deleteDB(){
		//TODO Database deletion for testing
		mContext.deleteDatabase("Devices.db");
	}
	

}
