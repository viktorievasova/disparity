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
import android.view.WindowManager;
import android.widget.ImageView;

/**
 * This activity shows the 2D result of the depth information.
 * 
 * @author viktorievasova
 *
 */
public class PreviewActivity extends Activity {

	private String TAG = "PreviewActivity";
	private Timer timer;
	private int time = 500;
	private Mat mRgba;
	private ImageView v;
	private static Handler handler;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_preview);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    v = (ImageView)findViewById(R.id.imageView);
	    handler = new Handler() {
	        public void handleMessage(Message msg) {
	        	v = (ImageView)findViewById(R.id.imageView);
		        mRgba = GalleryActivity.getImgForVisualization();
		        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2RGBA);
		        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
		        Utils.matToBitmap(mRgba, img);
		        v.setImageBitmap(img);
		        v.invalidate();
	        }
	    };
	    
        if (GalleryActivity.areNew2DDataAvailable()){
	        mRgba = GalleryActivity.getImgForVisualization();
	        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2RGBA);
	        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
	        Utils.matToBitmap(mRgba, img);
	        v.setImageBitmap(img);
	        v.invalidate();
        }
        timer = new Timer();
        timer.schedule(new Checking2DDataTimerTask(), time);
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
        	Log.i(TAG, "timer was canceled");
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        timer = new Timer();
        timer.schedule(new Checking2DDataTimerTask(), time);
    }
    
    int no_data_available = 0;
    /**
     * This class checks the availability of new data for 2D visualization.
     * @author viktorievasova
     *
     */
    private class Checking2DDataTimerTask extends TimerTask{
    	/**
    	 * Updates the visualized Mat if there is new one available.
    	 * If the Mat was not updated for 50 seconds, it stops the running Timer.
    	 */
    	@Override
    	public void run(){
    		if(GalleryActivity.areNew2DDataAvailable()){
    			handler.obtainMessage().sendToTarget();
    			no_data_available = 0;
    		}else{
    			no_data_available++;
    		}
    		timer.cancel();
    		if (no_data_available < 50000/time){
    			timer = new Timer();
    			timer.schedule(new Checking2DDataTimerTask(), time);
    		}else{
    			Log.i(TAG, "there were no new data for 50s");
    		}
    	}
    }
}
