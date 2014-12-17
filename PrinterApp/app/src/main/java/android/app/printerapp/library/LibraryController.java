package android.app.printerapp.library;

import android.annotation.SuppressLint;
import android.app.printerapp.devices.DevicesListController;
import android.app.printerapp.devices.database.DatabaseController;
import android.app.printerapp.model.ModelFile;
import android.app.printerapp.model.ModelPrinter;
import android.app.printerapp.octoprint.StateUtils;
import android.os.Environment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Map;


/**
 * This class will handle the file storage architecture and retrieval in a static way
 * so every class can access these methods from wherever
 * @author alberto-baeza
 *
 */
@SuppressLint("DefaultLocale")
public class LibraryController {

	private static ArrayList<File> mFileList = new ArrayList<File>();
	private static File mCurrentPath;
	
	public LibraryController(){

        //TODO nope
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

        if (files !=null)
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
				if ((hasExtension(0, file.getName())) || (hasExtension(1, file.getName()))){
					addToList(file);
				}
				
				
			}

		}
		
		//Set new current path
		mCurrentPath = path;
	}
	
	//Retrieve only files from the individual printers
	public static void retrievePrinterFiles(Long id){
		
		mFileList.clear();
		
		if (id!=null){
		
			ModelPrinter p = DevicesListController.getPrinter(id);

			for (File f : p.getFiles()){
				
				addToList(f);
					
			}
		}
		
		//Set the current path pointing to a printer so we can go back
		mCurrentPath = new File("printer/" + id);
	}
	
	//Retrieve favorites
	public static void retrieveFavorites(){

		
		for (Map.Entry<String, ?> entry : DatabaseController.getPreferences(DatabaseController.TAG_FAVORITES).entrySet()){
			
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

                    if ((p.getStatus()!= StateUtils.STATE_ADHOC)&&(p.getStatus()!=StateUtils.STATE_NEW))
                        //we add a printer/ parent to determine inside a printer
                        addToList(new File("printer/" + p.getId()));
				}
								
			} else {

                if ((path.equals("favorites"))){
                    retrieveFavorites();
                } else{

                    if ((path.equals("current"))){

                        retrieveFiles(mCurrentPath, false);

                    }else {
                        ///any other folder will open normally
                        retrieveFiles(new File(path), false);
                    }

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
				
				return filename.endsWith("thumb");
			}
		};
		
		
		if (file.list(f).length > 0) return true;
		else return false;
	}
	
	/**
	 * Method to check if a file is a proper .gcode or .stl
	 * @param type 0 for .stl 1 for .gcode
	 * @param name name of the file
	 * @return true if it's the desired type
	 */

	public static boolean hasExtension(int type, String name){
		
		switch (type){
			
		case 0: if (name.toLowerCase().endsWith(".stl"))  return true;
			break;
		case 1: if ((name.toLowerCase().endsWith(".gcode")) || (name.toLowerCase().endsWith(".gco"))) return true;
			break;
		}
		
		return false;
	}
	
	public static void addToList(File m ){
		mFileList.add(m);
	}

	public static File getCurrentPath(){
		return mCurrentPath;
	}

    /**
     * Delete files recursively
     * @param file
     */
    public static void deleteFiles(File file){


        if (file.isDirectory()){

            for (File f : file.listFiles()){

                deleteFiles(f);

            }

        }

        file.delete();

    }
}
