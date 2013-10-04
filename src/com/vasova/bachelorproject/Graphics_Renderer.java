package com.vasova.bachelorproject;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

/**
 * A class implementing the renderer interface.
 * The Graphics_Renderer is responsible for creating and drawing the scene of a 3D model of the disparity map.  
 * @author viktorievasova
 *
 */
public class Graphics_Renderer implements  GLSurfaceView.Renderer{
	private DisparityModel mesh;
	
	public float angleY = 0f;
		
	public float deltaX;
	public float deltaY;
	
	/**
	 * Constructor of this class. Is called when the instance of this class is created. 
	 * @param vertices an array of coordinates of vertices that are visualized. Each sequence of three floats represents the x, y and z coordinates of one point.
	 * Each sequence of nine floats represents one triangle in a space.
	 * @param colors an array of RGBA values of a color of a vertex. A sequence of four floats corresponds to a RGBA color of one vertex in float[] vertices.
	 */
	public Graphics_Renderer(float[] vertices, float[] colors) {
		this.mesh = new DisparityModel(vertices, colors);
	}
	
	/**
	 * This method is called when the data for rendering a disparity map should be changed.
	 * It creates a new DisparityModel and draws it.
	 * @param vertices an array of coordinates of vertices that are visualized. Each sequence of three floats represents the x, y and z coordinates of one point.
	 * Each sequence of nine floats represents one triangle in a space.
	 * @param colors an array of RGBA values of a color of a vertex. A sequence of four floats corresponds to a RGBA color of one vertex in float[] vertices.
	 */
	public void setNewData(float[] vertices, float[] colors){
		this.mesh = new DisparityModel(vertices, colors);
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		gl.glLoadIdentity();
		gl.glTranslatef(0.0f, 0.0f, 0.0f);
		gl.glRotatef(angleY, 0f, 1f, 0f); 
		mesh.draw(gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		GLES20.glClearColor(0.2f, 0.5f, 0.65f, 1.0f);
		GLES20.glEnable(GLES20.GL_DEPTH_TEST);
		GLES20.glDepthFunc(GLES20.GL_LEQUAL);
		GLES20.glDepthMask(true);
		
		if(height == 0) {
			height = 1;
		}

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();
		
		GLU.gluPerspective(gl, 25.0f,  (float)width / (float)height, 1, 100);
        GLU.gluLookAt(gl, 0f, 0f, 16f, 0.0f, 0.0f, 0f, 0.0f, 1.0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
	}	
}