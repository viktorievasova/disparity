package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.vasova.bachelorproject.Adapter.GridImage;


public class GalleryActivity extends Activity {

	public static boolean inForeground = false;
	
	private MenuItem processItem;
	private MenuItem settingsItem;
	private MenuItem deleteItem;
	
	private Context context = this;
	
	public static Mat imgForVisualization;	
	public static Mat disparityMap;
	public static Mat result;
	
	public static Registration registration;
	
	private GridView gridView;
    private Adapter adapter;
   

    static ArrayList<String> files;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        inForeground = true;
		registration = new Registration();
        
        adapter = new Adapter(this);
        files = MainActivity.galleryList;
        //update image db
        ArrayList<GridImage> db_update = new ArrayList<GridImage>();
        for (String s : files) {
        	GridImage image = new GridImage(s, 0);
        	for(GridImage gi : MainActivity.imagesDB){
        		if (gi.getImagePath().equals(s)){
        			image.setImagePath(gi.getImagePath());
        			image.setState(gi.getState());
        			break;
        		}
        	}
        	db_update.add(image);
        }
        MainActivity.imagesDB = db_update;
        
        //set adapter
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            	if (MainActivity.imagesDB.get(position).getState() == 1){
            		MainActivity.imagesDB.get(position).setState(0);
            	}else{
            		MainActivity.imagesDB.get(position).setState(1);
            	}
                adapter.notifyDataSetChanged();
            }
        });
    }
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		//start the action for the selected files:
		if (item == processItem){
			ArrayList<String> selectedf = getSelectedFiles();
			if (selectedf.size() < 2){
				return true;
			}
			
			ArrayList<Mat> images = new ArrayList<Mat>();
			for (int i = 0; i < selectedf.size(); i++){
				images.add(Highgui.imread(selectedf.get(i)));
			}
			
			registration.setImages(images);
			imgForVisualization = registration.getImgForVisualization();
			disparityMap = registration.getDisparityMap();
			
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
	
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		processItem = menu.add("Process");
		settingsItem = menu.add("Settings");
		deleteItem = menu.add("Delete");
		ArrayList<String> selectedf = getSelectedFiles();
		if (selectedf.size() == 0){
			deleteItem.setEnabled(false);
		}else{
			deleteItem.setEnabled(true);
		}
		return true;
	}
	
	private void deleteItems(){
		ArrayList<String> selectedf = getSelectedFiles();
		for(int i = 0; i < selectedf.size(); i++){
			String path = selectedf.get(i);
			File f = new File(path);
			if (f.delete()){
				for(GridImage gi : MainActivity.imagesDB){
					if (gi.getImagePath().equals(path)){
						MainActivity.imagesDB.remove(gi);
						MainActivity.galleryList.remove(path);
						break;
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
	}
	
	private ArrayList<String> getSelectedFiles(){
		ArrayList<String> selectedF = new ArrayList<String>();
		for (int i = 0; i < MainActivity.imagesDB.size(); i++){
			GridImage gi = MainActivity.imagesDB.get(i);
			if (gi.getState() == 1){
				selectedF.add(gi.getImagePath());
			}
		}
		
		return selectedF;
	}
	
	public static Mat getOriginal(){
		return imgForVisualization;
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