package android.app.printerapp.viewer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

public class WitboxFaces {
	private final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
            "attribute vec4 vPosition;" +
            "void main() {" +
            // The matrix must be included as a modifier of gl_Position.
            // Note that the uMVPMatrix factor *must be first* in order
            // for the matrix multiplication product to be correct.
            "  gl_Position = uMVPMatrix * vPosition;" +
            "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
            "uniform vec4 vColor;" +
            "void main() {" +
            "  gl_FragColor = vColor;" +
            "}";
    
    public static int WITBOX_WITDH = 105;
	public static int WITBOX_HEIGHT = 200;
	public static int WITBOX_LONG = 148;
	
    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawListBuffer;

    private final int mProgram;

    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    List<Float> lineCoordsList = new ArrayList<Float>();
    private float mCoordsArray [];
    int vertexCount;
    final int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex

    float color[] = {0.260784f, 0.460784f, 0.737255f, 0.6f };
    
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices
    
    static float planeCoordsDown[] = {
        -WITBOX_LONG,  WITBOX_WITDH, 0,   // top left
        -WITBOX_LONG, -WITBOX_WITDH, 0,   // bottom left
         WITBOX_LONG, -WITBOX_WITDH, 0,   // bottom right
         WITBOX_LONG,  WITBOX_WITDH, 0 }; // top right
    
    static float planeCoordsBack[] = {
        -WITBOX_LONG,  WITBOX_WITDH, WITBOX_HEIGHT,   // top left
        -WITBOX_LONG,  WITBOX_WITDH, 0,   // bottom left
         WITBOX_LONG,  WITBOX_WITDH, 0,   // bottom right
         WITBOX_LONG,  WITBOX_WITDH, WITBOX_HEIGHT }; // top right
    
    static float planeCoordsRight[] = {
         WITBOX_LONG, -WITBOX_WITDH, WITBOX_HEIGHT,   // top left
         WITBOX_LONG, -WITBOX_WITDH, 0,   // bottom left
         WITBOX_LONG,  WITBOX_WITDH, 0,   // bottom right
         WITBOX_LONG,  WITBOX_WITDH, WITBOX_HEIGHT }; // top right
    
    static float planeCoordsLeft[] = {
        -WITBOX_LONG, -WITBOX_WITDH, WITBOX_HEIGHT,   // top left
        -WITBOX_LONG, -WITBOX_WITDH, 0,   // bottom left
        -WITBOX_LONG,  WITBOX_WITDH, 0,   // bottom right
        -WITBOX_LONG,  WITBOX_WITDH, WITBOX_HEIGHT }; // top right

    

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     *
     * Alberto: change alpha according to face
     */
    public WitboxFaces(int face) {		
    	switch (face) {
    	case ViewerRenderer.DOWN:
    		mCoordsArray = planeCoordsDown;
    		break;
    	case ViewerRenderer.BACK:
    		mCoordsArray = planeCoordsBack;
            color[3] = 0.6f;
    		break;
    	case ViewerRenderer.RIGHT:
    		mCoordsArray = planeCoordsRight;
            color[3] = 0.5f;
    		break;
    	case ViewerRenderer.LEFT:
    		mCoordsArray = planeCoordsLeft;
            color[3] = 0.5f;
    		break;
    	}
    		    	
    	vertexCount = mCoordsArray.length / COORDS_PER_VERTEX;
    	
    	// initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
        // (# of coordinate values * 4 bytes per float)
        		mCoordsArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        mVertexBuffer.put(mCoordsArray);
        mVertexBuffer.position(0);
        
     // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 2 bytes per short)
                drawOrder.length * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();
        mDrawListBuffer.put(drawOrder);
        mDrawListBuffer.position(0);


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
        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables
    }
    
    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
	    // Add program to OpenGL environment
	    GLES20.glUseProgram(mProgram);
	   
		GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

	    // Enable a handle to the triangle vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	            GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

	    // get handle to fragment shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

	    // Set color for drawing the triangle
	    GLES20.glUniform4fv(mColorHandle, 1, color, 0);
	    
	    // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        ViewerRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        ViewerRenderer.checkGlError("glUniformMatrix4fv");

     // Draw the square
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, drawOrder.length,
                GLES20.GL_UNSIGNED_SHORT, mDrawListBuffer);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}

