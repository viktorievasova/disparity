package com.vasova.bachelorproject;

import org.opencv.core.Mat;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class Graphics_GLSurfaceView extends GLSurfaceView{

	Graphics_Renderer a_renderer;
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
	
}
