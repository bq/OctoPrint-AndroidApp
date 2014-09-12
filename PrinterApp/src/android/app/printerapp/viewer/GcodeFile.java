package android.app.printerapp.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.app.printerapp.R;
import android.app.printerapp.devices.printview.PrintViewFragment;
import android.app.printerapp.library.StorageModelCreation;

public class GcodeFile  {
	private static final String TAG = "gcode";
	public static final int COORDS_PER_VERTEX = 3;
	private static Context mContext;
	private static File mFile;
	private static DataStorage mData;
	
	private static ProgressDialog mProgressDialog;
	private static Thread mThread;
	
	private static String [] mSplitLine;
	private static int mMaxLayer;
	
	private static int mMode = 0;
	
	private static boolean mContinueThread = true;
	
	public static void openGcodeFile (Context context, File file, DataStorage data, int mode) {
		Log.i(TAG, " Open GcodeFile ");
		mFile = file;		
		mData = data;
		mMode = mode;
		mContinueThread = true;
		if(mMode!= ViewerMain.DO_SNAPSHOT) mProgressDialog = prepareProgressDialog(context);

		mData.setPathFile(mFile.getAbsolutePath());		
		mData.initMaxMin();
		mMaxLayer=-1;
		startThreadToOpenFile (context);		
	}
	
	public static void startThreadToOpenFile (final Context context) {
		mThread = new Thread () {
			@Override
			public void run () {
				String line;
				StringBuilder allLines = new StringBuilder ("");

				try {
					int maxLines=0;
					BufferedReader countReader = new BufferedReader(new FileReader(mFile));
					while ((line = countReader.readLine()) != null && mContinueThread) {
						allLines.append(line + "\n");
						maxLines++;
					}
					countReader.close();
					

					if(mMode== ViewerMain.PRINT_PREVIEW) mData.setMaxLinesFile(maxLines);
					if(mMode!= ViewerMain.DO_SNAPSHOT) mProgressDialog.setMax(maxLines);
					if (mContinueThread) processGcode(allLines, maxLines);

					if (mContinueThread) mHandler.sendEmptyMessage(0);
								
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		};	
		
		mThread.start();
	}
	
	public void saveNameFile () {
		mData.setPathFile(mFile.getName().replace(".", "-"));
	}
	
	private static ProgressDialog prepareProgressDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(R.string.loading_gcode);
		progressDialog.setMax(0);
		progressDialog.setMessage(context.getResources().getString(R.string.be_patient));
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(false);
		
		progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		    	mContinueThread = false;
		    	try {
					mThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	ViewerMain.resetWhenCancel();
		    }
		});
		
		progressDialog.show();
		
		return progressDialog;
	}
	
	public static void processGcode(StringBuilder allLines, int maxLines) {
		int index=0;
		int lastIndex = 0;
		int lines = 0;
		String line="";
		
		boolean end = false;
		boolean start = false;
		float x=0;
		float y=0;
		float z=0;
		int type = -1;
		int length = 0;
		int layer = 0;
		
		while (lines<maxLines && mContinueThread) {	
			index = allLines.indexOf("\n", lastIndex);
			line = allLines.substring(lastIndex, index);

			if (line.contains("END GCODE")) end = true;
			
			if (line.contains("end of START GCODE")) start = true;
			
			if(line.contains("MOVE")) {
	        	 type = DataStorage.MOVE;
	         }else if(line.contains("FILL")) {
	        	 type = DataStorage.FILL;
	         }else if(line.contains("PERIMETER")) {
	        	 type = DataStorage.PERIMETER;
	         }else if(line.contains("RETRACT")) {
	        	 type = DataStorage.RETRACT;
	         }else if(line.contains("COMPENSATE")) {
	        	 type = DataStorage.COMPENSATE;
	         }else if(line.contains("BRIDGE")) {
	        	 type = DataStorage.BRIDGE;
	         }else if(line.contains("SKIRT")) {
	        	 type = DataStorage.SKIRT;	            		 
	         } else if(line.contains("WALL-INNER")) {
	        	 type = DataStorage.WALL_INNER;	            		 
	         } else if(line.contains("WALL-OUTER")) {
	        	 type = DataStorage.WALL_OUTER;	            		 
	         }  else if(line.contains("SUPPORT")) {
	        	 type = DataStorage.SUPPORT;	
	         }
			
			//From comments
			if (line.contains("LAYER")) {
	        	 int pos = line.indexOf(":");
	        	 layer = Integer.parseInt(line.substring(pos+1, line.length()));
			}	 
					
			if (line.startsWith("G0") || line.startsWith("G1")) {
				mSplitLine = line.split(" ");
				
				//Get the coord from the line					
				for (int i=0; i<mSplitLine.length; i++) {
					if (mSplitLine[i].length()<=1) continue;
					if (mSplitLine[i].startsWith("X")) {
						mSplitLine[i] = mSplitLine[i].replace("X", "");
						x= Float.valueOf(mSplitLine[i])-WitboxFaces.WITBOX_LONG;
					} else if (mSplitLine[i].startsWith("Y")) {
						mSplitLine[i] = mSplitLine[i].replace("Y", "");
						y = Float.valueOf(mSplitLine[i])-WitboxFaces.WITBOX_WITDH;
					} else if (mSplitLine[i].startsWith("Z")) {
						mSplitLine[i] = mSplitLine[i].replace("Z", "");
						z = Float.valueOf(mSplitLine[i]);
					} 
				}
	
				 if (line.startsWith("G0")) {
					 mData.addLineLength(length);
		             length = 1;
		            
				 } else if (line.startsWith("G1")) {
					 //GCode saves the movement from one type to another (i.e wall_inner-wall_outer) in the list of the previous type. 
					 //If we have just started a line, we set again the colour of the first vertex to avoid wrong colour
					 //This avoids gradients in rendering.			 
					 if (length == 1) mData.changeTypeAtIndex(mData.getTypeListSize()-1, type);
	
					 length ++;
					 
					
					 if (start && !end) mData.adjustMaxMin(x,y, z);
				 }
		 
				 mData.addVertex(x);
				 mData.addVertex(y);
				 mData.addVertex(z);
				 mData.addLayer(layer);
				 mData.addType(type);
				 			 		 
				 if (layer>mMaxLayer) mMaxLayer = layer;
				 		
			}
					
			 lines++;
			 lastIndex = index+1;

			 if (mMode!= ViewerMain.DO_SNAPSHOT && lines % (maxLines/10) == 0)mProgressDialog.setProgress(lines);	
		}		
	}
	
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
    		if (mData.getCoordinateListSize() < 1) {
    			Toast.makeText(mContext, R.string.error_opening_invalid_file, Toast.LENGTH_SHORT).show();
    			ViewerMain.resetWhenCancel();
    			if(mMode!= ViewerMain.DO_SNAPSHOT) mProgressDialog.dismiss();
    			return;
    		}
    		
    		mData.setMaxLayer(mMaxLayer);
     		
    		mData.fillVertexArray();
    		mData.fillTypeArray();
    		mData.fillLayerArray();
    		
    		mData.clearVertexList();
    		mData.clearLayerList();
    		mData.clearTypeList();


    		if(mMode==ViewerMain.DONT_SNAPSHOT) {
    			ViewerMain.initSeekBar(mMaxLayer);
    			ViewerMain.initSeekBar(mMaxLayer);
	    		ViewerMain.draw();
				mProgressDialog.dismiss();  
    		} else if(mMode==ViewerMain.PRINT_PREVIEW) {
    			PrintViewFragment.drawPrintView();
    			PrintViewFragment.drawPrintView();
    			mProgressDialog.dismiss();
    		} else if (mMode==ViewerMain.DO_SNAPSHOT) {
				StorageModelCreation.takeSnapshot();
    		}
        }
    };
 }