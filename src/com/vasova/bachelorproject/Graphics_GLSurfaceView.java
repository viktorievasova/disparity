package com.vasova.bachelorproject;

import org.opencv.core.Mat;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Graphics_GLSurfaceView extends GLSurfaceView{

	private Graphics_Renderer a_renderer;
	private float previousX;
	//private float previousY;
		
	public Graphics_GLSurfaceView(Context context){
		super(context);	    
	}
	
	public void setImageData(Mat oImage, Mat dImage){
		if (a_renderer == null){
			a_renderer = new Graphics_Renderer(oImage, dImage);
			setRenderer(a_renderer);
		}else{
			//changeImageData(oImage, dImage);
		}
	}
	
	@Override
	public void onPause(){
		super.onPause();
		a_renderer.releaseMeshBuffers();
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (event != null){
			float x = event.getX();
			//float y = event.getY();
			
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				
				if(a_renderer != null){
					float deltaX = (x - previousX) / 2f;
					//float deltaY = (y - previousY) / 2f;
					
					a_renderer.angleY += deltaX;
					//a_renderer.angleX += deltaY;
				}
			}
			previousX = x;
			//previousY = y;
			
			return true;
		}else{
			return super.onTouchEvent(event);
		}
	}
	
	
}
