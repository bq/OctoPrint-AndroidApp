package android.app.printerapp.library;

import java.io.File;
import java.util.ArrayList;

import android.app.printerapp.model.ModelFile;
import android.os.Environment;


/**
 * This class will handle the file storage architecture and retrieval in a static way
 * so every class can access these methods from wherever
 * @author alberto-baeza
 *
 */
public class StorageController {
	
	static ArrayList<ModelFile> mFileList = new ArrayList<ModelFile>();
	
	public StorageController(){
		
		retrieveFiles();
	
	}
	
	public static ArrayList<ModelFile> getFileList(){
		return mFileList;
	}
	
	
	//Retrieve files from our system architecture.
	public void retrieveFiles(){
		
						
		File mainFolder = getParentFolder();
		File trashFolder = new File(Environment.getExternalStorageDirectory() + "/PrintManager");
		
		/**
		 * CHANGED FILE LOGIC, NOW RETRIEVES FOLDERS INSTEAD OF FILES, AND PARSES
		 * INDIVIDUAL ELEMENTS LATER ON.
		 */
		File[] files = mainFolder.listFiles();
		for (File file : files){
			if (file.isDirectory()){	
				
				ModelFile m = new ModelFile(file.getName(), "Internal storage");
				
				//TODO: Move this to the ModelFile code
				m.setPathStl(retrieveFile(m.getName(), "_stl"));	
				m.setPathGcode(retrieveFile(m.getName(), "_gcode"));	
				m.setSnapshot(getParentFolder() + "/" + m.getName() + "/" + m.getName() + ".png");
				mFileList.add(m);
				
			}
		}
		
		//TODO: TEST trash storage
		
		File[] trashFiles = trashFolder.listFiles();
		for (File file : trashFiles){
			if (!file.isDirectory()){
				ModelFile m = new ModelFile(file.getName(), "Trash storage");
				m.setPathStl(file.getAbsolutePath());	
				m.setPathGcode(file.getAbsolutePath());	
				mFileList.add(m);
			}
		}
								
	}
	
	//Retrieve main folder or create if doesn't exist
	//TODO: Changed main folder to FILES folder.
	public static File getParentFolder(){
		String parentFolder = Environment.getExternalStorageDirectory().toString();
		File mainFolder = new File(parentFolder + "/PrintManager/Files");
		mainFolder.mkdirs();
		File temp_file = new File(mainFolder.toString());

		return temp_file;
	}
	
	
	//Retrieve certain element from file storage
	public static String retrieveFile(String name, String type){
		
		String result = null;
		
		try {
			File folder = new File(getParentFolder() + "/" + name + "/" + type + "/");
			String file = folder.listFiles()[0].getName();
			
			result = folder + "/" + file; 
		
		} catch (ArrayIndexOutOfBoundsException e){
			
		}
		
		
		return result;	
		
	}
	
	public static void addToList(ModelFile m ){
		mFileList.add(m);
	}

}
