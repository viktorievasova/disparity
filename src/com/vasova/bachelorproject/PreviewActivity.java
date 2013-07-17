package com.vasova.bachelorproject;

import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;

public class PreviewActivity extends Activity {

	private Timer timer;
	private int time = 500;
	
	String path;
	Mat mRgba;
	
	ImageView v;
		
	private static Handler mHandler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    setContentView(R.layout.activity_preview);
	    v = (ImageView)findViewById(R.id.imageView);
	    mHandler = new Handler() {
	        public void handleMessage(Message msg) {
	        	v = (ImageView)findViewById(R.id.imageView);
		        mRgba = GalleryActivity.getImgForVisualization();
		        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2RGBA);
		        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
		        Utils.matToBitmap(mRgba, img);
		        v.setImageBitmap(img);
		    	Log.d("CheckingViewDataTimerTask", "handler was called");
	        }
	    };
        if (GalleryActivity.getImgForVisualization() != null){
	        mRgba = GalleryActivity.getImgForVisualization();
	        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2RGBA);
	        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(mRgba, img);
	        v.setImageBitmap(img);
        }
        timer = new Timer();
        timer.schedule(new CheckingViewDataTimerTask(), time);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_preview, menu);
		return true;
	}
	
	 @Override
    protected void onPause(){
        super.onPause();
        if(timer != null){
        	timer.cancel();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        timer = new Timer();
        timer.schedule(new CheckingViewDataTimerTask(), time);
    }
    
    private class CheckingViewDataTimerTask extends TimerTask{
    	@Override
    	public void run(){
    		Log.i("GraphicsActivity", "checking available data");
    		if(GalleryActivity.areNewDataAvailable()){
    			GalleryActivity.setNewDataAvailable(false);
    			mRgba = GalleryActivity.getImgForVisualization();
    			mHandler.obtainMessage().sendToTarget();
    		}
    		timer.cancel();
    		timer = new Timer();
    		
    		timer.schedule(new CheckingViewDataTimerTask(), time);
    	}
    }
}
