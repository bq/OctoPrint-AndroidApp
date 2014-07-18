package android.app.printerapp.viewer;

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
	
	private boolean mEnableDraw = false;
	
	private int mMaxLayer;
	private int mActualLayer;
	
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
	
	
	public List<Float> get () {
		return mVertexList;
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
	
	public void fillVertexArray () {
		mVertexArray = new float [mVertexList.size()];
		
		centerSTL();
	}
	
	public void initMaxMin () {
		setMaxX(-Float.MAX_VALUE);
		setMaxY(-Float.MAX_VALUE);
		setMaxZ(-Float.MAX_VALUE);
		setMinX(Float.MAX_VALUE);
		setMinY(Float.MAX_VALUE);
		setMinZ(Float.MAX_VALUE);		
	}
	
	public void centerSTL(){	
		float distX = mMinX + (mMaxX - mMinX)/2;
		float distY = mMinY + (mMaxY - mMinY)/2;
		float distZ = mMinZ;
						
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
		mNormalArray = new float [mNormalList.size()];
		
		for (int i=0; i<mNormalList.size(); i++) {
			mNormalArray[i] = mNormalList.get(i);
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
	
	public void enableDraw () {
		mEnableDraw = true;
	}
	
	public boolean isDrawEnabled() {
		if (mEnableDraw) return true;
		return false;
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
}