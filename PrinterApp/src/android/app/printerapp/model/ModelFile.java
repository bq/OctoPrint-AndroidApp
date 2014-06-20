package android.app.printerapp.model;

/**
 * Model class to define a printable element, with a reference to its STL, GCODE and storage.
 * @author alberto-baeza
 *
 */
public class ModelFile {
	
	//Element name
	private String mName;
	
	//Reference to its original stl
	private String mPathStl;
	
	//Reference to its gcode list
	private String mPathGcode;
	
	//Reference to storage
	private String mStorage;
	
	public ModelFile (String filename, String storage){
		
		mName = filename;
		mStorage = storage;
		
	}
	
	/***************
	 * GETS
	 ***************/
	
	public String getName(){
		return mName;
	}
	
	public String getStl(){
		return mPathStl;
	}
	
	public String getGcodeList(){
		return mPathGcode;
	}

	public String getStorage(){
		return mStorage;
	}
		
	/********************
	 * 	 SETS
	 * *****************/
	
	public void setPathStl(String path){	
		mPathStl = path;
	}
	
	public void setPathGcode(String path){
		mPathGcode = path;
	}
	

}
