package com.vasova.bachelorproject;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class Graphics_GLSurfaceView extends GLSurfaceView{

	public Graphics_GLSurfaceView(Context context){
		super(context);
		
		setEGLContextClientVersion(2);
		setRenderMode(RENDERMODE_WHEN_DIRTY);
	}
	
}
