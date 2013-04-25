package com.vasova.bachelorproject;

import org.opencv.core.Mat;

import android.opengl.GLSurfaceView;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;

public class GraphicsActivity extends Activity {

	private GLSurfaceView mGLView;

	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mGLView = new Graphics_GLSurfaceView(this, GalleryActivity.original1, GalleryActivity.result);
        setContentView(mGLView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        mGLView.onPause();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mGLView.onResume();
    }
}

	
	/*
	private GLSurfaceView glView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		glView = new GLSurfaceView(this); 
		glView.setRenderer(new Graphics_Renderer(GalleryActivity.disparityMap, GalleryActivity.original1)); 
		setContentView(glView);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_graphics, menu);
		return true;
	}
	
	@Override
	public void onPause()
    {
        super.onPause();
        glView.onPause();
    }
	
	@Override
	public void onResume()
    {
        super.onResume();
        glView.onResume();
    }

}
*/