package com.vasova.bachelorproject;

/**
 * This class represents a record of an images saved in the table in the SQLite database.
 * @author viktorievasova
 *
 */
public class ImageDBRecord {
	private String path;
	/**
	 * Creates new record of an image.
	 * @param p the absolute path to the image file. 
	 */
	public ImageDBRecord(String p) {
		this.path = p;
	}
	
	/**
	 * Returns the absolute path to the image file.
	 * @return the absolute path to the image file.
	 */
	public String getPath(){
		return this.path;
	}
	
	/**
	 * Sets the absolute path to the image file.
	 * @param p the absolute path to the image file.
	 */
	public void setPath(String p){
		this.path = p;
	}
	
}
