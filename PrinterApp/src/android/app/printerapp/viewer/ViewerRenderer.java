package android.app.printerapp.viewer;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;
import android.app.printerapp.viewer.Geometry.*;


public class ViewerRenderer implements GLSurfaceView.Renderer {
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
	
	public static float mSceneAngleX = -40f;
	public static float mSceneAngleY = 0f;
	
	public static float RED = 0.80f;
	public static float GREEN = 0.1f;
	public static float BLUE = 0.1f;
	public static float ALPHA = 0.9f;
	
	public static final int DOWN=0;
	public static final int RIGHT=1;
	public static final int BACK=2;
	public static final int LEFT=3;
	
	public static final float LIGHT_X=600;
	public static final float LIGHT_Y=0;
	public static final float LIGHT_Z=600;
	
	public static final int NORMAL = 0;
	public static final int XRAY = 1;
	public static final int TRANSPARENT = 2;
	public static final int LAYERS = 3;
	
	private int mState;

	private StlObject mStlObject;
	private GcodeObject mGcodeObject;
	private WitboxPlate mWitboxFaceDown;
	private WitboxFaces mWitboxFaceRight;
	private WitboxFaces mWitboxFaceBack;
	private WitboxFaces mWitboxFaceLeft;
	private WitboxPlate mInfinitePlane;
	private DataStorage mData;
			
	private boolean mShowLeftWitboxFace = true;
	private boolean mShowRightWitboxFace = true;
	private boolean mShowBackWitboxFace= true;
	private boolean mShowDownWitboxFace = true;
	
	public float[] final_matrix_R_Render = new float[16];
	public float[] final_matrix_S_Render = new float[16];
	public float[] final_matrix_T_Render = new float[16];
	
	private final float[] mVPMatrix = new float[16]; //Model View Projection Matrix
	private final float[] mProjectionMatrix = new float[16];
	private final float[] mViewMatrix = new float[16];
	private final float[] mRotationMatrix = new float[16];
	private final float[] mAccumulatedRotationMatrix = new float[16];
	private final float[] mTemporaryMatrix = new float [16];
    private final float[] invertedViewProjectionMatrix = new float[16];
		
	private final float[] mLightVector = new float [4];
	float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};
			
	private boolean mSnapShot = false;
	
	private boolean mIsStl;
	
	//Variables Touch events
	private boolean objectPressed = false;
	
	float mMoveX;
	float mMoveY;
	float mMoveZ;

	public ViewerRenderer (DataStorage data, Context context, int state, boolean doSnapshot, boolean stl) {	
		this.mData = data;
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
		if (mStlObject!= null) mStlObject.setTransparent(transparent);
	}
	
	public void setXray (boolean xray) {
		if (mStlObject!= null) mStlObject.setXray(xray);
	}
	
	public boolean touchPoint (float x, float y) {
		Ray ray = convertNormalized2DPointToRay(x, y);
		 	 		 
        Box objectBox = new Box (mData.getMinX(), mData.getMaxX(), mData.getMinY(), mData.getMaxY(), mData.getMinZ(), mData.getMaxZ());

        // If the ray intersects (if the user touched a part of the screen that
        // intersects the stl object's bounding box), then set objectPressed =
        // true.
        objectPressed = Geometry.intersects(objectBox, ray);
        
        if (objectPressed) mStlObject.setColor(StlObject.colorSelectedObject);
        else mStlObject.setColor(StlObject.colorNormal);
                
        return objectPressed;
	}
	
	public void dragObject (float x, float y) {
		Ray ray = convertNormalized2DPointToRay(x, y);

		Point touched = Geometry.intersectionPointWitboxPlate(ray);
        mMoveX = touched.x ;
        mMoveY = touched.y;
        mMoveZ = touched.z;
                
        refreshObjectCoordinates (mMoveX, mMoveY);       
    }
	
	private void refreshObjectCoordinates (float x, float y) {		
		mData.setMaxX(x + mData.getLong()/2);
		mData.setMaxY(y + mData.getWidth()/2);
		mData.setMinX(x - mData.getLong()/2);
		mData.setMinY(y - mData.getWidth()/2);
	}
	
	
	 private Ray convertNormalized2DPointToRay(float normalizedX, float normalizedY) {
		 
	        // We'll convert these normalized device coordinates into world-space
	        // coordinates. We'll pick a point on the near and far planes, and draw a
	        // line between them. To do this transform, we need to first multiply by
	        // the inverse matrix, and then we need to undo the perspective divide.
	        final float[] nearPointNdc = {normalizedX, normalizedY, -1, 1};
	        final float[] farPointNdc =  {normalizedX, normalizedY,  1, 1};
	        
	        final float[] nearPointWorld = new float[4];
	        final float[] farPointWorld = new float[4];
	        
	        Matrix.multiplyMV(
	            nearPointWorld, 0, invertedViewProjectionMatrix, 0, nearPointNdc, 0);
	        Matrix.multiplyMV(
	            farPointWorld, 0, invertedViewProjectionMatrix, 0, farPointNdc, 0);

	        // Why are we dividing by W? We multiplied our vector by an inverse
	        // matrix, so the W value that we end up is actually the *inverse* of
	        // what the projection matrix would create. By dividing all 3 components
	        // by W, we effectively undo the hardware perspective divide.
	        divideByW(nearPointWorld);
	        divideByW(farPointWorld);

	        // We don't care about the W value anymore, because our points are now
	        // in world coordinates.
	        Point nearPointRay = 
	            new Point(nearPointWorld[0], nearPointWorld[1], nearPointWorld[2]);
				
	        Point farPointRay = 
	            new Point(farPointWorld[0], farPointWorld[1], farPointWorld[2]);

	        return new Ray(nearPointRay,  Geometry.vectorBetween(nearPointRay, farPointRay));
	 }    
	 
	  private void divideByW(float[] vector) {
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
	  
	@Override
	public void onSurfaceCreated(GL10 unused, EGLConfig config) {
		// Set the background frame color
		GLES20.glClearColor(1.0f, 1.0f, 1.0f, 0.0f);		
		
		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
		
		Matrix.setIdentityM(mAccumulatedRotationMatrix, 0);
		
		mLightVector[0] = LIGHT_X;
		mLightVector[1] = LIGHT_Y;
		mLightVector[2] = LIGHT_Z;

		while (!mData.isDrawEnabled()) ; //wait	
		
		if (mIsStl) mStlObject = new StlObject (mData, mContext, mState);
		else mGcodeObject = new GcodeObject (mData, mContext);
		
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
	        float h = mData.getHeight();
	        float l = mData.getLong();
	        float w = mData.getWidth();
	        
	        l = l/ratio; //We calculate the height related to the square in the frustum with this width 
	        w = w/ratio;
	        
	        float dh = (float) (h / (Math.tan(Math.toRadians(45/2))));
	        float dl = (float) (l/ (2*Math.tan(Math.toRadians(45/2))));
	        float dw = (float) (w/ (2*Math.tan(Math.toRadians(45/2))));
	        
	        Log.i(TAG, "WIDTH " +mData.getWidth() + " HEIGHT " + mData.getHeight() + " LONG " + mData.getLong() + " dw " + dw + " dh " + dh + " dl " + dl);

	        if (dw>dh && dw>dl) mCameraZ = OFFSET_BIG_HEIGHT*h;
	        else if (dh>dl) mCameraZ = OFFSET_HEIGHT*h;
	        else mCameraZ = OFFSET_BIG_HEIGHT*h;
	        
	        dl = dl + Math.abs(mData.getMinY());
	        dw = dw + Math.abs(mData.getMinX());
	        
	        if (dw>dh && dw>dl) mCameraY = - dw;
	        else if (dh>dl) mCameraY = -dh;
	        else mCameraY = - dl;        
        } else {
        	mCameraY = -300f;
        	mCameraZ = 300f;
        }
    	
        mSceneAngleX = -40f;

	}

	@Override
	public void onDrawFrame(GL10 unused) {
		// Draw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
                
		float[] vp = new float[16];
		float[] mv = new float[16];
		float[] mvp = new float[16];
		
		float[] model = new float [16];
		
		float[] lightPosInEyeSpace = new float[4];
				
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
        Matrix.multiplyMM(mTemporaryMatrix, 0, mRotationMatrix, 0, mAccumulatedRotationMatrix, 0);
        System.arraycopy(mTemporaryMatrix, 0, mAccumulatedRotationMatrix, 0, 16);
        
        // Rotate the object taking the overall rotation into account.
        
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
  
        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(vp, 0,mVPMatrix, 0, mAccumulatedRotationMatrix, 0);
            
        System.arraycopy(mTemporaryMatrix, 0, mVPMatrix, 0, 16);
        
        Matrix.invertM(invertedViewProjectionMatrix, 0, vp, 0);

        //Set ModelViewMatrix
        Matrix.multiplyMM(mv, 0, mViewMatrix, 0, mAccumulatedRotationMatrix, 0);
        
        //Set Light direction     
        Matrix.multiplyMV(lightPosInEyeSpace, 0, mv, 0, mLightVector, 0);   
        
        Matrix.setIdentityM(model, 0);
        Matrix.translateM(model, 0, mMoveX, mMoveY, mMoveZ);  
        Matrix.multiplyMM(mvp, 0, vp, 0, model, 0);
                   
        if (mIsStl) mStlObject.draw(mvp, mv, lightPosInEyeSpace);
        else mGcodeObject.draw(vp);
        
        if (mSnapShot) {
        	mInfinitePlane.draw(vp, mv);
        	takeScreenShot(unused);
        } else {
        	if (mShowDownWitboxFace) mWitboxFaceDown.draw(vp, mv);      
        	if (mShowBackWitboxFace) mWitboxFaceBack.draw(vp);
        	if (mShowRightWitboxFace) mWitboxFaceRight.draw(vp);
        	if (mShowLeftWitboxFace) mWitboxFaceLeft.draw(vp);
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
        int pixelsBuffer[] = new int[screenshotSize];
        bb.asIntBuffer().get(pixelsBuffer);
        bb = null;
        Bitmap bitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
        bitmap.setPixels(pixelsBuffer, screenshotSize-mWidth, -mWidth, 0, 0, mWidth, mHeight);
        pixelsBuffer = null;

        short sBuffer[] = new short[screenshotSize];
        ShortBuffer sb = ShortBuffer.wrap(sBuffer);
        bitmap.copyPixelsToBuffer(sb);

        //Making created bitmap (from OpenGL points) compatible with Android bitmap
        for (int i = 0; i < screenshotSize; ++i) {                  
            short v = sBuffer[i];
            sBuffer[i] = (short) (((v&0x1f) << 11) | (v&0x7e0) | ((v&0xf800) >> 11));
        }
        sb.rewind();
        bitmap.copyPixelsFromBuffer(sb);
        
        try {
            FileOutputStream fos = new FileOutputStream(mData.getPathSnapshot());
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }      
	}
	
	public void setLightVector (float dx, float dy) {		
		mLightVector[0]=dx*LIGHT_X;
		mLightVector[2]=dy*LIGHT_Z;

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