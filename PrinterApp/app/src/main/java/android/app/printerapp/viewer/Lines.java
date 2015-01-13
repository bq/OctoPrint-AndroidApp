package android.app.printerapp.viewer;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by alberto-baeza on 12/2/14.
 */
public class Lines {

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

    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    private static final float TRANSPARENCY = 0.5f;

    private static final float[] X_COLOR = { 0.0f, 0.9f, 0.0f, TRANSPARENCY };
    private static final float[] Y_COLOR = { 1.0f, 0.0f, 0.0f, TRANSPARENCY };
    private static final float[] Z_COLOR = { 0.0f, 0.0f, 1.0f, TRANSPARENCY };



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
        float lineCoords[] = new float[6];
        int vertexCount;
        int vertexStride = COORDS_PER_VERTEX * 4; // bytes per vertex
        // Set color with red, green, blue and alpha (opacity) values
        private final short drawOrder[] = { 0, 1, 2, 0, 2, 3 }; // order to draw vertices


        public Lines() {

            lineCoords[0] = 0.0f;
            lineCoords[1] = 0.0f;
            lineCoords[2] = 0.0f;
            lineCoords[3] = 0.0f;
            lineCoords[4] = 0.0f;
            lineCoords[5] = 0.0f;

            mCoordsArray = lineCoords;

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

    private float[] drawXAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[6];

        tempAxis[0] = point.x - 100;
        tempAxis[1] = point.y;
        tempAxis[2] = z;
        tempAxis[3] = point.x + 100;
        tempAxis[4] = point.y;
        tempAxis[5] = z;

        return tempAxis;

    } ;

    private float[] drawYAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[6];

        tempAxis[0] = point.x;
        tempAxis[1] = point.y - 100;
        tempAxis[2] = z;
        tempAxis[3] = point.x;
        tempAxis[4] = point.y + 100;
        tempAxis[5] = z;

        return tempAxis;

    } ;

    private float[] drawZAxis(Geometry.Point point, float z){

        float [] tempAxis = new float[6];

        tempAxis[0] = point.x;
        tempAxis[1] = point.y;
        tempAxis[2] = z - 100;
        tempAxis[3] = point.x;
        tempAxis[4] = point.y;
        tempAxis[5] = z + 100;

        return tempAxis;

    } ;

    public void draw(DataStorage data, float[] mvpMatrix, int currentAxis) {

        // Add program to OpenGL environment
        GLES20.glUseProgram(mProgram);

        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");

        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);



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

            GLES20.glDrawArrays(GLES20.GL_LINES, 0, vertexCount);

            float points [] = new float[20];

            int i = 0;
            float radius = (float)0.75;

            for (float angle = 0; angle < 2*Math.PI; angle += 0.630) {
                points[i] = radius * (float)Math.cos(angle);
                points[i] = radius * (float)Math.sin(angle);

                i++;
            }

            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, vertexCount);


            // Disable vertex array
            GLES20.glDisableVertexAttribArray(mPositionHandle);
        }


    }

    }

