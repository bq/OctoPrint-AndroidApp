package android.app.printerapp.model;

import java.io.File;

import android.app.printerapp.R;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Model class to define a printable element, with a reference to its STL, GCODE and storage.
 * @author alberto-baeza
 *
 */
@SuppressWarnings("serial")
public class ModelFile extends File {

	
	//Reference to its original stl
	private String mPathStl;
	
	//Reference to its gcode list
	private String mPathGcode;
	
	//Reference to storage
	private String mStorage;
	
	//Reference to image
	private Drawable mSnapshot;
	
	public ModelFile(String filename, String storage){
		super(filename);

		mStorage = storage;
		
	}
	
	/***************
	 * GETS
	 ***************/
		
	public String getStl(){
		return mPathStl;
	}
	
	public String getGcodeList(){
		return mPathGcode;
	}

	public String getStorage(){
		return mStorage;
	}
	
	public Drawable getSnapshot(){
		return mSnapshot;
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
	
	public void setSnapshot(String path){
		
		try {
			mSnapshot = Drawable.createFromPath(path);
		} catch (Exception e){
			mSnapshot = Resources.getSystem().getDrawable(R.drawable.file_icon);
		}
		
		Log.i("MODEL","MY DRAWABLE: " + path)	;
	}
	

}
