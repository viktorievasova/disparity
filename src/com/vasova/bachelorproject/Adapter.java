package com.vasova.bachelorproject;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;



public class Adapter extends BaseAdapter  {

    private LayoutInflater mInflater;

    public Adapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GridImage object = MainActivity.imagesDB.get(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.galley_item, null);
            holder = new ViewHolder();
            holder.image = (ImageView) convertView.findViewById(R.id.image);
            holder.image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);//CENTER_CROP);
            holder.image.setPadding(2, 2, 2, 2);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File imgFile = new File(object.imgPath);
    	Bitmap a_bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
    	holder.image.setImageBitmap(a_bitmap);
    	
        if (object.getState() == 1) {
            holder.image.setBackgroundColor(Color.BLACK);
        } else {
            holder.image.setBackgroundColor(Color.WHITE);
        }
        return convertView;
    }

    @Override
    public int getCount() {
        return MainActivity.imagesDB.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    private static class ViewHolder {
        ImageView image;
    }

    /*
     * state is 0 when image is not selected, 1 when selected
     */
    public static class GridImage {

        private String imgPath;
        private int state;

        public GridImage(String path, int state) {
            super();
            this.imgPath = path;
            this.state = state;
        }

        public String getImagePath() {
            return imgPath;
        }

        public void setImagePath(String path) {
            this.imgPath = path;
        }

        public int getState() {
            return state;
        }

        public void setState(int state) {
            this.state = state;
        }   
    }
}

