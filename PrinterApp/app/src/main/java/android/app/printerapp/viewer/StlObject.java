package android.app.printerapp.viewer;

import android.content.Context;
import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class StlObject {
	
	private final String vertexShaderCode =
				"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
			  + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.	
			  + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
			  + "uniform vec4 a_Color;          \n"		// Color information we will pass in.

			  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
			  + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
			  
			  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
			  
			  + "void main()                    \n" 	// The entry point for our vertex shader.
			  + "{                              \n"		
			// Transform the vertex into eye space.
			  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              			\n"
			// Transform the normal's orientation into eye space.
			  + "   vec3 modelViewNormal = normalize(vec3(u_MVMatrix * vec4(a_Normal, 0.0)));   \n"
			// Will be used for attenuation.
			  + "   float distance = length(u_LightPos - modelViewVertex);             			\n"
			// Get a lighting direction vector from the light to the vertex.
			  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        			\n"
			// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
			// pointing in the same direction then it will get max illumination.
			  + "   float diffuse = abs(dot(modelViewNormal, lightVector));       				\n" 	  		  													  
			// Attenuate the light based on distance.
			  + "   diffuse +=0.2;  											   				\n"
			// Multiply the color by the illumination level. It will be interpolated across the triangle.
			  + "   v_Color = a_Color * diffuse;                                       			\n" 	 
			// gl_Position is a special variable used to store the final position.
			// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.		
			  + "   gl_Position = u_MVPMatrix * a_Position;                            			\n"     
			  + "}                                                                     			\n"; 
	
	private final String vertexOverhangShaderCode =
			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
		  + "uniform mat4 u_MVMatrix;       \n"		// A constant representing the combined model/view matrix.	
		  + "uniform mat4 u_MMatrix;       \n"		// A constant representing the model	
		  + "uniform vec3 u_LightPos;       \n"	    // The position of the light in eye space.
		  + "uniform vec4 a_Color;          \n"		// Color information we will pass in.
		  + "uniform vec4 a_ColorOverhang;  \n"		// Color information we will pass in.
		  + "uniform float a_CosAngle;		\n"		//Overhang angle

		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
		  + "attribute vec3 a_Normal;       \n"		// Per-vertex normal information we will pass in.
		  
		  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
		  
		  + "void main()                    \n" 	// The entry point for our vertex shader.
		  + "{                              \n"		
		// Transform the vertex into eye space.
		  + "   vec3 modelViewVertex = vec3(u_MVMatrix * a_Position);              			\n"
		// Transform the normal's orientation into eye space.
		  + "   vec3 modelViewNormal = normalize(vec3(u_MVMatrix * vec4(a_Normal, 0.0)));   \n"
		// Will be used for attenuation.
		  + "   float distance = length(u_LightPos - modelViewVertex);             			\n"
		// Get a lighting direction vector from the light to the vertex.
		  + "   vec3 lightVector = normalize(u_LightPos - modelViewVertex);        			\n"
		// Calculate the dot product of the light vector and vertex normal. If the normal and light vector are
		// pointing in the same direction then it will get max illumination.
		  + "   float diffuse = abs(dot(modelViewNormal, lightVector));       				\n" 	  		  													  
		// Attenuate the light based on distance.
		  + "   diffuse +=0.2;  											   				\n"
		// Choose the color depending if the model needs overhang or not.
		// Multiply the color by the illumination level. It will be interpolated across the triangle.
		  + "	vec3 overhang = normalize(vec3(u_MMatrix * vec4(a_Normal, 0.0)));   		\n"
		  + "	if (overhang.z < -a_CosAngle) 												\n"
		  + "	{                             			 									\n"
		  + "		v_Color = a_ColorOverhang * diffuse;									\n"
		  + "	} else {																	\n"	
		  + "   	v_Color = a_Color * diffuse;                                    		\n"
		  + "	}                             			 									\n"
		// gl_Position is a special variable used to store the final position.
		// Multiply the vertex by the matrix to get the final point in normalized screen coordinates.		
		  + "   gl_Position = u_MVPMatrix * a_Position;                            			\n"     
		  + "}                                                                     			\n"; 

   
	private final String fragmentShaderCode =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}   "
		  + ""
		  + "            					\n";

	private final int mProgram;
	private final int mProgramOverhang;

	private int mPositionHandle;
	private int mColorHandle;
	private int  mColorOverhangHandle;
	private int mCosAngleHandle;
	private int mNormalHandle;
	private int mMMatrixHandle;
	private int mMVPMatrixHandle;
	private int mMVMatrixHandle;
    private int mLightPosHandle;
	
	static final int COORDS_PER_VERTEX = 3;
	static final int COLORS_PER_VERTEX = 4;

	private final int VERTEX_STRIDE = COORDS_PER_VERTEX * 4; // 4 bytes per vertex
	 
	float mColor [];
	
	public static float colorNormal[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };	
	public static float colorOverhang[] = { 1f, 0f, 0f, 1.0f };	
	public static float colorSelectedObject[] = { 1.0f, 1.0f, 0.0f, 1.0f };	
	public static float colorObjectOut[] = {1.0f, 1.0f, 1.0f, 1.0f};	
	public static float colorObjectOutTouched[] = {1.0f, 1.0f, 1.0f, 1.0f};
	
	private final DataStorage mData;
	
	float [] mVertexArray;
	float [] mNormalArray;
	private final FloatBuffer mNormalBuffer;
	private final FloatBuffer mTriangleBuffer;

	private final int vertexCount;
	
	private boolean mTransparent ;
	private boolean mXray;
	private boolean mOverhang;
	
	private float mOverhangAngle= 45;

	public StlObject(DataStorage data, Context context, int state) {	
		this.mData = data;
				
		mVertexArray = mData.getVertexArray();
		mNormalArray = mData.getNormalArray();

		vertexCount = mVertexArray.length/COORDS_PER_VERTEX;
			
		configStlObject(state);

        int[] auxPlate;

        if (ViewerMainFragment.getCurrentPlate()!=null){
           auxPlate = ViewerMainFragment.getCurrentPlate();
        } else auxPlate = new int[]{WitboxFaces.WITBOX_LONG, WitboxFaces.WITBOX_WITDH, WitboxFaces.WITBOX_HEIGHT};

		
		//Set colour
		if (mData.getMaxX()>auxPlate[0] || mData.getMinX() < -auxPlate[0] || mData.getMaxY()>auxPlate[1]
			|| mData.getMinY()<-auxPlate[1] || mData.getMaxZ()>auxPlate[2] || mData.getMinZ()<0) setColor (colorObjectOut);
		else setColor (colorNormal);

		
		//Vertex buffer
		ByteBuffer vbb = ByteBuffer.allocateDirect(mVertexArray.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		mTriangleBuffer = vbb.asFloatBuffer();
		mTriangleBuffer.put(mVertexArray);
		mTriangleBuffer.position(0);
		
		//Normal buffer
		ByteBuffer nbb = ByteBuffer.allocateDirect(mNormalArray.length * 4);
		nbb.order(ByteOrder.nativeOrder());
		mNormalBuffer = nbb.asFloatBuffer();
		mNormalBuffer.put(mNormalArray);
		mNormalBuffer.position(0);
				
		// prepare shaders and OpenGL program
		int vertexOverhangShader = ViewerRenderer.loadShader(
	        GLES20.GL_VERTEX_SHADER,
	        vertexOverhangShaderCode);
	
		int vertexShader = ViewerRenderer.loadShader(
            GLES20.GL_VERTEX_SHADER,
            vertexShaderCode);
		
        	
        int fragmentShader = ViewerRenderer.loadShader(
                GLES20.GL_FRAGMENT_SHADER,
                fragmentShaderCode);

        mProgram = GLES20.glCreateProgram();             // create empty OpenGL Program
        mProgramOverhang = GLES20.glCreateProgram();
        
        GLES20.glAttachShader(mProgram, vertexShader);   // add the vertex shader to program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program

        GLES20.glAttachShader(mProgramOverhang, vertexOverhangShader);   // add the vertex overhang shader to program
        GLES20.glAttachShader(mProgramOverhang, fragmentShader); // add the fragment shader to program
        
        GLES20.glBindAttribLocation(mProgram, 0, "a_Position");
        GLES20.glBindAttribLocation(mProgram, 1, "a_Normal");
        
        GLES20.glBindAttribLocation(mProgramOverhang, 0, "a_Position");
        GLES20.glBindAttribLocation(mProgramOverhang, 1, "a_Normal");

        GLES20.glLinkProgram(mProgram);                  // create OpenGL program executables      
        GLES20.glLinkProgram(mProgramOverhang);          // create OpenGL program executables        
	}
	
	public void configStlObject (int state) {
		switch (state) {
		case ViewerSurfaceView.XRAY:
			setXray (true);
			break;
		case ViewerSurfaceView.TRANSPARENT:
			setTransparent (true);
			break;	
		case ViewerSurfaceView.OVERHANG:
			setOverhang (true);
			break;
		}
	}
	
	public void setTransparent (boolean transparent) {
		mTransparent = transparent;
	}
	
	public void setXray (boolean xray) {
		mXray = xray;
	}
	
	public void setOverhang (boolean overhang) {
		mOverhang = overhang;
	}
	
	public void setColor (float[] c) {
		mColor = c;
	}
	
	
	/**
	 * Draw STL
	 * ----------------------------------
	 */
	
	public void draw(float[] mvpMatrix, float[] mvMatrix, float [] lightVector, float [] mMatrix) {
		int program = mProgram;
		if (mOverhang){
			program = mProgramOverhang;
			GLES20.glUseProgram(mProgramOverhang);
		} else { 
			program = mProgram;
			GLES20.glUseProgram(mProgram);
		}

	    if (mTransparent)
			GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
	    else 
	    	GLES20.glBlendFunc(GLES20.GL_SRC_COLOR, GLES20.GL_CONSTANT_COLOR);  
	    
	    // get handle to vertex shader's vPosition member
	    mPositionHandle = GLES20.glGetAttribLocation(program, "a_Position");
        ViewerRenderer.checkGlError("glGetAttribLocation");

	    // Prepare the triangle coordinate data
	    GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
	                                 GLES20.GL_FLOAT, false,
	                                 VERTEX_STRIDE, mTriangleBuffer); 
	    
	    // Enable a handle to the facet vertices
	    GLES20.glEnableVertexAttribArray(mPositionHandle);
	    
	    if (mOverhang) {
	        mColorOverhangHandle = GLES20.glGetUniformLocation(program, "a_ColorOverhang");
	        ViewerRenderer.checkGlError("glGetUniformLocation COLOROVERHANG");
	        
	        GLES20.glUniform4fv(mColorOverhangHandle, 1, colorOverhang, 0);
	        ViewerRenderer.checkGlError("glUniform4fv");
	        
	        mCosAngleHandle = GLES20.glGetUniformLocation(program, "a_CosAngle");
	        ViewerRenderer.checkGlError("glGetUniformLocation");
	        
	        GLES20.glUniform1f(mCosAngleHandle, (float) Math.cos(Math.toRadians(mOverhangAngle))); 
	        
	        mMMatrixHandle = GLES20.glGetUniformLocation(program, "u_MMatrix"); 
	        ViewerRenderer.checkGlError("glGetUniformLocation");

	        // Pass in the modelview matrix.
	        GLES20.glUniformMatrix4fv(mMMatrixHandle, 1, false, mMatrix, 0); 
	        ViewerRenderer.checkGlError("glUniformMatrix4fv");
        } 

	    // get handle to fragment shader's vColor member
	    mColorHandle = GLES20.glGetUniformLocation(program, "a_Color");
        ViewerRenderer.checkGlError("glGetUniformLocation a_Color");

	    // Set color for drawing the facet 
	    GLES20.glUniform4fv(mColorHandle, 1, mColor, 0);
        ViewerRenderer.checkGlError("glUniform4fv");

        mNormalHandle = GLES20.glGetAttribLocation(program, "a_Normal");
        ViewerRenderer.checkGlError("glGetAttribLocation");

        // Pass in the normal information
        GLES20.glVertexAttribPointer(mNormalHandle, COORDS_PER_VERTEX, 
        							 GLES20.GL_FLOAT, false, 
        							 VERTEX_STRIDE, mNormalBuffer);
        
        GLES20.glEnableVertexAttribArray(mNormalHandle);

	    // get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVPMatrix");
        ViewerRenderer.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        ViewerRenderer.checkGlError("glUniformMatrix4fv");
        
        mMVMatrixHandle = GLES20.glGetUniformLocation(program, "u_MVMatrix"); 
        ViewerRenderer.checkGlError("glGetUniformLocation");

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(mMVMatrixHandle, 1, false, mvMatrix, 0); 
        ViewerRenderer.checkGlError("glUniformMatrix4fv");
           
        mLightPosHandle = GLES20.glGetUniformLocation(program, "u_LightPos");
        ViewerRenderer.checkGlError("glGetUniformLocation");
        
        GLES20.glUniform3f(mLightPosHandle, lightVector[0], lightVector[1], lightVector[2]);
        ViewerRenderer.checkGlError("glUniform3f");
        
        if (mXray) {       
	        for (int i=0; i<vertexCount/COORDS_PER_VERTEX; i++) {
	        	GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, i*3, 3);
	        }
        } else     
        	GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);
	}
}

