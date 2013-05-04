package com.vasova.bachelorproject;

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
        mGLView = new Graphics_GLSurfaceView(this, GalleryActivity.getOriginal(), GalleryActivity.getDisparity());
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

	
	