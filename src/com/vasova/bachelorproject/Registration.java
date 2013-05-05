package com.vasova.bachelorproject;

import java.util.ArrayList;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.ml.Ml;

import android.graphics.Canvas;
import android.graphics.Color;

public class Registration {
	
	public static String mi_string = "mutual information";
	public static String soad_string = "sum_of_absolute_differences";
	
	private boolean sum_of_absolute_differences;
	private boolean mutual_information;
	
	private ArrayList<MatOfKeyPoint> keyPoints;
	private MatOfDMatch[] matches;
	
	private ArrayList<Mat> imagesWithKeyPoints;
	private ArrayList<Mat> descriptors;
	private ArrayList<Mat> originalImages;
	
	public Registration(){
		//sum_of_absolute_differences is true by default
		sum_of_absolute_differences = true;		
	}
	
	public void setDataSet(ArrayList<Mat> images){
		this.originalImages = images;
		register();
	}
	
	public void register(){
		double[] overlap = getOverlap(originalImages.get(0), originalImages.get(1));
		System.out.println("overlap: ["+overlap[0]+" ;"+overlap[1]+" ;"+overlap[2]+" ;"+overlap[3]+" ;"+overlap[4]+" ;"+overlap[5]+"]");
		/*findKeyPoints();
		System.out.println("keypoints found");
		matchKeyPoints();
		System.out.println("keypoints matched");*/
	}
	
	private void findKeyPoints(){
		FeatureDetector surf = FeatureDetector.create(FeatureDetector.ORB);
		DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.ORB);
		
		keyPoints = new ArrayList<MatOfKeyPoint>();
		descriptors = new ArrayList<Mat>();
		
		//surf.detect(originalImages, keyPoints);
		//descriptor.compute(originalImages, keyPoints, descriptors);
		for(int i = 0; i < originalImages.size(); i++){
			MatOfKeyPoint cKeypoints = new MatOfKeyPoint();
			surf.detect(originalImages.get(i), cKeypoints);
			Mat cDescriptors = new Mat();
			descriptor.compute(originalImages.get(i), cKeypoints, cDescriptors);
			keyPoints.add(cKeypoints);
			descriptors.add(cDescriptors);
			Mat m = new Mat();
			Features2d.drawKeypoints(originalImages.get(i), cKeypoints, m);
			//imagesWithKeyPoints.add(m);
			//Highgui.imwrite("mnt/sdcard/Pictures/Gallery/keypoints"+ i +".jpg", m);
		}
	}
	
	private void matchKeyPoints(){
		DescriptorMatcher //matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		int n = originalImages.size();
		int numOfPairs = (n*(n-1))/2;
		int index = 0;
		
		
		matches = new MatOfDMatch[numOfPairs];
		for(int i = 0; i < originalImages.size(); i++){
			for (int j = i+1; j < originalImages.size(); j++){
				Mat descriptors1 = descriptors.get(i);
				Mat descriptors2 = descriptors.get(j);
				MatOfDMatch current_matches = new MatOfDMatch();
				matcher.match(descriptors2, descriptors1, current_matches);
				
				current_matches = filterMatches(current_matches, keyPoints.get(j), keyPoints.get(i));
				
				matches[index++] = current_matches;
				
				Mat drawM = new Mat();
				Features2d.drawMatches(originalImages.get(j), keyPoints.get(j), originalImages.get(i), keyPoints.get(i), current_matches, drawM);
				Highgui.imwrite("mnt/sdcard/Pictures/Gallery/matches.jpg", drawM);			
				
			}
		}
	
	}
	
	private MatOfDMatch filterMatches(MatOfDMatch matches, MatOfKeyPoint keypoints1, MatOfKeyPoint keypoints2){
		//matches - for each descriptor from image descriptors2 was found the nearest descriptor in descriptor1 using Euclidean metric
		
		DMatch[] dm = matches.toArray();
		
		ArrayList<Point> lpoints1 = new ArrayList<Point>(dm.length);
		ArrayList<Point> lpoints2 = new ArrayList<Point>(dm.length);
		
		KeyPoint[] kpoints1 = keypoints1.toArray();
		KeyPoint[] kpoints2 = keypoints2.toArray();
		
		for (int i = 0; i < dm.length; i++){
			DMatch dmatch = dm[i];
			lpoints1.add(kpoints1[dmatch.trainIdx].pt);
			lpoints2.add(kpoints2[dmatch.trainIdx].pt);
		}
		
		MatOfPoint2f points1 = new MatOfPoint2f(lpoints1.toArray(new Point[0]));
		MatOfPoint2f points2 = new MatOfPoint2f(lpoints2.toArray(new Point[0]));
		
		Mat m = Calib3d.findFundamentalMat(points1, points2, Calib3d.FM_RANSAC, 3, 0.99);
		
		return matches;
		
	}
	
	public ArrayList<MatOfKeyPoint> getKeyPoints(){
		return keyPoints;
	}
	
	public boolean isSetSumOfAbsoluteDiff(){
		return this.sum_of_absolute_differences;
	}
	
	public boolean isSetMutualInformation(){
		return this.mutual_information;
	}
	
	public void setRegistrationParametr(String s){
		if (s.equals(mi_string)){
			mutual_information = true;
			sum_of_absolute_differences = false;
		}else if (s.equals(soad_string)){
			sum_of_absolute_differences = true;
			mutual_information = false;
		}
	}
	
	
	private double[] getOverlap(Mat mat1, Mat mat2){
		int width = mat1.width();
		int height = mat1.height();

		//an array representing the overlap:[bottomX of img1, bottomY of img1, topX of img2, topY of img2, width, height]
		double[] overlap = new double[6];
		
		double diff = 1000d;
		Mat scaledImage1 = new Mat();
		Mat scaledImage2 = new Mat();
		
		double percentage = 30d;
		double newHeight = (double)height/100d*percentage;
		double newWidth = (double)width/100d*percentage;
		
		Imgproc.resize(mat1, scaledImage1, new Size(newWidth, newHeight));
		Imgproc.resize(mat2, scaledImage2, new Size(newWidth, newHeight));
		Highgui.imwrite("mnt/sdcard/Pictures/Gallery/scaled1.jpg", scaledImage1);
		Highgui.imwrite("mnt/sdcard/Pictures/Gallery/scaled2.jpg", scaledImage2);
		
		//coords for scaledImage1
		int currentBottomPosition1x = 1;
		int currentBottomPosition1y = 1;
		
		//coords for scaledImage2
		int currentTopPosition2x = scaledImage2.width()-1;
		int currentTopPosition2y = scaledImage2.height()-1;
		
		int windowWidth = 1;
		int windowHeight = 1;
		
		for(int h = 0; h < ((int)newHeight*2-1); h++){
			for (int w = 0; w < ((int)newWidth*2-1); w++){
				//get the area in mat1
				Mat subMat1 = scaledImage1.submat(new Range(currentBottomPosition1y - windowHeight, currentBottomPosition1y), 
												  new Range(currentBottomPosition1x - windowWidth, currentBottomPosition1x));
				//get the area in mat2
				Mat subMat2 = scaledImage2.submat(new Range(currentTopPosition2y, currentTopPosition2y + windowHeight), 
												  new Range(currentTopPosition2x, currentTopPosition2x + windowWidth));
				
				//calculate the difference
				Mat matrixDifference = new Mat();
				Core.absdiff(subMat1, subMat2, matrixDifference);
				matrixDifference = matrixDifference.mul(matrixDifference);
				
				double current_difference = Core.sumElems(matrixDifference).val[0];
				//System.out.println(current_difference);
				double windowSize = windowHeight*windowWidth;
				current_difference = current_difference/(windowSize);
				
				/*
				System.out.println("newWidth, newHeight: " + newWidth + "; "+ newHeight);
				System.out.println("currently at [w, h]: [" +w+"; " + h + "]");
				System.out.println("currentBottomPosition1x & currentBottomPosition1y: [" + currentBottomPosition1x + "; " + currentBottomPosition1y + "]   "+
						"  currentTopPosition2x & currentTopPosition2y: [" + currentTopPosition2x + "; " + currentTopPosition2y + "]   " +
						"  windowWidth & windowHeight: [" + windowWidth + "; " + windowHeight + "] ");
				System.out.println("current_difference: "+current_difference);		
				*/
				
				//if it is less than the min achieved value - keep it!
				if (current_difference < diff){
					//I found better solution
					diff = current_difference;
					overlap[0] = currentBottomPosition1x;
					overlap[1] = currentBottomPosition1y;
					overlap[2] = currentTopPosition2x;
					overlap[3] = currentTopPosition2y;
					overlap[4] = windowWidth;
					overlap[5] = windowHeight;
				}
				if (w < (int)newWidth-1){
					currentBottomPosition1x++;
					currentTopPosition2x--;
					windowWidth++;
				}else{
					windowWidth--;
				}
			}
			
			if (h < (int)newHeight-1){
				currentBottomPosition1y++;
				currentTopPosition2y--;
				windowHeight++;
			}else{
				windowHeight--;
			}
			currentBottomPosition1x = 1;
			currentTopPosition2x = (int)newWidth-1;
			windowWidth = 1;
		}

		return overlap;
				
		
	}
}