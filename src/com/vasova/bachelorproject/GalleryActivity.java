package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.opencv.core.Mat;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;
import com.vasova.bachelorproject.Adapter.GridImage;

/**
 * Activity for browsing images.
 * This activity is used to view thumbnails of images taken by the camera in MainActivity  
 * and enables user to select individual images.   
 * 
 * @author viktorievasova
 *
 */
public class GalleryActivity extends Activity {

	public static boolean inForeground = false;
	private static String TAG = "GalleryActivity";
	private Context context = this;
   		
	private GridView gridView;
    private Adapter adapter;
   
    private static ArrayList<ImageStructure> imagesForProcessing;
    
    private static ArrayList<GridImage> images;
    private static CalculationThread thread;
    
    private int outputMode = 3;
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        inForeground = true;
        if (thread != null){
        	thread.interrupt();
			vertices = new float[]{};
			colors = new float[]{};
        }
        adapter = new Adapter(this);
        images = readImagesFromDB();
        adapter.setListOfFiles(images);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            	if (images.get(position).getState() == 1){
            		images.get(position).setState(0);
            	}else{
            		images.get(position).setState(1);
            	}
                adapter.notifyDataSetChanged();
            }
        });
        
    }
	
	/**
	 * Opens the SQLite database of this application and reads all data from it. 
	 * 
	 * @return Returns an ArrayList of GridView filled with the data from the SQLite database.
	 */
	private ArrayList<GridImage> readImagesFromDB(){
    	ArrayList<GridImage> list = new ArrayList<GridImage>();   
    	DatabaseHandler db = ((BachalorProjectApplication)getApplication()).db;
    	List<ImageDBRecord> images = db.getAllImages();
    	for ( int i = images.size()-1; i >= 0; i--) {
    		ImageDBRecord img = images.get(i);
        	File f = new File(img.getPath());
        	if (!f.exists()){
        		db.deleteImage(img);
        	}else{
        		GridImage g_img = new GridImage(img.getPath(), 0);
        		list.add(g_img);
        	}
		}	
    	db.close();
	    return list;
    }
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		//start the action for the selected files:
		if (item.getItemId() == R.id.view_3D_menu_item){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() == 0){
				Toast.makeText(this, "You have not selected any images.", Toast.LENGTH_SHORT).show();
				return true;
			}else if(imagesForProcessing.size() != 2){
				Toast.makeText(this, "Two images must be selected.", Toast.LENGTH_SHORT).show();
				return true;
			}
			outputMode = 3;
			if (thread != null){
	        	thread.interrupt();
				vertices = new float[]{};
				colors = new float[]{};
	        }
			thread = new CalculationThread();
			thread.start();
			
			Intent intent = new Intent(this, GraphicsActivity.class);
			startActivity(intent);
			
		}else if (item.getItemId() == R.id.view_2D_menu_item){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() == 0){
				Toast.makeText(this, "You have not selected any images.", Toast.LENGTH_SHORT).show();
				return true;
			}else if(imagesForProcessing.size() != 2){
				Toast.makeText(this, "Two images must be selected.", Toast.LENGTH_SHORT).show();
				return true;
			}
			outputMode = 2;
			if (thread != null){
	        	thread.interrupt();
				vertices = new float[]{};
				colors = new float[]{};
	        }
			thread = new CalculationThread();
			thread.start();
			
			Intent intent = new Intent(this, PreviewActivity.class);
			startActivity(intent);
			
			
		/*}else if (item.getItemId() == R.id.settings_menu_item){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);*/
		}else if (item.getItemId() == R.id.delete_menu_item){
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
		}
		return true;
	}
	
	/** 
	 * {@inheritDoc} 
	 * */
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_gallery, menu);
		return true;
	}
	
	/**
	 * This method deletes all selected items.
	 */
	private void deleteItems(){
		ArrayList<ImageStructure> selectedf = getSelectedFiles();
		DatabaseHandler db = new DatabaseHandler(this);
		for(int i = 0; i < selectedf.size(); i++){
			String path = selectedf.get(i).path;
			File f = new File(path);
			if (f.delete()){
				for(GridImage gi : images){
					if (gi.getImagePath().equals(path)){
						images.remove(gi);
						db.deleteImage(new ImageDBRecord(gi.getImagePath()));
				    	break;
					}
				}
				adapter.notifyDataSetChanged();
			}
		}
		db.close();
	}
	
	/**
	 * This method grabs all selected from the of the GridView items and returns an ArrayList 
	 * of the ImageStructure objects representing the selected images.
	 * 
	 * @return Returns an ArrayList of ImageStructure 
	 */
	private ArrayList<ImageStructure> getSelectedFiles(){
		ArrayList<ImageStructure> selectedF = new ArrayList<ImageStructure>();
		
		for (int i = 0; i < images.size(); i++){
			GridImage gi = images.get(i);
			if (gi.getState() == 1){
				ImageStructure str = new ImageStructure();
				str.path = gi.getImagePath();
				selectedF.add(str);
			}
		}
		return selectedF;
	}
	
	
	private static float[] vertices;
	private static float[] colors;
	private static boolean new3DDataAvailable;
	
	private static Mat imgForVisualization;
	private static boolean new2DDataAvailable;		
		
	/**
	 * This method updates the data for 3D visualization and notifies the activity about the update.
	 * @param v an array of vertices, each three items represents one vertex as the coordinates x, y and z, 
	 * each nine items one triangle.
	 * @param c an array of colors, one color is assigned to one vertex in the v array. 
	 * Each four items represents one color with the RGBA values.
	 */
	public static synchronized void update3DData(float[] v, float[] c){
		vertices = v;
		colors = c;
		new3DDataAvailable = true;
    }
	
	/**
	 * This synchronized method returns an array of vertices ready for 3D visualization.
	 * 
	 * @return Returns an array of floats, each three items of the array represent one vertex for 3D reconstruction.
	 */
	public static synchronized float[] getVerticesFor3DVisualization(){
		new3DDataAvailable = false;
		return vertices;
	}
	
	/**
	 * This synchronized method returns an array of colors ready for 3D visualization.
	 * 
	 * @return Returns an array of colors for 3D reconstruction, each four items of the array represent one RGBA color.
	 */
	public static synchronized float[] getColorsFor3DVisualization(){
		new3DDataAvailable = false;
		return colors;
	}
	
	/**
	 * This synchronized method returns true if there are new data available for 3D visualization update or false if are not. 
	 * 
	 * @return Returns true (if there are new vertices and colors to 3D visualization) 
	 * or false (if the arrays of vertices and colors have not changed).
	 */
	public static synchronized boolean areNew3DDataAvailable(){
		return new3DDataAvailable;
	}
	
	/**
	 * This method sets the Mat m as the image for visualization and notifies the activity about new available data.
	 *  
	 * @param m Mat for 2D visualization. The type of the Mat needs be the CvType.CV_8UC3 type.
	 */
	public static synchronized void update2DData(Mat m){
		imgForVisualization = m;
		new2DDataAvailable = true;
    }
	

	/**
	 * This synchronized method returns an image as a Mat that should be visualized.
	 * Type of the matrix is CvType.CV_8UC3.
	 * 
	 * @return Returns Mat of CvType.CV_8UC3 type.
	 */
	public static synchronized Mat getImgForVisualization(){
		new2DDataAvailable = false;
		return imgForVisualization;
	}
	
	

	/**
	 * This synchronized method returns true if there is new image for visualization or false if is not.
	 * 
	 * @return Returns true (if there is new Mat for 2D visualization) 
	 * or false (if Mat have not changed).
	 */
	public static synchronized boolean areNew2DDataAvailable(){
		return new2DDataAvailable;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPause()
    {
		inForeground = false;
        super.onPause();
    }
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation also checks if there is a running CalculationThread in the background.
	 *  
	 */
	@Override
	public void onResume()
    {
		if (thread != null){
        	thread.interrupt();
			vertices = new float[]{};
			colors = new float[]{};
        }
		inForeground = true;
        super.onResume();
    }
	
	/**
	 * The CalculationThread class extending Thread is a class representing a thread running in the background
	 * and handling the calculation of the image processing.
	 * It initiates the Processing class and starts the computation.
	 * 
	 * @see com.vasova.bachelorproject.Processing
	 * 
	 * @author viktorievasova
	 * 
	 */
	class CalculationThread extends Thread{
		@Override
	    public void run() {
			Processing processing = new Processing();
			processing.setData(imagesForProcessing);
			processing.startProcessing(outputMode);
	    }
	}
}
