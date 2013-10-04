package com.vasova.bachelorproject;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * A class extending SQLiteOpenHelper.
 * It represents a SQLite table to store information about images taken by camera in the MainActivity of this application.
 * It stores the absolute paths of the files.
 * The path is unique for each image.
 * @author viktorievasova
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "imagesManager";

	// table name
	private static final String TABLE_IMAGES = "images";

	// column name for path
	private static final String KEY_PATH = "path";
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_IMAGES_TABLE = "CREATE TABLE " + TABLE_IMAGES + "(" + KEY_PATH + " TEXT"  + ")";
		db.execSQL(CREATE_IMAGES_TABLE);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_IMAGES);
		// Create tables again
		onCreate(db);
	}

	/**
	 * Adds an image record to the database.
	 * @param imageRecord the record of the image.
	 */
	public void addImage(ImageDBRecord imageRecord) {
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_PATH, imageRecord.getPath());
		// Inserting Row
		db.insert(TABLE_IMAGES, null, values);
		// Closing database connection
		db.close();
	}
	
	/**
	 * Getting all image records stored in the database. 
	 * @return List with all records in the database.
	 */
	public List<ImageDBRecord> getAllImages() {
		List<ImageDBRecord> imageList = new ArrayList<ImageDBRecord>();
		// query to select All
		String selectQuery = "SELECT  * FROM " + TABLE_IMAGES;
		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				ImageDBRecord imgRecord = new ImageDBRecord(cursor.getString(0));
				imageList.add(imgRecord);
			} while (cursor.moveToNext());
		}
		return imageList;
	}

	/**
	 * Updates an information about a single image.
	 * @param imgRecord the ImageDBRecord to update. 
	 * @return the number of rows that were affected by this update. 
	 */
	public int updateImages(ImageDBRecord imgRecord) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_PATH, imgRecord.getPath());
		// updating row
		return db.update(TABLE_IMAGES, values, KEY_PATH + " = ?",
				new String[] { String.valueOf(imgRecord.getPath()) });
	}

	/**
	 * Deletes an information about a single image.
	 * @param imgRecord the image record to update 
	 */
	public void deleteImage(ImageDBRecord imgRecord) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_IMAGES, KEY_PATH + " = ?", new String[] { String.valueOf(imgRecord.getPath()) });
		Log.d("deleteImage()", "deleting image");
		db.close();
	}

	/**
	 * Returns the number of rows in the table.
	 * @param imgRecord the number of rows in the table. 
	 */
	public int getImagesCount() {
		String countQuery = "SELECT  * FROM " + TABLE_IMAGES;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		int count = cursor.getCount();
		cursor.close();
		return count;
	}

}
