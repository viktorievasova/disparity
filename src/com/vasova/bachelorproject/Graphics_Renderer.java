package com.vasova.bachelorproject;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import org.opencv.core.Mat;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;

public class Graphics_Renderer implements  GLSurfaceView.Renderer{
	//private Square 		square;
	private MeshModel mesh;
	
	public float angleX = 0f;
	public float angleY = 0f;
	
	private float angleDelta = 0f;//0.4f;
	
	public float deltaX;
	public float deltaY;
	
	public Graphics_Renderer(Context context, Mat origImg, Mat dispImg) {
		//this.context = context;
		this.mesh = new MeshModel(origImg, dispImg);
	}
	
	public void releaseMeshBuffers(){
		if (this.mesh != null){
			this.mesh.releaseBuffers();
		}
	}
	
	@Override
	public void onDrawFrame(GL10 gl) {
	        
	        
		// clear Screen and Depth Buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

		// Reset the Modelview Matrix
		gl.glLoadIdentity();

		// Drawing
		gl.glTranslatef(0.0f, 0.0f, -5.0f);
		
		gl.glRotatef(angleY, 0f, 1f, 0f); 
		gl.glRotatef(angleX, 1f, 0f, 0f);				
		mesh.draw(gl);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		if(height == 0) {
			height = 1;
		}

		gl.glViewport(0, 0, width, height);
		gl.glMatrixMode(GL10.GL_PROJECTION);
		gl.glLoadIdentity();

		//Calculate The Aspect Ratio Of The Window
		//GLU.gluPerspective(gl, 45.0f, (float)width / (float)height, 0.1f, 100.0f);
        GLU.gluPerspective(gl, 25.0f,  (float)width / (float)height, 1, 100);
        GLU.gluLookAt(gl, 0f, 0f, 16f, 0.0f, 0.0f, 0f, 0.0f, 1.0f, 1.0f);

		gl.glMatrixMode(GL10.GL_MODELVIEW);
		gl.glLoadIdentity();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		
	}

	public void switchMode(){
		if (angleDelta == 0.4f){
			angleDelta = 0f;
		}else if (angleDelta == 0f){
			angleDelta = 0.4f;
		}
	}
}