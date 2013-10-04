package com.vasova.bachelorproject;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.vasova.bachelorproject.R;
import com.vasova.bachelorproject.MainActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

/**
 * This activity is the main activity of this application.
 * It handles the camera and the process of taking pictures.
 * 
 * @author viktorievasova
 *
 */
public class MainActivity extends Activity implements SensorEventListener{

	private static final String  TAG = "BachelorProject::MainActivity";
	public static boolean inBackground = false;
    private final String messageTakenPicture = "Your picture was saved";
	private boolean autoCaptureON = false;
	private boolean autoCapturingStarted = false;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean acc_initialized;
	private float mLastX, mLastY;
	private final float NOISE = (float) 1.0;
	private Timer timer;
    private int delay = 400;
    private CameraPreview preview;
    private Camera camera;
	
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		inBackground = false;
		
		DatabaseHandler db = ((BachalorProjectApplication)getApplication()).db;
		db.close();
        
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        
        preview = new CameraPreview(this, (SurfaceView)findViewById(R.id.surfaceView));
		preview.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		((RelativeLayout) findViewById(R.id.preview)).addView(preview);
		preview.setKeepScreenOn(true);
	}
	
	 @Override
    public void onPause()
    {
		 Log.i(TAG, "onPause");
		 inBackground = true;
		 
		 if (timer != null){
			 timer.cancel();
			 timer = null;
		 }
		 if(sensorManager != null){
			 sensorManager.unregisterListener(this);
		 }
		 
		 if(camera != null) {
			camera.stopPreview();
			preview.setCamera(null);
			camera.release();
			camera = null;
		}
		 
		 Button startCapturingButton = (Button)findViewById(R.id.startCapturingButton);
		 startCapturingButton.setSelected(false);
		 TextView autocapturing_textview = (TextView)findViewById(R.id.startCapturing_textView);
		 autocapturing_textview.setText("Press to start auto-capturing");
		 autoCapturingStarted = false;
		 
		 super.onPause();
    }

    @Override
    public void onResume()
    {
    	Log.i(TAG, "onResume");
    	super.onResume();
    	camera = Camera.open();
		camera.startPreview();
		preview.setCamera(camera);
        inBackground = false;
    }
    
    
    @Override
    public void onStop() {        
        super.onStop();
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    	
    }
    /**
     * This method handles the auto-capturing.
     * It observes the motion of the device.
	 * When the position of the device is still for an approximately 0.5 s, the picture is taken.
     */
    public void onSensorChanged(SensorEvent event){
    	if (autoCaptureON){
	    	if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
	    		float x = event.values[0];
	    		float y = event.values[1];
	    		
	    		if (!acc_initialized){
	    			mLastX = x; 
	    			mLastY = y; 
	    			acc_initialized = true;
		    		
	    			if (timer != null){
						timer.cancel();
					}
					timer = new Timer();
					TimerTask task = new TimerTask() {
						@Override
						public void run() {
							// TODO Auto-generated method stub
							Log.d("Accelerometer test", "TIME'S UP");
							takePicture();
							timer.cancel();
							timer = null;
						}
					};
					timer.schedule(task, delay);
	    		}else{
	    			float deltaX = Math.abs(mLastX - x);
	    			float deltaY = Math.abs(mLastY - y);
	    			
	    			if (deltaX < NOISE){
	    				deltaX = (float)0.0;
	    			}
	    			if (deltaY < NOISE){
	    				deltaY = (float)0.0;
	    			}
	    			
	    			if ( (deltaX > 0) || (deltaY > 0) ){
	    				if (timer != null){
							timer.cancel();
						}
						timer = new Timer();
						TimerTask task = new TimerTask() {
							@Override
							public void run() {
								// TODO Auto-generated method stub
								Log.d("Accelerometer test", "TIME'S UP");
								takePicture();
								timer.cancel();
								timer = null;
							}
						};
						timer.schedule(task, delay);
	    			}
	    		}	    		
	    	}
    	}
    }
	
    /**
     * Changes the visibility of the graphical user interface when clicking a button.
     * @param view the view.
     */
	public void autoCaptureStateChanged(View view){
    	ToggleButton autoCaptureButton = (ToggleButton)view;
    	this.autoCaptureON = autoCaptureButton.isChecked();
    	Button captureButton = (Button)findViewById(R.id.captureButton);
    	Button startCapturingButton = (Button)findViewById(R.id.startCapturingButton);
    	TextView startCapturing_text = (TextView)findViewById(R.id.startCapturing_textView);
    	
    	if (autoCaptureON){
    		captureButton.setEnabled(false);
    		captureButton.setVisibility(Button.INVISIBLE);
    		startCapturingButton.setEnabled(true);
    		startCapturingButton.setVisibility(Button.VISIBLE);
    		startCapturing_text.setVisibility(TextView.VISIBLE);
    	}else{
    		captureButton.setEnabled(true);
    		captureButton.setVisibility(Button.VISIBLE);
    		startCapturingButton.setEnabled(false);
    		startCapturingButton.setVisibility(Button.INVISIBLE);
    		startCapturing_text.setVisibility(TextView.INVISIBLE);
    		if (sensorManager != null){
    			sensorManager.unregisterListener(this);
    		}
    	}
    }
	
	/**
	 * This method starts the auto-capturing process.
	 * It activates the sensors manager to observe the motion of the device.
	 * @param view
	 */
	public void start_stopAutoCapturing(View view){
    	TextView autocapturing_textview = (TextView)findViewById(R.id.startCapturing_textView);
    	Button startCapturingButton = (Button)findViewById(R.id.startCapturingButton);
    	if (!autoCapturingStarted){
	    	acc_initialized = false;
			sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
			sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
			startCapturingButton.setSelected(true);
			autocapturing_textview.setText("Press to stop auto-capturing");
	        autoCapturingStarted = true;
    	}else{
    		sensorManager.unregisterListener(this);
    		startCapturingButton.setSelected(false);
    		autocapturing_textview.setText("Press to start auto-capturing");
    		autoCapturingStarted = false;
    	}
    }
	
	
	public void callTakePicture(View view){
		takePicture();
	}
	     
    /**
     * This method handles taking the picture.
     * It saves the picture on the SD Card and updates the records in the SQLIte database.
     */
    private void takePicture(){
    	if (GalleryActivity.inForeground || inBackground ){
    		return;
    	}
    	if(!autoCapturingStarted){
	    	Toast.makeText(this, messageTakenPicture, Toast.LENGTH_SHORT).show();
	    }else{
	    	runOnUiThread(new Runnable() {
	    		public void run(){
	    			Toast.makeText(getApplicationContext(), messageTakenPicture, Toast.LENGTH_SHORT).show();    
	    		}
	    	}); 
	    }
	    camera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }
    
    public static boolean isSDCARDAvailable(){
    	return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_main, menu);
		return true;
	}
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        
        if (item.getItemId() == R.id.gallery_menu_item){
        	Intent intent = new Intent(this, GalleryActivity.class);
        	startActivity(intent);
        }else if (item.getItemId() == R.id.sample_data_menu_item){
        	Intent intent = new Intent(this, SampleDataGalleryActivity.class);
        	startActivity(intent);
        }

        return true;
    }
    
	private void resetCam() {
		camera.startPreview();
		preview.setCamera(camera);
	}

	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
		}
	};

	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};
    
	int getOrientation(){
		return this.getResources().getConfiguration().orientation;
	}
	
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			String dirName, filePath;
			try {
				// Write to SD Card
				
				dirName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Gallery";
				String timeOfTakingPicture = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
				filePath = dirName + "/img" + timeOfTakingPicture + ".jpg";
	    		File galleryDir = new File(dirName);
				
	    		if (!galleryDir.exists()){
	    			Log.d("bachelor project", "creating directory for gallery");
	    			if(!galleryDir.mkdirs()){
	    				Log.d("bachelor project", "failed to create directory");
	    			}
	    		}
				
				//Log.i(TAG, "new image with path " + filePath);
				InputStream inputstream = new ByteArrayInputStream(data);
		        //Bitmap bmp = BitmapFactory.decodeStream(inputstream);
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inSampleSize = 2;
				Bitmap bmp = BitmapFactory.decodeStream(inputstream, null, options);

		       
		        
				Mat m  = new Mat();
				Utils.bitmapToMat(bmp, m);
				Imgproc.cvtColor(m, m, Imgproc.COLOR_BGRA2RGBA);
				
				if (m.width() > 1100){
					double width = 1100;
					double height = width/m.width() * m.height();
					Imgproc.resize(m, m, new Size(width, height));
				}
				
				if (getOrientation() == Configuration.ORIENTATION_PORTRAIT){
	                Core.flip(m.t(), m, 1);
				}
				Highgui.imwrite(filePath, m);
				
				//add img path to database
				DatabaseHandler db = ((BachalorProjectApplication)getApplication()).db;
				db.addImage(new ImageDBRecord(filePath));
				db.close();
				
				resetCam();
				
			} catch (Exception e) {
				Log.i(TAG, "exception: " + e.getMessage());
			} finally {
			}
			Log.d(TAG, "onPictureTaken - jpeg");
		}
	};
	
	
}
