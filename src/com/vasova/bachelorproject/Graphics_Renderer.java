package com.vasova.bachelorproject;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Mat;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.opengl.GLU;
import android.opengl.GLUtils;

public class Graphics_Renderer implements  GLSurfaceView.Renderer{
    
	private float width;
	private float height;
	private float k;
	
	private Square mSquare;
	private Graphics_GLSurfaceView view;
    private Context context;
    private MeshModel model;
    private float angleZ = 0f;

    private int[] mTexture = new int[1];

    public Graphics_Renderer(Context context, Graphics_GLSurfaceView view, Mat oImage, Mat dImage)
    {
    	this.view = view;
        this.context = context;
        model = new MeshModel(oImage, dImage);
    }

    public void onDrawFrame(GL10 gl)
    {
        gl.glClearColor(0f, 0f, 0f, 1.0f);
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
        gl.glPushMatrix();
        gl.glRotatef(angleZ, 0f, 1f, 0f);

        model.draw(gl);
        gl.glPopMatrix();
        angleZ += 0.4f;
    }
    
    public void onSurfaceCreated(GL10 gl, EGLConfig config)
    {
        gl.glLoadIdentity();
        GLU.gluPerspective(gl, 25.0f, (view.getWidth() * 1f) / view.getHeight(), 1, 100);
        GLU.gluLookAt(gl, 0f, 0f, 16f, 0.0f, 0.0f, 0f, 0.0f, 1.0f, 1.0f);
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
        //gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glEnable(GL10.GL_DEPTH_TEST);
        //gl.glEnable(GL10.GL_TEXTURE_2D);

        //loadTexture(gl, context, R.drawable.rock2);
        //gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        //gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
        //gl.glTexEnvx(GL10.GL_TEXTURE_ENV, GL10.GL_TEXTURE_ENV_MODE, GL10.GL_MODULATE);
    }

    public void onSurfaceChanged(GL10 gl, int w, int h)
    {
    	gl.glViewport(0, 0, w, h);
    }


    private void loadTexture(GL10 gl, Context mContext, int mTex)
    {
        gl.glGenTextures(1, mTexture, 0);
        gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture[0]);
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeResource(mContext.getResources(), mTex);
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
        bitmap.recycle();
    }
}
	

/*
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
*/