package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import android.os.Bundle;
import android.os.Environment;
import android.app.Activity;
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
 * Activity for browsing sample images.
 * This activity is used to view thumbnails of the sample input images for this application 
 * and enables user to select individual items.   
 * 
 * @author viktorievasova
 *
 */
public class SampleDataGalleryActivity extends Activity {

	public static boolean inForeground = false;
	private static String TAG = "SampleDataGallery";
   		
	private static GridView gridView;
    private static Adapter adapter;
   
    private static ArrayList<ImageStructure> imagesForProcessing;
    
    private static CalculationThread thread;
    private static ArrayList<GridImage> list;
    
    private int outputMode = 3;
    private static int[] image_ids = new int[]{R.drawable.dsc_0377, R.drawable.dsc_0378, R.drawable.dsc_0391, R.drawable.dsc_0392};
    private static String[] image_filenames = new String[]{"/sample_dsc_0377.jpg", "/sample_dsc_0378.jpg", "/sample_dsc_0391.jpg", "/sample_dsc_0392.jpg"};
	
    
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_data_gallery);
        inForeground = true;
        if (thread != null){
        	thread.interrupt();
			vertices = new float[]{};
			colors = new float[]{};
        }
        adapter = new Adapter(this);
        
        list = fillTheList();
        adapter.setListOfFiles(list);
        gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View v, int position, long arg3) {
            	if (SampleDataGalleryActivity.list.get(position).getState() == 1){
            		SampleDataGalleryActivity.list.get(position).setState(0);
            	}else{
            		SampleDataGalleryActivity.list.get(position).setState(1);
            	}
                adapter.notifyDataSetChanged();
            }
        });
    }
	
	/**
	 * Fills an ArrayList with the GridImage objects representing the objects of sample input images.
	 * 
	 * @return Returns an ArrayList with the sample data images as objects for a GridView.
	 */
	private ArrayList<GridImage> fillTheList(){
		ArrayList<GridImage> list = new ArrayList<GridImage>();
        GridImage image;
        String directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/ComputerVision_SampleData";
        File f = new File(directory);
        if (!f.exists()){
			if(!f.mkdirs()){
				Log.d(TAG, "failed to create directory");
			}
		}
        
        for (int i = 0; i < image_ids.length; i++){
			int image_id = image_ids[i];
			String filePath = directory + image_filenames[i];
			File img_file = new File(filePath);
			if (img_file.exists()){
				image = new GridImage(filePath, 0);
		        list.add(image);
				continue;
			}
			try{
				Mat img = Utils.loadResource(this, image_id);
				Highgui.imwrite(filePath, img);
				image = new GridImage(filePath, 0);
		        list.add(image);				
			}catch (Exception e){
				Log.i(TAG, "EXCEPTION caused by Utils.loadResources " + e.getMessage());
			}
		}
        return list;
	}
	
	@Override 
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.view_3D_menu_item){
			imagesForProcessing = getSelectedFiles();
			if (imagesForProcessing.size() < 2){
				Toast.makeText(this, "At least two images must be selected.", Toast.LENGTH_SHORT).show();
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
			if (imagesForProcessing.size() < 2){
				Toast.makeText(this, "At least two images must be selected.", Toast.LENGTH_SHORT).show();
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
			
			
		}/*else if (item.getItemId() == R.id.settings_menu_item){
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
		}*/
		return true;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_sample_data_gallery, menu);
		return true;
	}
	
	
	/**
	 * This method grabs all selected images from the GridView and returns an ArrayList 
	 * of the ImageStructure objects representing the selected images.
	 * 
	 * @return Returns an ArrayList of ImageStructure 
	 */
	private ArrayList<ImageStructure> getSelectedFiles(){
		ArrayList<ImageStructure> selectedF = new ArrayList<ImageStructure>();
		for (int i = 0; i < list.size(); i++){
			GridImage gi = list.get(i);
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
	
	
	@Override
	public void onPause()
    {
		inForeground = false;
        super.onPause();
    }
	
	/**
	 * {@inheritDoc}
	 * 
	 * This implementation also checks if there is running a CalculationThread in the background.
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
