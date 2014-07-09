package android.app.printerapp.library;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import android.app.printerapp.model.ModelFile;
import android.os.Environment;
import android.util.Log;


/**
 * This class will handle the file storage architecture and retrieval in a static way
 * so every class can access these methods from wherever
 * @author alberto-baeza
 *
 */
public class StorageController {
	
	private static ArrayList<File> mFileList = new ArrayList<File>();
	private static String mCurrentPath;
	
	public StorageController(){
		
		mFileList.clear();
		//Retrieve normal files
		retrieveFiles( getParentFolder(), false);
	
	}
	
	public static ArrayList<File> getFileList(){
		return mFileList;
	}
	
	public static void retrieveFiles(File path, boolean recursive){
			
		/**
		 * CHANGED FILE LOGIC, NOW RETRIEVES FOLDERS INSTEAD OF FILES, AND PARSES
		 * INDIVIDUAL ELEMENTS LATER ON.
		 */
		File[] files = path.listFiles();
		for (File file : files){
			
			if (file.isDirectory()){	
				
				if (isProject(file)){
					
					
					Log.i("OUT","It's also a project " + file.getAbsolutePath());
					
					ModelFile m = new ModelFile(file.getAbsolutePath(), "Internal storage");
					
					addToList(m);
					
				} else {
					
					//File folder = new File(file.getAbsolutePath());
					if (recursive) {
						
						//Retrieve files for the folder
						retrieveFiles(new File(file.getAbsolutePath()),true);
						
					} else addToList(file);
					
					
				}
				
				//TODO this will eventually go out
			} else {
				
				//Add only stl and gcode
				if ((file.getName().contains(".gcode")) || (file.getName().contains(".stl"))){
					addToList(file);
				}
				
				
			}

		}
		
		
		mCurrentPath = path.toString();
	}
	
	//TODO change this to database eventually
	public static ArrayList<ModelFile> getFavorites(){
		
		ArrayList<ModelFile> tempList = new ArrayList<ModelFile>();
		File path = new File(getParentFolder() + "/Files");
		File[] files = path.listFiles();
		
		for (File file : files){				
			if (isProject(file))tempList.add(new ModelFile(file.getAbsolutePath(), "Internal storage"));
		}
				
		return tempList;
		
	}
	
	
		
	//Retrieve main folder or create if doesn't exist
	//TODO: Changed main folder to FILES folder.
	public static File getParentFolder(){
		String parentFolder = Environment.getExternalStorageDirectory().toString();
		File mainFolder = new File(parentFolder + "/PrintManager");
		mainFolder.mkdirs();
		File temp_file = new File(mainFolder.toString());

		return temp_file;
	}
	
	public static void createFolder(String name){
			
		File newFolder = new File(mCurrentPath + "/" + name);
		
		if (!newFolder.mkdir()){
			
			Log.i("OUT","Error!");
			
		} else {
			
			addToList(newFolder);
			
			Log.i("OUT","Success!");
		}
			
		
	}
	
	
	//Retrieve certain element from file storage
	public static String retrieveFile(String name, String type){
		
		String result = null;
		
		try {
			File folder = new File(name + "/" + type + "/");
			Log.i("OUT","It's gonna crash searching for " + folder.getAbsolutePath());
			String file = folder.listFiles()[0].getName();
			
			result = folder + "/" + file; 
		
		} catch (ArrayIndexOutOfBoundsException e){
			
		}
		
		
		return result;	
		
	}
	
	public static void reloadFiles(String path){
		
		mFileList.clear();
		
		if (path.equals("all")){
			retrieveFiles(getParentFolder(), true);
			mCurrentPath = getParentFolder().getAbsolutePath();
		} else retrieveFiles(new File(path), false);
		
	}
	
	public static boolean isProject(File file){
		
		FilenameFilter f = new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String filename) {
				
				return filename.endsWith("jpg");
			}
		};
		
		if (file.list(f).length > 0) return true;
		else return false;
	}
	
	public static void addToList(File m ){
		mFileList.add(m);
	}

	public static String getCurrentPath(){
		return mCurrentPath;
	}
}
