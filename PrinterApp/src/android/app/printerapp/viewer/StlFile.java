package android.app.printerapp.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import com.devsmart.android.IOUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.opengl.Matrix;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;
import android.app.printerapp.R;
import android.app.printerapp.library.StorageController;
import android.app.printerapp.library.StorageModelCreation;
import android.app.printerapp.viewer.Geometry.*;


public class StlFile {
		
	private static final String TAG = "STLFile";
	
	private static File mFile;

	String mStringAux="";
	
	private static ProgressDialog mProgressDialog;
	private static DataStorage mData;
	private static Context mContext;
	private static boolean mDoSnapshot;
	private static Thread mThread;
	
	private static boolean mContinueThread = true;
	
	private static final int COORDS_PER_TRIANGLE = 9;
		
	public static void openStlFile (Context context, File file, DataStorage data, boolean doSnapshot) {
		Log.i(TAG, "Open File");
		mDoSnapshot = doSnapshot;
		mContinueThread = true;
		
		if (!mDoSnapshot) mProgressDialog = prepareProgressDialog(context);
		mData = data;
		mContext = context;
		mFile = file;
		Uri uri = Uri.fromFile(file);
		
		mData.setPathFile(mFile.getAbsolutePath());	
		mData.initMaxMin();
		
		startThreadToOpenFile(context, uri);
	}
	
	public static void startThreadToOpenFile (final Context context, final Uri uri) {
		mThread = new Thread () {
			@Override
			public void run () {
				byte [] arrayBytes = toByteArray (context, uri);
				
				try {
					if (isText(arrayBytes)) {
						Log.e(TAG,"trying text... ");
						if(mContinueThread) processText(mFile);						
					} else {
						Log.e(TAG,"trying binary...");
						if(mContinueThread) processBinary(arrayBytes);
					} 
				} catch (Exception e) {
					e.printStackTrace();
				}

				if(mContinueThread) mHandler.sendEmptyMessage(0);
			}
		};	
		
		mThread.start();
	}
	
	
	
	private static byte [] toByteArray (Context context, Uri filePath) {
		InputStream inputStream = null;
		byte [] arrayBytes = null;
		try {		
			inputStream = context.getContentResolver().openInputStream(filePath);		
			arrayBytes = IOUtils.toByteArray(inputStream);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {		
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}	
		
		return arrayBytes;
	}
	
	private static boolean isText (byte [] bytes) {
		for (byte b : bytes) {
			if (b == 0x0a || b == 0x0d || b == 0x09) {
				// white spaces
				continue;
			}
			if (b < 0x20 || (0xff & b) >= 0x80) {
				// control codes
				return false;
			}
		}
		return true;		
	}
		
	
	/**
	 * 
	 * Progress Dialog
	 * ----------------------------------
	 */
	private static ProgressDialog prepareProgressDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(R.string.loading_stl);
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
		
		if (!mDoSnapshot) progressDialog.show();
		
		return progressDialog;
	}
	
	private static int getIntWithLittleEndian(byte[] bytes, int offset) {		
		return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
	}
	
	 private static Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	    		if (mData.getCoordinateListSize() < 1) {
	    			Toast.makeText(mContext, R.string.error_opening_invalid_file, Toast.LENGTH_SHORT).show();
	    			ViewerMain.resetWhenCancel();
	    			if (!mDoSnapshot) mProgressDialog.dismiss();
	    			return;
	    		}
	    		
	    		mData.fillVertexArray();
	    		mData.fillNormalArray();
	    		
	    		mData.clearNormalList();
	    		mData.clearVertexList();
	    		
				//Finish 
				if (!mDoSnapshot) {
					ViewerMain.draw();
					mProgressDialog.dismiss();  
				} else {
					StorageModelCreation.takeSnapshot();
				}    		
	        }
	 };

	private static void processText (File file) {
		String line;
		try {
			int maxLines=0;
			StringBuilder allLines = new StringBuilder ("");
			BufferedReader countReader = new BufferedReader(new FileReader(file));
			while ((line = countReader.readLine()) != null && mContinueThread) {
				if (line.trim().startsWith("vertex ")) {
					line = line.replaceFirst("vertex ", "").trim();
					allLines.append(line+"\n");
					maxLines++;
					if (maxLines%1000==0 && !mDoSnapshot) mProgressDialog.setMax(maxLines);
				}
			}
				
			if (!mDoSnapshot) mProgressDialog.setMax(maxLines);
			
			countReader.close();
			
			
			int lines =0;
		
			int firstVertexIndex = 0;
			int secondVertexIndex = 0;
			int thirdVertexIndex = 0;
			int initialVertexIndex = -1;

			while (lines < maxLines && mContinueThread) {
				firstVertexIndex =  allLines.indexOf("\n", thirdVertexIndex+1);
				secondVertexIndex = allLines.indexOf("\n", firstVertexIndex+1);
				thirdVertexIndex = allLines.indexOf("\n", secondVertexIndex+1);
							
				line = allLines.substring(initialVertexIndex+1, thirdVertexIndex);			
				initialVertexIndex = thirdVertexIndex;
				
				processTriangle(line);
				lines+=3;
				
				if (lines % (maxLines/10) == 0) {
					if (!mDoSnapshot) mProgressDialog.setProgress(lines);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void processTriangle (String line) throws Exception {		
		String[] vertex = line.split("\n");
				
		String[] vertexValues = vertex[0].split(" ");	
		float x = Float.parseFloat(vertexValues [0]);
		float y = Float.parseFloat(vertexValues [1]);
		float z = Float.parseFloat(vertexValues [2]);
		Vector v0 = new Vector (x,y,z);
		mData.adjustMaxMin(x, y, z);
		mData.addVertex(x);
		mData.addVertex(y);
		mData.addVertex(z);
		
		vertexValues = vertex[1].split(" ");	
		x = Float.parseFloat(vertexValues [0]);
		y = Float.parseFloat(vertexValues [1]);
		z = Float.parseFloat(vertexValues [2]);
		Vector v1 = new Vector (x,y,z);
		mData.adjustMaxMin(x, y, z);
		mData.addVertex(x);
		mData.addVertex(y);
		mData.addVertex(z);
		
		vertexValues = vertex[2].split(" ");	
		x = Float.parseFloat(vertexValues [0]);
		y = Float.parseFloat(vertexValues [1]);
		z = Float.parseFloat(vertexValues [2]);
		Vector v2 = new Vector (x,y,z);
		mData.adjustMaxMin(x, y, z);
		mData.addVertex(x);
		mData.addVertex(y);
		mData.addVertex(z);
		
		//Calculate triangle normal vector
		Vector normal = Vector.normalize(Vector.crossProduct(Vector.substract(v1 , v0), Vector.substract(v2, v0)));
		
		mData.addNormal(normal.x);
		mData.addNormal(normal.y);
		mData.addNormal(normal.z);		
	
	}
	
	private static void processBinary(byte[] stlBytes) throws Exception {			
		int vectorSize = getIntWithLittleEndian(stlBytes, 80);
				
		if (!mDoSnapshot) mProgressDialog.setMax(vectorSize);
		for (int i = 0; i < vectorSize; i++) {
			if(!mContinueThread) break;
			float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
			float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
			float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
			Vector v0 = new Vector (x,y,z);

			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
		
			x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
			y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
			z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
			Vector v1 = new Vector (x,y,z);

			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
			x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
			y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
			z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
			Vector v2 = new Vector (x,y,z);

			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
			//Calculate triangle normal vector
			Vector normal = Vector.normalize(Vector.crossProduct(Vector.substract(v1 , v0), Vector.substract(v2, v0)));
				
			mData.addNormal(normal.x);
			mData.addNormal(normal.y);
			mData.addNormal(normal.z);		
			
			
			if (i % (vectorSize / 10) == 0) {
				if (!mDoSnapshot) mProgressDialog.setProgress(i);
			}
		}
	}
	
	private static float[] setTransformationVector (float x, float y, float z, float[] rotationMatrix, float scaleFactorX, float scaleFactorY, float scaleFactorZ, float adjustZ, Point center) {
		float [] vector = new float [4];
		float [] result = new float [4];
		
		vector[0] = x;
		vector[1] = y;
		vector[2] = z;
		
		Matrix.multiplyMV(result, 0, rotationMatrix, 0, vector, 0);

		result[0] = result[0]*scaleFactorX+center.x;		
		result[1] = result[1]*scaleFactorY+center.y;		
		result[2] = result[2]*scaleFactorZ+center.z + adjustZ;		

		return result;
	}
	
	public static boolean checkIfNameExists (String projectName) {
		File check = new File (StorageController.getParentFolder().getAbsolutePath() + "/Files/" + projectName);
		if (check.exists()) return true;
		
		return false;
	}
	
	public static boolean saveModel (List<DataStorage> dataList, String projectName) {		
		float[] coordinates = null;
		int coordinateCount = 0;
		float[] rotationMatrix = new float [16];
		float scaleFactorX=0;
		float scaleFactorY=0;
		float scaleFactorZ=0;
		Point center = new Point (0,0,0);	
		float adjustZ = 0;
		float[] vector = new float[3];

		//Calculating buffer size
		for (int i=0; i<dataList.size(); i++) coordinateCount+= dataList.get(i).getVertexArray().length;
				
		if (coordinateCount==0) return false;
		
		int offset=(coordinateCount/COORDS_PER_TRIANGLE)*4; //each triangle needs its normal coords and flag to indicate the end.
		
		ByteBuffer bb = ByteBuffer.allocateDirect((coordinateCount+offset) * 4 + 84);
	    bb.order(ByteOrder.LITTLE_ENDIAN);
	    
	    //Header
	    byte[] header = new byte[80];
	    bb.put(header);
	    bb.putInt(coordinateCount/COORDS_PER_TRIANGLE);
	    
		for (int i=0;i<dataList.size(); i++) {
			DataStorage data = dataList.get(i);
			rotationMatrix = data.getRotationMatrix(); 
			scaleFactorX = data.getLastScaleFactorX();
			scaleFactorY = data.getLastScaleFactorY();
			scaleFactorZ = data.getLastScaleFactorZ();
			adjustZ = data.getAdjustZ();
			center = data.getLastCenter();
			coordinates = data.getVertexArray();
			
		    for (int j=0; j<coordinates.length; j+=9) {
		    	//Normal data. It is not necessary to store the info
		    	bb.putFloat(0);
		    	bb.putFloat(0);
		    	bb.putFloat(0);
		    	
		    	//Triangle Data, 3 vertex with 3 coordinates (x,y,z) each one.
		    	vector = setTransformationVector (coordinates[j], coordinates[j+1], coordinates[j+2], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
		    	bb.putFloat(vector[0]);
		    	bb.putFloat(vector[1]);
		    	bb.putFloat(vector[2]);
		    	
		    	vector = setTransformationVector (coordinates[j+3], coordinates[j+4], coordinates[j+5], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
		    	bb.putFloat(vector[0]);
		    	bb.putFloat(vector[1]);
		    	bb.putFloat(vector[2]);
		    	
		    	vector = setTransformationVector (coordinates[j+6], coordinates[j+7], coordinates[j+8], rotationMatrix, scaleFactorX, scaleFactorY, scaleFactorZ, adjustZ, center);
		    	bb.putFloat(vector[0]);
		    	bb.putFloat(vector[1]);
		    	bb.putFloat(vector[2]);
				
		    	bb.putShort((short)0); // end of triangle		    	
		    }
		}
		
		bb.position(0);
	    byte[] data = bb.array();
	    String path = StorageController.getParentFolder().getAbsolutePath() + "/" + projectName + ".stl";
	    try {
            FileOutputStream fos = new FileOutputStream(path);
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }     
	    
	    File file = new File (path);
	    StorageModelCreation.createFolderStructure(mContext, file);
	    file.delete();
	    
	    return true;
	}
}