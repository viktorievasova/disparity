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
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import com.vasova.bachelorproject.Adapter.GridImage;
import com.vasova.bachelorproject.R;
import com.vasova.bachelorproject.MainActivity;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

public class MainActivity extends Activity implements CvCameraViewListener, SensorEventListener{

	private static final String  TAG             = "OCVSample::Activity";

	public static boolean inBackground = false;
	
    public static final int      VIEW_MODE_RGBA  = 0;
    public static final int      VIEW_MODE_GRAY  = 1;
    public static final int      VIEW_MODE_CANNY = 2;

    private static int           viewMode       = VIEW_MODE_RGBA;
    private MenuItem             mItemPreviewRGBA;
    private MenuItem			 mItemGallery;
    private Mat                  mRgba;
    private Mat                  mIntermediateMat;

    private Mat	takenPicture;
    
    private boolean isCameraRunning;
    
    private CameraBridgeViewBase mOpenCvCameraView;

    private Timer t;
    private int d = 400;
    
    public static ArrayList<String> galleryList;
    public String dataFileName = "galleryData";
    public static ArrayList<GridImage> imagesDB;

//	public String selectedFilesFileName = "selectedData";
	private boolean autoCaptureON = false;
	private boolean autoCapturingStarted = false;
	private SensorManager sensorManager;
	
	
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
		Log.i(TAG, "called onCreate");
		inBackground = false;
        /*inicializace noveho seznamu vyfocenych obrazku*/
		imagesDB = new ArrayList<GridImage>();
		galleryList = new ArrayList<String>();

        readData();
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        /*nastaveni vytvoreneho layoutu pro MainActivity*/
        setContentView(R.layout.activity_main);
        
        /*osetreni kamery*/
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
                
	}

    public void onAccuracyChanged(Sensor sensor, int accuracy){
    	System.out.println("Accuracy changed to " + accuracy);
    }
    
    public void start_stopAutoCapturing(View view){
    	Button autoCaptureButton = (Button)findViewById(R.id.startCapturingButton);
    	if (!autoCapturingStarted){
	    	/*vytvoreni SensorManageru pro typ akcelerometru k detekci pohybu zarizeni*/
	        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
	        sensorManager.registerListener(this, 
	        								sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), 
	        								SensorManager.SENSOR_DELAY_NORMAL);
	        autoCaptureButton.setText("Stop Capturing");
	        autoCapturingStarted = true;
    	}else{
    		//sensorManager = null;
    		autoCaptureButton.setText("Start Capturing");
    		autoCapturingStarted = false;
    	}
    }
    
    /*zmeni-li se poloha zarizeni, anuluje se bezici timer, spusti a naplanuje se novy*/
	public void onSensorChanged(SensorEvent event){
    	//System.out.println("Got a sensor event of type " + event.sensor.getType());
    	if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
    		//System.out.println("Accelerometer change [ " + event.values[0] + ", " 
    		//										     + event.values[1] + ", " 
    		//										     + event.values[2] + "]");
    		if (t != null){
    			t.cancel();
    		}

    		t = new Timer();
    		TimerTask task = new TimerTask() {
    			/*dobehne-li timer do konce, znamena to, ze po urcitou dobu "d" se zarizeni nepohlo, v tu chvili se sejme obrazek*/
    			@Override
    			public void run() {
    				// TODO Auto-generated method stub
    				System.out.println("timer run()");
    				t.cancel();
    				t = null;
    			}
    		};
    		t.schedule(task, d);
    	}
    }
    public void callTakePicture(View view){
    	takePicture();
    }
    
    /*metoda, ktera snima obrazek*/
    private void takePicture(){
    	System.out.println("method to take a picture was started");
    	if (GalleryActivity.inForeground == true || inBackground == true || isCameraRunning == false){
    		return;
    	}
    	
    	if (takenPicture != null){
    		try{
    			//conversion bgr->rgb
    			Mat pictureToWrite = new Mat();
    			Imgproc.cvtColor(takenPicture, pictureToWrite, Imgproc.COLOR_BGRA2RGBA);
    			
    			/*generovani cesty k souboru, pripadne vytvoreni souboru, pokud neexistuje*/
    			String dirName = "Gallery";
	    		String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + dirName;
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
		    		addImgToList(filePath);
	                if(!autoCapturingStarted){
		    			String message = "Your picture was saved";
			    		Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
		    		}
	    		}
    		}catch(Exception e){
    			System.out.println("an error occured while writting the file. " + "exception:  " + e);
    		}
    	}
    }
    
    
    public void autoCaptureChanged(View view){
    	ToggleButton autoCaptureButton = (ToggleButton)view;
    	this.autoCaptureON = autoCaptureButton.isChecked();
    	Button captureButton = (Button)findViewById(R.id.captureButton);
    	Button startCapturingButton = (Button)findViewById(R.id.startCapturingButton);
    	if (autoCaptureON){
    		captureButton.setEnabled(false);
    		captureButton.setVisibility(Button.INVISIBLE);
    		startCapturingButton.setEnabled(true);
    		startCapturingButton.setVisibility(Button.VISIBLE);
    	}else{
    		captureButton.setEnabled(true);
    		captureButton.setVisibility(Button.VISIBLE);
    		startCapturingButton.setEnabled(false);
    		startCapturingButton.setVisibility(Button.INVISIBLE);
    	}
    }
    
    @Override
    public void onPause()
    {
    	inBackground = true;
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        if (t != null){
        	t.cancel();
        	t = null;
        }
        super.onPause();
    }

    @Override
    public void onResume()
    {
    	super.onResume();
        inBackground = false;
        
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }
    
    
    @Override
    public void onStop() {
    	writeData();
        //t.cancel();
        
        if (mOpenCvCameraView != null){
            mOpenCvCameraView.disableView();
        }
        super.onStop();

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
        switch (MainActivity.viewMode) {
            case MainActivity.VIEW_MODE_GRAY:
            {
                Imgproc.cvtColor(inputFrame, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
            } break;
            case MainActivity.VIEW_MODE_RGBA:
            {
                inputFrame.copyTo(mRgba);
                //Core.putText(mRgba, "OpenCV+Android", new Point(10, inputFrame.rows() - 10), 3, 1, new Scalar(255, 0, 0, 255), 2);
            } break;
            case MainActivity.VIEW_MODE_CANNY:
            {
                Imgproc.Canny(inputFrame, mIntermediateMat, 80, 100);
                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
            } break;
        }

        takenPicture = inputFrame;
        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        //mItemPreviewRGBA = menu.add("Preview RGBA");
        //mItemPreviewGray = menu.add("Preview GRAY");
        //mItemPreviewCanny = menu.add("Canny");
        mItemGallery = menu.add("Gallery");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemPreviewRGBA)
        {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            viewMode = VIEW_MODE_RGBA;
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
			//FileOutputStream fosSelected = openFileOutput(selectedFilesFileName, MODE_PRIVATE);
			BufferedWriter bffw = new BufferedWriter(new OutputStreamWriter(fos));
			//BufferedWriter bffwSelected = new BufferedWriter(new OutputStreamWriter(fosSelected));
			
			String line;
			for (int i = 0; i < galleryList.size(); i++){
				line = galleryList.get(i) + "\n";
				if(line.equals("gallery is empty")){
					continue;
				}else{
					bffw.write(line);
				}
			}
			
			/*
			for (int i = 0; i < selectedFiles.size(); i++){
				line = selectedFiles.get(i) + "\n";
				bffwSelected.write(line);
			}
			*/
			bffw.close();
			fos.close();
			//bffwSelected.close();
			//fosSelected.close();
		}catch(Exception e){
			Log.d("CameraApp","failed to write data");
		}
	}
	
	private void readData(){
		try{
			FileInputStream fis = openFileInput(dataFileName);
			//FileInputStream fisSelected = openFileInput(selectedFilesFileName);
			
			galleryList = new ArrayList<String>();
			//selectedFiles = new ArrayList<String>();
			BufferedReader bffr = new BufferedReader(new InputStreamReader(fis));
			//BufferedReader bffrSelected = new BufferedReader(new InputStreamReader(fisSelected));
			
			String line;
			String filePath;
			while ((line = bffr.readLine()) != null){
				filePath = line;
				File f = new File(filePath);
				if (f.exists()){
					galleryList.add(filePath);	
				}				
			}
			
			String lineSelected;
			//while((lineSelected = bffrSelected.readLine()) != null){
				//filePath = lineSelected;
				//File f = new File(filePath);
				//if (f.exists()){
					//selectedFiles.add(filePath);
				//}
			//}
			bffr.close();
			fis.close();
			
			//bffrSelected.close();
			//fisSelected.close();
		}catch(Exception e){
			System.out.println(e);
		}
	}

}
