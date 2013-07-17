package com.vasova.bachelorproject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.vasova.bachelorproject.Adapter.GridImage;
import com.vasova.bachelorproject.R;
import com.vasova.bachelorproject.MainActivity;

import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements CvCameraViewListener, SensorEventListener{

	private static final String  TAG = "BachelorProject::MainActivity";

	public static ArrayList<String> galleryList;
    public String dataFileName = "galleryData";
    public static ArrayList<GridImage> imagesDB;

	public static boolean inBackground = false;
	
    private MenuItem             mItemPreviewRGBA;
    private MenuItem			 mItemGallery;
    private Mat                  mRgba;
    private Mat                  mIntermediateMat;

    private Mat	takenPicture;
    private final String messageTakenPicture = "Your picture was saved";
	
    private boolean isCameraRunning;
    
    private CameraBridgeViewBase mOpenCvCameraView;

	private boolean autoCaptureON = false;
	private boolean autoCapturingStarted = false;
	private SensorManager sensorManager;
	private Sensor accelerometer;
	private boolean acc_initialized;
	private float mLastX, mLastY;
	private final float NOISE = (float) 1.0;
	
	private Timer timer;
    private int delay = 400;
    
	
    public MainActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		inBackground = false;
        /*inicializace noveho seznamu vyfocenych obrazku a nacteni dat*/
		imagesDB = new ArrayList<GridImage>();
		galleryList = new ArrayList<String>();
		readData();
        
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
	}
	
	 @Override
    public void onPause()
    {
		 Log.i(TAG, "onPause");
		 inBackground = true;
		 if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
		 if (timer != null){
			 timer.cancel();
			 timer = null;
		 }
		 
		 //pri zavoleni onPause() se vypne automaticke snimani obrazu
		 if(sensorManager != null){
			 sensorManager.unregisterListener(this);
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
    	
        inBackground = false;
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    
    
    @Override
    public void onStop() {
    	writeData();
        
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
        super.onStop();
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    	
    }
    
    /*zmeni-li se poloha zarizeni, anuluje se bezici timer, naplanuje se a spusti novy*/
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
	
	public void start_stopAutoCapturing(View view){
    	TextView autocapturing_textview = (TextView)findViewById(R.id.startCapturing_textView);
    	Button startCapturingButton = (Button)findViewById(R.id.startCapturingButton);
    	if (!autoCapturingStarted){
	    	/*vytvoreni SensorManageru pro typ akcelerometru k detekci pohybu zarizeni*/
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
	     
    /*metoda, ktera snima obrazek*/
    private void takePicture(){
    	Log.d(TAG, "method to take a picture was started");
    	if (GalleryActivity.inForeground || inBackground ||  !isCameraRunning){
    		return;
    	}
    	
    	if (takenPicture != null && isSDCARDAvailable()){
    		try{
    			//conversion bgr->rgb
    			Mat pictureToWrite = new Mat();
    			Imgproc.cvtColor(takenPicture, pictureToWrite, Imgproc.COLOR_BGRA2RGBA);
    			if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
	    			Mat dst = new Mat();
	    			Core.transpose(pictureToWrite, dst);
	    			Core.flip(dst, dst, 1);
	    			pictureToWrite = dst;
    			}
    			/*generovani cesty k souboru, pripadne vytvoreni souboru, pokud neexistuje*/
    			String dirName = "Gallery";
	    		String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + dirName;
    			//String dirPath = "/mnt/sdcard/Pictures" + "/" + dirName;
	    		File galleryDir = new File(dirPath);
	    		
	    		if (!galleryDir.exists()){
	    			Log.d("bachelor project", "creating directory for gallery");
	    			if(!galleryDir.mkdirs()){
	    				Log.d("bachelor project", "failed to create directory");
	    			}
	    		}
	    		/*vytvoreni nazvu pro ukladany soubor a zapsani souboru*/
	    		String timeOfTakingPicture = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
	    		String filePath = dirPath + "/img" + timeOfTakingPicture + ".jpg";
	    		if (Highgui.imwrite(filePath, pictureToWrite)){
		    		Log.d(TAG, "image saved with path: " + filePath);
		    		addImgToList(filePath);
		    		if(!autoCapturingStarted){
		    			Toast.makeText(this, messageTakenPicture, Toast.LENGTH_SHORT).show();
		    		}else{
		    			runOnUiThread(new Runnable() {
		    			   public void run(){
		    			      Toast.makeText(getApplicationContext(), messageTakenPicture, Toast.LENGTH_SHORT).show();    
		    			   }
		    			}); 
		    		}
	                
	    		}
    		}catch(Exception e){
    			Log.d(TAG, "an error occured while writting the file. " + "exception:  " + e);
    		}
    	}
    }
    
    public static boolean isSDCARDAvailable(){
    	return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)? true :false;
	}

    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        isCameraRunning = true;
    }

    public void onCameraViewStopped() {
    	isCameraRunning = false;
    	mRgba.release();
        mIntermediateMat.release();
    }

    public Mat onCameraFrame(Mat inputFrame) {
        inputFrame.copyTo(mRgba);
        takenPicture = inputFrame;
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemGallery = menu.add("Gallery");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
        {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        }
        else if (item == mItemGallery){
        	Intent intent = new Intent(this, GalleryActivity.class);
        	startActivity(intent);
        }

        return true;
    }
    
    
    private void addImgToList(String imgPath){
    	ArrayList<String> tmpList = new ArrayList<String>();
    	tmpList.add(imgPath);
    	for (int i = 0; i < galleryList.size(); i++){
    		tmpList.add(galleryList.get(i));
    	}
    	galleryList = tmpList;
    }
    
    private void writeData(){
		try{
			FileOutputStream fos = openFileOutput(dataFileName, MODE_PRIVATE);
			BufferedWriter bffw = new BufferedWriter(new OutputStreamWriter(fos));
			
			String line;
			for (int i = 0; i < galleryList.size(); i++){
				line = galleryList.get(i) + "\n";
				if(line.equals("gallery is empty")){
					continue;
				}else{
					bffw.write(line);
				}
			}
			
			bffw.close();
			fos.close();
			
		}catch(Exception e){
			Log.d("CameraApp","failed to write data");
		}
	}
	
	private void readData(){
		try{
			FileInputStream fis = openFileInput(dataFileName);
			
			galleryList = new ArrayList<String>();
			BufferedReader bffr = new BufferedReader(new InputStreamReader(fis));
			
			String line;
			String filePath;
			while ((line = bffr.readLine()) != null){
				filePath = line;
				File f = new File(filePath);
				if (f.exists()){
					galleryList.add(filePath);	
				}
			}
			bffr.close();
			fis.close();
		}catch(Exception e){
			Log.d(TAG, "exception " + e);
		}
	}
	

}
