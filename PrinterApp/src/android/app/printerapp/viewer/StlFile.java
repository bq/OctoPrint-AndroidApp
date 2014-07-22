package android.app.printerapp.viewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;

import com.devsmart.android.IOUtils;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.app.printerapp.R;
import android.app.printerapp.viewer.Geometry.*;


public class StlFile {
		
	private static final String TAG = "STLFile";
	
	private static File mFile;

	String mStringAux="";
	
	private static ProgressDialog mProgressDialog;
	private static DataStorage mData;
	static Thread mThread;
		
	public static void openStlFile (Context context, File file, DataStorage data) {
		Log.i(TAG, "Open File");
		mProgressDialog = prepareProgressDialog(context);
		mData = data;

		mFile = file;
		Uri uri = Uri.fromFile(file);
		
		mData.setPathFile(mFile.getName().replace(".", "-"));	
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
						processText(mFile);						
					} else {
						Log.e(TAG,"trying binary...");
						processBinary(arrayBytes);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				mHandler.sendEmptyMessage(0);
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
		
		progressDialog.show();
		
		return progressDialog;
	}
	
	private static int getIntWithLittleEndian(byte[] bytes, int offset) {
		return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
	}
	
	 private static Handler mHandler = new Handler() {
	        @Override
	        public void handleMessage(Message msg) {
	    		if (mData.getCoordinateListSize() < 1) {
	    			mProgressDialog.dismiss();
	    			return;
	    		}
	    			
	    		mData.fillVertexArray();
	    		mData.fillNormalArray();
	    		
	    		mData.clearNormalList();
	    		mData.clearVertexList();
	    		
				mData.enableDraw ();	    						
	    		//ProgressDialog
	    		mProgressDialog.dismiss();      		
		        	
	        }
	 };

	private static void processText (File file) {
		String line;
		try {
			int maxLines=0;
			StringBuilder allLines = new StringBuilder ("");
			BufferedReader countReader = new BufferedReader(new FileReader(file));
			while ((line = countReader.readLine()) != null) {
				if (line.trim().startsWith("vertex ")) {
					line = line.replaceFirst("vertex ", "").trim();
					allLines.append(line+"\n");
					maxLines++;
					if (maxLines%1000==0) mProgressDialog.setMax(maxLines);
				}
			}
				
			mProgressDialog.setMax(maxLines);
			
			countReader.close();
			
			
			int lines =0;
		
			int firstVertexIndex = 0;
			int secondVertexIndex = 0;
			int thirdVertexIndex = 0;
			int initialVertexIndex = -1;

			while (lines < maxLines) {
				firstVertexIndex =  allLines.indexOf("\n", thirdVertexIndex+1);
				secondVertexIndex = allLines.indexOf("\n", firstVertexIndex+1);
				thirdVertexIndex = allLines.indexOf("\n", secondVertexIndex+1);
							
				line = allLines.substring(initialVertexIndex+1, thirdVertexIndex);			
				initialVertexIndex = thirdVertexIndex;
				
				processTriangle(line);
				lines+=3;
				
				if (lines % (maxLines/10) == 0) {
					mProgressDialog.setProgress(lines);
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
						
		mProgressDialog.setMax(vectorSize);
		for (int i = 0; i < vectorSize; i++) {		
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
				mProgressDialog.setProgress(i);
			}
		}
	}
}