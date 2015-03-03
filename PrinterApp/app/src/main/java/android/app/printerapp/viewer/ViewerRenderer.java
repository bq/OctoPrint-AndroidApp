package android.app.printerapp.viewer;

import android.app.printerapp.Log;
import android.app.printerapp.library.LibraryModelCreation;
import android.app.printerapp.viewer.Geometry.Box;
import android.app.printerapp.viewer.Geometry.Point;
import android.app.printerapp.viewer.Geometry.Ray;
import android.app.printerapp.viewer.Geometry.Vector;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.AsyncTask;
import android.view.View;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class ViewerRenderer implements GLSurfaceView.Renderer  {
	Context mContext;
	private static String TAG = "ViewerRenderer";
	
	public static float Z_NEAR =1f;
	public static float Z_FAR = 3000f;
	
	private static float OFFSET_HEIGHT = 2f;
	private static float OFFSET_BIG_HEIGHT = 5f;

    private static final float ANGLE_X = 0f;
    private static final float ANGLE_Y = -5f;
    private static final float CAMERA_DEFAULT_X = 0f;
    private static final float CAMERA_DEFAULT_Y = -300f;
    private static final float CAMERA_DEFAULT_Z = 350f;
    private static final float POSITION_DEFAULT_X = 0f;
    private static final float POSITION_DEFAULT_Y = -50f;
	
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
    public static float mCurrentSceneAngleX = 0f;
    public static float mCurrentSceneAngleY = 0f;
	
	public static float RED = 0.80f;
	public static float GREEN = 0.1f;
	public static float BLUE = 0.1f;
	public static float ALPHA = 0.9f;
	
	public static final int DOWN=0;
	public static final int RIGHT=1;
	public static final int BACK=2;
	public static final int LEFT=3;
    public static final int FRONT=4;
    public static final int TOP=5;
	
	public static final float LIGHT_X=0;
	public static final float LIGHT_Y=0;
	public static final float LIGHT_Z=2000;
	
	public static final int NORMAL = 0;
	public static final int XRAY = 1;
	public static final int TRANSPARENT = 2;
	public static final int LAYERS = 3;

	private int mState;

	private List<StlObject> mStlObjectList = new ArrayList<StlObject>();
	private GcodeObject mGcodeObject;
	private WitboxPlate mWitboxFaceDown;
	private WitboxFaces mWitboxFaceRight;
	private WitboxFaces mWitboxFaceBack;
	private WitboxFaces mWitboxFaceLeft;
    private WitboxFaces mWitboxFaceFront;
    private WitboxFaces mWitboxFaceTop;
	private WitboxPlate mInfinitePlane;
	private List<DataStorage> mDataList;

			
	private boolean mShowLeftWitboxFace = true;
	private boolean mShowRightWitboxFace = true;
	private boolean mShowBackWitboxFace= true;
	private boolean mShowDownWitboxFace = true;
    private boolean mShowFrontWitboxFace = true;
    private boolean mShowTopWitboxFace = true;
	
	public float[] final_matrix_R_Render = new float[16];
	public float[] final_matrix_S_Render = new float[16];
	public float[] final_matrix_T_Render = new float[16];
	
	private final float[] mVPMatrix = new float[16]; //Model View Projection Matrix
	private final float[] mModelMatrix = new float[16];
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	private final float[] mTemporaryMatrix = new float [16];
    private final static float[] invertedMVPMatrix = new float[16];
    
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
			
	private int mMode = 0;
		
	//Variables Touch events
	private int mObjectPressed=-1;
	
	//Variables for object edition
	float mDx = POSITION_DEFAULT_X;
	float mDy = POSITION_DEFAULT_Y;
	float mDz;

	private float mScaleFactorX=1.0f;
	private float mScaleFactorY=1.0f;
	private float mScaleFactorZ=1.0f;
	
	private Vector mVector = new Vector (1,0,0); //default
	
	public final static int INSIDE_NOT_TOUCHED = 0;
	public final static int OUT_NOT_TOUCHED = 1;
	public final static int INSIDE_TOUCHED = 2;
	public final static int OUT_TOUCHED = 3;

    private Circles mCircle;
    private int[] mPlate;

    private int mAxis = -1;
			
	public ViewerRenderer (List<DataStorage> dataList, Context context, int state, int mode) {	
		this.mDataList = dataList;
		this.mContext = context;
		this.mState = state;
		
		this.mMode = mode;
        this.mPlate = ViewerMainFragment.getCurrentPlate();
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
	
	public void setOverhang (boolean overhang) {
		for (int i=0; i<mStlObjectList.size(); i++) 
			mStlObjectList.get(i).setOverhang (overhang);
	}
	
	public void setRotationVector (Vector vector) {
		mVector = vector;
	}

    public void setCurrentaxis ( int axis) {

        mAxis = axis;

    }
	
	public void setObjectPressed (int i) {
		mObjectPressed = i;
	}

	public void deleteObject (int i) {
		if (!mDataList.isEmpty()) {
			mStlObjectList.remove(i);
			mDataList.remove(i);
            mObjectPressed = -1;
            changeTouchedState();

		}

	}
	
	private boolean isStl() {
		if (mDataList.size()>0)
			if (mDataList.get(0).getPathFile().endsWith(".stl") || mDataList.get(0).getPathFile().endsWith(".STL")) return true;
		
		return false;
	}
	
	public int objectPressed (float x, float y) {
		int object = -1;
		if (mDataList!=null && !mDataList.isEmpty()) {
			Ray ray = convertNormalized2DPointToRay(x, y);
			 	 
			for (int i=0; i<mDataList.size(); i++) {
		        Box objectBox = new Box (mDataList.get(i).getMinX(), mDataList.get(i).getMaxX(), mDataList.get(i).getMinY(), mDataList.get(i).getMaxY(), mDataList.get(i).getMinZ(), mDataList.get(i).getMaxZ());

		        // If the ray intersects (if the user touched a part of the screen that
		        // intersects the stl object's bounding box), then set objectPressed 	        
		        if (Geometry.intersects(objectBox, ray)) {
		        	object = i;
		        	break;
		        }               
			}       
		}
		
		if (mObjectPressed!=object && object!=-1) setObjectPressed(object);	
		changeTouchedState();
		return object;
	}

	public void changeTouchedState () {
		for (int i=0; i<mDataList.size();i++) {
			DataStorage d = mDataList.get(i);
			if (i==mObjectPressed) {
				if (!Geometry.isValidPosition(d.getMaxX(), d.getMinX(), d.getMaxY(), d.getMinY(), mDataList, i)) mDataList.get(i).setStateObject(OUT_TOUCHED);
				else mDataList.get(i).setStateObject(INSIDE_TOUCHED);
			} else {
				if (!Geometry.isValidPosition(d.getMaxX(), d.getMinX(), d.getMaxY(), d.getMinY(), mDataList, i)) mDataList.get(i).setStateObject(OUT_NOT_TOUCHED);
				else mDataList.get(i).setStateObject(INSIDE_NOT_TOUCHED);
			}
		}
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

        //Out of the plate
        if (maxX > mPlate[0] + data.getLong()|| minX < -mPlate[0] - data.getLong()
                || maxY > mPlate[1] + data.getWidth() || minY < -mPlate[1] - data.getWidth()) {

            return;

        } else {
            mDataList.get(mObjectPressed).setLastCenter(new Point (touched.x,touched.y,data.getLastCenter().z));

            data.setMaxX(maxX);
            data.setMaxY(maxY);
            data.setMinX(minX);
            data.setMinY(minY);

            /******
             * Calculate new center by adding all previous centers
             ******/
            float finalx = 0;
            float finaly = 0;
            int i = 0;

            for (DataStorage element : mDataList){

                finalx += element.getLastCenter().x;
                finaly += element.getLastCenter().y;
                i++;

            }

            finalx = finalx/i;
            finaly = finaly/i;

            ViewerMainFragment.setSlicingPosition(finalx,finaly);
        }



    }
	

	
	public void scaleObject (float fx, float fy, float fz, boolean error) {
		if (/*Math.abs(fx)>0.1 && */Math.abs(fx)<10 && /*Math.abs(fy)>0.1 && */Math.abs(fy)<10 &&/* Math.abs(fz)>0.1 && */Math.abs(fz)<10) {	//Removed min value
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

            //Out of the plate
            if (maxX > mPlate[0] || minX < -mPlate[0]
                    || maxY > mPlate[1] || minY < -mPlate[1]) {

                if (error){
                    if (maxX > mPlate[0] || minX < -mPlate[0]) ViewerMainFragment.displayErrorInAxis(0);
                    if (maxY > mPlate[1] || minY < -mPlate[1]) ViewerMainFragment.displayErrorInAxis(1);
                }


                return;

            }else {

                data.setMaxX(maxX);
                data.setMaxY(maxY);
                data.setMaxZ(maxZ);

                data.setMinX(minX);
                data.setMinY(minY);
                data.setMinZ(minZ);

                data.setLastScaleFactorX(mScaleFactorX);
                data.setLastScaleFactorY(mScaleFactorY);
                data.setLastScaleFactorZ(mScaleFactorZ);
            }


		}
	}

    /**
     * Changed rotation logic to rotate around plate's global axes
     *
     * Alberto
     * @param angle degrees to rotate
     */
	public void setRotationObject (float angle) {
		DataStorage data = mDataList.get(mObjectPressed);


        //Get the object's rotation matrix
		float [] rotateObjectMatrix = data.getRotationMatrix();

        Point center = data.getLastCenter();

        float[] mTemporaryMatrix = new float[16];
        float[] mFinalMatrix = new float[16];

        //Set a new identity matrix
        Matrix.setIdentityM(mTemporaryMatrix,0);

        //Move the matrix to the origin
        Matrix.translateM(mTemporaryMatrix, 0, 0.0f, 0.0f, 0.0f);

        //Rotate in the origin
        Matrix.rotateM(mTemporaryMatrix, 0, angle, mVector.x, mVector.y, mVector.z);

        //Multiply by the object's matrix to get the new position
        Matrix.multiplyMM(mFinalMatrix, 0, mTemporaryMatrix, 0, rotateObjectMatrix, 0);


        //Set the new rotation matrix
        data.setRotationMatrix(mFinalMatrix);
	}

	public void refreshRotatedObjectCoordinates () {	
		final AsyncTask<Void, Void, Void> task = new AsyncTask<Void, Void, Void>() {

			protected void onPreExecute() {
				ViewerMainFragment.configureProgressState(View.VISIBLE);
			}
			
			@Override
			protected Void doInBackground(Void... params) {

                //TODO Random crash
                try{

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
                    if (minZ!=0) adjustZ = - data.getMinZ() + (float)DataStorage.MIN_Z; //TODO CHECK

                    data.setAdjustZ(adjustZ);
                    data.setMinZ(minZ+adjustZ); //Readjust min and max
                    data.setMaxZ(maxZ+adjustZ);

                } catch (ArrayIndexOutOfBoundsException e ){

                    e.printStackTrace();
                }



				return null;
			}
			
			protected void onPostExecute(final Void unused) {
				ViewerMainFragment.configureProgressState(View.GONE);
                ViewerMainFragment.displayModelSize(mObjectPressed);

			}		
		};
		
		 task.execute();
			
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
	        
	        
	        Matrix.multiplyMV(nearPointWorld, 0, invertedMVPMatrix, 0, nearPointNdc, 0);
	        Matrix.multiplyMV(farPointWorld, 0, invertedMVPMatrix, 0, farPointNdc, 0);

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
	    
	  private void setColor (int object) {
		  StlObject stl = mStlObjectList.get(object);
		  switch (mDataList.get(object).getStateObject()) {
		  case INSIDE_NOT_TOUCHED:
			  stl.setColor(StlObject.colorNormal);
			  break;
		  case INSIDE_TOUCHED:
			  stl.setColor(StlObject.colorSelectedObject);
			  break;
		  case OUT_NOT_TOUCHED: 
			  stl.setColor(StlObject.colorObjectOut);
			  break;
		  case OUT_TOUCHED: 
			  stl.setColor(StlObject.colorObjectOutTouched);
			  break;
		  }
	  }

    public void generatePlate(int[] type){

        try{

            mPlate = type;

            //Create plate to pre-generate the plate
            if (mMode == ViewerMainFragment.PRINT_PREVIEW){
                mWitboxFaceBack = new WitboxFaces (BACK, mPlate);
                mWitboxFaceRight = new WitboxFaces (RIGHT, mPlate);
                mWitboxFaceLeft = new WitboxFaces (LEFT, mPlate);
                mWitboxFaceFront = new WitboxFaces (FRONT, mPlate);
                mWitboxFaceTop = new WitboxFaces (TOP, mPlate);
                mWitboxFaceDown = new WitboxPlate (mContext, false, mPlate);
            }


            mWitboxFaceBack.generatePlaneCoords(BACK, type);
            mWitboxFaceRight.generatePlaneCoords(RIGHT, type);
            mWitboxFaceLeft.generatePlaneCoords(LEFT, type);
            mWitboxFaceFront.generatePlaneCoords(FRONT, type);
            mWitboxFaceTop.generatePlaneCoords(TOP, type);
            mWitboxFaceDown.generatePlaneCoords(type, false);


        } catch (NullPointerException e){

           e.printStackTrace();
        }


    }

	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {

		// Set the background frame color
		//GLES20.glClearColor( 0.9f, 0.9f, 0.9f, 1.0f);
        GLES20.glClearColor( 0.149f, 0.196f, 0.22f, 1.0f);

		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

		Matrix.setIdentityM(mModelMatrix, 0);
        mCurrentSceneAngleX = 0f;
        mCurrentSceneAngleY = 0f;
			
        mSceneAngleX = ANGLE_X;
        mSceneAngleY = ANGLE_Y;
        
        if (mDataList.size()>0)
			if (isStl()) {

				//First, reset the stl object list
				mStlObjectList.clear();
	
				//Add the new ones.
				for (int i=0; i<mDataList.size(); i++) {
					if (mDataList.get(i).getVertexArray()!=null) {

                        Log.i("VERTEX", "adding");
                        mStlObjectList.add(new StlObject (mDataList.get(i), mContext, mState));
                    }
				    else Log.i("VERTEX", "ONE NULL " + i);
                }
				
			} else if (mDataList.size()>0) {

                //TODO Random crash
                try {
                    mGcodeObject = new GcodeObject (mDataList.get(0), mContext);
                } catch (NullPointerException e){
                    e.printStackTrace();
                }

            }


		
		if (mMode == ViewerMainFragment.DO_SNAPSHOT || mMode == ViewerMainFragment.PRINT_PREVIEW) mInfinitePlane = new WitboxPlate (mContext, true, ViewerMainFragment.getCurrentPlate());

        mWitboxFaceBack = new WitboxFaces (BACK, mPlate);
        mWitboxFaceRight = new WitboxFaces (RIGHT, mPlate);
        mWitboxFaceLeft = new WitboxFaces (LEFT, mPlate);
        mWitboxFaceFront = new WitboxFaces (FRONT, mPlate);
        mWitboxFaceTop = new WitboxFaces (TOP, mPlate);
        mWitboxFaceDown = new WitboxPlate (mContext, false, mPlate);


        mCircle = new Circles();

	}

	@Override
	public void onSurfaceChanged(GL10 unused, int width, int height) {
		mWidth = width;
		mHeight = height;

        Log.i("OUT", "Width: " + width + " ; Height: " + height);

		// Adjust the viewport based on geometry changes,
        // such as screen rotation
        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;
        	
        // this projection matrix is applied to object coordinates		
        Matrix.perspectiveM(mProjectionMatrix, 0, 45, ratio, Z_NEAR, Z_FAR);
        
        if (mMode == ViewerMainFragment.DO_SNAPSHOT || mMode == ViewerMainFragment.PRINT_PREVIEW) {
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

            mDx = - data.getLastCenter().x;
            mDy = - data.getLastCenter().y;

            mSceneAngleX = -40f;
            mSceneAngleY = 0f;

        } else {

        	mCameraY = CAMERA_DEFAULT_Y;
        	mCameraZ = CAMERA_DEFAULT_Z;
        }

	}

    /**
     * Translate the matrix x;y pixels
     * Check for screen limits
     * @param x
     * @param y
     * @param z
     */
    void matrixTranslate(float x, float y, float z)
    {

        // Translate slots.
        mDx+=x;
        mDy+=y;

        if ((mDx < -300) || (mDx > 300)) mDx-=x ;
        if ((mDy < -250) || (mDy > 250)) mDy-=y ;
        mViewMatrix[14] += z;
    }



	@Override
	public void onDrawFrame(GL10 unused){
		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        if (isStl()) 
        	for (int i=0; i<mStlObjectList.size(); i++)
        		setColor(i);
        
	    GLES20.glEnable (GLES20.GL_BLEND);
	 	
		// Enable depth testing
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, mCameraX, mCameraY, mCameraZ, mCenterX, mCenterY, mCenterZ, 0f, 0.0f, 1.0f);

        //Apply translation
        mViewMatrix[12] += mDx;
        mViewMatrix[13] += mDy;
        
        // Calculate the projection and view transformation
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        /**
         * ROTATE FIRST THE X AXIS AROUND ITSELF MODEL X ROTATION
         */
                        
        //Set Identity
        Matrix.setIdentityM(mRotationMatrix, 0);

        //Move the matrix to the origin
        Matrix.translateM(mRotationMatrix, 0, 0.0f, 0.0f, 0.0f);

        //Rotation x
        Matrix.rotateM(mRotationMatrix, 0, mSceneAngleX, 0.0f, 0.0f, 1.0f);

        mCurrentSceneAngleX +=mSceneAngleX;

        //Reset angle, we store the rotation in the matrix
        mSceneAngleX=0;

        //Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mModelMatrix, 0, mRotationMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);


        /**
         * ROTATE SECOND THE Y AXIS AROUND THE SCENE ROTATION X MODEL
         */

        //Set Identity
        Matrix.setIdentityM(mRotationMatrix, 0);

        //Move the matrix to the origin
        Matrix.translateM(mRotationMatrix, 0, 0.0f, 0.0f, 0.0f);

        //RotationY
        Matrix.rotateM(mRotationMatrix, 0, mSceneAngleY, 1.0f, 0.0f, 0.0f);

        mCurrentSceneAngleY +=mSceneAngleY;

        mSceneAngleY=0;

        //Transport to degrees
        if (mCurrentSceneAngleX > 180) mCurrentSceneAngleX-=360;
        else if (mCurrentSceneAngleX < -180) mCurrentSceneAngleX+=360;

        //Transport to degrees
        if (mCurrentSceneAngleY > 180) mCurrentSceneAngleY-=360;
        else if (mCurrentSceneAngleY < -180) mCurrentSceneAngleY+=360;

        //Multiply the current rotation by the accumulated rotation, and then set the accumulated rotation to the result.
        Matrix.multiplyMM(mTemporaryMatrix, 0, mRotationMatrix, 0, mModelMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mModelMatrix, 0, 16);
                
        Matrix.multiplyMM(mMVPMatrix, 0,mVPMatrix, 0, mModelMatrix, 0);  
        Matrix.multiplyMM(mMVMatrix, 0,mViewMatrix, 0, mModelMatrix, 0);         
        
        //invertedMVPMatrix is used to detect clicks
        Matrix.invertM(invertedMVPMatrix, 0, mMVPMatrix, 0);
        
        //Set Light direction  
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, LIGHT_X, LIGHT_Y, LIGHT_Z);      
      	               
        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        if (mDataList.size()>0) {
            if (mObjectPressed!=-1) {

                //Check pressed object to avoid index out of bounds when deleting models
                if (mObjectPressed < mDataList.size()){
                    DataStorage data = mDataList.get(mObjectPressed);
                    Point center = data.getLastCenter();

                    Log.i("CENTER", "Settings center @" + center.x + ";" + center.y + ";" + center.z);

                    Matrix.setIdentityM(mTemporaryModel, 0);
                    Matrix.translateM(mTemporaryModel, 0, center.x, center.y, center.z);
                    Matrix.scaleM(mTemporaryModel, 0, data.getLastScaleFactorX(), data.getLastScaleFactorY(), data.getLastScaleFactorZ());

                    Matrix.translateM(mTemporaryModel, 0, 0, 0, data.getAdjustZ());

                    //Object rotation
                    float [] rotateObjectMatrix = data.getRotationMatrix();

                    //Multiply the model by the accumulated rotation
                    Matrix.multiplyMM(mObjectModel, 0, mTemporaryModel, 0,rotateObjectMatrix, 0);
                    Matrix.multiplyMM(mMVPObjectMatrix, 0,mMVPMatrix, 0, mObjectModel, 0);

                    Matrix.multiplyMM(mMVObjectMatrix, 0,mMVMatrix, 0, mObjectModel, 0);
                    Matrix.transposeM(mTransInvMVMatrix, 0, mMVObjectMatrix, 0);
                    Matrix.invertM(mTransInvMVMatrix, 0, mTransInvMVMatrix, 0);
                } else {

                    Log.i("Multiply", "IndexOutOfBounds " + mObjectPressed);

                }


            }


            if (isStl())
                for (int i=0; i<mStlObjectList.size(); i++) {
                    if (i==mObjectPressed) {

                        try{

                            if (mDataList.size() > 0){
                                mDataList.get(mObjectPressed).setModelMatrix(mObjectModel);
                                mStlObjectList.get(mObjectPressed).draw(mMVPObjectMatrix, mTransInvMVMatrix, mLightPosInEyeSpace, mObjectModel);
                                mCircle.draw(mDataList.get(mObjectPressed ), mMVPMatrix, mAxis);

                            }


                        } catch (IndexOutOfBoundsException e){

                            Log.i("Slicer", "IndexOutOfBounds " + mObjectPressed);

                        }


                    } else  {
                        float [] modelMatrix = mDataList.get(i).getModelMatrix();
                        float [] mvpMatrix = new float[16];
                        float [] mvMatrix = new float[16];
                        float [] mvFinalMatrix = new float[16];

                        Matrix.multiplyMM(mvpMatrix, 0,mMVPMatrix, 0, modelMatrix, 0);

                        Matrix.multiplyMM(mvMatrix, 0,mMVMatrix, 0, modelMatrix, 0);

                        Matrix.transposeM(mvFinalMatrix, 0, mvMatrix, 0);
                        Matrix.invertM(mvFinalMatrix, 0, mvFinalMatrix, 0);

                        mStlObjectList.get(i).draw(mvpMatrix, mvFinalMatrix, mLightPosInEyeSpace, modelMatrix);
                    }
                }
            else {

                //TODO Random crash
                try{


                    if (mGcodeObject!=null) mGcodeObject.draw(mMVPMatrix);
                } catch (NullPointerException e){

                    e.printStackTrace();
                }
            }
        }




        if (mMode == ViewerMainFragment.DO_SNAPSHOT) {
        	mInfinitePlane.draw(mMVPMatrix, mMVMatrix);
        	takeSnapshot(unused);



        } else {

        	if (mShowDownWitboxFace) mWitboxFaceDown.draw(mMVPMatrix, mMVMatrix);
        	if (mShowBackWitboxFace) mWitboxFaceBack.draw(mMVPMatrix);
        	if (mShowRightWitboxFace) mWitboxFaceRight.draw(mMVPMatrix);
        	if (mShowLeftWitboxFace) mWitboxFaceLeft.draw(mMVPMatrix);
            if (mShowFrontWitboxFace) mWitboxFaceFront.draw(mMVPMatrix);
            if (mShowTopWitboxFace) mWitboxFaceTop.draw(mMVPMatrix);

        } 
	}
	
	private void takeSnapshot (GL10 unused) {			
    	Log.i(TAG, "TAKING SNAPSHOT");
		int minX = 0;
		int minY = 0; 
        
        int screenshotSize = mWidth * mHeight;
        ByteBuffer bb = ByteBuffer.allocateDirect(screenshotSize * 4);
        bb.order(ByteOrder.nativeOrder());
        

        GLES20.glReadPixels(minX, minY, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, bb);



        LibraryModelCreation.saveSnapshot(mWidth, mHeight, bb);
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


    /***********************************************************************************
     *
     * Methods that use camera angles and position
     *
     ***********************************************************************************/

    /**
     * Static values for camera auto movement and rotation
     */
    private static final double CAMERA_MIN_TRANSLATION_DISTANCE = 0.1;
    private static final int CAMERA_MAX_ROTATION_DISTANCE = 5;
    private static final int CAMERA_MIN_ROTATION_DISTANCE = 1;
    private static final double POSITION_MIN_TRANSLATION_DISTANCE = 0.05;


    /**
     * Animation to restore initial position
     * @return
     */
    public boolean restoreInitialCameraPosition(float dx, float dy, boolean zoom, boolean rotation){

        float dyx = 0;

        if (!zoom) dyx+= POSITION_DEFAULT_Y;

        //Plate translation
        if ((int)mDx < (int)(POSITION_DEFAULT_X - dx)) mDx+= POSITION_MIN_TRANSLATION_DISTANCE;
        else if ((int)mDx> (int)(POSITION_DEFAULT_X - dx)) mDx-=POSITION_MIN_TRANSLATION_DISTANCE;

        if ((int)mDy < (int)(dyx - dy)) mDy+= POSITION_MIN_TRANSLATION_DISTANCE;
        else if ((int)mDy > (int)(dyx - dy)) mDy-=POSITION_MIN_TRANSLATION_DISTANCE;


        if (!zoom) {

            //Move X axis
            if ((int) mCameraX < CAMERA_DEFAULT_X) mCameraX += CAMERA_MIN_TRANSLATION_DISTANCE;
            else if ((int) mCameraX > CAMERA_DEFAULT_X) mCameraX -= CAMERA_MIN_TRANSLATION_DISTANCE;

            //Move Y axis
            if ((int) mCameraY < CAMERA_DEFAULT_Y) mCameraY += CAMERA_MIN_TRANSLATION_DISTANCE;
            else if ((int) mCameraY > CAMERA_DEFAULT_Y) mCameraY -= CAMERA_MIN_TRANSLATION_DISTANCE;


            //Move Z axis
            if ((int) mCameraZ < CAMERA_DEFAULT_Z) mCameraZ += CAMERA_MIN_TRANSLATION_DISTANCE;
            else if ((int) mCameraZ > CAMERA_DEFAULT_Z) mCameraZ -= CAMERA_MIN_TRANSLATION_DISTANCE;

        }

        if (rotation){

            //Rotate X axis
            if ((int)mCurrentSceneAngleX < ANGLE_X) {

                //Slow rotation when approaching the final value
                if ((int)mCurrentSceneAngleX > (ANGLE_X -10f)) mSceneAngleX = CAMERA_MIN_ROTATION_DISTANCE;
                else mSceneAngleX = CAMERA_MAX_ROTATION_DISTANCE;
            }
            else if ((int)mCurrentSceneAngleX > ANGLE_X){

                //Slow rotation when approaching the final value
                if ((int)mCurrentSceneAngleX < (ANGLE_X + 10f)) mSceneAngleX = -CAMERA_MIN_ROTATION_DISTANCE;
                else mSceneAngleX = -CAMERA_MAX_ROTATION_DISTANCE;
            }

            //Rotate Y axis
            if ((int)mCurrentSceneAngleY < ANGLE_Y) {

                //Slow rotation when approaching the final value
                if ((int)mCurrentSceneAngleY > (ANGLE_Y -10f)) mSceneAngleY = CAMERA_MIN_ROTATION_DISTANCE;
                else mSceneAngleY = CAMERA_MAX_ROTATION_DISTANCE;
            }
            else if ((int)mCurrentSceneAngleY > ANGLE_Y) {

                //Slow rotation when approaching the final value
                if ((int)mCurrentSceneAngleY < (ANGLE_Y + 10f)) mSceneAngleY = -CAMERA_MIN_ROTATION_DISTANCE;
                else mSceneAngleY = -CAMERA_MAX_ROTATION_DISTANCE;
            }

        }


        //Return true when we get the final values
        if (((((int)mCameraZ == CAMERA_DEFAULT_Z) && ((int)mCameraY == CAMERA_DEFAULT_Y) && ((int)mCameraX == CAMERA_DEFAULT_X)) || (zoom))
                && (((int) mCurrentSceneAngleX == ANGLE_X) && ((int) mCurrentSceneAngleY == ANGLE_Y)|| (!rotation))
                && ((int) mDx== (int)(POSITION_DEFAULT_X - dx)) && ((int) mDy == (int)(dyx - dy))) return true;
        else {

            return false;
        }

    }


}