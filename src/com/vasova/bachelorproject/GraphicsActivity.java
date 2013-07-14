package com.vasova.bachelorproject;


import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.util.Log;
import android.app.Activity;
import android.content.pm.ActivityInfo;

public class GraphicsActivity extends Activity {

	private Graphics_GLSurfaceView mGLView;
	private Timer timer;
	private int time = 500;
	
	 /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        mGLView = new Graphics_GLSurfaceView(this);
        setContentView(mGLView);
        
        if (GalleryActivity.getImgForVisualization() != null){
        	mGLView.setImageData(GalleryActivity.getImgForVisualization(), GalleryActivity.getDisparity());
        }
        timer = new Timer();
        timer.schedule(new CheckingDataTimerTask(), time);
    }
    

    @Override
    protected void onPause()
    {
        super.onPause();
        if (mGLView != null){
        	mGLView.onPause();
        }
        if(timer != null){
        	timer.cancel();
        }
        
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if (mGLView != null){
        	mGLView.onResume();
        }
        timer = new Timer();
        timer.schedule(new CheckingDataTimerTask(), time);
    }
    
    private class CheckingDataTimerTask extends TimerTask{
    	@Override
    	public void run(){
    		Log.i("GraphicsActivity", "checking available data");
    		if(GalleryActivity.areNewDataAvailable()){
    			GalleryActivity.setNewDataAvailable(false);
    			mGLView.setImageData(GalleryActivity.getImgForVisualization(), GalleryActivity.getDisparity());
    		}
    		timer.cancel();
    		timer = new Timer();
    		timer.schedule(new CheckingDataTimerTask(), time);
    	}
    }
}

	
	