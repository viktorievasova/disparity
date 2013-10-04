package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * This class extends the BaseAdapter class implemented to use the GridView for image browsing.
 * 
 * @author viktorievasova
 *
 */

public class Adapter extends BaseAdapter  {

    private LayoutInflater mInflater;
    ArrayList<GridImage> list_of_files;
    
    public Adapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    /**
     * Method to set the data to the Adapter.  
     * @param list an ArrayList of GridImage items.
     */
    public void setListOfFiles(ArrayList<GridImage> list){
    	this.list_of_files = list;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridImage object = list_of_files.get(position);
        ViewHolder holder;
          if (convertView == null) {
            convertView = mInflater.inflate(R.layout.gallery_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            holder.image.setPadding(2, 2, 2, 2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        File imgFile = new File(object.imgPath);
    	Bitmap a_bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    	if (a_bitmap != null){
    		int max = Math.max(a_bitmap.getWidth(), a_bitmap.getHeight());
    		Bitmap resizedBitmap;
    		if (max == a_bitmap.getWidth()){
    			int width = 140;
    			int height = 105;
    			resizedBitmap = Bitmap.createScaledBitmap(a_bitmap, width, height, true);
    		}else{
    			int height = 100;
    			int width = 80;
    			resizedBitmap = Bitmap.createScaledBitmap(a_bitmap, width, height, true);
    		}
    		holder.image.setImageBitmap(resizedBitmap);
        }
        if (object.getState() == 1) {
        	holder.image.setBackgroundResource(R.drawable.background_img_gallery_pressed);
        } else {
        	holder.image.setBackgroundResource(R.drawable.background_img_gallery);
        }
        return convertView;
    }

    /**
     * returns the number of items in the Adapter.
     */
    @Override
    public int getCount() {
        return list_of_files.size();
    }

    /**
     * returns an object on the specified position.
     */
    @Override
    public Object getItem(int position) {
        return position;
    }

    /**
     * returns the id of the objec on the specified position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    private static class ViewHolder {
        ImageView image;
    }

    /**
     * A class defining an image object to be viewed in the Adapter.
     * It stores the path of the image file and a state if the image is selected or not.
     */
    public static class GridImage {

        private String imgPath;
        private int state;

        /**
         * The constructor of this class.
         * 
         * @param path the absolute path of the image file.
         * @param state a state of the image. It is equal to 0 when the GridImage is not selected, 1 when selected.
         */
        public GridImage(String path, int state) {
            super();
            this.imgPath = path;
            this.state = state;
        }

        /**
         * Returns the absolute path of the image file.
         * @return the absolute path of the image.
         */
        public String getImagePath() {
            return imgPath;
        }

        /**
         * Sets the absolute path of the image file.
         * @param path	the absolute path of the image.
         */
        public void setImagePath(String path) {
            this.imgPath = path;
        }

        /**
         * Returns 0 if the GridImage is not selected, 1 when it is selected.
         */
        public int getState() {
            return state;
        }

        /**
         * Sets a state of the GridImage.
         * @param state state of the GridImage. Set to 0 when it is not selected, 1 when selected.
         */
        public void setState(int state) {
            this.state = state;
        }   
    }
}

