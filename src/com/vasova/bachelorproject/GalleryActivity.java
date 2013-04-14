package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.os.Bundle;
import android.os.Environment;
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
	private MenuItem graphicsItem;
	
	public static Square square;
	
	public static Mat original1;
	public static Mat original2;
	
	public static Mat disparityMap;
	
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 * An ArrayList of Strings list_of_files is initialized.
	 * An Adapter in set for the ListActivity
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		inForeground = true;
		System.out.println("gallery view was created");
		list_of_files = MainActivity.galleryList;
        
        super.onCreate(savedInstanceState);
        
        registerForContextMenu(getListView());
        adapter = new Adapter(this, list_of_files);
        setListAdapter(adapter);
        
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
		getMenuInflater().inflate(R.menu.activity_gallery, menu);
		processItem = menu.add("process");
		graphicsItem = menu.add("graphics");
		return true;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		//start the action for the selected files:
		if (item == graphicsItem){
			Intent intent = new Intent(this, GraphicsActivity.class);
        	startActivity(intent);
		}else if (item == processItem){
			if (MainActivity.selectedFiles.size() < 2){
				//return true;
			}

			original1 = Highgui.imread("mnt/sdcard/Pictures/Gallery/rubberwhale1.png"); 
			original2 = Highgui.imread("mnt/sdcard/Pictures/Gallery/rubberwhale2.png");
			disparityMap = new Mat();
			
			int minDisparity = 0;
			int numOfDisparities = 16*8;
			int SADWindowSize = 7;
			
			StereoSGBM s = new StereoSGBM(minDisparity, numOfDisparities, SADWindowSize, 
					       0, 0, 0, 0, 0, 0, 0, false);
			s.compute(original1, original2, disparityMap);
			
			String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + 
					"/Gallery/disparity_map.jpg";

			Highgui.imwrite(filename, disparityMap);
			
			Intent intent = new Intent(this, GraphicsActivity.class);
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
