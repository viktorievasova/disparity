package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;

public class GalleryActivity extends Activity {

	public static boolean inForeground = false;
	private Adapter adapter;
	
	private MenuItem processItem;
	private MenuItem settingsItem;
	private MenuItem deleteItem;
	
	private Context context = this;
	
	public static Mat original1;
	public static Mat original2;
	
	public static Mat disparityMap;
	public static Mat result;
	
	public static Registration registration;
	
	public void onCreate(Bundle savedInstanceState){
		inForeground = true;
		registration = new Registration();
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_gallery);
	    GridView gridview = (GridView) findViewById(R.id.gridview);
	    adapter = new Adapter(this);
	    gridview.setAdapter(adapter);
	    
	    
	    gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
	        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
	        	ImageView imgv = (ImageView) v;//parent.getChildAt(position);
	        	String selectedImagePath = (String) MainActivity.galleryList.get(position);
	        	
	        	if (MainActivity.selectedFiles.contains(selectedImagePath)){
	        		MainActivity.selectedFiles.remove(selectedImagePath);
	        		imgv.setBackgroundColor(Color.WHITE);
	        	}else{
			    	MainActivity.selectedFiles.add(selectedImagePath);
			    	imgv.setBackgroundColor(Color.BLACK);
	        	}
	        }
	    });
	    
	}
	
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		processItem = menu.add("Process");
		settingsItem = menu.add("Settings");
		deleteItem = menu.add("Delete");
		if (MainActivity.selectedFiles.size() == 0){
			deleteItem.setEnabled(false);
		}else{
			deleteItem.setEnabled(true);
		}
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		//start the action for the selected files:
		if (item == processItem){
			if (MainActivity.selectedFiles.size() < 2){
				return true;
			}

			//original1 = Highgui.imread("mnt/sdcard/Pictures/Gallery/mmsM.png"); 
			//original2 = Highgui.imread("mnt/sdcard/Pictures/Gallery/mmsL.png");
			
			original1 = Highgui.imread(MainActivity.selectedFiles.get(0)); 
			original2 = Highgui.imread(MainActivity.selectedFiles.get(1));
			
			
			ArrayList<Mat> images = new ArrayList<Mat>();
			images.add(original1);
			images.add(original2);
			registration.setDataSet(images);
			
			disparityMap = new Mat();			
			int channels = 3;
			int minDisparity = 0;
			int numOfDisparities = ((original1.width()/8) + 15) & -16;
			int SADWindowSize = 3;
			int P1 =  8*channels*SADWindowSize*SADWindowSize;
			int P2 = 32*channels*SADWindowSize*SADWindowSize;
			int uniquenessRatio = 10;
			int disp12MaxDiff = 1;
			int speckleWindowSize = 100;
			int speckleRange = 32;
			int preFilterCap = 63;
			
			StereoSGBM s = new StereoSGBM(minDisparity, numOfDisparities, SADWindowSize, 
				       P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio,speckleWindowSize, speckleRange, false);
			
			s.compute(original1, original2, disparityMap);
			//zapis obrazku:
			String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + 
					"/Gallery/diparityMap.jpg";
			result = new Mat();
			disparityMap.convertTo(result, CvType.CV_8U, 255/(numOfDisparities*16.0));
			if(Highgui.imwrite(filename, result)){
				System.out.println("writting ok");
			}else{
				System.out.println("writting failed");
			}
			
			Intent intent = new Intent(this, GraphicsActivity.class);
        	startActivity(intent);
			
		}else if (item == settingsItem){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}else if (item == deleteItem){
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setTitle("Delete items");
			builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					deleteItems();
				}
			});
			
			builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.cancel();
					
				}
			});
			
			AlertDialog aDialog = builder.create();
			aDialog.show();
		}
		return true;
	}
	
	private void deleteItems(){
		for(int i = 0; i < MainActivity.selectedFiles.size(); i++){
			String path = MainActivity.selectedFiles.get(i);
			File f = new File(path);
			if (f.delete()){
				MainActivity.selectedFiles.remove(i);
				adapter.remove(path);
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	public static Mat getOriginal(){
		return original1;
	}
	
	public static Mat getDisparity(){
		return disparityMap;
	}
	
	@Override
	public void onPause()
    {
		inForeground = false;
        super.onPause();
    }
	
	@Override
	public void onResume()
    {
		inForeground = true;
        super.onResume();
    }
	
	@Override
	public void onStop(){
		super.onStop();
	}
}
