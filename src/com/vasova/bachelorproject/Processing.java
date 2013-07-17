package com.vasova.bachelorproject;

import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.highgui.Highgui;

public class Processing {
	private ArrayList<ImageStructure> imageData;
	private Registration registration;
	
	public Processing(){
		registration = new Registration();
	}
	
	public void setData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
	}
	
	public void startProcessing(){
		registration.setImageData(this.imageData);
		Mat img = registration.getImgForVisualization();
		/*update image data set*/
		imageData = registration.getData();
		
		GalleryActivity.setImgForVisualization(img);
		GalleryActivity.setNewDataAvailable(true);
		
		extractKeyPoints();
		
		KeyPointMatcher matcher = new KeyPointMatcher();
		matcher.setData(imageData);
		matcher.match();

		GalleryActivity.setImgForVisualization(matcher.getImgForVis());
		GalleryActivity.setNewDataAvailable(true);
		
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
	
	private void extractKeyPoints(){
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.HARRIS);
		DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		ImageStructure imgStr;
		MatOfKeyPoint cKeypoints;
		Mat cImage;
		Mat cDescriptors;
		for(int i = 0; i < this.imageData.size(); i++){
			imgStr = this.imageData.get(i);
			cKeypoints = new MatOfKeyPoint();
			cImage = Highgui.imread(imgStr.path);
			
			detector.detect(cImage, cKeypoints);
			cDescriptors = new Mat();
			descriptor.compute(cImage, cKeypoints, cDescriptors);
			
			imgStr.keyPoints = cKeypoints;
			imgStr.descriptors = cDescriptors;
			
			//Mat m = new Mat();
			//Features2d.drawKeypoints(image, cKeypoints, m);
			//Highgui.imwrite("mnt/sdcard/Pictures/Gallery/keypoints"+ i +".jpg", m);
		}
	}

}
