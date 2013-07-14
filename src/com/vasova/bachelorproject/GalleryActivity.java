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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.vasova.bachelorproject.Adapter.GridImage;


public class GalleryActivity extends Activity {

	public static boolean inForeground = false;
	
	private Context context = this;
	
	private static Mat imgForVisualization;	
	private static Mat disparityMap;
	
	public static Processing processing;
	
	private GridView gridView;
    private Adapter adapter;
   
    static ArrayList<String> files;
    private ArrayList<String> imagesForProcessing;
    
    private CalculationThread thread;
    private static boolean newDataAvailable;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        inForeground = true;
		processing = new Processing();
        
        adapter = new Adapter(this);
        if (MainActivity.galleryList == null){
        	//---osetrit
        }else{
        	files = MainActivity.galleryList;
        }
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
		if (item.getItemId() == R.id.process){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() < 2){
				Toast.makeText(this, "At least two images must be selected.", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			Intent intent = new Intent(this, GraphicsActivity.class);
			startActivity(intent);
			
			thread = new CalculationThread();
			thread.run();
			
		}else if (item.getItemId() == R.id.settings){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}else if (item.getItemId() == R.id.delete){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() < 1){
				Toast.makeText(this, "At least one image must be selected", Toast.LENGTH_SHORT).show();
				return true;
			}
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
		}else if (item.getItemId() == R.id.view){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() < 2){
				Toast.makeText(this, "At least two images must be selected.", Toast.LENGTH_SHORT).show();
				return true;
			}
			
			processing.setData(imagesForProcessing);
			processing.startProcessing();
			String path = processing.getTempResult().get(2);
			Intent intent = new Intent(this, PreviewActivity.class);
			intent.putExtra("filepath", path);
        	startActivity(intent);
		}
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_gallery, menu);
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
	
	
	public static Mat getDisparity(){
		return disparityMap;
	}
	
	public static Mat getImgForVisualization(){
		return imgForVisualization;
	}
	
	public static void setImgForVisualization(Mat i){
		imgForVisualization = i;
		newDataAvailable = true;
	}
	
	public static boolean areNewDataAvailable(){
		return newDataAvailable;
	}
	
	public static void setNewDataAvailable(boolean b){
		newDataAvailable = b;
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
	
	
	class CalculationThread extends Thread{
		@Override
	    public void run() {
			boolean mi = processing.isSetMutualInformation();
			processing = new Processing();
			if(mi){
				processing.setRegistrationParametr(Registration.mi_string);
			}else{
				processing.setRegistrationParametr(Registration.soad_string);
			}
			processing.setData(imagesForProcessing);
			processing.startProcessing();
			imgForVisualization = processing.getImgForVisualization();
			setNewDataAvailable(true);
	    }
	}
}