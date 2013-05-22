package com.vasova.bachelorproject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;

import org.opencv.calib3d.StereoSGBM;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
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
import org.opencv.imgproc.Imgproc;

import android.os.Environment;
import android.util.Log;

public class Registration {
	
	String registration_tag = "Registration";
	
	public static String mi_string = "mutual information";
	public static String soad_string = "sum_of_absolute_differences";
	
	private boolean sum_of_absolute_differences;
	private boolean mutual_information;
	
	private ArrayList<MatOfKeyPoint> keyPoints;
	
	private ArrayList<Mat> descriptors;
	private ArrayList<Mat> originalImages;
	
	private ArrayList<Mat> disparities;
	private ArrayList<MatOfDMatch> matches;
	
	
	private ArrayList<double[]> overlaps;
	private double[] areaScore;
	private Point[] upperEdges;
	
	private Mat imageWithThemostInformation;
	private Mat disparityMap;
	
	private ArrayList<ImageStructure> images;
	
	public Registration(){
		//sum_of_absolute_differences the default method of registration
		sum_of_absolute_differences = true;
		mutual_information = false;
	}
	
	public void setImages(ArrayList<Mat> images){
		this.originalImages = images;
		areaScore = new double[images.size()];
		upperEdges = new Point[images.size()];
		register();
		
		int indexOfMaxScore = -1;
		double score = 0;
		for (int i = 0; i < areaScore.length; i++){
			if (areaScore[i] >= score){
				indexOfMaxScore = i;
			}
		}
		imageWithThemostInformation = this.images.get(indexOfMaxScore).mat;
		System.out.println(indexOfMaxScore);
	}
	
	private void register(){
		System.out.println("registration started at " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) );
		images = new ArrayList<Registration.ImageStructure>();
		Mat image;
		for(int i = 0; i < originalImages.size(); i++){
			image = originalImages.get(i);
			ImageStructure imgStruct = new ImageStructure(image.clone());
			imgStruct.scales = new ArrayList<Mat>();
			imgStruct.scales.add(image.clone());
			for(int s = 0; s < 4; s++){
				Imgproc.pyrDown(image, image);
				imgStruct.scales.add(image.clone());
			}
			images.add(imgStruct);
		}
		getOverlaps();
		//findKeyPoints();
		//matchKeyPoints();
		//calculateDisparityMaps();
		System.out.println("registration ended at " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) );
		for (int i = 0; i < overlaps.size(); i++){
			double[] o = overlaps.get(i);
			System.out.println("[ "+ o[0] +", " + o[1] +", " + o[2] +", " + o[3] +", " + o[4] +", " + o[5] +" ]");
		}
		for (int i = 0; i < upperEdges.length; i++){
			Point edge = upperEdges[i];
			System.out.println( i + "->> [" + edge.x + ", " + edge.y + "]");
		}
	}
	
	public Mat getImgForVisualization(){
		return imageWithThemostInformation;
	}
	
	public Mat getDisparityMap(){
		return disparityMap;
	}
	
	private void calculateDisparityMaps(){
		int indexInOverlaps = 0;
		disparities = new ArrayList<Mat>();
		for(int i = 0; i < originalImages.size(); i++){
			Mat image1 = originalImages.get(i);
			for (int j = i+1; j < originalImages.size(); j++){
				Mat image2 = originalImages.get(j);
				Mat left = getLeftImage(image1, image2, overlaps.get(indexInOverlaps));
				Mat right;
				if (left.equals(image1)){
					right = image2;
				}else{
					right = image1;
				}
				Mat currentDisparityMap = new Mat();			
				
				int channels = 3;
				int preFilterCap = 63;
				
				int SADWindowSize = 3;
				
				int minDisparity = 0;
				int numOfDisparities = ((image1.width()/8) + 15) & -16;
				
				int P1 =  8*channels*SADWindowSize*SADWindowSize;
				int P2 = 32*channels*SADWindowSize*SADWindowSize;
				
				int disp12MaxDiff = 1;
				int uniquenessRatio = 10;
				int speckleWindowSize = 100;
				int speckleRange = 32;
				
				
				StereoSGBM s = new StereoSGBM(minDisparity, numOfDisparities, SADWindowSize, 
					       P1, P2, disp12MaxDiff, preFilterCap, uniquenessRatio,speckleWindowSize, speckleRange, false);
				s.compute(left, right, currentDisparityMap);
				disparities.add(currentDisparityMap);
				this.disparityMap = currentDisparityMap;
				Mat result = new Mat();
				String filename = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/Gallery/diparityMap.jpg";
				disparityMap.convertTo(result, CvType.CV_8U, 255/(numOfDisparities*16.0));
				Highgui.imwrite(filename, result);
				
				indexInOverlaps++;
			}
		}
		
	}
	
	private Mat getLeftImage(Mat img1, Mat img2, double[] overlap){
		Mat leftMat;
		double bottomXinImg1 = overlap[0];
		double widthOfOverlap = overlap[4];
		if (bottomXinImg1 - widthOfOverlap > 0){
			leftMat = img1;
		}else{
			leftMat = img2;
		}
		return leftMat;
	}
	
	private Mat getUpperImage(Mat img1, Mat img2, double[] overlap){
		Mat upperMat;
		double bottomYinImg1 = overlap[1];
		double heightOfOverlap = overlap[5];
		if (bottomYinImg1 - heightOfOverlap > 0){
			upperMat = img1;
		}else{
			upperMat = img2;
		}
		return upperMat;
	}
	
	private void findKeyPoints(){
		FeatureDetector surf = FeatureDetector.create(FeatureDetector.HARRIS);
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
			Highgui.imwrite("mnt/sdcard/Pictures/Gallery/keypoints"+ i +".jpg", m);
		}
	}
	
	private void matchKeyPoints(){
		DescriptorMatcher //matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE);
		matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
		//int n = originalImages.size();
		//int numOfPairs = (n*(n-1))/2;
		
		matches = new ArrayList<MatOfDMatch>();
		for(int i = 0; i < originalImages.size(); i++){
			for (int j = i+1; j < originalImages.size(); j++){
				Mat descriptors1 = descriptors.get(i);
				Mat descriptors2 = descriptors.get(j);
				MatOfDMatch current_matches = new MatOfDMatch();
				matcher.match(descriptors2, descriptors1, current_matches);
				
				//current_matches = filterMatches(current_matches, keyPoints.get(j), keyPoints.get(i));
				
				matches.add(current_matches);
				
				Mat drawM = new Mat();
				Features2d.drawMatches(originalImages.get(j), keyPoints.get(j), originalImages.get(i), keyPoints.get(i), current_matches, drawM);
				Highgui.imwrite("mnt/sdcard/Pictures/Gallery/matches"+i+j+".jpg", drawM);			
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
		
		//MatOfPoint2f points1 = new MatOfPoint2f(lpoints1.toArray(new Point[0]));
		//MatOfPoint2f points2 = new MatOfPoint2f(lpoints2.toArray(new Point[0]));
		
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
		ImageStructure img1;
		ImageStructure img2;
		overlaps = new ArrayList<double[]>();
		for(int i = 0; i < originalImages.size() - 1; i++){
			int j = i+1;
			
			img1 = images.get(i);
			img2 = images.get(j);
			
			
			double[] overlap = new double[6];
			if (this.sum_of_absolute_differences){
				overlap = getOverlapSOAD(img1, img2);
			}else if(this.mutual_information){
				overlap = getOverlapMI(img1.mat, img2.mat);
			}
			overlaps.add(overlap);
			
			if (i==0){
				upperEdges[i] = new Point(0,0);
				Mat left = getLeftImage(img1.mat, img2.mat, overlap);
				Mat upper = getUpperImage(img1.mat, img2.mat, overlap);
				if (left.equals(img2)){
					if (upper.equals(img2)){
						upperEdges[j] = new Point(0-(img2.width - overlap[4]), 0+(img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(0-(img2.width - overlap[4]), 0-(img2.height - overlap[5]));
					}
				}else{
					if (upper.equals(img2)){
						upperEdges[j] = new Point(0+(img2.width - overlap[4]), 0+(img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(0+(img2.width - overlap[4]), 0-(img2.height - overlap[5]));
					}
				}
			}else{
				Mat left = getLeftImage(img1.mat, img2.mat, overlap);
				Mat upper = getUpperImage(img1.mat, img2.mat, overlap);
				if (left.equals(img2)){
					if (upper.equals(img2)){
						upperEdges[j] = new Point(upperEdges[j-1].x - (img2.width - overlap[4]), upperEdges[j-1].y + (img2.height - overlap[5]) );
					}else{
						upperEdges[j] = new Point(upperEdges[j-1].x - (img2.width - overlap[4]), upperEdges[j-1].y - (img2.height - overlap[5]) );
					}
				}else{
					if (upper.equals(img2)){
						upperEdges[j] = new Point(upperEdges[j-1].x + (img2.width - overlap[4]), upperEdges[j-1].y + (img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(upperEdges[j-1].x + (img2.width - overlap[4]), upperEdges[j-1].y - (img2.height - overlap[5]));
					}
				}
			}
			double score = overlap[4] * overlap[5];
			areaScore[i] += score;
			areaScore[j] += score;
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
        //entropy = entropy;
        //cout << entropy <<endl;
        return entropy;

    }

	
	private double[] getOverlapSOAD(ImageStructure image1, ImageStructure image2){
		double[] overlap = new double[6];
		double[] previousOverlap = new double[0];
		double[] currentOverlap;
		int previousWidth = image1.scales.get(image1.scales.size() - 1).width();
		int previousHeight = image1.scales.get(image1.scales.size() - 1).height();
		int currentWidth;
		int currentHeight;
		for (int s = image1.scales.size() - 1; s >= 0; s--){
			currentOverlap = getOverlapSOADforScaledImages(image1.scales.get(s), image2.scales.get(s), previousOverlap);
			if (s != 0){
				currentWidth = image1.scales.get(s-1).width();
				currentHeight = image1.scales.get(s-1).height();
				previousOverlap = resizeCoords(currentOverlap, previousWidth, previousHeight, currentWidth, currentHeight);
				previousWidth = currentWidth;
				previousHeight = currentHeight;
			}
		}
		overlap = previousOverlap;
		//System.out.println("overlap: [" + overlap[0] + ", " + overlap[1] + ", " + overlap[2] + ", " + overlap[3] + ", " + overlap[4] + ", " + overlap[5] + "]");
		return overlap;
	}
	
    private double[] getOverlapSOADforScaledImages(Mat mat1, Mat mat2, double[] prevOverlap){
		int width = mat1.width();
		int height = mat1.height();

		//an array representing the overlap:[bottomX of img1, bottomY of img1, topX of img2, topY of img2, width, height]
		double[] overlap = new double[6];
		
		double diff = 1000d;
		Mat scaledImage1 = mat1;
		Mat scaledImage2 = mat2;
		
		int xWindowBoundsFrom; 
		int yWindowBoundsFrom;
		
		int xWindowBoundsTo; 
		int yWindowBoundsTo;
		
		int startWindowWidth;
		int startWindowHeight; 
		
		int startBottomPosition1x;
		int startBottomPosition1y;
		
		int startTopPosition2x;
		int startTopPosition2y;
		
		//coords for scaledImage1
		int currentBottomPosition1x;
		int currentBottomPosition1y;
		
		//coords for scaledImage2
		int currentTopPosition2x;
		int currentTopPosition2y;
		
		int windowWidth;
		int windowHeight;
	
		if (prevOverlap.length == 0){	//pokud zaciname (jeste neni k dispozici zadne vypocitane prekryti), prochazime vsechny mozne prekryvy
			xWindowBoundsFrom = 1; 
			yWindowBoundsFrom = 1;
			
			xWindowBoundsTo = width*2 - 1; 
			yWindowBoundsTo = height*2 - 1;
			
			startBottomPosition1x = xWindowBoundsFrom;
			startBottomPosition1y = yWindowBoundsFrom;
			
			startWindowWidth = 1;
			startWindowHeight = 1;
			
			startTopPosition2x = scaledImage2.width()-startWindowWidth;
			startTopPosition2y = scaledImage2.height()-startWindowHeight;
						
		}else {						//jinak vylepsujeme prekryti v rozmezi pixelRange pixelu
			int pixelRange = 5;
			Mat left = getLeftImage(scaledImage1, scaledImage2, prevOverlap);
			//Mat upper = getUpperImage(scaledImage1, scaledImage2, prevOverlap);
			
			if (left.equals(scaledImage2)){
				int minX = (int)prevOverlap[0] - pixelRange;
				int minY = (int)prevOverlap[1] - pixelRange;
				if (minX < 1){
					xWindowBoundsFrom = 1;
					startWindowWidth = 1;
					startBottomPosition1x = 1;
					startTopPosition2x = width - 1;
					xWindowBoundsTo = xWindowBoundsFrom + 2*pixelRange;
				}else{
					xWindowBoundsFrom = minX;
					startWindowWidth = minX;
					startBottomPosition1x = minX;
					startTopPosition2x = width - minX;
					xWindowBoundsTo = xWindowBoundsFrom + 2*pixelRange;
				}
				
				if (minY < 1){
					yWindowBoundsFrom = 1;
					startWindowHeight = 1;
					startBottomPosition1y = 1;
					startTopPosition2y = height - 1;
					yWindowBoundsTo = yWindowBoundsFrom + 2*pixelRange;
				}else{
					yWindowBoundsFrom = minY;
					startWindowHeight = minY;
					startBottomPosition1y = minY;
					startTopPosition2y = height - minY;
					yWindowBoundsTo = yWindowBoundsFrom + 2*pixelRange;
				}
			}else{
				int minX = (width - (int)prevOverlap[4]) - pixelRange;
				int minY = (height - (int)prevOverlap[5]) - pixelRange;
				
				if (minX < 0){
					int x  = (width - (int)prevOverlap[4]);
					startTopPosition2x = 0 + (pixelRange - x);
					startWindowWidth = width - (pixelRange - x);
					xWindowBoundsFrom = width - (pixelRange - x);
					startBottomPosition1x = width - (pixelRange - x);
					xWindowBoundsTo = xWindowBoundsFrom + 2*pixelRange;
				}else{
					startTopPosition2x = 0;
					startWindowWidth = (int)prevOverlap[4] + pixelRange;
					//if (startWindowWidth > width){
						//startWindowWidth = width;
					//}
					xWindowBoundsFrom = width;
					startBottomPosition1x = width;
					xWindowBoundsTo = xWindowBoundsFrom + 2*pixelRange;
				}
				
				if (minY < 0){
					int y  = (height - (int)prevOverlap[5]);
					startTopPosition2y = 0 + (pixelRange - y);
					startWindowHeight = height - (pixelRange - y);
					yWindowBoundsFrom = height - (pixelRange - y);
					startBottomPosition1y = height - (pixelRange - y);
					yWindowBoundsTo = yWindowBoundsFrom + 2*pixelRange;
				}else{
					startTopPosition2y = 0;
					startWindowHeight = (int)prevOverlap[5] + pixelRange;
					yWindowBoundsFrom = height;
					startBottomPosition1y = height;
					yWindowBoundsTo = yWindowBoundsFrom + 2*pixelRange;
				}
			}
		}
		
		currentBottomPosition1x = startBottomPosition1x;
		currentBottomPosition1y = startBottomPosition1y;
		
		currentTopPosition2x = startTopPosition2x;
		currentTopPosition2y = startTopPosition2y;
		
		windowWidth = startWindowWidth;
		windowHeight = startWindowHeight;
		
		for(int h = yWindowBoundsFrom-1; h < yWindowBoundsTo; h++){
			for (int w = xWindowBoundsFrom-1; w < xWindowBoundsTo; w++){
				//Log.i(registration_tag, " H & W: " + height +", " + width);
				//Log.i(registration_tag, "Range H: [" + (currentBottomPosition1y - windowHeight) +", " + currentBottomPosition1y + "]");
				//Log.i(registration_tag, "Range W: [" + (currentBottomPosition1x - windowWidth) +", " + currentBottomPosition1x + "]");
				
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
				if ((current_difference < diff) && windowSize > (width*height/4)){
					//I found better solution
					diff = current_difference;
					overlap[0] = currentBottomPosition1x;
					overlap[1] = currentBottomPosition1y;
					overlap[2] = currentTopPosition2x;
					overlap[3] = currentTopPosition2y;
					overlap[4] = windowWidth;
					overlap[5] = windowHeight;
				}
				if (w < (int)width-1){
					currentBottomPosition1x++;
					currentTopPosition2x--;
					windowWidth++;
				}else{
					windowWidth--;
				}
			}
			if (h < (int)height-1){
				currentBottomPosition1y++;
				currentTopPosition2y--;
				windowHeight++;
			}else{
				windowHeight--;
			}
			currentBottomPosition1x = startBottomPosition1x;
			currentTopPosition2x = startTopPosition2x;
			windowWidth = startWindowWidth;
		}
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
	
	private class ImageStructure{
		ArrayList<Mat> scales;
		Mat mat;
		int width;
		int height;
		
		public ImageStructure(Mat m){
			this.mat = m;
			this.width = m.width();
			this.height = m.height();
		}
	}

}