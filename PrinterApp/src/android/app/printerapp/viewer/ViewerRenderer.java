package android.app.printerapp.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.app.printerapp.library.StorageModelCreation;
import android.app.printerapp.viewer.Geometry.*;


public class ViewerRenderer implements GLSurfaceView.Renderer  {
	Context mContext;
	private static String TAG = "ViewerRenderer";
	
	public static float Z_NEAR =1f;
	public static float Z_FAR = 3000f;
	
	private static float OFFSET_HEIGHT = 1f;
	private static float OFFSET_BIG_HEIGHT = 5f;
	
	private static int mWidth;
	private static int mHeight;
	
	public static float mCameraX = 0f;
	public static float mCameraY = 0f;
	public static float mCameraZ = 0f;
	
	public static float mCenterX = 0f;
	public static float mCenterY = 0f;
	public static float mCenterZ = 0f;
	
	public static float mSceneAngleX = 0f;
	public static float mSceneAngleY = 0f;
	
	public static float RED = 0.80f;
	public static float GREEN = 0.1f;
	public static float BLUE = 0.1f;
	public static float ALPHA = 0.9f;
	
	public static final int DOWN=0;
	public static final int RIGHT=1;
	public static final int BACK=2;
	public static final int LEFT=3;
	
	public static final float LIGHT_X=0;
	public static final float LIGHT_Y=0;
	public static final float LIGHT_Z=2000;
	
	public static final int NORMAL = 0;
	public static final int XRAY = 1;
	public static final int TRANSPARENT = 2;
	public static final int LAYERS = 3;
	
	private static final float OFFSET = 0.1f;

	
	private int mState;

	private List<StlObject> mStlObjectList = new ArrayList<StlObject>();
	private GcodeObject mGcodeObject;
	private WitboxPlate mWitboxFaceDown;
	private WitboxFaces mWitboxFaceRight;
	private WitboxFaces mWitboxFaceBack;
	private WitboxFaces mWitboxFaceLeft;
	private WitboxPlate mInfinitePlane;
	private List<DataStorage> mDataList;

			
	private boolean mShowLeftWitboxFace = true;
	private boolean mShowRightWitboxFace = true;
	private boolean mShowBackWitboxFace= true;
	private boolean mShowDownWitboxFace = true;
	
	public float[] final_matrix_R_Render = new float[16];
	public float[] final_matrix_S_Render = new float[16];
	public float[] final_matrix_T_Render = new float[16];
	
	private final float[] mVPMatrix = new float[16]; //Model View Projection Matrix
	private final float[] mModelMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	private final float[] mTemporaryMatrix = new float [16];
    private final static float[] invertedVPMatrix = new float[16];
    
	float[] mMVMatrix = new float[16];
	float[] mMVPMatrix = new float[16];	
	float[] mMVPObjectMatrix = new float[16];	
	float[] mMVObjectMatrix = new float[16];	
	float[] mTransInvMVMatrix = new float[16];	
	float[] mObjectModel = new float [16];
	float[] mTemporaryModel = new float[16];
	
    //Light	
	float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
	float[] mLightPosInEyeSpace = new float[4];
	float[] mLightPosInWorldSpace = new float[4];
	float[] mLightModelMatrix = new float[16];	
			
	private boolean mSnapShot = false;
	
	private boolean mIsStl;
	
	//Variables Touch events
	private int mObjectPressed=-1;
	
	//Variables for object edition
	float mDx;
	float mDy;
	float mDz;

	private float mScaleFactorX=1.0f;
	private float mScaleFactorY=1.0f;
	private float mScaleFactorZ=1.0f;
	
	private Vector mVector = new Vector (1,0,0); //default
	private float mRotateAngle=0;
	private float mTotalAngle=0;
	
	private final int INSIDE_NOT_TOUCHED = 0;
	private final int OUT = 1;
	private final int INSIDE_TOUCHED = 2;
			
	public ViewerRenderer (List<DataStorage> dataList, Context context, int state, boolean doSnapshot, boolean stl) {	
		this.mDataList = dataList;
		this.mContext = context;
		this.mState = state;
		
		this.mSnapShot = doSnapshot;
		this.mIsStl = stl;
	}
	
	public void showBackWitboxFace (boolean draw) {
		mShowBackWitboxFace = draw;
	}
	
	public void showRightWitboxFace (boolean draw) {
		mShowRightWitboxFace = draw;
	}
	
	public void showLeftWitboxFace (boolean draw) {
		mShowLeftWitboxFace = draw;
	}
	
	public void showDownWitboxFace (boolean draw) {
		mShowDownWitboxFace = draw;
	}
	
	public boolean getShowRightWitboxFace () {
		return mShowRightWitboxFace;
	}
	
	public boolean getShowLeftWitboxFace () {
		return mShowLeftWitboxFace;
	}
	
	public boolean getShowDownWitboxFace () {
		return mShowDownWitboxFace;
	}
	
	public boolean getShowBackWitboxFace () {
		return mShowBackWitboxFace;
	}
	
	public void setTransparent (boolean transparent) {
		for (int i=0; i<mStlObjectList.size(); i++) 
			mStlObjectList.get(i).setTransparent(transparent);
	}
	
	public void setXray (boolean xray) {
		for (int i=0; i<mStlObjectList.size(); i++) 
			mStlObjectList.get(i).setXray (xray);
	}
	
	public void setRotationVector (Vector vector) {
		mVector = vector;
	}
	
	public void setObjectPressed (int i) {
		mObjectPressed = i;
	}
	
	public void deleteObject (int i) {
		if (!mDataList.isEmpty()) {
			mStlObjectList.remove(i);
			mDataList.remove(i);
		}
	}
	
	public int objectPressed (float x, float y) {
		int object = -1;
		if (mDataList!=null && !mDataList.isEmpty()) {
			Ray ray = convertNormalized2DPointToRay(x, y);
			 	 
			for (int i=0; i<mDataList.size(); i++) {
		        Box objectBox = new Box (mDataList.get(i).getMinX(), mDataList.get(i).getMaxX(), mDataList.get(i).getMinY(), mDataList.get(i).getMaxY(), mDataList.get(i).getMinZ(), mDataList.get(i).getMaxZ());
		
		        // If the ray intersects (if the user touched a part of the screen that
		        // intersects the stl object's bounding box), then set objectPressed =
		        // true.		        
		        if (Geometry.intersects(objectBox, ray) && object==-1) {
		        	object = i;
		        	setObjectPressed(i);	
		        	if (mDataList.get(i).getStateObject()==INSIDE_NOT_TOUCHED) mDataList.get(i).setStateObject(INSIDE_TOUCHED);
		        }	        
		        
		        if (object!=i && mDataList.get(i).getStateObject()==INSIDE_TOUCHED) mDataList.get(i).setStateObject(INSIDE_NOT_TOUCHED);
			}       
		}
		return object;
	}
	
	public void relocate (int objectToFit) {
		DataStorage data = mDataList.get(objectToFit);
		float width = data.getMaxX() - data.getMinX();
		float deep = data.getMaxY() - data.getMinY();
		
		float setMinX=Float.MAX_VALUE;
		int index =-1;
		
		float newMaxX;
		float newMinX;
		float newMaxY;
		float newMinY;
		
		for (int i=0; i<mDataList.size(); i++) {
			if (i!= objectToFit) {
				DataStorage d = mDataList.get(i); 
				if (d.getMinX()<setMinX) {
					setMinX = d.getMinX();
					index = i;
				}
				//UP
				newMaxX = data.getMaxX();
				newMinX = data.getMinX();
				newMaxY = data.getLastCenter().y + Math.abs(d.getMaxY() - d.getLastCenter().y) + deep + OFFSET;
				newMinY = data.getLastCenter().y + Math.abs(d.getMaxY() - d.getLastCenter().y) +OFFSET; 
							
				if (fits(newMaxX, newMinX, newMaxY, newMinY, objectToFit)) {
					refreshFitCoordinates(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				}
				
				//RIGHT
				newMaxX = data.getLastCenter().x + Math.abs(d.getMaxX() - d.getLastCenter().x) + width + OFFSET;
				newMinX = data.getLastCenter().x + Math.abs(d.getMaxX() - d.getLastCenter().x) + OFFSET;
				newMaxY = data.getMaxY();
				newMinY = data.getMinY();	
						
				if (fits(newMaxX, newMinX, newMaxY, newMinY, objectToFit)) {
					refreshFitCoordinates(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				}
				
				//DOWN
				newMaxX = data.getMaxX();
				newMinX = data.getMinX();
				newMaxY = data.getLastCenter().y - (Math.abs(d.getMinY() - d.getLastCenter().y) + OFFSET);
				newMinY = data.getLastCenter().y - (Math.abs(d.getMinY() - d.getLastCenter().y) + deep + OFFSET); 	
						
				if (fits(newMaxX, newMinX, newMaxY, newMinY, objectToFit)) {
					refreshFitCoordinates(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				} 
				
				//LEFT
				newMaxX = data.getLastCenter().x - (Math.abs(d.getMinX() - d.getLastCenter().x)+ OFFSET);
				newMinX = data.getLastCenter().x - (Math.abs(d.getMinX() - d.getLastCenter().x) + width + OFFSET);
				newMaxY = data.getMaxY();
				newMinY = data.getMinY();		
						
				if (fits(newMaxX, newMinX, newMaxY, newMinY, objectToFit)) {
					refreshFitCoordinates(newMaxX, newMinX, newMaxY, newMinY, data);
					break;
				} else if (i==mDataList.size()-2) {					
					newMaxX = setMinX;
					newMinX = setMinX - width;
					newMaxY = mDataList.get(index).getMaxY();
					newMinY = mDataList.get(index).getMinY();	
					
					data.setStateObject(OUT);
					
					refreshFitCoordinates(newMaxX, newMinX, newMaxY, newMinY, data);
				}	
				
						
			}
		}
	}
	
	public boolean fits (float newMaxX, float newMinX, float newMaxY, float newMinY, int objectToFit) {
		boolean overlaps = false; 
		boolean outOfPlate = false;
		int k = 0;

		if (newMaxX > WitboxFaces.WITBOX_LONG || newMinX < -WitboxFaces.WITBOX_LONG 
				|| newMaxY > WitboxFaces.WITBOX_WITDH || newMinY < -WitboxFaces.WITBOX_WITDH) outOfPlate = true;
			
		while (!outOfPlate && !overlaps && k <mDataList.size()) {	
			if (k!=objectToFit) {
				if (Geometry.overlaps(newMaxX, newMinX, newMaxY, newMinY, mDataList.get(k)))  overlaps = true;
			}		
			k++;
		}

		if (!outOfPlate && !overlaps) 					
			return true;
		
		else return false;
	}
	
	public void refreshFitCoordinates (float newMaxX, float newMinX, float newMaxY, float newMinY, DataStorage d) {		
		d.setMaxX(newMaxX);
		d.setMinX(newMinX);
		d.setMaxY(newMaxY);
		d.setMinY(newMinY);
		
		float newCenterX = newMinX + (newMaxX-newMinX)/2;
		float newCenterY = newMinY + (newMaxY-newMinY)/2;
		float newCenterZ = d.getLastCenter().z;

		Point newCenter = new Point (newCenterX, newCenterY, newCenterZ );

		d.setLastCenter(newCenter);
		
		float [] modelMatrix = d.getModelMatrix();
		Matrix.translateM(modelMatrix, 0, newCenterX, newCenterY, newCenterZ);
		d.setModelMatrix(modelMatrix);
	}
		
	public void dragObject (float x, float y) {
		Ray ray = convertNormalized2DPointToRay(x, y);

		Point touched = Geometry.intersectionPointWitboxPlate(ray);
		           
		DataStorage data = mDataList.get(mObjectPressed);
		
        float dx = touched.x-data.getLastCenter().x;
		float dy = touched.y-data.getLastCenter().y;
		
		float maxX = data.getMaxX() + dx;
		float maxY = data.getMaxY() + dy;
		float minX = data.getMinX() + dx;
		float minY = data.getMinY() + dy;
		mDataList.get(mObjectPressed).setLastCenter(new Point (touched.x,touched.y,data.getLastCenter().z));
		
		data.setMaxX(maxX);
		data.setMaxY(maxY);
		data.setMinX(minX);
		data.setMinY(minY);
		
		//We change the colour if we are outside Witbox Plate
		if (maxX>WitboxFaces.WITBOX_LONG || minX < -WitboxFaces.WITBOX_LONG || maxY>WitboxFaces.WITBOX_WITDH || minY<-WitboxFaces.WITBOX_WITDH) 
			data.setStateObject(OUT);
		else data.setStateObject(INSIDE_TOUCHED);	
				
    }
	
	public void checkIfOverlaps () {		
		//Check if the model overlaps with another one. If this is the case, the model that is being edited turns grey.
		//User can move an object that was overlaping another one, so both should change its colour.
		for (int i=0; i<mDataList.size(); i++) {
			DataStorage data = mDataList.get(i);
			
			float maxX = data.getMaxX();
			float minX = data.getMinX();
			float maxY = data.getMaxY();
			float minY = data.getMinY();

			for (int j=0;j<mDataList.size(); j++) {
				if (i!=j)
					if (Geometry.overlaps(maxX, minX, maxY, minY, mDataList.get(j))) {
						data.setStateObject(OUT);
						break;
					}
					
					if (i==mObjectPressed) data.setStateObject(INSIDE_TOUCHED);	
					else data.setStateObject(INSIDE_NOT_TOUCHED);
					
			}
		}
	}
	
	public void scaleObject (float fx, float fy, float fz) {
		if (Math.abs(fx)>0.1 && Math.abs(fx)<10&& Math.abs(fy)>0.1 && Math.abs(fy)<10 && Math.abs(fz)>0.1 && Math.abs(fz)<10) {	
			mScaleFactorX = fx;
			mScaleFactorY = fy;
			mScaleFactorZ = fz;
			
			DataStorage data = mDataList.get(mObjectPressed);
			
			Point lastCenter = data.getLastCenter();
			
			float maxX = data.getMaxX()-lastCenter.x;
			float maxY = data.getMaxY()-lastCenter.y;
			float maxZ = data.getMaxZ();
			float minX = data.getMinX()-lastCenter.x;
			float minY = data.getMinY()-lastCenter.y;
			float minZ = data.getMinZ();
			
			float lastScaleFactorX = data.getLastScaleFactorX();
			float lastScaleFactorY = data.getLastScaleFactorY();
			float lastScaleFactorZ = data.getLastScaleFactorZ();
			
			maxX = (maxX+(Math.abs(mScaleFactorX)-Math.abs(lastScaleFactorX))*(maxX/Math.abs(lastScaleFactorX)))+lastCenter.x;
			maxY = (maxY+(mScaleFactorY-lastScaleFactorY)*(maxY/lastScaleFactorY))+lastCenter.y;
			maxZ = (maxZ+(mScaleFactorZ-lastScaleFactorZ)*(maxZ/lastScaleFactorZ))+lastCenter.z;
			
			minX = (minX+(Math.abs(mScaleFactorX)-Math.abs(lastScaleFactorX))*(minX/Math.abs(lastScaleFactorX)))+lastCenter.x;
			minY = (minY+(mScaleFactorY-lastScaleFactorY)*(minY/lastScaleFactorY))+lastCenter.y;
			minZ = (minZ+(mScaleFactorZ-lastScaleFactorZ)*(minZ/lastScaleFactorZ))+lastCenter.z;
						
			data.setMaxX(maxX);
			data.setMaxY(maxY);
			data.setMaxZ(maxZ);
			
			data.setMinX(minX);
			data.setMinY(minY);
			data.setMinZ(minZ);
									
			data.setLastScaleFactorX(mScaleFactorX);
			data.setLastScaleFactorY(mScaleFactorY);
			data.setLastScaleFactorZ(mScaleFactorZ);

			
			if (maxX>WitboxFaces.WITBOX_LONG || minX < -WitboxFaces.WITBOX_LONG 
					|| maxY>WitboxFaces.WITBOX_WITDH || minY<-WitboxFaces.WITBOX_WITDH || maxZ>WitboxFaces.WITBOX_HEIGHT) data.setStateObject(OUT);
			else checkIfOverlaps();
		}
	}
	
	public void setAngleRotationObject (float angle) {
		mRotateAngle = angle;
		mTotalAngle += angle;		
	}
	
	public void resetTotalAngle() {
		mTotalAngle = 0;
	}
	
	public void refreshRotatedObjectCoordinates () {	
		final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			protected void onPreExecute() {			
				ViewerMain.configureProgressState(View.VISIBLE);
				ViewerSurfaceView.setLockEditionMode(true);
			}
			
			@Override
			protected Void doInBackground(Void... params) {
				DataStorage data = mDataList.get(mObjectPressed);

				data.initMaxMin();
				float [] coordinatesArray = data.getVertexArray();
				float x,y,z;
				
				float [] vector = new float [4];
				float [] result = new float [4];
				float [] aux = new float [16];
				
				float[] rotationMatrix = data.getRotationMatrix();
				
				for (int i=0; i<coordinatesArray.length; i+=3) {
					vector[0] = coordinatesArray[i];
					vector[1] = coordinatesArray[i+1];
					vector[2] = coordinatesArray[i+2];
					
					Matrix.setIdentityM(aux, 0);
					Matrix.multiplyMM(aux, 0, rotationMatrix, 0, aux, 0);
					Matrix.multiplyMV(result, 0, aux, 0, vector, 0);
									
					x = result [0];
					y = result [1];
					z = result [2];
							
					data.adjustMaxMin(x, y, z);
				}		
							
				float maxX = data.getMaxX();
				float minX = data.getMinX();
				float minY = data.getMinY();
				float maxY = data.getMaxY();
				float maxZ = data.getMaxZ();
				float minZ = data.getMinZ();	
				
				Point lastCenter = data.getLastCenter();
				//We have to introduce the rest of transformations.
				maxX = maxX*Math.abs(mScaleFactorX)+lastCenter.x;
				maxY = maxY*mScaleFactorY+lastCenter.y;
				maxZ = maxZ*mScaleFactorZ+lastCenter.z;
				
				minX = minX*Math.abs(mScaleFactorX)+lastCenter.x;
				minY = minY*mScaleFactorY+lastCenter.y;
				minZ = minZ*mScaleFactorZ+lastCenter.z;	

				data.setMaxX(maxX);
				data.setMaxY(maxY);
				
				data.setMinX(minX);
				data.setMinY(minY);
			
				float adjustZ = 0;
				if (minZ!=0) adjustZ= -data.getMinZ();
				
				data.setAdjustZ(adjustZ);
				data.setMinZ(data.getMinZ()+adjustZ);			
				data.setMaxZ(data.getMaxZ()+adjustZ);
				
				if (maxX>WitboxFaces.WITBOX_LONG || minX < -WitboxFaces.WITBOX_LONG 
						|| maxY>WitboxFaces.WITBOX_WITDH || minY<-WitboxFaces.WITBOX_WITDH || maxZ>WitboxFaces.WITBOX_HEIGHT) data.setStateObject(OUT);
				else checkIfOverlaps ();
				return null;
			}
			
			protected void onPostExecute(final Void unused) {
				ViewerMain.configureProgressState(View.GONE);
				ViewerSurfaceView.setLockEditionMode(false);
			}		
		};
		
		if (mTotalAngle!=0) task.execute();
			
	}
	
	
	 private static Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {		 
	        // We'll convert these normalized device coordinates into world-space
	        // coordinates. We'll pick a point on the near and far planes, and draw a
	        // line between them. To do this transform, we need to first multiply by
	        // the inverse matrix, and then we need to undo the perspective divide.
	        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
	        final float[] farPointNdc =  {normalizedX, normalizedY,  1, 1};
	        
	        final float[] nearPointWorld = new float[4];
	        final float[] farPointWorld = new float[4];
	        
	        
	        Matrix.multiplyMV(nearPointWorld, 0, invertedVPMatrix, 0, nearPointNdc, 0);
	        Matrix.multiplyMV(farPointWorld, 0, invertedVPMatrix, 0, farPointNdc, 0);

	        // Why are we dividing by W? We multiplied our vector by an inverse
	        // matrix, so the W value that we end up is actually the *inverse* of
	        // what the projection matrix would create. By dividing all 3 components
	        // by W, we effectively undo the hardware perspective divide.
	        divideByW(nearPointWorld);
	        divideByW(farPointWorld);

	        // We don't care about the W value anymore, because our points are now
	        // in world coordinates.
	        Point nearPointRay = new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
				
	        Point farPointRay = new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

	        return new Ray(nearPointRay,  Geometry.vectorBetween(nearPointRay, farPointRay));
	 }    
	 
	  private static void divideByW(float[] vector) {
		  vector[0] /= vector[3];
	      vector[1] /= vector[3];
	      vector[2] /= vector[3];
	  }
	  
	  public float getWidthScreen () {
		  return mWidth;
	  }
	  
	  public float getHeightScreen () {
		  return mHeight;
	  }
	  
	  public void exitEditionMode () {
		  DataStorage data = mDataList.get(mObjectPressed);

		  switch (data.getStateObject()) {
		  case INSIDE_NOT_TOUCHED:
		  case INSIDE_TOUCHED:
			  data.setStateObject(INSIDE_NOT_TOUCHED);
			  break;
		  case OUT: 
			  data.setStateObject(OUT);
			  break;
		  }
	  }
	  
	  private void setColor (int object) {
		  StlObject stl = mStlObjectList.get(object);
		  switch (mDataList.get(object).getStateObject()) {
		  case INSIDE_NOT_TOUCHED:
			  stl.setColor(StlObject.colorNormal);
			  break;
		  case INSIDE_TOUCHED:
			  stl.setColor(StlObject.colorSelectedObject);
			  break;
		  case OUT: 
			  stl.setColor(StlObject.colorObjectOut);
			  break;
		  }
	  }
	  
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);		
		
		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		Matrix.setIdentityM(mModelMatrix, 0);
			
        mSceneAngleX = -40f;	

		while (!mDataList.get(mDataList.size()-1).isDrawEnabled()) ; //wait	if the last element has not been opened
	
		if (mIsStl) {
			if (mDataList.size()>1) relocate (mDataList.size()-1);

			//First, reset the stl object list
			for (int i=0; i<mStlObjectList.size(); i++)mStlObjectList.remove(i);
			//Add the new ones.
			for (int i=0; i<mDataList.size(); i++) mStlObjectList.add(new StlObject (mDataList.get(i), mContext, mState));
			
		} else mGcodeObject = new GcodeObject (mDataList.get(0), mContext);
		
		if (mSnapShot) mInfinitePlane = new WitboxPlate (mContext, true);

		mWitboxFaceBack = new WitboxFaces (BACK);
		mWitboxFaceRight = new WitboxFaces (RIGHT);
		mWitboxFaceLeft = new WitboxFaces (LEFT);
		mWitboxFaceDown = new WitboxPlate (mContext, false);
	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;
				
		// Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        	
        // this projection matrix is applied to object coordinates		
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, ratio, Z_NEAR, Z_FAR);	
        
        if (mSnapShot) {
        	DataStorage data = mDataList.get(0);
	        float h = data.getHeight();
	        float l = data.getLong();
	        float w = data.getWidth();
	        
	        l = l/ratio; //We calculate the height related to the square in the frustum with this width 
	        w = w/ratio;
	        
	        float dh = (float) (h / (Math.tan(Math.toRadians(45/2))));
	        float dl = (float) (l/ (2*Math.tan(Math.toRadians(45/2))));
	        float dw = (float) (w/ (2*Math.tan(Math.toRadians(45/2))));
	        
	        if (dw>dh && dw>dl) mCameraZ = OFFSET_BIG_HEIGHT*h;
	        else if (dh>dl) mCameraZ = OFFSET_HEIGHT*h;
	        else mCameraZ = OFFSET_BIG_HEIGHT*h;
	        
	        dl = dl + Math.abs(data.getMinY());
	        dw = dw + Math.abs(data.getMinX());
	        
	        if (dw>dh && dw>dl) mCameraY = - dw;
	        else if (dh>dl) mCameraY = -dh;
	        else mCameraY = - dl;        
        } else {
        	mCameraY = -300f;
        	mCameraZ = 300f;
        }
	}

	@Override
	public void onDrawFrame(GL10 unused) {
		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (mIsStl) 
        	for (int i=0; i<mStlObjectList.size(); i++)
        		setColor(i);
        
	    GLES20.glEnable (GLES20.GL_BLEND);
	 	
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, mCameraX, mCameraY, mCameraZ, mCenterX, mCenterY, mCenterZ, 0f, 0.0f, 1.0f);
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
                        
        //Set Identity
        Matrix.setIdentityM(mRotationMatrix, 0);
                        
        //Rotation x       
        Matrix.rotateM(mRotationMatrix, 0, mSceneAngleX, 0.0f, 0.0f, 1.0f);
        
        //RotationY
        Matrix.rotateM(mRotationMatrix, 0, mSceneAngleY, 1.0f, 0.0f, 0.0f);
        
        //Reset angle, we store the rotation in the matrix
        mSceneAngleX=0;
        mSceneAngleY=0;
        
        //Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mRotationMatrix, 0, mModelMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);
        
        // Rotate the object taking the overall rotation into account.
        
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
  
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(mMVPMatrix, 0,mVPMatrix, 0, mModelMatrix, 0);  
        Matrix.multiplyMM(mMVMatrix, 0,mViewMatrix, 0, mModelMatrix, 0);         
        
        Matrix.invertM(invertedVPMatrix, 0, mMVPMatrix, 0);
        
        //Set Light direction  
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, LIGHT_X, LIGHT_Y, LIGHT_Z);      
      	               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);
      
        if (mDataList.size()>0 && mObjectPressed!=-1) {
	        DataStorage data = mDataList.get(mObjectPressed);
	        Point center = data.getLastCenter();
	
	        Matrix.setIdentityM(mTemporaryModel, 0);
	        Matrix.translateM(mTemporaryModel, 0, center.x, center.y, center.z);  
	        Matrix.scaleM(mTemporaryModel, 0, data.getLastScaleFactorX(), data.getLastScaleFactorY(), data.getLastScaleFactorZ());
	        
	        Matrix.translateM(mTemporaryModel, 0, 0, 0, data.getAdjustZ());
	
	        //Object rotation  
	        float [] rotateObjectMatrix = data.getRotationMatrix();
	        Matrix.rotateM(rotateObjectMatrix, 0, mRotateAngle, mVector.x, mVector.y, mVector.z);
	
	        if (mObjectPressed!=-1) mDataList.get(mObjectPressed).setRotationMatrix(rotateObjectMatrix);
	
	        //Reset angle, we store the rotation in the matrix
	        mRotateAngle=0;
	
	        //Multiply the model by the accumulated rotation
	        Matrix.multiplyMM(mObjectModel, 0, mTemporaryModel, 0,rotateObjectMatrix, 0);     	
	        Matrix.multiplyMM(mMVPObjectMatrix, 0,mMVPMatrix, 0, mObjectModel, 0);   

	        Matrix.multiplyMM(mMVObjectMatrix, 0,mMVMatrix, 0, mObjectModel, 0);   
	        Matrix.transposeM(mTransInvMVMatrix, 0, mMVObjectMatrix, 0);
	        Matrix.invertM(mTransInvMVMatrix, 0, mTransInvMVMatrix, 0);
        }
	                                                                                              
        if (mIsStl) 
    		for (int i=0; i<mStlObjectList.size(); i++) 
    			if (i==mObjectPressed) {
    				mDataList.get(i).setModelMatrix(mObjectModel);

    				mStlObjectList.get(i).draw(mMVPObjectMatrix, mTransInvMVMatrix, mLightPosInEyeSpace);
    			} else  {
    				float [] modelMatrix = mDataList.get(i).getModelMatrix();
    				float [] mvpMatrix = new float[16];
    				float [] mvMatrix = new float[16];
    				float [] mvFinalMatrix = new float[16];

    				Matrix.multiplyMM(mvpMatrix, 0,mMVPMatrix, 0, modelMatrix, 0);  
    				
    				Matrix.multiplyMM(mvMatrix, 0,mMVMatrix, 0, modelMatrix, 0);  
    				
    		        Matrix.transposeM(mvFinalMatrix, 0, mvMatrix, 0);
    		        Matrix.invertM(mvFinalMatrix, 0, mvFinalMatrix, 0);
    		        
    				mStlObjectList.get(i).draw(mvpMatrix, mvFinalMatrix, mLightPosInEyeSpace);
    			}
        else 
        	mGcodeObject.draw(mMVPMatrix); 
        

        if (mSnapShot) {
        	mInfinitePlane.draw(mMVPMatrix, mMVMatrix);
        	takeScreenShot(unused);
        } else {
        	if (mShowDownWitboxFace) mWitboxFaceDown.draw(mMVPMatrix, mMVMatrix);      
        	if (mShowBackWitboxFace) mWitboxFaceBack.draw(mMVPMatrix);
        	if (mShowRightWitboxFace) mWitboxFaceRight.draw(mMVPMatrix);
        	if (mShowLeftWitboxFace) mWitboxFaceLeft.draw(mMVPMatrix);
        }        
	}
	
	private void takeScreenShot (GL10 unused) {	
    	Log.i(TAG, "TAKING SNAPSHOT");
		int minX = 0;
		int minY = 0; 
        
        int screenshotSize = mWidth * mHeight;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        

        GLES20.glReadPixels(minX, minY, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);
        StorageModelCreation.saveScreenShot(mWidth, mHeight, bb);
	}

	public void setSceneAngleX (float x) {
		mSceneAngleX += x;	
	}
	
	public void setSceneAngleY (float y) {
		mSceneAngleY += y;
	}
	
	public void setCameraPosX (float x) {
		mCameraX = x;
	}
	
	public void setCameraPosY (float y) {
		mCameraY = y;
	}
	
	public void setCameraPosZ (float z) {
		mCameraZ = z;
	}
	
	public float getCameraPosX () {
		return mCameraX;
	}
	
	public float getCameraPosY () {
		return mCameraY;
	}
	
	public float getCameraPosZ () {
		return mCameraZ;
	}
	
	public void setCenterX (float x) {
		mCenterX += x;
	}
	
	public void setCenterY (float y) {
		mCenterY += y;
	}
	
	public void setCenterZ (float z) {
		mCenterZ += z;
	}
	
	public void setZNear (float h) {
		double ang = Math.toRadians(45/2);
		float valor = (float) Math.tan(ang);
		
		Z_NEAR = valor*(h/2); 
	}
	
	
	   /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){

        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }
    
    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
     public static void checkGlError(String glOperation) {
         int error;
         while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
             Log.e(TAG, glOperation + ": glError " + error);
             throw new RuntimeException(glOperation + ": glError " + error);
         }
     }
}