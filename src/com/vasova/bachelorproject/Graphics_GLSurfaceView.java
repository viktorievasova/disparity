package com.vasova.bachelorproject;

import org.opencv.core.Mat;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class Graphics_GLSurfaceView extends GLSurfaceView{

	Graphics_Renderer a_renderer;
	float previousX;
	float previousY;
	
	public Graphics_GLSurfaceView(Context context, Mat oImage, Mat dImage){
			super(context);
	        a_renderer = new Graphics_Renderer(context, oImage, dImage);
	        setRenderer(a_renderer);	    
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
			float y = event.getY();
			
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				
				if(a_renderer != null){
					float deltaX = (x - previousX) / 2f;
					//float deltaY = (y - previousY) / 2f;
					
					a_renderer.angleY += deltaX;
					//a_renderer.angleX += deltaY;
					
				}
				
			}
			previousX = x;
			previousY = y;
			
			return true;
		}else{
			return super.onTouchEvent(event);
		}
	}
	
}
