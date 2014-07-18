package android.app.printerapp.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.printerapp.R;

public class GcodeFile  {
	private static final String TAG = "GCodeFile";
	public static final int COORDS_PER_VERTEX = 3;
	private static File mFile;
	private static DataStorage mData;
	
	private static ProgressDialog mProgressDialog;
	private static Thread mThread;
	private static int mMaxLayer;
	
	static String [] mSplitLine;
	
	private static int mLines=0;
	private static boolean mEnd = false;
	private static boolean mStart = false;
		
	static float mX;
	static float mY;
	static float mZ;
	static float mF;
	private static float mLastX=0;
	private static float mLastY=0;
	private static float mLastZ=0;
	
	private static int mLength; 
	private static int mLayer;
	private static int mType;
	private static float mExecutionTime;
	
	public static void openGcodeFile (Context context, File file, DataStorage data) {
		mFile = file;		
		mData = data;
		mProgressDialog = prepareProgressDialog(context);
		mData.setPathFile(mFile.getName().replace(".", "-"));
		
		mData.initMaxMin();

		startThreadToOpenFile (context);		
	}
	
	public static void startThreadToOpenFile (final Context context) {
		mThread = new Thread () {
			@Override
			public void run () {
				Log.i(TAG, "Starting thread to open file");
				String line;
				StringBuilder allLines = new StringBuilder ("");

				try {
					int maxLines=0;
					BufferedReader countReader = new BufferedReader(new FileReader(mFile));
					while ((line = countReader.readLine()) != null) {
						allLines.append(line + "\n");
						maxLines++;
					}
					countReader.close();
					
					mProgressDialog.setMax(maxLines);
					int index=0;
					int lastIndex = 0;
					while (mLines<maxLines) {	
						index = allLines.indexOf("\n", lastIndex);
						line = allLines.substring(lastIndex, index);
						fillCoordinateList (line);
						mLines++;
						lastIndex = index+1;
						
						if (mLines % (maxLines/10) == 0)mProgressDialog.setProgress(mLines);
					}
					
					mHandler.sendEmptyMessage(0);
								
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
		
		progressDialog.show();
		
		return progressDialog;
	}
	
    private static Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
    		if (mData.getCoordinateListSize() < 1) {
    			mProgressDialog.dismiss();
    			return;
    		}
    		
    		mData.setMaxLayer(mMaxLayer);
     		
    		mData.fillVertexArray();
    		mData.fillTypeArray();
    		mData.fillLayerArray();
    		
    		mData.clearVertexList();
    		mData.clearLayerList();
    		mData.clearTypeList();
    		
    		ViewerMain.initSeekBar(mMaxLayer);
    		
			mData.enableDraw ();

    		//ProgressDialog
    		mProgressDialog.dismiss();      		
	        	
        }
    };
	
	
	static void fillCoordinateList (String line) {
		if (line.contains("END GCODE")) mEnd = true;
		
		if (line.contains("end of START GCODE")) mStart = true;
		
		if(line.contains("MOVE")) {
        	 mType = DataStorage.MOVE;
         }else if(line.contains("FILL")) {
        	 mType = DataStorage.FILL;
         }else if(line.contains("PERIMETER")) {
        	 mType = DataStorage.PERIMETER;
         }else if(line.contains("RETRACT")) {
        	 mType = DataStorage.RETRACT;
         }else if(line.contains("COMPENSATE")) {
        	 mType = DataStorage.COMPENSATE;
         }else if(line.contains("BRIDGE")) {
        	 mType = DataStorage.BRIDGE;
         }else if(line.contains("SKIRT")) {
        	 mType = DataStorage.SKIRT;	            		 
         } else if(line.contains("WALL-INNER")) {
        	 mType = DataStorage.WALL_INNER;	            		 
         } else if(line.contains("WALL-OUTER")) {
        	 mType = DataStorage.WALL_OUTER;	            		 
         }  else if(line.contains("SUPPORT")) {
        	 mType = DataStorage.SUPPORT;	
         }
		
		//From comments
		if (line.contains("LAYER")) {
        	 int pos = line.indexOf(":");
        	 mLayer = Integer.parseInt(line.substring(pos+1, line.length()));
		}	 
				
		if (line.startsWith("G0") || line.startsWith("G1")) {
			mSplitLine = line.split(" ");
			
			//Get the coord from the line					
			for (int i=0; i<mSplitLine.length; i++) {
				if (mSplitLine[i].length()<=1) continue;
				if (mSplitLine[i].startsWith("X")) {
					mSplitLine[i] = mSplitLine[i].replace("X", "");
					mX= Float.valueOf(mSplitLine[i])-WitboxFaces.WITBOX_LONG;
				} else if (mSplitLine[i].startsWith("Y")) {
					mSplitLine[i] = mSplitLine[i].replace("Y", "");
					mY = Float.valueOf(mSplitLine[i])-WitboxFaces.WITBOX_WITDH;
				} else if (mSplitLine[i].startsWith("Z")) {
					mSplitLine[i] = mSplitLine[i].replace("Z", "");
					mZ = Float.valueOf(mSplitLine[i]);
				} else if (mSplitLine[i].startsWith("F")) {
					mSplitLine[i] = mSplitLine[i].replace("F", "");
					mF = Float.valueOf(mSplitLine[i]);
				}
			}
			
			 
			 if (line.startsWith("G0")) {
				 mData.addLineLength(mLength);
	             mLength = 1;
	            
			 } else if (line.startsWith("G1")) {
				 //GCode saves the movement from one type to another (i.e wall_inner-wall_outer) in the list of the previous type. 
				 //If we have just started a line, we set again the colour of the first vertex to avoid wrong colour
				 //This avoids gradients in rendering.			 
				 if (mLength == 1) mData.changeTypeAtIndex(mData.getTypeListSize()-1, mType);

				 mLength ++;
				 
				
				 if (mStart && !mEnd) mData.adjustMaxMin(mX,mY, mZ);

				 //Get distance for each axis
	             float dx = Math.abs(mLastX-mX);
	             float dy = Math.abs(mLastY-mY);
	             float dz = Math.abs(mLastZ-mZ);
	             		             
	             float dist = (float) Math.sqrt(dx*dx+dy*dy+dz*dz);
	             
	             mExecutionTime = mExecutionTime + dist/mF;	//estimate the time 
			 }
			 
			 mData.addVertex(mX);
			 mData.addVertex(mY);
			 mData.addVertex(mZ);
			 mData.addLayer(mLayer);
			 mData.addType(mType);
			 
			 mLastX = mX;
			 mLastY = mY; 
			 mLastZ = mZ;
			 
			 if (mLayer>mMaxLayer) mMaxLayer = mLayer;

		}else if(line.startsWith("M")) {
        	//Execution time when a Machine code is executed. 
            mExecutionTime += 0.016666667; //add half a second to time for mcode
        }else if(line.startsWith("G4")) {
        	//G4 is a wait command. So add wait time to execution time.
        	String time = line.replace("G4 P", "");
        	mExecutionTime += Double.parseDouble(time)/60;
        }
	}
 }