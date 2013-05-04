package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.GridView;

public class Adapter extends BaseAdapter{
	private Context context;
	private ArrayList<String> data;
	
	public Adapter(Context c){
		context = c;
		data = MainActivity.galleryList;
	}

	@Override
	public int getCount() {
        return data.size();
    }

	@Override
    public Object getItem(int position) {
        return null;
    }

	@Override
    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {  // if it's not recycled, initialize some attributes
            imageView = new ImageView(context);
            imageView.setLayoutParams(new GridView.LayoutParams(150, 120));
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);//CENTER_CROP);
            imageView.setPadding(5, 1, 5, 1);
            if(MainActivity.selectedFiles.contains(MainActivity.galleryList.get(position))){
            	imageView.setBackgroundColor(Color.BLACK);
            }else{
            	imageView.setBackgroundColor(Color.WHITE);
            }
        } else {
            imageView = (ImageView) convertView;
        }

        //imageView.setImageResource(mThumbIds[position]);
        File imgFile = new File(data.get(position));
    	Bitmap a_bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    	imageView.setImageBitmap(a_bitmap);
        return imageView;
    }
    
    public void remove(String path){
    	data.remove(path);
    }
}
