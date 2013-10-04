package com.vasova.bachelorproject;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.WindowManager;

/**
 * This activity holds the Graphics_GLSurfaceView to view the 3D model of the disparity map.
 * 
 * @author viktorievasova
 *
 */
public class GraphicsActivity extends Activity {

	private static Graphics_GLSurfaceView mGLView;
	private Timer timer;
	private int time = 500;
	private static Handler handler = new Handler() {
        public void handleMessage(Message msg) {
        	float[] vertices = GalleryActivity.getVerticesFor3DVisualization();
	        float[] colors = GalleryActivity.getColorsFor3DVisualization();
	        mGLView.set3DData(vertices, colors);
        }
    };
    
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	    
	    mGLView = new Graphics_GLSurfaceView(this);
	   
	    if (GalleryActivity.areNew3DDataAvailable()){
        	float[] vertices = GalleryActivity.getVerticesFor3DVisualization();
        	float[] colors = GalleryActivity.getColorsFor3DVisualization();
        	mGLView.set3DData(vertices, colors);
        }else{
        	mGLView.set3DData(new float[0], new float[0]);
        }
	    setContentView(mGLView);
        timer = new Timer();
        timer.schedule(new Checking3DDataTimerTask(), time);
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
        timer.schedule(new Checking3DDataTimerTask(), time);
    }
    
    int no_data_available = 0;    
    /**
     * This class checks the availability of new data for 3D rendering.
     * @author viktorievasova
     *
     */
    private class Checking3DDataTimerTask extends TimerTask{
    	/**
    	 * Updates the data for rendering if there are new data available.
    	 * If the rendering data were not updated for 50 seconds, it stops the running Timer.
    	 */
    	@Override
    	public void run(){
    		if(GalleryActivity.areNew3DDataAvailable()){
    			handler.obtainMessage().sendToTarget();
    			no_data_available = 0;
    		}else{
    			no_data_available++;
    		}
    		timer.cancel();
    		if (no_data_available < 50000/time){
    			timer = new Timer();
    			timer.schedule(new Checking3DDataTimerTask(), time);
    		}
    	}
    }
}
