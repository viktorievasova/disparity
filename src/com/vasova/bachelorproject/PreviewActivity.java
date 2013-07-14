package com.vasova.bachelorproject;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.view.Menu;
import android.widget.ImageView;

public class PreviewActivity extends Activity {

	String path;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_preview);
	    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
	        
        setContentView(R.layout.activity_preview);
        ImageView v = (ImageView)findViewById(R.id.imageView);
        path = getIntent().getExtras().getString("filepath");
        Mat mRgba = Highgui.imread(path);
        Imgproc.cvtColor(mRgba, mRgba, Imgproc.COLOR_BGRA2RGBA);
        Bitmap img = Bitmap.createBitmap(mRgba.cols(), mRgba.rows(),Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mRgba, img);
        v.setImageBitmap(img);
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
        
    }

    @Override
    protected void onResume(){
        super.onResume();
    }
}
