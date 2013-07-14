package com.vasova.bachelorproject;

import java.util.ArrayList;

import org.opencv.core.Mat;

public class Processing {
	private ArrayList<String> imagesPaths;
	private Registration registration;
	
	public Processing(){
		registration = new Registration();
	}
	
	public void setData(ArrayList<String> paths){
		this.imagesPaths = paths;
	}
	
	public void startProcessing(){
		registration.setImages(this.imagesPaths);
	}
	
	public Mat getImgForVisualization(){
		return registration.getImgForVisualization();
	}
	
	public ArrayList<String> getTempResult(){
		return registration.getTempResult();
	}
	
	public boolean isSetSumOfAbsoluteDiff(){
		return registration.isSetSumOfAbsoluteDiff();
	}
	
	public boolean isSetMutualInformation(){
		return registration.isSetMutualInformation();
	}
	
	public void setRegistrationParametr(String p){
		registration.setRegistrationParametr(p);
	}
}
