package com.vasova.bachelorproject;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Mat;

import android.opengl.GLSurfaceView;

public class Graphics_Renderer implements  GLSurfaceView.Renderer{
    
	private Mat disparityMat;
	private Mat originalImg;
		
	private float width;
	private float height;
	private float k;
	
	private Square mSquare;
	
	public Graphics_Renderer(Mat dispMat, Mat origImg)
    {   
        mSquare = new Square();
        this.disparityMat = dispMat;
        this.originalImg = origImg;
        this.width = dispMat.width();
        this.height = dispMat.height();
        k = (6.0f/width); //width of each pixel-square
    }
	
	private float rquad;
	
    public void onDrawFrame(GL10 gl){
    	gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        
        width = 50;
        height = 30;
        k = (6.0f/width); //width of each pixel-square
        float delta_x = k;
        float delta_y = -k;
        float delta_z = 0;
       
        float zoom = 4f;
        
        mSquare.setConstantValue(k);
        for (int j = 0; j < height; j++){
        	for(int i = 0; i < width; i++){
        		gl.glLoadIdentity();
        		//zoom out
        		gl.glTranslatef(0, 0, -zoom);
        		gl.glRotatef(rquad, 0.0f, 1.0f, 0.0f);	//Rotate on the Y axis
        		
        		double[] color = originalImg.get(j, i);
        		mSquare.setColor(color);
        		
        		double[] disparity = disparityMat.get(j, i);
        		delta_z = (float)(disparity[0]/(500));
        		gl.glTranslatef(i*delta_x, j*delta_y, delta_z);
        		
        		mSquare.draw(gl);						//Draw
        		rquad += 0.0005f;
        		
	        }
        }
        

    }
	    
    public void onSurfaceChanged(GL10 gl, int width, int height){
	
    	gl.glViewport(0, 0, width, height);
    	float ratio = (float) width / height;
    	gl.glMatrixMode(GL10.GL_PROJECTION);
    	gl.glLoadIdentity();
    	gl.glFrustumf(-ratio, ratio, -1, 1, 0.8f, 25);
	
    }
	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		gl.glDisable(GL10.GL_DITHER);
	    gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);
	    gl.glClearColor(1,1,1,1);
	    gl.glDisable(GL10.GL_CULL_FACE);	//disabled to see color from both sides of the square
		gl.glShadeModel(GL10.GL_SMOOTH);
		gl.glEnable(GL10.GL_DEPTH_TEST);
	}
}
