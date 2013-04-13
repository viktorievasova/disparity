package com.vasova.bachelorproject;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;


public class Adapter extends ArrayAdapter<String>{

	private final Context context;
	private final ArrayList<String> data;
 
	//contructor is defined by Context and ArrayList of Strings
	public Adapter(Context context, ArrayList<String> values) {
		super(context, R.layout.activity_gallery, values);
		this.context = context;
		this.data = values;
	}
 
	/*
	 * (non-Javadoc)
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View, android.view.ViewGroup)
	 * Overrided method gets a View that displays the data at the specified position in the data set.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.list_row, parent, false);
		
		//data for CheckedTextView and ImageView are set:
		
		CheckedTextView textView = (CheckedTextView) view.findViewById(R.id.path_field);
		textView.setOnClickListener(new View.OnClickListener() {
	        public void onClick(View v){
	        	CheckedTextView txtView = (CheckedTextView)v;
	            if (txtView.isChecked()){
	            	//we have to uncheck row --> remove filepath from the list of checked files
	            	MainActivity.selectedFiles.remove((String)txtView.getText());
	            }else{
	            	//otherwise add filepath of selected file to the list
	            	MainActivity.selectedFiles.add((String)txtView.getText());
	            }
	            //txtView.toggle();
	        	txtView.setChecked(!txtView.isChecked());
	        }
	    });
		String s = data.get(position);
		textView.setText(s);
		if (MainActivity.selectedFiles.contains(s)){
			textView.setChecked(true);
		}
 
		ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
		File imgFile = new File(data.get(position));
    	Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);

        return view;
	}


	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}
	
}
