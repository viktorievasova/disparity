package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Bundle;
import android.os.Environment;
import android.renderscript.Type;
import android.app.ListActivity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class GalleryActivity extends ListActivity {

	public static boolean inForeground = false;
	private ArrayList<String> list_of_files;
	private int listPosition;
	private Adapter adapter;
	
	private MenuItem processItem;
	private MenuItem settingsItem;
		
	public static Mat original1;
	public static Mat original2;
	
	public static Mat disparityMap;
	public static Mat result;
	
	public static Registration registration;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * An ArrayList of Strings list_of_files is initialized.
	 * An Adapter in set for the ListActivity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		inForeground = true;
		super.onCreate(savedInstanceState);
		list_of_files = MainActivity.galleryList;
        this.setTitle("Gallery");
        
        registerForContextMenu(getListView());
        adapter = new Adapter(this, list_of_files);
        setListAdapter(adapter);
        registration = new Registration();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		//get selected items
		String selectedValue = (String) getListAdapter().getItem(position);
		Toast.makeText(this, selectedValue, Toast.LENGTH_SHORT).show();
		listPosition = position;
	}

	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 * Method defines callback to be invoked when the context menu for this view is being built.
	 * There is an option to delete item.
	 */
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo){
		super.onCreateContextMenu(menu, v, menuInfo);

		menu.setHeaderTitle("menu");
		menu.add(0, v.getId(), 0, "delete");
	}
	
	@Override 
	public boolean onContextItemSelected(MenuItem item){
		//if an item should be deleted:
		if(item.getTitle() == "delete"){
			AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
			//get the position of deleting file:
			listPosition = menuInfo.position;
			//get the path of the file:
			String path = list_of_files.get(listPosition);
			File f = new File(path);
			if (f.delete()){
				//list_of_files.remove(listPosition);
				adapter.remove(path);
				MainActivity.galleryList.remove(path);
				//if the file was checked - it is necessary to remove the path from the list of selected files
				if (MainActivity.selectedFiles.contains(path)){
					MainActivity.selectedFiles.remove(path);
				}
				adapter.notifyDataSetChanged();
				System.out.println("file " + path + " was deleted");
				
			}else{
				System.out.println("file " + path + " could not be deleted");
			}
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.activity_gallery, menu);
		processItem = menu.add("Process");
		settingsItem = menu.add("Settings");
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		//start the action for the selected files:
		if (item == processItem){
			if (MainActivity.selectedFiles.size() < 2){
				//return true;
			}

			//original2 = Highgui.imread("mnt/sdcard/Pictures/Gallery/mmsL.jpg"); 
			//original1 = Highgui.imread("mnt/sdcard/Pictures/Gallery/mmsM.jpg");

			original1 = Highgui.imread("mnt/sdcard/Pictures/Gallery/tsucuba_left.png"); 
			original2 = Highgui.imread("mnt/sdcard/Pictures/Gallery/tsucuba_right.png");
			/*
			original2 = Highgui.imread(MainActivity.selectedFiles.get(0)); 
			original2 = Highgui.imread(MainActivity.selectedFiles.get(1));
			*/
			
			Mat[] images = {original1, original2};
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
			
			//s.compute(original1, original2, disparityMap);
			//zapis obrazku:
			String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + 
					"/Gallery/diparityMap.jpg";
			result = new Mat();
			disparityMap.convertTo(result, CvType.CV_8U, 255/(numOfDisparities*16.0));
			//Highgui.imwrite(filename, result);
			
			Intent intent = new Intent(this, GraphicsActivity.class);
        	startActivity(intent);
			
		}else if (item == settingsItem){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}
		return true;
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
