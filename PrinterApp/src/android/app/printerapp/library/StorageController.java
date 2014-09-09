package android.app.printerapp.library;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
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
	
	//Retrieve only files from the individual printers
	public static void retrievePrinterFiles(String source){
		
		mFileList.clear();
		
		if (source!=null){
		
			ModelPrinter p = DevicesListController.getPrinter(source);

			for (File f : p.getFiles()){
				
				addToList(f);
					
			}
		}
		
		//Set the current path pointing to a printer so we can go back
		mCurrentPath = new File("printer/" + source);
	}
	
	//Retrieve favorites
	public static void retrieveFavorites(){
		
		mFileList.clear();
		
		for (Map.Entry<String, ?> entry : DatabaseController.getFavorites().entrySet()){
			
			ModelFile m = new ModelFile(entry.getValue().toString(), "favorite");
			mFileList.add(m);
			
		}
		
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
		File folder = new File(name + "/" + type + "/");
		try {
			
			String file = folder.listFiles()[0].getName();
			
			result = folder + "/" + file; 
		
			
			//File still in favorites
		} catch (Exception e){
			//e.printStackTrace();
		}
		
		
		return result;	
		
	}
	
	//Reload the list with another path
	public static void reloadFiles(String path){
		
		mFileList.clear();
		
		//Retrieve every single file by recursive search
		//TODO Not retrieving printers
		if (path.equals("all")){
			retrieveFiles(getParentFolder(), true);
			mCurrentPath = getParentFolder();
		} else {
			
			//Retrieve only the printers like folders
			if ((path.equals("printer"))){
				
				for (ModelPrinter p : DevicesListController.getList()){
					
					//we add a printer/ parent to determine inside a printer
					addToList(new File("printer/" + p.getName()));
				}
								
			} else {
				
				///any other folder will open normally
				retrieveFiles(new File(path), false);
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
