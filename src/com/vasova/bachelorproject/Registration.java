package com.vasova.bachelorproject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Range;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.util.Log;

public class Registration {
	
	String TAG = "Registration";
	
	public static String mi_string = "mutual information";
	public static String soad_string = "sum_of_absolute_differences";
	
	private boolean sum_of_absolute_differences;
	private boolean mutual_information;
	
	//private ArrayList<double[]> overlaps;
	private double[] areaScore;
	private Point[] upperEdges;
	
	private Mat imageWithTheLargestInformation;
	
	private ArrayList<ImageStructure> imageData;
	
	private ArrayList<String> tempResult = new ArrayList<String>();
	
	public ArrayList<String> getTempResult(){
		return tempResult;
	}
	
	public Registration(){
		//sum_of_absolute_differences the default method of registration
		sum_of_absolute_differences = true;
		mutual_information = false;
	}
	
	public void setImageData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
		areaScore = new double[imageData.size()];
		upperEdges = new Point[imageData.size()];
		register();
		
		int indexOfMaxScore = -1;
		double score = 0;
		for (int i = 0; i < areaScore.length; i++){
			if (areaScore[i] >= score){
				indexOfMaxScore = i;
			}
		}
		imageWithTheLargestInformation = Highgui.imread(this.imageData.get(indexOfMaxScore).path);
		System.out.println(indexOfMaxScore);
	}
	
	public ArrayList<ImageStructure> getData(){
		return this.imageData;
	}
	
	private ArrayList<Mat> readImages(ArrayList<ImageStructure> imgData){
		ArrayList<Mat> images = new ArrayList<Mat>();
		for (int i = 0; i < imgData.size(); i++){
			images.add(Highgui.imread(imgData.get(i).path));
		}
		return images;
	}
	
	private void register(){
		System.out.println("registration started at " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) );
		Mat image;
		ImageStructure imgStr;
		for(int i = 0; i < imageData.size(); i++){
			imgStr = imageData.get(i);
			image = Highgui.imread(imgStr.path);
			imgStr.width = image.width();
			imgStr.height = image.height();
			imgStr.scales = new ArrayList<Mat>();
			imgStr.scales.add(image.clone());
			for(int s = 0; s < 4; s++){
				Imgproc.pyrDown(image, image);
				imgStr.scales.add(image.clone());
			}
		}
		getOverlaps();
		
		System.out.println("registration ended at " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) );
		for (int i = 0; i < this.imageData.size() - 1; i++){
			double[] o = this.imageData.get(i).relativePositionWithNeighbour;
			System.out.println("[ "+ o[0] +", " + o[1] +", " + o[2] +", " + o[3] +", " + o[4] +", " + o[5] +" ]");
		}
		for (int i = 0; i < upperEdges.length; i++){
			Point edge = upperEdges[i];
			System.out.println( i + "->> [" + edge.x + ", " + edge.y + "]");
		}

	}
	
	public Mat getImgForVisualization(){
		//return imageWithTheLargestInformation;
		return Highgui.imread(tempResult.get(2));
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
		
		Mat mat1;
		Mat mat2;
		
		//overlaps = new ArrayList<double[]>();
		for(int i = 0; i < imageData.size() - 1; i++){
			int j = i+1;
			
			img1 = imageData.get(i);
			img2 = imageData.get(j);
			
			mat1 = Highgui.imread(img1.path);
			mat2 = Highgui.imread(img2.path);
			
			double[] overlap = new double[6];
			if (this.sum_of_absolute_differences){
				overlap = getOverlapSOAD(img1, img2);
				
			}else if(this.mutual_information){
				overlap = getOverlapMI(img1, img2);
			}
			overlap[0] = overlap[0] - 1;
			overlap[1] = overlap[1] - 1;
			
			//overlaps.add(overlap);
			img1.relativePositionWithNeighbour = overlap;
			
			if (i==0){
				upperEdges[i] = new Point(0,0);
				Mat left = getLeftImage(mat1, mat2, overlap);
				Mat upper = getUpperImage(mat1, mat2, overlap);
				if (left.equals(mat2)){
					if (upper.equals(mat2)){
						upperEdges[j] = new Point(0-(img2.width - overlap[4]), 0-(img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(0-(img2.width - overlap[4]), 0+(img2.height - overlap[5]));
					}
				}else{
					if (upper.equals(mat2)){
						upperEdges[j] = new Point(0+(img2.width - overlap[4]), 0-(img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(0+(img2.width - overlap[4]), 0+(img2.height - overlap[5]));
					}
				}
			}else{
				Mat left = getLeftImage(mat1, mat2, overlap);
				Mat upper = getUpperImage(mat1, mat2, overlap);
				if (left.equals(mat2)){
					if (upper.equals(mat2)){
						upperEdges[j] = new Point(upperEdges[j-1].x - (img2.width - overlap[4]), upperEdges[j-1].y - (img2.height - overlap[5]) );
					}else{
						upperEdges[j] = new Point(upperEdges[j-1].x - (img2.width - overlap[4]), upperEdges[j-1].y + (img2.height - overlap[5]) );
					}
				}else{
					if (upper.equals(mat2)){
						upperEdges[j] = new Point(upperEdges[j-1].x + (img2.width - overlap[4]), upperEdges[j-1].y - (img2.height - overlap[5]));
					}else{
						upperEdges[j] = new Point(upperEdges[j-1].x + (img2.width - overlap[4]), upperEdges[j-1].y + (img2.height - overlap[5]));
					}
				}
			}
			System.out.println("registration actually ended at " + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) );
			//----------------debugovani - zakresleni prekryti----------------
			//[bottomX of img1, bottomY of img1, topX of img2, topY of img2, width, height]
			Mat result1 = mat1.clone();
			double bX = overlap[0];
			double bY = overlap[1];
			double bX_w = overlap[0] - overlap[4];
			double bY_h = overlap[1] - overlap[5];
			Core.line(result1, new Point(bX, bY),  new Point(bX_w, bY), new Scalar(0d, 0d, 1d));
			Core.line(result1, new Point(bX, bY),  new Point(bX, bY_h), new Scalar(0d, 0d, 1d));
			Core.line(result1, new Point(bX_w, bY_h),  new Point(bX_w, bY), new Scalar(0d, 0d, 1d));
			Core.line(result1, new Point(bX_w, bY_h),  new Point(bX, bY_h), new Scalar(0d, 0d, 1d));
			
			Mat result2 = mat2.clone();
			double tX = overlap[2];
			double tY = overlap[3];
			double tX_w = overlap[2] + overlap[4];
			double tY_h = overlap[3] + overlap[5];
			Core.line(result2, new Point(tX, tY),  new Point(tX_w, tY), new Scalar(0d, 0d, 1d));
			Core.line(result2, new Point(tX, tY),  new Point(tX, tY_h), new Scalar(0d, 0d, 1d));
			Core.line(result2, new Point(tX_w, tY_h),  new Point(tX_w, tY), new Scalar(0d, 0d, 1d));
			Core.line(result2, new Point(tX_w, tY_h),  new Point(tX, tY_h), new Scalar(0d, 0d, 1d));
			
			
			Highgui.imwrite("mnt/sdcard/Pictures/Gallery/overlap1.jpg", result1);
			Highgui.imwrite("mnt/sdcard/Pictures/Gallery/overlap2.jpg", result2);
			
			tempResult.add("mnt/sdcard/Pictures/Gallery/overlap1.jpg");
			tempResult.add("mnt/sdcard/Pictures/Gallery/overlap2.jpg");
			
			int x2 = (int)overlap[2];
			int y2 = (int)overlap[3];
			
			Mat viewoverlap = mat1.clone();
			Mat m = mat2.clone();
			
			for (int h = (int)bY_h; h < bY-1; h++){
				for(int w = (int)bX_w; w < bX-1; w++){
					//Log.i("registration", "img1:[" +w+", "+h+"], img2:["+x2+", "+y2+"]");
					double [] color1 = viewoverlap.get(h, w);
					double [] color2 = m.get(y2, x2);
					double [] newColor = {((color1[0] + color2[0])/2), ((color1[1] + color2[1])/2), ((color1[2] + color2[2])/2)};
					viewoverlap.put(h, w, newColor);
					x2++;
				}
				
				y2++;
				x2 = (int)overlap[2];
			}
			Highgui.imwrite("mnt/sdcard/Pictures/Gallery/viewOverlap.jpg", viewoverlap);
			tempResult.add("mnt/sdcard/Pictures/Gallery/viewOverlap.jpg");
			
			//---------------- --------------------- ----------------
			
			double score = overlap[4] * overlap[5];
			areaScore[i] += score;
			areaScore[j] += score;
		}
	
	}
	
	private double[] getOverlapMI(ImageStructure image1, ImageStructure image2){
		//TODO
		return new double[] {0d, 0d, 0d, 0d, 0d, 0d};
	}
	private float computeShannonEntropy(Mat r, Mat g, Mat b){
        float entropy = 0.0f;
        //TODO
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
	
		int pixelRange = 5;	//velikost okoli na kterem se hleda vylepseni vysledku
		
		if (prevOverlap.length == 0){	//pokud zaciname (jeste neni k dispozici zadne vypocitane prekryti), prochazime vsechny mozne prekryvy
										//tj zaciname od 1 prekryvajiciho se pixlu v levem hornim rohu
			xWindowBoundsFrom = 1; 		//x-ova a y-ova souradnice pocatku prekryvani  
			yWindowBoundsFrom = 1;
			
			xWindowBoundsTo = width*2 - 1; //pocet kroku provedenych smerem po ose x a ose y
			yWindowBoundsTo = height*2 - 1;
			
			startBottomPosition1x = xWindowBoundsFrom;
			startBottomPosition1y = yWindowBoundsFrom;
			
			startWindowWidth = 1;
			startWindowHeight = 1;
			
			startTopPosition2x = scaledImage2.width()-startWindowWidth;
			startTopPosition2y = scaledImage2.height()-startWindowHeight;
						
		}else {						//jinak vylepsujeme prekryti v rozmezi pixelRange pixelu
			Mat left = getLeftImage(scaledImage1, scaledImage2, prevOverlap);
			Mat upper = getUpperImage(scaledImage1, scaledImage2, prevOverlap);
			
			if (left.equals(scaledImage2)){
				int shiftedX = (int)prevOverlap[0] - pixelRange;
				if (shiftedX < 1){
					xWindowBoundsFrom = 1;
					startWindowWidth = 1;
					startBottomPosition1x = 1;
					startTopPosition2x = width - 1;
				}else{
					xWindowBoundsFrom = shiftedX;
					startWindowWidth = shiftedX;
					startBottomPosition1x = shiftedX;
					startTopPosition2x = width - shiftedX;
				}
				
				if(upper.equals(scaledImage2)){
					int shiftedY = (int)prevOverlap[1] - pixelRange;
					if (shiftedY < 1){
						yWindowBoundsFrom = 1;
						startWindowHeight = 1;
						startBottomPosition1y = 1;
						startTopPosition2y = height - 1;
					}else{
						yWindowBoundsFrom = shiftedY;
						startWindowHeight = shiftedY;
						startBottomPosition1y = shiftedY;
						startTopPosition2y = height - shiftedY;
					}
				}else{
					if (height - (int)prevOverlap[5] < pixelRange){
						yWindowBoundsFrom = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startWindowHeight = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startBottomPosition1y = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startTopPosition2y = (-1) * ((int)prevOverlap[3] + (int)prevOverlap[1] - pixelRange - (int)prevOverlap[5]);
					}else{
						yWindowBoundsFrom = (int)prevOverlap[1];
						startWindowHeight = (int)prevOverlap[5] + pixelRange;
						startBottomPosition1y = (int)prevOverlap[1];
						startTopPosition2y = (int)prevOverlap[3];
					}
				}
			}else{
				int shiftedX = (width - (int)prevOverlap[4]) - pixelRange;
				
				if (shiftedX < 0){
					int x  = (width - (int)prevOverlap[4]);
					xWindowBoundsFrom = width - (pixelRange - x);
					startWindowWidth = width - (pixelRange - x);
					startBottomPosition1x = width - (pixelRange - x);
					startTopPosition2x = 0 + (pixelRange - x);
				}else{
					xWindowBoundsFrom = (int)prevOverlap[0];//(~width)
					startWindowWidth = (int)prevOverlap[4] + pixelRange;
					startBottomPosition1x = (int)prevOverlap[0];//(~width)
					startTopPosition2x = 0;
				}
				
				if (upper.equals(scaledImage2)){
					int shiftedY = (int)prevOverlap[1] - pixelRange;
					if (shiftedY < 1){
						yWindowBoundsFrom = 1;
						startWindowHeight = 1;
						startBottomPosition1y = 1;
						startTopPosition2y = height - 1;
					}else{
						yWindowBoundsFrom = shiftedY;
						startWindowHeight = shiftedY;
						startBottomPosition1y = shiftedY;
						startTopPosition2y = height - shiftedY;
					}
					
				}else{
					if (height - (int)prevOverlap[5] < pixelRange){
						yWindowBoundsFrom = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startWindowHeight = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startBottomPosition1y = 2*(int)prevOverlap[1] - pixelRange - (int)prevOverlap[5];
						startTopPosition2y = (-1) * ((int)prevOverlap[3] + (int)prevOverlap[1] - pixelRange - (int)prevOverlap[5]);
					}else{
						yWindowBoundsFrom = (int)prevOverlap[1];
						startWindowHeight = (int)prevOverlap[5] + pixelRange;
						startBottomPosition1y = (int)prevOverlap[1];
						startTopPosition2y = (int)prevOverlap[3];
					}
				}
			}
			xWindowBoundsTo = xWindowBoundsFrom + 2*pixelRange;
			yWindowBoundsTo = yWindowBoundsFrom + 2*pixelRange;
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
				
				//if it is less than the min achieved value - keep it!
				if ((current_difference < diff) && windowSize > (width*height/4)){
					//I found better solution:
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

}
