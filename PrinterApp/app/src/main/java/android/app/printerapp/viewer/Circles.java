package android.app.printerapp.viewer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Draw a circle around the model to show on which axis is it going to be rotated
 * Created by alberto-baeza on 1/12/15.
 */
public class Circles {

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
                    " gl_PointSize = 5.0;" +
                    "}";

    private final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    //Axis
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    //Default line width
    public static final float LINE_WIDTH = 2f;

    private static final float TRANSPARENCY = 0.5f;

    //Axis color
    private static final float[] X_COLOR = { 0.0f, 0.9f, 0.0f, TRANSPARENCY };
    private static final float[] Y_COLOR = { 1.0f, 0.0f, 0.0f, TRANSPARENCY };
    private static final float[] Z_COLOR = { 0.0f, 0.0f, 1.0f, TRANSPARENCY };

    //Circle vertices
    private float vertices[] = new float[364 * 3];

    //Maximum radius of the circle
    private float maxRadius = 0;

    final FloatBuffer mVertexBuffer;

    private final ShortBuffer mDrawListBuffer;
    final int mProgram;
    int mPositionHandle;
    int mColorHandle;
    float mCoordsArray [];
    float mCurrentColor [];

    private int mMVPMatrixHandle;

    // number of coordinates per vertex in this array
    final int COORDS_PER_VERTEX = 3;
    int vertexCount;
    int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
    // Set color with red, green, blue and alpha (opacity) values
    private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices


    public Circles() {

        mCoordsArray = vertices;

        vertexCount = mCoordsArray.length / COORDS_PER_VERTEX;

        // initialize vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect(
                // (# of coordinate values * 4 bytes per float)
                mCoordsArray.length * 4);
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();
        //mVertexBuffer.put(mCoordsArray);
        //mVertexBuffer.position(0);

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

    //X axis coordinates
    private float[] drawXAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = tempAxis[0];
            tempAxis[(i * 3)+ 1] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[1]);
            tempAxis[(i * 3)+ 2] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[2]);
        }

        return tempAxis;

    } ;

    //Y axis coordinates
    private float[] drawYAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[0]);
            tempAxis[(i * 3)+ 1] = tempAxis[1];
            tempAxis[(i * 3)+ 2] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[2]);
        }

        return tempAxis;

    } ;

    //Z axis coordinates
    private float[] drawZAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[364 * 3];


        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z;

        for(int i =1; i <364; i++){
            tempAxis[(i * 3)+ 0] = (float) (maxRadius * Math.cos((3.14/180) * (float)i ) + tempAxis[0]);
            tempAxis[(i * 3)+ 1] = (float) (maxRadius * Math.sin((3.14/180) * (float)i ) + tempAxis[1]);
            tempAxis[(i * 3)+ 2] = tempAxis[2];
        }

        return tempAxis;

    } ;


    //Get the maximum radius from the model sizes
    public float getRadius(DataStorage data){

        float values[] = new float[3];
        float value = 0;

        values[0] = data.getMaxX() - data.getMinX(); //Max width
        values[1] = data.getMaxY() - data.getMinY(); //Max depth
        values[2] = data.getMaxZ() - data.getMinZ() - data.getAdjustZ(); //Max height

        for (int i = 0; i < values.length; i++){

            if (values[i]> value) value = values[i]; //Keep the biggest one

        }

        return (value / 2) + 20f;
    }

    //Draw the circle
    public void draw(DataStorage data, float[] mvpMatrix, int currentAxis) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        GLES20.glEnable (GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        maxRadius = getRadius(data); //Change max radius

        //Draw a different axis depending on the selected one
        switch (currentAxis){

            case X_AXIS:
                mCoordsArray = drawXAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = X_COLOR;
                break;
            case Y_AXIS:
                mCoordsArray = drawYAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = Y_COLOR;
                break;
            case Z_AXIS:
                mCoordsArray = drawZAxis(data.getLastCenter(), data.getTrueCenter().z);
                mCurrentColor = Z_COLOR;
                break;
            default:
                mCoordsArray = null;
                break;

        }


        if (mCoordsArray!=null) {

            mVertexBuffer.put(mCoordsArray);
            mVertexBuffer.position(0);

            // Prepare the triangle coordinate data
            GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                    GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

            // get handle to fragment shader's vColor member
            mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

            // Set color for drawing the triangle
            GLES20.glUniform4fv(mColorHandle, 1, mCurrentColor, 0);

            // get handle to shape's transformation matrix
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
            ViewerRenderer.checkGlError("glGetUniformLocation");

            // Apply the projection and view transformation
            GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
            ViewerRenderer.checkGlError("glUniformMatrix4fv");

            GLES20.glLineWidth(LINE_WIDTH);
            GLES20.glDrawArrays(GLES20.GL_LINE_STRIP, 0, vertexCount);


            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }


    }
}
