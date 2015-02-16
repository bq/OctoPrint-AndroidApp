package android.app.printerapp.viewer;

import android.app.printerapp.Log;
import android.app.printerapp.viewer.Geometry.Point;
import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;

public class DataStorage {		
	private List<Float> mVertexList = new ArrayList<Float>();
	private List<Float> mNormalList = new ArrayList<Float>();
	private List<Integer> mLineLengthList = new ArrayList<Integer>();
	private List<Integer> mLayerList = new ArrayList<Integer>();
	private List<Integer> mTypeList = new ArrayList<Integer>();
	
	private float[] mVertexArray;
	private float[] mNormalArray;
	private int [] mLayerArray;
	private int[] mTypeArray;
		
	private int mMaxLayer;
	private int mActualLayer;
	private int mMaxLines;
	
	private float mMinX;
	private float mMaxX;
	private float mMinY;
	private float mMaxY;
	private float mMinZ;
	private float mMaxZ;
	
	private String mPath;
	private String mPathSnapshot;
		
	public static final int MOVE = 0;
	public static final int FILL = 1;
	public static final int PERIMETER = 2;
	public static final int RETRACT = 3;
	public static final int COMPENSATE = 4;
	public static final int BRIDGE=5;
	public static final int SKIRT=6;
	public static final int WALL_INNER = 7;
	public static final int WALL_OUTER = 8;
	public static final int SUPPORT = 9;
	
	public static final int TRIANGLE_VERTEX = 3;
	
	private float [] mRotationMatrix = new float[16];
	private float [] mModelMatrix = new float[16];
	private float mLastScaleFactorX= 1.0f;
	private float mLastScaleFactorY= 1.0f;
	private float mLastScaleFactorZ= 1.0f;	
	private Point mLastCenter = new Point (0,0,0);
	private int mStateObject;
	private float mAdjustZ;

    public static final double MIN_Z = 0.1;
	
	public DataStorage () {
		Matrix.setIdentityM(mRotationMatrix, 0);
		Matrix.setIdentityM(mModelMatrix, 0);
	}
	

	public void copyData (DataStorage d) {
		for (int i=0; i<d.getLineLengthList().size(); i++) mLineLengthList.add(d.getLineLengthList().get(i));
	
		mVertexArray = new float[d.getVertexArray().length];
		for (int i=0; i<d.getVertexArray().length; i++) {
			mVertexArray[i] = d.getVertexArray()[i];
		}
		
		mNormalArray = new float[d.getNormalArray().length];
		for (int i=0; i<d.getNormalArray().length; i++) {
			mNormalArray[i] = d.getNormalArray()[i];
		}

		mMaxLayer = d.getMaxLayer();
		mActualLayer = d.getActualLayer();
		mMinX = d.getMinX();
		mMinY = d.getMinY();
		mMinZ = d.getMinZ();
		mMaxX = d.getMaxX();
		mMaxY = d.getMaxY();
		mMaxZ = d.getMaxZ();

		mPath = d.getPathFile();
		mPathSnapshot = d.getPathSnapshot();
		
		mLastScaleFactorX = d.getLastScaleFactorX();
		mLastScaleFactorY = d.getLastScaleFactorY();
		mLastScaleFactorZ = d.getLastScaleFactorZ();
		
		mAdjustZ = d.getAdjustZ();
		
		mLastCenter = new Point (d.getLastCenter().x, d.getLastCenter().y, d.getLastCenter().z);
		
	    for (int i=0; i<mRotationMatrix.length; i++) mRotationMatrix[i] = d.getRotationMatrix()[i];	
	    for (int i=0; i<mModelMatrix.length; i++) mModelMatrix[i] = d.getModelMatrix()[i];	   	       
	}
	
	public void setMaxLinesFile (int maxLines) {
		mMaxLines = maxLines;
	}
	
	public int getMaxLinesFile () {
		return mMaxLines;
	}
	
	public int getCoordinateListSize () {
		return mVertexList.size();
	}
	
	public void addVertex (float v) {
		mVertexList.add(v);
	}
	
	public void addLayer (int layer) {
		mLayerList.add(layer);
	}

	public void addType (int type) {
		mTypeList.add(type);
	}
	
	public void addNormal (float normal) {
		mNormalList.add(normal);
	}
	
	
	public void addLineLength (int length) {
		mLineLengthList.add(length);
	}
	
	public void fillVertexArray (boolean center) {
		mVertexArray = new float [mVertexList.size()];

		centerSTL(center);
	}
	
	public void initMaxMin () {
		setMaxX(-Float.MAX_VALUE);
		setMaxY(-Float.MAX_VALUE);
		setMaxZ(-Float.MAX_VALUE);
		setMinX(Float.MAX_VALUE);
		setMinY(Float.MAX_VALUE);
		setMinZ(Float.MAX_VALUE);		
	}
	
	public void centerSTL(boolean center){

		float distX = 0;
		float distY = 0;
        float distZ = mMinZ;

        if (center){

            distX = mMinX + (mMaxX - mMinX)/2;
            distY = mMinY + (mMaxY - mMinY)/2;

            //Show the model slightly above the plate
            distZ = mMinZ - (float)MIN_Z;

        }



        Log.i("PrintView", distZ + "");
				
		for (int i = 0; i < mVertexList.size(); i=i+3) {
		    mVertexArray[i] = mVertexList.get(i)   - distX;
			mVertexArray[i+1] = mVertexList.get(i+1) - distY;
			mVertexArray[i+2] = mVertexList.get(i+2) - distZ;		
		}
		
		//Adjust max, min
		mMinX = mMinX - distX;
		mMaxX = mMaxX - distX;
		mMinY = mMinY - distY;
		mMaxY = mMaxY - distY;
		mMinZ = mMinZ - distZ;
		mMaxZ = mMaxZ - distZ;		
	}
	
	public void fillNormalArray() {
		mNormalArray = new float [mNormalList.size()*TRIANGLE_VERTEX];	
		int index =0;

		float x;
		float y;
		float z;
		
		for (int i=0; i<mNormalList.size(); i+=3) {
			x = mNormalList.get(i);
			y = mNormalList.get(i+1);
			z = mNormalList.get(i+2);
			
			for (int j=0; j<TRIANGLE_VERTEX; j++) {
				mNormalArray[index] = x;
				mNormalArray[index+1] = y;
				mNormalArray[index+2] = z;
				index+=3;
			}
			
		}
	}
	
	public void fillLayerArray () {
		mLayerArray = new int [mLayerList.size()];
		
		for (int i=0; i<mLayerList.size(); i++) {
			mLayerArray[i] = mLayerList.get(i);
		}
		
	}
	
	public void fillTypeArray () {
		mTypeArray = new int [mTypeList.size()];
		
		for (int i=0; i<mTypeList.size(); i++) {
			mTypeArray [i] = mTypeList.get(i);
		}		
	}
	
	public float[] getVertexArray () {
		return mVertexArray;
	}
	
	public float[] getNormalArray() {
		return mNormalArray;
	}
	
	public int[] getTypeArray() {
		return mTypeArray;
	}
	
	public int[] getLayerArray() {
		return mLayerArray;
	}
	
	public void clearVertexList() {
		mVertexList.clear();
	}
	
	public void clearNormalList() {
		mNormalList.clear();
	}
	
	public void clearLayerList() {
		mLayerList.clear();
	}
	
	public void clearTypeList(){
		mTypeList.clear();
	}
	
	public List<Integer> getLineLengthList () {
		return mLineLengthList;
	}
	
	public void changeTypeAtIndex (int index, int type) {
		mTypeList.set(index, type);
	}
	
	public int getTypeListSize () {
		return mTypeList.size();
	}
	
	public void setActualLayer(int layer) {
		mActualLayer = layer;
	}
	
	public int getActualLayer () {
		return mActualLayer;
	}
	
	
	public void setMaxLayer(int maxLayer) {
		mMaxLayer = maxLayer;
		mActualLayer = maxLayer;
	}
	
	public int getMaxLayer () {
		return mMaxLayer;
	}
	
	public float getHeight () {
		return mMaxZ-mMinZ;
	}

	public float getWidth () {
		return mMaxY-mMinY;
	}
	
	public float getLong () {
		return mMaxX-mMinX;
	}
	
	public void adjustMaxMin(float x, float y, float z) { 
		if (x > mMaxX) {
			mMaxX = x;
		}
		if (y > mMaxY) {
			mMaxY = y;
		}
		
		if (z > mMaxZ) {
			mMaxZ = z;
		}
		if (x < mMinX) {
			mMinX = x;
		}
		if (y < mMinY) {
			mMinY = y;
		}
		if (z < mMinZ) {
			mMinZ = z;
		}		
	}
	
	public void setMinX (float x) {
		mMinX = x;
	}
	
	public float getMinX () {
		return mMinX;
	}
	
	public void setMinY (float y) {
		mMinY = y;
	}
	
	public float getMinY () {
		return mMinY;
	}
	
	public void setMinZ (float z) {
		mMinZ = z;
	}
	
	public float getMinZ () {
		return mMinZ;
	}
	
	public void setMaxX (float x) {
		mMaxX = x;
	}
	
	public float getMaxX () {
		return mMaxX;
	}
	
	public void setMaxY (float y) {
		mMaxY = y;
	}
	
	public float getMaxY () {
		return mMaxY;
	}
	
	public void setMaxZ (float z) {
		mMaxZ = z;
	}
	
	public float getMaxZ () {
		return mMaxZ;
	}
		
	public void setPathFile (String path) {
		mPath = path;
	}
	
	public String getPathFile () {
		return mPath;
	}
	
	public void setPathSnapshot (String path) {
		mPathSnapshot = path;
	}
	
	public String getPathSnapshot () {
		return mPathSnapshot;
	}
	
	/************************* EDITION INFORMATION ********************************/
	public void setLastCenter (Point p) {
        mLastCenter = p;
	}

    public Point getTrueCenter() {

        float x = (mMaxX + mMinX) / 2;
        float y = (mMaxY + mMinY) / 2;
        float z = (mMaxZ + mMinZ) / 2;

        return new Point(x,y,z);

    }

	public Point getLastCenter () {
		return mLastCenter;
	}
	
	public void setRotationMatrix (float [] m) {
		for (int i=0;i<mRotationMatrix.length; i++) {
			mRotationMatrix[i] = m[i];
		}
	}
	
	public float[] getRotationMatrix () {
		return mRotationMatrix;
	}
	
	public void setModelMatrix (float [] m) {
		for (int i=0;i<mModelMatrix.length; i++) {
			mModelMatrix[i] = m[i];
		}
	}
	
	public float[] getModelMatrix () {
		return mModelMatrix;
	}
	
	public void setLastScaleFactorX (float f) {
		mLastScaleFactorX=f;
	}
	 
	public void setLastScaleFactorY (float f) {
		mLastScaleFactorY=f;
	}
	
	public void setLastScaleFactorZ (float f) {
		mLastScaleFactorZ=f;
	}
	
	public float getLastScaleFactorX () {
		return mLastScaleFactorX;
	}
	 
	public float getLastScaleFactorY () {
		return mLastScaleFactorY;
	}
	
	public float getLastScaleFactorZ () {
		return mLastScaleFactorZ;
	}
	
	public void setStateObject (int state) {
		mStateObject = state;
	}
	
	public int getStateObject() {
		return mStateObject;
	}
	
	public void setAdjustZ (float z) {
		mAdjustZ = z;
	}
	
	public float getAdjustZ () {
		return mAdjustZ;
	}
}