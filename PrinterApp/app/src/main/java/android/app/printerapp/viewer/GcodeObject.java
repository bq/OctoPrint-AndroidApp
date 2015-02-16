package android.app.printerapp.viewer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import android.app.printerapp.Log;
import android.content.Context;
import android.opengl.GLES20;

public class GcodeObject {
	
	private static String TAG = "GCodeObject";

	 private final String vertexShaderCode =
	            // This matrix member variable provides a hook to manipulate
	            // the coordinates of the objects that use this vertex shader
	            "uniform mat4 u_MVPMatrix;" +
	            
	            "attribute vec4 a_Position;" +
	            "attribute vec4 a_Color;"    +	            
  				
	            "varying vec4 v_Color;"		 +// This will be passed into the fragment shader.
	            "void main() {" +
	            "v_Color = a_Color ;" +
	            // The matrix must be included as a modifier of gl_Position.
	            // Note that the uMVPMatrix factor *must be first* in order
	            // for the matrix multiplication product to be correct.
	            "  gl_Position = u_MVPMatrix * a_Position;" +
	            "}";

	    private final String fragmentShaderCode =
	            "precision mediump float;" +
	            "varying vec4 v_Color;" +
	            "void main() {" +
	            "  gl_FragColor = v_Color;" +
	            "}";
          
	
	private final int mProgram;
	private int mPositionHandle;
	private int mColorHandle;
	private int mMVPMatrixHandle;
	
	static final int COORDS_PER_VERTEX = 3;
	static final int COLORS_PER_VERTEX = 4;
	
	private static final int LAYERS_TO_RENDER = 50;

	private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	private final int COLORS_STRIDE = COLORS_PER_VERTEX * 4; // 4 bytes per vertex

	 
 
	float colorBlue[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };
	float colorRed[] = { 1.0f, 0.0f, 0.0f, 1.0f };
	float colorYellow[] = { 1.0f, 1.0f, 0.0f, 1.0f };
	float colorGreen[] = { 0.0f, 1.0f, 0.0f, 1.0f };
	float colorWhite[] = {1.0f, 1.0f, 1.0f, 1.0f};
	
		
	private final DataStorage mData;
	
	private float [] mVertexArray;
	private int [] mLayerArray;
	private int [] mTypeArray;
	private float [] mColorArray;
	private List<Integer> mLineLength = new ArrayList<Integer>();
	private final FloatBuffer mVertexBuffer;
	private final FloatBuffer mColorBuffer;

	private int mLayer;
		
	private boolean mTransparent ;
	private boolean mXray;

	public GcodeObject(DataStorage data, Context context) {	
		this.mData = data;
		this.mLayer = data.getActualLayer();
		
		Log.i(TAG, "Creating GCode Object");
		
		mVertexArray = mData.getVertexArray();
		mLayerArray = mData.getLayerArray();
		mTypeArray = mData.getTypeArray();
		mLineLength = mData.getLineLengthList();
		mColorArray = getColorArray();

	
		//Vertex buffer
		ByteBuffer vbb = ByteBuffer.allocateDirect(mVertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mVertexBuffer = vbb.asFloatBuffer();
		mVertexBuffer.put(mVertexArray);
		mVertexBuffer.position(0);
		
		//Color buffer
		ByteBuffer cbb = ByteBuffer.allocateDirect(mColorArray.length * 4);
		cbb.order(ByteOrder.nativeOrder());
		mColorBuffer = cbb.asFloatBuffer();
		mColorBuffer.put(mColorArray);
		mColorBuffer.position(0);
		
				
		// prepare shaders and OpenGL program
        int vertexShader = ViewerRenderer.loadShader(
                GLES20.GL_VERTEX_SHADER,
                vertexShaderCode);
        int fragmentShader = ViewerRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        
        GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
        GLES20.glBindAttribLocation(mProgram, 1, "a_Color");

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables        
	}
	
	public float [] getColorArray () {
		List <Float> list = new ArrayList<Float>();
		float [] color=null;		

		for (int i=0; i<mTypeArray.length; i++) {
			switch (mTypeArray[i]) {
			case DataStorage.WALL_INNER:
				color = colorGreen;
				break;
			case DataStorage.WALL_OUTER:
				color = colorRed;
				break;
			case DataStorage.FILL:
				color =  colorYellow;
				break;
			case DataStorage.SKIRT:
				color = colorGreen;
				break;
			case DataStorage.SUPPORT:
				color = colorBlue;
				break;
			default:
				color = colorYellow;
				break;
			}
			
			for (int j=0; j<color.length; j++) list.add(color[j]);
						
		}
		
		float [] finalColor = new float [list.size()];
		
		for (int i=0; i<list.size(); i++) {
			finalColor[i] = list.get(i);
		}
		
		return finalColor;
		
	}
	
	public void setTransparent (boolean transparent) {
		mTransparent = transparent;
	}
	
	public void setXray (boolean xray) {
		mXray = xray;
	}
	
	public boolean getTransparent () {
		return mTransparent;
	}
	
	public boolean getXray () {
		return mXray;
	}
	
	/**
	 * DRAW GCOde
	 * ----------------------------------
	 */
	
	public void draw(float[] mvpMatrix) {
		mLayer = mData.getActualLayer();

		int layerMin = mLayer - LAYERS_TO_RENDER;
		int vertexCount =0;
		int vertexCountMin =0;
						
		for (int i=0; i<mLayerArray.length; i++)  
		   	if (mLayerArray[i] <= mLayer)  
		   		vertexCount++; //Total number of vertex from layer 0 to mLayer
		
		for (int i=0; i<mLayerArray.length; i++) 
			if (mLayerArray[i]<=layerMin) 
				vertexCountMin++; //Total number of vertex from layer 0 to layerMin
		
		int length = 0;
	    GLES20.glUseProgram(mProgram);
	    
	    GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_CONSTANT_COLOR);  
	    
	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "a_Position");

	    // Prepare the Vertex coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 VERTEX_STRIDE, mVertexBuffer); 
	    
	    // Enable a handle to the facet vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    
        mColorHandle = GLES20.glGetAttribLocation(mProgram, "a_Color");
        ViewerRenderer.checkGlError("glGetAttribLocation");

        GLES20.glVertexAttribPointer(mColorHandle, COLORS_PER_VERTEX, 
        							GLES20.GL_FLOAT, false,
        							COLORS_STRIDE, mColorBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
	    
   
	    // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "u_MVPMatrix");
        ViewerRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        ViewerRenderer.checkGlError("glUniformMatrix4fv");
               
        
        for (int i=0; i<mLineLength.size(); i++) {    	
    		if (mLineLength.get(i)>1) { 
    			if (mTypeArray[length]!=DataStorage.FILL || length>vertexCountMin) 			        		
    				GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, length, mLineLength.get(i));
    				if (length>=vertexCount) break; 
    		}    		
			length+=mLineLength.get(i);		
        }
                      
	}	
}
