package com.vasova.bachelorproject;

import java.util.ArrayList;
import java.util.List;

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

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
	
	private ArrayList<double[]> overlaps;
	private double[] areaScore;
	private Mat imageWithThemostInformation;
	
	public Registration(){
		//sum_of_absolute_differences the default method of registration
		sum_of_absolute_differences = true;
		mutual_information = false;
	}
	
	public void setImages(ArrayList<Mat> images){
		this.originalImages = images;
		areaScore = new double[images.size()];
		register();
		
		int indexOfMaxScore = -1;
		double score = 0;
		for (int i = 0; i < areaScore.length; i++){
			if (areaScore[i] > score){
				indexOfMaxScore = i;
			}
		}
		imageWithThemostInformation = this.originalImages.get(indexOfMaxScore);
	}
	
	private void register(){
		getOverlaps();		
		findKeyPoints();
		matchKeyPoints();
	}
	
	private void createDataSet(){
		for(int i = 0; i < originalImages.size(); i++){
			for (int j = i+1; j < originalImages.size(); j++){
				//for each pair in originalImages create data set: {index of img1, index of img2, matches between img1&img2, overlap}
				
			}
		}
		
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
		
		//Mat m = Calib3d.findFundamentalMat(points1, points2, Calib3d.FM_RANSAC, 3, 0.99);
		
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
	
	private void getOverlaps(){
		Mat img1;
		Mat img2;
		for(int i = 0; i < originalImages.size(); i++){
			img1 = originalImages.get(i);
			for (int j = i+1; j < originalImages.size(); j++){
				img2 = originalImages.get(j);
				double[] overlap = new double[6];
				if (sum_of_absolute_differences){
					overlap = getOverlapSOAD(img1, img2);
				}else if(mutual_information){
					overlap = getOverlapMI(img1, img2);
				}
				overlaps.add(overlap);
				double score = overlap[4] * overlap[5];
				areaScore[i] += score;
				areaScore[j] += score;
			}
		}
	
	}
	
	private double[] getOverlapMI(Mat mat1, Mat mat2){
		int width = mat1.width();
		int height = mat1.height();

		//an array representing the overlap:[bottomX of img1, bottomY of img1, topX of img2, topY of img2, width, height]
		double[] overlap = new double[6];
		
		double mutualInformation = 0d;
		Mat scaledImage1 = new Mat();
		Mat scaledImage2 = new Mat();
		
		double percentage = 30d;
		double newHeight = (double)height/100d*percentage;
		double newWidth = (double)width/100d*percentage;
		
		Imgproc.resize(mat1, scaledImage1, new Size(newWidth, newHeight));
		Imgproc.resize(mat2, scaledImage2, new Size(newWidth, newHeight));
		
		//coords for scaledImage1
		int currentBottomPosition1x = 1;
		int currentBottomPosition1y = 1;
		
		//coords for scaledImage2
		int currentTopPosition2x = scaledImage2.width()-1;
		int currentTopPosition2y = scaledImage2.height()-1;
		
		int windowWidth = 1;
		int windowHeight = 1;
		
		//Imgproc.cvtColor(mat1, mat1, Imgproc.COLOR_BGR2GRAY);
		//Imgproc.cvtColor(mat2, mat2, Imgproc.COLOR_BGR2GRAY);
		//Highgui.imwrite("mnt/sdcard/Pictures/Gallery/orig1.jpg", mat1);
		//Highgui.imwrite("mnt/sdcard/Pictures/Gallery/orig2.jpg", mat2);
		
		for(int h = 0; h < ((int)newHeight*2-1); h++){
			for (int w = 0; w < ((int)newWidth*2-1); w++){
				//get the area in mat1
				Mat subMat1 = scaledImage1.submat(new Range(currentBottomPosition1y - windowHeight, currentBottomPosition1y), 
												  new Range(currentBottomPosition1x - windowWidth, currentBottomPosition1x));
				//get the area in mat2
				Mat subMat2 = scaledImage2.submat(new Range(currentTopPosition2y, currentTopPosition2y + windowHeight), 
												  new Range(currentTopPosition2x, currentTopPosition2x + windowWidth));
				
				Mat subMat12 = new Mat();
				//calculate the entropy
				List<Mat> imageChannels1 = new ArrayList<Mat>();
				Core.split(subMat1, imageChannels1); 
				
                List<Mat> imageChannels2 = new ArrayList<Mat>();
                Core.split(subMat2, imageChannels2);
                
                List<Mat> imageChannels12 = new ArrayList<Mat>();
				Core.split(subMat12, imageChannels12);
                
                MatOfInt[] channels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
                MatOfInt histSize = new MatOfInt(255);
                MatOfFloat ranges = new MatOfFloat(0f, 256f);

                int windowSize = windowWidth * windowHeight;
                Mat histogram1r = new Mat();
                Mat histogram1g = new Mat();
                Mat histogram1b = new Mat();
                
                Mat histogram2r = new Mat();
                Mat histogram2g = new Mat();
                Mat histogram2b = new Mat();
                
                Mat histogram12r = new Mat();
                Mat histogram12g = new Mat();
                Mat histogram12b = new Mat();
                
                Imgproc.calcHist(imageChannels1, channels[0], new Mat(), histogram1r, histSize, ranges);
                Core.normalize(histogram1r, histogram1r, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels1, channels[1], new Mat(), histogram1g, histSize, ranges);
                Core.normalize(histogram1g, histogram1g, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels1, channels[2], new Mat(), histogram1b, histSize, ranges);
                Core.normalize(histogram1b, histogram1b, windowSize, 0, Core.NORM_INF);
                
                Imgproc.calcHist(imageChannels2, channels[0], new Mat(), histogram2r, histSize, ranges);
                Core.normalize(histogram2r, histogram2r, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels2, channels[1], new Mat(), histogram2g, histSize, ranges);
                Core.normalize(histogram2g, histogram2g, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels2, channels[2], new Mat(), histogram2b, histSize, ranges);
                Core.normalize(histogram2b, histogram2b, windowSize, 0, Core.NORM_INF);
                
                Imgproc.calcHist(imageChannels12, channels[0], new Mat(), histogram12r, histSize, ranges);
                Core.normalize(histogram12r, histogram12r, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels12, channels[1], new Mat(), histogram12g, histSize, ranges);
                Core.normalize(histogram12g, histogram12g, windowSize, 0, Core.NORM_INF);
                Imgproc.calcHist(imageChannels12, channels[2], new Mat(), histogram12b, histSize, ranges);
                Core.normalize(histogram12b, histogram12b, windowSize, 0, Core.NORM_INF);
                
                float entropy1 = computeShannonEntropy(histogram1r, histogram1g, histogram1b);
                float entropy2 = computeShannonEntropy(histogram2r, histogram2g, histogram2b);
                float entropy12 = computeShannonEntropy(histogram12r, histogram12g, histogram12b);
                
                double current_mutualInformation = entropy1 + entropy2 - entropy12;
                //we maximize mutual information function
                if (current_mutualInformation > mutualInformation){
                	mutualInformation = current_mutualInformation;
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

		//overlap = resizeCoords(overlap, (int)newWidth, (int)newHeight, mat1.width(), mat1.height());
		return overlap;
	}
   
	int histSize = 255;
	
	private float getHistogramBinValue(Mat hist, int binNum){
        return (float)hist.get(binNum,0)[0];
    }
	
	private float getFrequencyOfBin(Mat channel){
        float frequency = 0.0f;
        for( int i = 1; i < histSize; i++ )
        {
            float Hc = Math.abs(getHistogramBinValue(channel,i));
            frequency += Hc;
        }
        return frequency;
    }
	private float computeShannonEntropy(Mat r, Mat g, Mat b){
        float entropy = 0.0f;
        float frequency = getFrequencyOfBin(r);
        for( int i = 1; i < histSize; i++ ){
            float Hc = Math.abs(getHistogramBinValue(r,i));
            entropy += -(Hc/frequency) * Math.log10((Hc/frequency));
        }
        frequency = getFrequencyOfBin(g);
        for( int i = 1; i < histSize; i++ )
        {
            float Hc = Math.abs(getHistogramBinValue(g,i));
            entropy += -(Hc/frequency) * Math.log10((Hc/frequency));
        }
        frequency = getFrequencyOfBin(b);
        for( int i = 1; i < histSize; i++ )
        {
            float Hc = Math.abs(getHistogramBinValue(b,i));
            entropy += -(Hc/frequency) * Math.log10((Hc/frequency));
        }
        entropy = entropy;
        //cout << entropy <<endl;
        return entropy;

    }
	
    private double[] getOverlapSOAD(Mat mat1, Mat mat2){
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
		Highgui.imwrite("mnt/sdcard/Pictures/Gallery/orig1.jpg", mat1);
		Highgui.imwrite("mnt/sdcard/Pictures/Gallery/orig2.jpg", mat2);
		
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
				if ((current_difference < diff) && windowSize > (newHeight*newWidth/5)){
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

		overlap = resizeCoords(overlap, (int)newWidth, (int)newHeight, mat1.width(), mat1.height());
		return overlap;
	}


	private double[] resizeCoords(double[] oldCoords, int oldW, int oldH, int w, int h){
		//an array representing the overlap:[bottomX of img1, bottomY of img1, topX of img2, topY of img2, width, height]
		double[] coords = new double[6];
		
		coords[0] = (int)(oldCoords[0]/oldW*w);
		coords[1] = (int)(oldCoords[1]/oldH*h);
		coords[2] = (int)(oldCoords[2]/oldW*w);
		coords[3] = (int)(oldCoords[3]/oldH*h);
		
		coords[4] = (int)(oldCoords[4]/oldW*w);
		coords[5] = (int)(oldCoords[5]/oldH*h);
		
		return coords;
	}

}