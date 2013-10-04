package com.vasova.bachelorproject;

import android.app.Application;

/**
 * BachalorProjectApplication extends Application.
 * It holds the SQLite database so all of the activities of this application can share it.
 * 
 * @author viktorievasova
 *
 */
public class BachalorProjectApplication extends Application {
	/**
	 * SQLite DatabaseHandler shared with all activities of this application.
	 */
	public DatabaseHandler db;
	
	/**
	 * An initialization of the SQLite databse.
	 */
	@Override
	public void onCreate(){
		db = new DatabaseHandler(this);
	}
}
