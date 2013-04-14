package com.vasova.bachelorproject;

import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.view.Menu;

public class GraphicsActivity extends Activity {

	private GLSurfaceView glView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/*getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN); 
		glView = new GLSurfaceView(this); 
		glView.setRenderer(new Graphics_Renderer(true)); 
		setContentView(glView);
		*/
		glView = new GLSurfaceView(this);

		// Check if the system supports OpenGL ES 2.0.
		final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
		final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

		if (supportsEs2) 
		{
			// Request an OpenGL ES 2.0 compatible context.
			glView.setEGLContextClientVersion(2);

			// Set the renderer to our demo renderer, defined below.
			glView.setRenderer(new Graphics_Renderer(GalleryActivity.disparityMap, GalleryActivity.original1));
		} 
		else 
		{
			// This is where you could create an OpenGL ES 1.x compatible
			// renderer if you wanted to support both ES 1 and ES 2.
			return;
		}

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
