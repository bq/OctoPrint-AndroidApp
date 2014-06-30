package android.app.printerapp.devices.database;

import android.app.printerapp.devices.database.DeviceInfo.FeedEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;


/**
 * This class will handle Database interaction on a static way
 * @author alberto-baeza
 *
 */
public class DatabaseController {
	
	static DatabaseHelper mDbHelper;
	static SQLiteDatabase mDb;
	
	public DatabaseController(Context context){
		context.deleteDatabase("Devices.db");
		mDbHelper = new DatabaseHelper(context);
	
	}
	
	public static void writeDb(String name, String address){
		
		// Gets the data repository in write mode
		mDb = mDbHelper.getWritableDatabase();

		// Create a new map of values, where column names are the keys
		ContentValues values = new ContentValues();
		values.put(FeedEntry.DEVICES_NAME, name);
		values.put(FeedEntry.DEVICES_ADDRESS, address);
		
		mDb.insert(FeedEntry.TABLE_NAME, null, values);
		
		mDb.close();
		
	}
	
	public static Cursor retrieveDeviceList(){
		
		// Gets the data repository in read mode
		mDb = mDbHelper.getReadableDatabase();
		
		String selectQuery ="SELECT * FROM " + FeedEntry.TABLE_NAME;
		
		Cursor c = mDb.rawQuery(selectQuery, null);
	
		return c;			
	}
	
	public static void closeDb(){
		
		
		if (mDb.isOpen())mDb.close();
		
	}
	

}
