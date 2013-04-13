package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.calib3d.StereoSGBM;
import android.opengl.GLSurfaceView;

import android.os.Bundle;
import android.os.Environment;
import android.app.ListActivity;
import android.content.Intent;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
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
				return true;
			}

			Registration r = new Registration();
			Mat preview = Highgui.imread(MainActivity.selectedFiles.get(0));
			/*ArrayList<int[]> overlaps = r.register(MainActivity.selectedFiles);
			System.out.println(MainActivity.selectedFiles.get(0));
			System.out.println("overlaps: " );
			
			for(int i = 0; i < overlaps.size(); i++){
				int[] o = overlaps.get(i);
				System.out.println("overlap: [" + o[0] +", "+ o[1] +"; " + o[2] +", "+ o[3] +"] over [" + o[4] +", "+ o[5] +"; " + o[6] +", "+ o[7] +"]");
				if (i < MainActivity.selectedFiles.size() - 1){
					double[] data = {255.0, 0.0, 0.0};
					for (int x = o[2]-1; x < o[0]; x++){
						preview.put(o[1], x, data);
						preview.put(o[3], x, data);
					}
					for (int y = o[3]-1; y < o[1]; y++){
						preview.put(y, o[0], data);
						preview.put(y, o[2], data);
					}
				}
			}
			*/
			GLSurfaceView view = new GLSurfaceView(this);
			
			Mat M1 = Highgui.imread(MainActivity.selectedFiles.get(0)); 
			Mat M2 = Highgui.imread(MainActivity.selectedFiles.get(1));
			Mat Mdisp = new Mat();
			StereoSGBM s = new StereoSGBM(1, 16*2, 3);
			s.compute(M1, M2, Mdisp);
    		
			String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + 
								"/Gallery/disparity_map.jpg";
			
			if(Highgui.imwrite(filename, Mdisp)){
				System.out.println("imwriting done");
			}
			/*
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
            builder1.setMessage("preview");
            builder1.setCancelable(true);
            builder1.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            
            AlertDialog alert = builder1.create();
            alert.show();
			*/
		}
		return true;

	}
	
	@Override
	public void onPause()
    {
		inForeground = false;
        System.out.println("gallery paused");
        super.onPause();
    }
	
	@Override
	public void onResume()
    {
		inForeground = true;
        System.out.println("gallery paused");
        super.onPause();
    }
	
	@Override
	public void onStop(){
		System.out.println("---------STOPPING GALLERY ACTIVITY----------------");
		super.onStop();
	}

}
