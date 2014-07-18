package android.app.printerapp.library;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.printerapp.R;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

/**
 * 
 * this method will create a new folder structure from a file in our system
 * @author alberto-baeza
 *
 */
public class StorageModelCreation {
	
	
	//Static method to create a folder structure
	public static void createFolderStructure(Context context, File source){
		//Catch null pointer because file browser buttons aren't implemented
		if (source!=null){
			String name = source.getName().substring(0, source.getName().lastIndexOf('.'));
			
			File root = new File(StorageController.getParentFolder().getAbsolutePath() +
					"/Files/" + name);
			
			//root folder
			if (root.mkdirs()){
				
				File gcodeFolder = new File(root.getAbsolutePath() + "/_gcode");
				File stlFolder = new File(root.getAbsolutePath() + "/_stl");
				
				//gcode folder
				if (gcodeFolder.mkdir()){
					
					
					
				}
				
				//stl folder
				if (stlFolder.mkdir()){
					
					try{
					
						File target = new File(stlFolder.getAbsolutePath() + "/" + source.getName());
		
						
						if(source.exists()){
		                    
		                    InputStream in = new FileInputStream(source);
		                    OutputStream out = new FileOutputStream(target);
		         
		                    // Copy the bits from instream to outstream
		                    byte[] buf = new byte[1024];
		                    int len;
		                     
		                    while ((len = in.read(buf)) > 0) {
		                        out.write(buf, 0, len);
		                    }
		                     
		                    in.close();
		                    out.close();
		                                          
		                }else{
		
		                }
						
						createFile(root.getAbsolutePath() + "/" + name + ".jpg", context, R.drawable.random);
					
					} catch (IOException e){
						e.printStackTrace();
					}
					
				}
				
			}
		
			
			
		}
		
	}	
	
	 /**
	   * Create an output file from raw resources.
	   * 
	   * @param outputFile
	   * @param context
	   * @param inputRawResources
	   * @throws IOException
	   */
	  public static void createFile(final String outputFile,
	      final Context context, final int resource)
	      throws IOException {

	    final OutputStream outputStream = new FileOutputStream(outputFile);

	    final Resources resources = context.getResources();
	    final byte[] largeBuffer = new byte[1024 * 4];
	    int bytesRead = 0;


	      final InputStream inputStream = resources.openRawResource(resource);
	      while ((bytesRead = inputStream.read(largeBuffer)) > 0) {
	        if (largeBuffer.length == bytesRead) {
	          outputStream.write(largeBuffer);
	        } else {
	          final byte[] shortBuffer = new byte[bytesRead];
	          System.arraycopy(largeBuffer, 0, shortBuffer, 0, bytesRead);
	          outputStream.write(shortBuffer);
	        }
	      }
	      inputStream.close();
	    
	      
	    outputStream.flush();
	    outputStream.close();
	  }
	



}
