package android.app.printerapp.library;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.os.Environment;


/**
 * This class will handle the file storage architecture and retrieval in a static way
 * so every class can access these methods from wherever
 * @author alberto-baeza
 *
 */
public class StorageController {
	
	private static ArrayList<File> mFileList = new ArrayList<File>();
	private static File mCurrentPath;
	
	public StorageController(){
		
		mFileList.clear();
		//Retrieve normal files
		retrieveFiles( getParentFolder(), false);
	
	}
	
	public static ArrayList<File> getFileList(){
		return mFileList;
	}
	
	/**
	 * Retrieve files from the provided path
	 * If it's recursive, also search inside folders
	 * @param path
	 * @param recursive
	 */
	public static void retrieveFiles(File path, boolean recursive){
			
		/**
		 * CHANGED FILE LOGIC, NOW RETRIEVES FOLDERS INSTEAD OF FILES, AND PARSES
		 * INDIVIDUAL ELEMENTS LATER ON.
		 */
		File[] files = path.listFiles();
		for (File file : files){
			
			//If folder
			if (file.isDirectory()){	
				
				//If project
				if (isProject(file)){
					
					//Create new project
					ModelFile m = new ModelFile(file.getAbsolutePath(), "Internal storage");
					
					addToList(m);
					
				//Normal folder
				} else {
					
					if (recursive) {
						
						//Retrieve files for the folder
						retrieveFiles(new File(file.getAbsolutePath()),true);
						
					} else addToList(file);
					
					
				}
				
			//TODO this will eventually go out
			} else {
				
				//Add only stl and gcode
				//TODO usb lists .gco
				if ((file.getName().contains(".gcode")) || (file.getName().contains(".stl"))){
					addToList(file);
				}
				
				
			}

		}
		
		//Set new current path
		mCurrentPath = path;
	}
	
	//Retrieve only files from the individul printers
	//TODO filter by printer
	public static void retrievePrinterFiles(String source){
		
		for (ModelPrinter p : DevicesListController.getList()){
		
			for (File f : p.getFiles()){
				
				if (source!=null){
					
					if (f.getParent().equals(source)) addToList(f);
					
				} else addToList(f);
				
				
				
			}
			
		}
	}
	
	//TODO change this to database/folder eventually
	public static ArrayList<ModelFile> getFavorites(){
		
		
		
		ArrayList<ModelFile> tempList = new ArrayList<ModelFile>();
		
		
		try{
			File path = new File(getParentFolder() + "/Files");
			
			//temp solution, Files shouldn't be created here
			path.mkdirs();
			File[] files = path.listFiles();
			
			for (File file : files){				
				if (isProject(file))tempList.add(new ModelFile(file.getAbsolutePath(), "Internal storage"));
			}
		} catch (Exception e){
			e.printStackTrace();
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
	
	
	//Create a new folder in the current path
	//TODO add dialogs maybe
	public static void createFolder(String name){
			
		File newFolder = new File(mCurrentPath + "/" + name);
		
		if (!newFolder.mkdir()){
			
		} else {
			
			addToList(newFolder);
		}
			
		
	}
	
	
	//Retrieve a certain element from file storage
	public static String retrieveFile(String name, String type){
		
		String result = null;
		
		try {
			File folder = new File(name + "/" + type + "/");
			String file = folder.listFiles()[0].getName();
			
			result = folder + "/" + file; 
		
		} catch (ArrayIndexOutOfBoundsException e){
			
		}
		
		
		return result;	
		
	}
	
	//Reload the list with another path
	public static void reloadFiles(String path){
		
		mFileList.clear();
		
		if (path.equals("all")){
			retrieveFiles(getParentFolder(), true);
			retrievePrinterFiles(null);
			mCurrentPath = getParentFolder();
		} else {
			
			if ((path.equals("witbox")) || (path.equals("sd"))){
				
				retrievePrinterFiles(path);
				
			} else {
				retrieveFiles(new File(path), false);
				
				File f = new File(path);
				
				//if it's not the parent folder, make a back folder
				if (!f.getAbsolutePath().equals(StorageController.getParentFolder().toString())) {

					//TODO change folder names
					StorageController.addToList(new File(f.getParentFile().toString()));
				}
			}
			
			
		}

				
	}
	
	//Check if a folder is also a project
	//TODO return individual results according to the amount of elements found
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

	public static File getCurrentPath(){
		return mCurrentPath;
	}
}
