package com.vasova.bachelorproject;

import java.util.ArrayList;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
public class Registration {
	
	String TAG = "Registration";
	private ArrayList<ImageStructure> imageData;
	
	public void setImageData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
		register();
	}
	
	/**
	 * Returns the updated set of input data.
	 * After registration new information such as the relative position of the neighbouring images or scale-space for the image
	 * is stored in the ArrayList<ImageStructure> imageData.
	 * To use this information in the further processing, it is necessary to get the current version of the input data-set. 
	 * @return an ArrayList of the updated images.
	 */
	public ArrayList<ImageStructure> getData(){
		return this.imageData;
	}
	
	/**
	 * This method starts the process of the image registration.
	 */
	private void register(){
		Mat image;
		ImageStructure imgStr;
		for(int i = 0; i < imageData.size(); i++){
			imgStr = imageData.get(i);
			image = Highgui.imread(imgStr.path);
			imgStr.width = image.width();
			imgStr.height = image.height();
			imgStr.scales = new ArrayList<Mat>();
			imgStr.scales.add(image.clone());
			while(image.width() > 17 && image.height() > 17){
				Imgproc.pyrDown(image, image);
				imgStr.scales.add(image.clone());
			}
		}
		getOverlaps();
	}
	
	
	/**
	 * Returns the image which is situated on the left side (in the sense of their relative position). 
	 * @param img1 the first image.
	 * @param img2 the second image.
	 * @param overlap the overlap of img1 and img2.
	 * @return one of the images which is situated on the left side from the other one.
	 */
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
	
	/**
	 * Returns the image which is situated on above (in the sense of their relative position). 
	 * @param img1 the first image.
	 * @param img2 the second image.
	 * @param overlap the overlap of img1 and img2.
	 * @return one of the images which is situated above the other one.
	 */
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
	
	/**
	 * Finds the overlap for every neighbouring images in the input data-set.
	 */
	private void getOverlaps(){
		ImageStructure img1;
		ImageStructure img2;
		
		for(int i = 0; i < imageData.size() - 1; i++){
			int j = i+1;
			img1 = imageData.get(i);
			img2 = imageData.get(j);
			double[] overlap = new double[6];
			overlap = getOverlap(img1, img2);
			overlap[0] = overlap[0] - 1;
			overlap[1] = overlap[1] - 1;
			img1.relative_position_with_neighbour = overlap;
		}
	
	}
	
	/**
	 * Finds the overlap of two images.
	 * @param image1 the first image.
	 * @param image2 the second image.
	 * @return an array representing the overlap: x and y coordinate of the bottom right corner of the overlap in the first image,
	 * x and y coordinate of the top left corner of the overlap in the second image, the width sand height of the overlap.
	 */
	private double[] getOverlap(ImageStructure image1, ImageStructure image2){
		double[] overlap = new double[6];
		double[] previousOverlap = new double[0];
		double[] currentOverlap;
		int previousWidth = image1.scales.get(image1.scales.size() - 1).width();
		int previousHeight = image1.scales.get(image1.scales.size() - 1).height();
		int currentWidth;
		int currentHeight;
		for (int s = image1.scales.size() - 1; s >= 0; s--){
			currentOverlap = getOverlapforScaledImages(image1.scales.get(s), image2.scales.get(s), previousOverlap);
			if (previousWidth >= 200){
				currentWidth = image1.scales.get(0).width();
				currentHeight = image1.scales.get(0).height();
				previousOverlap = resizeCoords(currentOverlap, previousWidth, previousHeight, currentWidth, currentHeight);
				previousWidth = currentWidth;
				previousHeight = currentHeight;
				break;
			}else if (s != 0){
				currentWidth = image1.scales.get(s-1).width();
				currentHeight = image1.scales.get(s-1).height();
				previousOverlap = resizeCoords(currentOverlap, previousWidth, previousHeight, currentWidth, currentHeight);
				previousWidth = currentWidth;
				previousHeight = currentHeight;
			}
		}
		overlap = previousOverlap;
		return overlap;
	}
	
	/**
	 * Finds the more accurate result for the overlap detected for the previous scale.
	 * The algorithm is based on sum of absolute differences.
	 * The image scales must be in the same size.
	 * If there was not found any overlap before, set the prevOverlap = [].
	 * @param mat1 the scaled image of the first image. 
	 * @param mat2 the scaled image of the second image.
	 * @param prevOverlap 
	 * @return an array representing the overlap: x and y coordinate of the bottom right corner of the overlap in the first image,
	 * x and y coordinate of the top left corner of the overlap in the second image, the width sand height of the overlap.
	 */
    private double[] getOverlapforScaledImages(Mat mat1, Mat mat2, double[] prevOverlap){
		int width = mat1.width();
		int height = mat1.height();

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
	
		int pixelRange = 5;
		
		if (prevOverlap.length == 0){	
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
						
		}else {
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
				
				if ((current_difference < diff) && windowSize > (width*height/4)){
					//we found a better solution:
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


    /**
     * Resizes the coordinates of an overlap between two scaled images.
     * It is assumed that we lower scale to the higher  
     * @param oldCoords the overlap information of the previous scale. the overlap is represented as following: x and y coordinate of the bottom right corner of the overlap in the first image,
	 * x and y coordinate of the top left corner of the overlap in the second image, the width sand height of the overlap. 
     * @param oldW the width of the previous scale. It is assumed that the previous width is larger than the current width.
     * @param oldH the height of the previous scale. It is assumed that the previous height is larger than the current height. 
     * @param w the width of the current scale. It is assumed that the previous width is larger than the current width.
     * @param h the height of the current scale. It is assumed that the previous height is larger than the current height.
     * @return
     */
	private double[] resizeCoords(double[] oldCoords, int oldW, int oldH, int w, int h){
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
