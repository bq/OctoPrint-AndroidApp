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


public class StlFile implements Runnable{
		
	private static final String TAG = "STLFile";
	
	private File mFile;
	private Uri mUri;
	private Context mContext;

	String mStringAux="";
	
	private ProgressDialog mProgressDialog;
	private DataStorage mData;
	Thread mThread;
		
	public StlFile (Context context, File file, DataStorage data) {
		Log.i(TAG, "Open File");
		this.mContext= context;
		this.mFile = file;
		this.mUri = Uri.fromFile(file);
		this.mData = data;
		this.mProgressDialog = prepareProgressDialog(context);
		mData.setPathFile(mFile.getName().replace(".", "-"));
		
		mData.setMaxX(Float.MIN_VALUE);
		mData.setMaxY(Float.MIN_VALUE);
		mData.setMaxZ(Float.MIN_VALUE);
		mData.setMinX(Float.MAX_VALUE);
		mData.setMinY(Float.MAX_VALUE);
		mData.setMinZ(Float.MAX_VALUE);
		
		this.mThread = new Thread (this);	
		mThread.start();
	
	}
	
	private byte [] toByteArray (Context context, Uri filePath) {
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
	
	private boolean isText (byte [] bytes) {
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
	private ProgressDialog prepareProgressDialog(Context context) {
		ProgressDialog progressDialog = new ProgressDialog(context);
		progressDialog.setTitle(R.string.loading_stl);
		progressDialog.setMax(0);
		progressDialog.setMessage(mContext.getResources().getString(R.string.be_patient));
		progressDialog.setIndeterminate(false);
		progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressDialog.setCancelable(false);
		
		progressDialog.show();
		
		return progressDialog;
	}
	
	private int getIntWithLittleEndian(byte[] bytes, int offset) {
		return (0xff & bytes[offset]) | ((0xff & bytes[offset + 1]) << 8) | ((0xff & bytes[offset + 2]) << 16) | ((0xff & bytes[offset + 3]) << 24);
	}
	
	@Override
	public void run() {		
		byte [] arrayBytes = toByteArray (mContext, mUri);
		
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
	
	 private Handler mHandler = new Handler() {
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

	
	private void processText (File file) {
		String line;
		try {
			int maxLines=0;
			StringBuilder allLines = new StringBuilder ("");
			BufferedReader countReader = new BufferedReader(new FileReader(file));
			while ((line = countReader.readLine()) != null) {
				allLines.append(line+"\n");
				maxLines++;
			}
			countReader.close();
			
			mProgressDialog.setMax(maxLines);
			
			int lines =0;
			int index = 0;
			int lastIndex = 0;

			while (lines < maxLines) {
				index = allLines.indexOf("\n", lastIndex);
				line = allLines.substring(lastIndex, index);
				processLine(line);
				lines++;
				lastIndex = index+1;
				
				if (lines % (maxLines/10) == 0) {
					System.gc();
					mProgressDialog.setProgress(lines);
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	public void processLine (String stlLine) throws Exception {
		stlLine = stlLine.trim();
		if (stlLine.startsWith("facet normal ")) {
			stlLine= stlLine.replaceFirst("facet normal ", "");
			String[] normalValue = stlLine.split(" ");
			for (int j=0; j<3; j++) {
				mData.addNormal(Float.parseFloat(normalValue[0]));
				mData.addNormal(Float.parseFloat(normalValue[1]));
				mData.addNormal(Float.parseFloat(normalValue[2]));
			}
		}
		
		if (stlLine.startsWith("vertex ")) {
			stlLine= stlLine.replaceFirst("vertex ", "");
			String[] vertexValue = stlLine.split(" ");
			
			float x= Float.parseFloat(vertexValue[0]);
			float y= Float.parseFloat(vertexValue[1]);
			float z= Float.parseFloat(vertexValue[2]);
								
			mData.adjustMaxMin(x, y, z);
															
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
		}	
	}
	
	private void processBinary(byte[] stlBytes) throws Exception {			
		int vectorSize = getIntWithLittleEndian(stlBytes, 80);
						
		mProgressDialog.setMax(vectorSize);
		for (int i = 0; i < vectorSize; i++) {
			for (int j=0; j<3; j++) {
				mData.addNormal(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50)));
				mData.addNormal(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 4)));
				mData.addNormal(Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 8)));
			}
			
			float x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 12));
			float y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 16));
			float z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 20));
			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
			x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 24));
			y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 28));
			z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 32));
			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
			x = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 36));
			y = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 40));
			z = Float.intBitsToFloat(getIntWithLittleEndian(stlBytes, 84 + i * 50 + 44));
			mData.adjustMaxMin(x, y, z);
			mData.addVertex(x);
			mData.addVertex(y);
			mData.addVertex(z);
			
			if (i % (vectorSize / 10) == 0) {
				mProgressDialog.setProgress(i);
			}
		}
	}
}