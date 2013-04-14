package com.vasova.bachelorproject;


import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Mat;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import android.opengl.Matrix;

public class Graphics_Renderer implements  GLSurfaceView.Renderer{
    
	private Mat disparityMat;
	private Mat originalImg;
	
	private FloatBuffer mTriangle1Vertices;
	private FloatBuffer mTriangle2Vertices;

	private float[] mModelMatrix = new float[16];
	private float[] mViewMatrix = new float[16];
	private float[] mProjectionMatrix = new float[16];
	private float[] mMVPMatrix = new float[16];
	
	private int mMVPMatrixHandle;
	private int mPositionHandle;
	private int mColorHandle;
	
	/** How many bytes per float. */
	private final int mBytesPerFloat = 4;
	/** How many elements per vertex. */
	private final int mStrideBytes = 7 * mBytesPerFloat;	
	/** Offset of the position data. */
	private final int mPositionOffset = 0;
	/** Size of the position data in elements. */
	private final int mPositionDataSize = 3;
	/** Offset of the color data. */
	private final int mColorOffset = 3;
	/** Size of the color data in elements. */
	private final int mColorDataSize = 4;	
	
	private float width;
	private float height;
	private float k;
	
	public Graphics_Renderer(Mat dispMat, Mat origImg)
    {
		this.disparityMat = dispMat;
		this.originalImg = origImg;
		
		this.width = dispMat.width();
		this.height = dispMat.height(); 
		
		float c1 = (float)(Math.random()*255/100);
		float c2 = (float)(Math.random()*255/100);
		float c3 = (float)(Math.random()*255/100);
		
		k = (4.0f/width);//0.5f; //width of each square
		
		float startPosition_x1 = -2.0f;
		float startPosition_x2 = startPosition_x1 + k;
		float startPosition_y1 = 1.25f;
		float startPosition_y2 = startPosition_y1 + k; 
		
		float z = 0.0f;
		
		float[] triangle1VerticesData = {
				// X, Y, Z, 
				// R, G, B, A
				startPosition_x1, startPosition_y1, z, 
	            c1, c2, c3, 1.0f,

	            startPosition_x2, startPosition_y1, z,
	            c1, c2, c3, 1.0f,

	            startPosition_x1, startPosition_y2, z, 
	            c1, c2, c3, 1.0f
		};
		float[] triangle2VerticesData = {
				// X, Y, Z, 
				// R, G, B, A
				startPosition_x2, startPosition_y2, z, 
				c1, c2, c3, 1.0f,

	            startPosition_x1, startPosition_y2, z,
	            c1, c2, c3, 1.0f,

	            startPosition_x2, startPosition_y1, z, 
	            c1, c2, c3, 1.0f
		};
		
		// Initialize the buffers.
		mTriangle1Vertices = ByteBuffer.allocateDirect(triangle1VerticesData.length * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle1Vertices.put(triangle1VerticesData).position(0);

		mTriangle2Vertices = ByteBuffer.allocateDirect(triangle2VerticesData.length * 4)
        .order(ByteOrder.nativeOrder()).asFloatBuffer();
		mTriangle2Vertices.put(triangle2VerticesData).position(0);
    }
    public void onDrawFrame(GL10 gl){
    	GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);			        
    	/*
    	long time = SystemClock.uptimeMillis() % 10000L;
        float angleInDegrees = (360.0f / 10000000.0f) * ((int) time);
        Matrix.rotateM(mProjectionMatrix, 0, angleInDegrees, 1.0f, 1.0f, 0.0f);        
        */

    	float[] modelM = mModelMatrix;
        Matrix.setIdentityM(modelM, 0);
        Matrix.translateM(modelM, 0, 0.0f, 0.0f, 0.0f);        
        float delta_x = k;
        float delta_y = -k;
        float delta_z = 0;
        int w = (int)width;
        int h = (int)height;

		for (int j = 0; j < h; j++){
        	for(int i = 0; i < w; i++){
        		if(i==0 && j != 0){
        			Matrix.translateM(modelM, 0, -w*delta_x, delta_y, 0);
        		}
        		double[] color = originalImg.get(j, i);
        		changeColorInBuffer(color);
        		
        		double[] disparity = disparityMat.get(j, i);
        		
        		Matrix.translateM(modelM, 0, 0, 0.0f, -delta_z);
        		delta_z = (float)(disparity[0]/100);
        		Matrix.translateM(modelM, 0, delta_x, 0.0f, delta_z);
        		//Matrix.translateM(modelM, 0, delta_x, 0.0f, 0.0f);
        		
        		
        		drawTriangle(mTriangle1Vertices);
                drawTriangle(mTriangle2Vertices);
                
	        }
        }
    }
    
    private void changeColorInBuffer(double[] color){
    	//color positions in buffer: (3, 4, 5) (10, 11, 12) (17, 18, 19)
    	for (int i = 0; i < 3; i++){
    		mTriangle1Vertices.put(3+(i*7), ((float)color[2]/255.0f));
    		mTriangle1Vertices.put(4+(i*7), ((float)color[1]/255.0f));
        	mTriangle1Vertices.put(5+(i*7), ((float)color[0]/255.0f));
        	
        	mTriangle2Vertices.put(3+(i*7), ((float)color[2]/255.0f));
        	mTriangle2Vertices.put(4+(i*7), ((float)color[1]/255.0f));
        	mTriangle2Vertices.put(5+(i*7), ((float)color[0]/255.0f));
    	}
    	
    }
    public void onSurfaceChanged(GL10 gl, int width, int height){
    	GLES20.glViewport(0, 0, width, height);

		// Create a new perspective projection matrix. The height will stay the same
		// while the width will vary as per aspect ratio.
		final float ratio = (float) width / height;
		final float left = -ratio;
		final float right = ratio;
		final float bottom = -1.0f;
		final float top = 1.0f;
		final float near = 1.0f;
		final float far = 10.0f;
		Matrix.frustumM(mProjectionMatrix, 0, left, right, bottom, top, near, far);
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config)                  //15
    {
    	// Set the background clear color to gray.
		GLES20.glClearColor(0.5f, 0.5f, 0.5f, 0.5f);

		// Position the eye behind the origin.
		final float eyeX = 0.0f;
		final float eyeY = 0.0f;
		final float eyeZ = 1.5f;

		// We are looking toward the distance
		final float lookX = 0.0f;
		final float lookY = 0.0f;
		final float lookZ = -5.0f;

		// Set our up vector. This is where our head would be pointing were we holding the camera.
		final float upX = 0.0f;
		final float upY = 1.0f;
		final float upZ = 0.0f;

		// Set the view matrix. This matrix can be said to represent the camera position.
		// NOTE: In OpenGL 1, a ModelView matrix is used, which is a combination of a model and
		// view matrix. In OpenGL 2, we can keep track of these matrices separately if we choose.
		Matrix.setLookAtM(mViewMatrix, 0, eyeX, eyeY, eyeZ, lookX, lookY, lookZ, upX, upY, upZ);

		final String vertexShader =
			"uniform mat4 u_MVPMatrix;      \n"		// A constant representing the combined model/view/projection matrix.
		  + "attribute vec4 a_Position;     \n"		// Per-vertex position information we will pass in.
		  + "attribute vec4 a_Color;        \n"		// Per-vertex color information we will pass in.			  
		  + "varying vec4 v_Color;          \n"		// This will be passed into the fragment shader.
		  + "void main()                    \n"		// The entry point for our vertex shader.
		  + "{                              \n"
		  + "   v_Color = a_Color;          \n"		// Pass the color through to the fragment shader. 
		  											// It will be interpolated across the triangle.
		  + "   gl_Position = u_MVPMatrix   \n" 	// gl_Position is a special variable used to store the final position.
		  + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in 			                                            			 
		  + "}                              \n";    // normalized screen coordinates.

		final String fragmentShader =
			"precision mediump float;       \n"		// Set the default precision to medium. We don't need as high of a 
													// precision in the fragment shader.				
		  + "varying vec4 v_Color;          \n"		// This is the color from the vertex shader interpolated across the 
		  											// triangle per fragment.			  
		  + "void main()                    \n"		// The entry point for our fragment shader.
		  + "{                              \n"
		  + "   gl_FragColor = v_Color;     \n"		// Pass the color directly through the pipeline.		  
		  + "}                              \n";												

		// Load in the vertex shader.
		int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);

		if (vertexShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(vertexShaderHandle, vertexShader);

			// Compile the shader.
			GLES20.glCompileShader(vertexShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(vertexShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(vertexShaderHandle);
				vertexShaderHandle = 0;
			}
		}

		if (vertexShaderHandle == 0)
		{
			throw new RuntimeException("Error creating vertex shader.");
		}

		// Load in the fragment shader shader.
		int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);

		if (fragmentShaderHandle != 0) 
		{
			// Pass in the shader source.
			GLES20.glShaderSource(fragmentShaderHandle, fragmentShader);

			// Compile the shader.
			GLES20.glCompileShader(fragmentShaderHandle);

			// Get the compilation status.
			final int[] compileStatus = new int[1];
			GLES20.glGetShaderiv(fragmentShaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

			// If the compilation failed, delete the shader.
			if (compileStatus[0] == 0) 
			{				
				GLES20.glDeleteShader(fragmentShaderHandle);
				fragmentShaderHandle = 0;
			}
		}

		if (fragmentShaderHandle == 0)
		{
			throw new RuntimeException("Error creating fragment shader.");
		}

		// Create a program object and store the handle to it.
		int programHandle = GLES20.glCreateProgram();

		if (programHandle != 0) 
		{
			// Bind the vertex shader to the program.
			GLES20.glAttachShader(programHandle, vertexShaderHandle);			

			// Bind the fragment shader to the program.
			GLES20.glAttachShader(programHandle, fragmentShaderHandle);

			// Bind attributes
			GLES20.glBindAttribLocation(programHandle, 0, "a_Position");
			GLES20.glBindAttribLocation(programHandle, 1, "a_Color");

			// Link the two shaders together into a program.
			GLES20.glLinkProgram(programHandle);

			// Get the link status.
			final int[] linkStatus = new int[1];
			GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

			// If the link failed, delete the program.
			if (linkStatus[0] == 0) 
			{				
				GLES20.glDeleteProgram(programHandle);
				programHandle = 0;
			}
		}

		if (programHandle == 0)
		{
			throw new RuntimeException("Error creating program.");
		}
        
        // Set program handles. These will later be used to pass in values to the program.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(programHandle, "u_MVPMatrix");        
        mPositionHandle = GLES20.glGetAttribLocation(programHandle, "a_Position");
        mColorHandle = GLES20.glGetAttribLocation(programHandle, "a_Color");        
        
        // Tell OpenGL to use this program when rendering.
        GLES20.glUseProgram(programHandle);        
    }    

    private void drawTriangle(final FloatBuffer aTriangleBuffer)
	{		
		// Pass in the position information
		aTriangleBuffer.position(mPositionOffset);
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
                
        GLES20.glEnableVertexAttribArray(mPositionHandle);        
        
        // Pass in the color information
        aTriangleBuffer.position(mColorOffset);
        GLES20.glVertexAttribPointer(mColorHandle, mColorDataSize, GLES20.GL_FLOAT, false,
        		mStrideBytes, aTriangleBuffer);        
        
        GLES20.glEnableVertexAttribArray(mColorHandle);
        
		// This multiplies the view matrix by the model matrix, and stores the result in the MVP matrix
        // (which currently contains model * view).
        Matrix.multiplyMM(mMVPMatrix, 0, mViewMatrix, 0, mModelMatrix, 0);
        
        // This multiplies the modelview matrix by the projection matrix, and stores the result in the MVP matrix
        // (which now contains model * view * projection).
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mMVPMatrix, 0);

        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mMVPMatrix, 0);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, 3);                               
	}
}
