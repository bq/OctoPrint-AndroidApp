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

    /*
     public static int WITBOX_WITDH = 105;
	public static int WITBOX_HEIGHT = 200;
	public static int WITBOX_LONG = 148;
     */

    public static final int TYPE_WITBOX = 0;
    public static final int TYPE_HEPHESTOS = 1;
    public static final int TYPE_CUSTOM = 2;

    public static int WITBOX_WITDH = 105;
	public static int WITBOX_HEIGHT = 200;
	public static int WITBOX_LONG = 148;

    public static int HEPHESTOS_WITDH = 105;
    public static int HEPHESTOS_HEIGHT = 180;
    public static int HEPHESTOS_LONG = 108;

    public int [] mSizeArray;
    public int mType;
	
    private FloatBuffer mVertexBuffer;
    private ShortBuffer mDrawListBuffer;

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

    //float color[] = {0.260784f, 0.460784f, 0.737255f, 0.6f };
    float color[] = {1.0f, 1.0f, 1.0f, 0.6f };


    
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices

    private float[] planeCoordsDown;
    private float[] planeCoordsBack;
    private float[] planeCoordsRight;
    private float[] planeCoordsLeft;

    
    /*static float planeCoordsDown[] = {
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
        -WITBOX_LONG,  WITBOX_WITDH, WITBOX_HEIGHT }; // top right*/

    

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     *
     * Alberto: change alpha according to face
     */
    public WitboxFaces(int face, int[] type) {

    	/*switch (face) {
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
    	}*/

        mType = face;

        generatePlaneCoords(face,type);

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

    public void generatePlaneCoords(int face, int[] type){


        mSizeArray = type;


        switch (face) {
            case ViewerRenderer.DOWN:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], 0,   // top left
                        -mSizeArray[0], -mSizeArray[1], 0,   // bottom left
                        mSizeArray[0], -mSizeArray[1], 0,   // bottom right
                        mSizeArray[0],  mSizeArray[1], 0 }; // top right
                break;
            case ViewerRenderer.BACK:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2],   // top left
                        -mSizeArray[0],  mSizeArray[1], 0,   // bottom left
                        mSizeArray[0],  mSizeArray[1], 0,   // bottom right
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] }; // top right
                color[3] = 0.3f;
                break;
            case ViewerRenderer.RIGHT:
                mCoordsArray  = new float[]{
                        mSizeArray[0], -mSizeArray[1], mSizeArray[2],   // top left
                        mSizeArray[0], -mSizeArray[1], 0,   // bottom left
                        mSizeArray[0],  mSizeArray[1], 0,   // bottom right
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] }; // top right
                color[3] = 0.35f;
                break;
            case ViewerRenderer.LEFT:
                mCoordsArray = new float[]{
                        -mSizeArray[0], -mSizeArray[1], mSizeArray[2],   // top left
                        -mSizeArray[0], -mSizeArray[1], 0,   // bottom left
                        -mSizeArray[0],  mSizeArray[1], 0,   // bottom right
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2] }; // top right

                color[3] = 0.35f;
                break;
            case ViewerRenderer.FRONT:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  -mSizeArray[1], mSizeArray[2],   // top left
                        -mSizeArray[0],  -mSizeArray[1], 0,   // bottom left
                         mSizeArray[0],  -mSizeArray[1], 0,   // bottom right
                         mSizeArray[0],  -mSizeArray[1], mSizeArray[2] }; // top right

                color[3] = 0.3f;
                break;

            case ViewerRenderer.TOP:
                mCoordsArray = new float[]{
                        -mSizeArray[0],  mSizeArray[1], mSizeArray[2],   // top left
                        -mSizeArray[0],  -mSizeArray[1], mSizeArray[2],   // bottom left
                        mSizeArray[0],  -mSizeArray[1], mSizeArray[2],   // bottom right
                        mSizeArray[0],  mSizeArray[1], mSizeArray[2] }; // top right

                color[3] = 0.4f;
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

        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //enable cull face to hide sides if they overlap with each other
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        //Since every wall is facing the same side, we need to treat them differently
        switch(mType){

            case ViewerRenderer.RIGHT:
            case ViewerRenderer.FRONT:
            case ViewerRenderer.TOP:

                GLES20.glCullFace(GLES20.GL_FRONT);

                break;

            case ViewerRenderer.BACK:
            case ViewerRenderer.LEFT:


                GLES20.glCullFace(GLES20.GL_BACK);

                break;
        }

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


        /*******
         * Draw the boders of the plate
         *******/

        //Disable depth test to draw lines always in the front
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        //new color
        float color2[] = {1.0f, 1.0f, 1.0f, 0.01f };
        GLES20.glUniform4fv(mColorHandle, 1, color2, 0);

        //Draw as a line loop
        GLES20.glLineWidth(3f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, vertexCount);

        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

	    // Disable vertex array
	    GLES20.glDisableVertexAttribArray(mPositionHandle);


    }
}

