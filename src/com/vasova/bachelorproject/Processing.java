package com.vasova.bachelorproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.TermCriteria;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.*;
import android.util.Log;

/**
 * This class handles the major steps of the reconstruction pipeline.
 * Due to the computational cost, the methods were designed to run on a different thread, separated from the main thread.  
 * @author viktorievasova
 *
 */
public class Processing {
	private ArrayList<ImageStructure> imageData;
	private Registration registration;
	private KeyPointMatcher matcher;
	private String TAG = "Processing";
	
	/**
	 * The constructor of this class.
	 * Initializes the global variables of this class.
	 */
	public Processing(){
		registration = new Registration();
		matcher = new KeyPointMatcher();
	}
	
	/**
	 * Sets the input data-set of images for the further processing.
	 * @param imgData an ArrayList of the input pictures.
	 */
	public void setData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
	}
	
	/**
	 * Starts and handles the process of the calculation.
	 * @param mode a parameter determining the result of the calculation. Set to 2 for result in 2D, set to 3 for the result of 3D model.
	 */
	public void startProcessing(int mode){
		long startTime = System.currentTimeMillis();
		registration.setImageData(this.imageData);
		if (Thread.currentThread().isInterrupted()){
			return;
		}
		imageData = registration.getData();
		matcher.setData(imageData);
		matcher.match();
		if (Thread.currentThread().isInterrupted()){
			return;
		}
		imageData = matcher.getData();
		getMaxDepthInformation();
		filterOutExtremePoints();
		//data visualization
		if (mode == 2){
			prepare2DData();
		}else if (mode == 3){
			prepare3DData();
		}
		long endTime   = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		Log.i(TAG, "RUNNING TIME: " + totalTime/1000 + "s");
	}
		
	private double minDistance;
	private double maxDistance;
	private double averageDistance;
	
	private int delta = 70;
	
	/**
	 * This method iterates all detected matches and finds the minimal and maximal information.
	 */
	private void getMaxDepthInformation(){
		minDistance = 1000;
		maxDistance = 0;
		ImageStructure img1 = imageData.get(0);
		for (int i = 0; i < img1.keyPoints.size(); i++){
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			double x1 = keypoint.keypoint.pt.x;
			double y1 = keypoint.keypoint.pt.y;
			double x2 = keypoint.correspondingKeypoint.keypoint.pt.x;
			double y2 = keypoint.correspondingKeypoint.keypoint.pt.y;
			double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
			
			if (distance < minDistance){
				minDistance = distance;
			}
			if (distance > maxDistance){
				maxDistance = distance;
			}
		}
		averageDistance = minDistance + (maxDistance - minDistance)/2;
	}
	
	/**
	 * This method dispose the detected matches that are situated in the closest 20%.
	 */
	private void filterOutExtremePoints(){
		ImageStructure img1 = imageData.get(0);
		for (int i = 0; i < img1.keyPoints.size(); i++){
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			double x1 = keypoint.keypoint.pt.x;
			double y1 = keypoint.keypoint.pt.y;
			double x2 = keypoint.correspondingKeypoint.keypoint.pt.x;
			double y2 = keypoint.correspondingKeypoint.keypoint.pt.y;
			double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
			if (distance > (minDistance + (maxDistance - minDistance)*0.8)){
				keypoint.correspondingKeypoint = null;
			}
		}
	}
	
	/**
	 * This method draws the result into a Mat.
	 * The depth of the each correspondences is expressed by the color.
	 * At first it draws the correspondences of SURF matches and the progressively updates the image 
	 * by drawing the dense correspondences calculated for the surroundings of each SURF keypoint by
	 * opticalFlow() method.   
	 */
	private void prepare2DData(){
		ImageStructure img1 = imageData.get(0);
		Mat m = new Mat(img1.width, img1.height, CvType.CV_8UC3);
		for (int i = 0; i < img1.keyPoints.size(); i++){
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			double distance = Math.sqrt(Math.pow(keypoint.shift.x, 2) + Math.pow(keypoint.shift.y, 2));
			double color = distance/maxDistance*255;
			Core.circle(m, keypoint.keypoint.pt, 4, new Scalar(color, color, color), 6);
		}
		GalleryActivity.update2DData(m);
		
		//for each SURF keypoint get optical flow and draw it:
		for (int i = 0; i < img1.keyPoints.size(); i++){
			if (Thread.currentThread().isInterrupted()){
				return;
			}
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			KeyPoint point = keypoint.keypoint;
			KeyPoint corrpoint = keypoint.correspondingKeypoint.keypoint;
			
			Point keypointShiftDirection = keypoint.shift;//new Point(corrpoint.pt.x - point.pt.x, corrpoint.pt.y - point.pt.y);
			double kp_norm = Math.sqrt(Math.pow(keypointShiftDirection.x, 2) + Math.pow(keypointShiftDirection.y, 2));
			keypointShiftDirection.x = keypointShiftDirection.x/kp_norm;
			keypointShiftDirection.y = keypointShiftDirection.y/kp_norm;
			
			double keypoint_angle = Math.atan2(keypointShiftDirection.y, keypointShiftDirection.x);
			
			int startRow1 = (int)point.pt.x - delta;
			int endRow1 = (int)point.pt.x + delta;
			int startCol1 = (int)point.pt.y - delta;
			int endCol1 = (int)point.pt.y + delta;
			
			int startRow2 = (int)corrpoint.pt.x - delta;
			int endRow2 = (int)corrpoint.pt.x + delta;
			int startCol2 = (int)corrpoint.pt.y - delta;
			int endCol2 = (int)corrpoint.pt.y + delta;
			
			Point leftCorner1 = new Point(startRow1,startCol1);
			Point rightCorner1 = new Point(endRow1, endCol1);

			Point leftCorner2 = new Point(startRow2,startCol2);
			Point rightCorner2 = new Point(endRow2, endCol2);
			
			Mat image1GrayScale = Highgui.imread(imageData.get(0).path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			Mat image2GrayScale = Highgui.imread(imageData.get(1).path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			
			Mat orig1 = Highgui.imread(imageData.get(0).path);
			Mat orig2 = Highgui.imread(imageData.get(1).path);

			if ((leftCorner1.x < 0) || (leftCorner1.y < 0) || (leftCorner2.x < 0) || (leftCorner2.y < 0)){
				continue;
			}
			if ((rightCorner1.x > image1GrayScale.cols() - 1) || (rightCorner1.y > image1GrayScale.rows() - 1) ||
				(rightCorner2.x > image2GrayScale.cols() - 1) || (rightCorner2.y > image2GrayScale.rows() - 1))	{
				continue;
			}
			
			Mat rectangle1Grayscale = image1GrayScale.submat(new Rect(leftCorner1, rightCorner1)).clone();
			Mat rectangle2Grayscale = image2GrayScale.submat(new Rect(leftCorner2, rightCorner2)).clone();
			
			Mat rectangleRGB1 = orig1.submat(new Rect(leftCorner1, rightCorner1)).clone();
			Mat rectangleRGB2 = orig2.submat(new Rect(leftCorner2, rightCorner2)).clone();
			//debbug:
			Imgproc.pyrUp(rectangleRGB1, rectangleRGB1);
			Imgproc.pyrUp(rectangleRGB1, rectangleRGB1);
			Imgproc.pyrUp(rectangleRGB2, rectangleRGB2);
			Imgproc.pyrUp(rectangleRGB2, rectangleRGB2);
			
			opticalFlow(rectangle1Grayscale, rectangle2Grayscale, keypointShiftDirection, keypoint_angle, leftCorner1, leftCorner2);
			if (points1 == null || points2 == null){
				continue;
			}
			Random random = new Random();

			ArrayList<Point> p1 = points1;
			ArrayList<Point> p2 = points2;
			for(int n = 0; n < p1.size(); n++){
				double x1 = p1.get(n).x + leftCorner1.x;
				double y1 = p1.get(n).y + leftCorner1.y;
				double x2 = p2.get(n).x + leftCorner2.x;
				double y2 = p2.get(n).y + leftCorner2.y;
				
				double c1 = 255*random.nextDouble();
				double c2 = 255*random.nextDouble();
				double c3 = 255*random.nextDouble();
				Scalar color = new Scalar(c1, c2, c3);
				
				Point currpointShiftDirection = new Point(x2 - x1, y2 - y1);
				double ckp_norm = Math.sqrt(Math.pow(currpointShiftDirection.x, 2) + Math.pow(currpointShiftDirection.y, 2));
				currpointShiftDirection.x = currpointShiftDirection.x/ckp_norm;
				currpointShiftDirection.y = currpointShiftDirection.y/ckp_norm;
				
				double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				double c = distance/maxDistance*255;
				Core.circle(m, new Point(x1, y1), 4, new Scalar(c, c, c), 6);
				Core.circle(rectangleRGB1, new Point(p1.get(n).x * 4, p1.get(n).y *4), 2, color, 2);
				Core.circle(rectangleRGB2, new Point(p2.get(n).x *4, p2.get(n).y *4), 2, color, 2);
			}
			GalleryActivity.update2DData(m);
		}
	}
	
	
	/**
	 * This method generated the arrays of vertices and colors needed for graphical 3D visualization in OpenGL ES.
	 * At first it creates the data of the correspondences of SURF matches and the progressively updates the 3D model 
	 * by generating new arrays with dense correspondences calculated for the surroundings of each SURF keypoint by
	 * opticalFlow() method and send the updated data to the renderer.
	 */
	private void prepare3DData(){
		ImageStructure img1 = imageData.get(0);		
		Mat inputImage = Highgui.imread(img1.path);
		// body trojuhelniku pro opengl:
		
		int max_num_of_triangles = img1.keyPoints.size();
		int real_num_of_triangles = 0;
		int max_num_of_vertices = max_num_of_triangles * 3;
		
		float[] vertices = new float[max_num_of_vertices*3];
		float[] colors = new float[max_num_of_vertices * 4];
		int index_of_vertices = 0;
		int index_of_colors = 0;

		int imageWidth = img1.width;
		int imageHeight = img1.height;
		float k;
		float width, height;
		if (imageWidth > imageHeight){//horizontal
			width = 8f;
			height = 8f/(float)imageWidth*(float)imageHeight;
			k = width/imageWidth;
		}else{//portrait
			height = 8f;
			width = 8f/(float)imageHeight*(float)imageWidth;
			k = height/imageHeight;
		}
		float start_x;
		float start_y;
		
		float triangle_height = height/imageHeight*20;
		float triangle_width = width/imageWidth*20;
		
		start_x = 0f - width/2;
		start_y = 0f + height/2;
		
		for (int i = 0; i < img1.keyPoints.size(); i++){
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			real_num_of_triangles++;
			double x1 = keypoint.keypoint.pt.x;
			double y1 = keypoint.keypoint.pt.y;
			double x2 = keypoint.correspondingKeypoint.keypoint.pt.x;
			double y2 = keypoint.correspondingKeypoint.keypoint.pt.y;
			double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
			
			double[] color = getColor(inputImage, (int)keypoint.keypoint.pt.x, (int)keypoint.keypoint.pt.y);
			
			float c1 = (float)color[2]/255f;
			float c2 = (float)color[1]/255f;
			float c3 = (float)color[0]/255f;
			
			Point t1 = new Point(start_x + k*(float)x1, start_y - k*(float)y1 + triangle_height/2);
			Point t2 = new Point(start_x + k*(float)x1 - triangle_width/2, start_y - k*(float)y1 - triangle_height/2);
			Point t3 = new Point(start_x + k*(float)x1 + triangle_width/2, start_y - k*(float)y1 - triangle_height/2);
			
			float z;
			if (distance > averageDistance){
				z = 0 + (float)(Math.abs(distance - averageDistance))/30f;
			}else if (distance < averageDistance){
				z = 0 - (float)(Math.abs(distance - averageDistance))/30f;
			}else{
				z = 0;
			}
			
			vertices[index_of_vertices++] = (float)t1.x;
			vertices[index_of_vertices++] = (float)t1.y;
			vertices[index_of_vertices++] = z;
			
			vertices[index_of_vertices++] = (float)t2.x;
			vertices[index_of_vertices++] = (float)t2.y;
			vertices[index_of_vertices++] = z;
			
			vertices[index_of_vertices++] = (float)t3.x;
			vertices[index_of_vertices++] = (float)t3.y;
			vertices[index_of_vertices++] = z;
			
			//the same color for each vertex:
			for (int t = 0; t < 3; t++){
				colors[index_of_colors++] = c1;
				colors[index_of_colors++] = c2;
				colors[index_of_colors++] = c3;
				colors[index_of_colors++] = 1.0f;
			}	
		}
		GalleryActivity.update3DData(vertices, colors);
		
		int delta = 70;
		int index_in_array = 0;
		for (int i = 0; i < img1.keyPoints.size(); i++){
			if (Thread.currentThread().isInterrupted()){
				return;
			}
			KeyPointStructure keypoint = img1.keyPoints.get(i);
			if (keypoint.correspondingKeypoint == null){
				continue;
			}
			KeyPoint point = keypoint.keypoint;
			KeyPoint corrpoint = keypoint.correspondingKeypoint.keypoint;
			Point keypointShiftDirection = new Point(corrpoint.pt.x - point.pt.x, corrpoint.pt.y - point.pt.y);
			double kp_norm = Math.sqrt(Math.pow(keypointShiftDirection.x, 2) + Math.pow(keypointShiftDirection.y, 2));
			keypointShiftDirection.x = keypointShiftDirection.x/kp_norm;
			keypointShiftDirection.y = keypointShiftDirection.y/kp_norm;
			
			double keypoint_angle = Math.atan2(keypointShiftDirection.y, keypointShiftDirection.x);
			
			int startRow1 = (int)point.pt.x - delta;
			int endRow1 = (int)point.pt.x + delta;
			int startCol1 = (int)point.pt.y - delta;
			int endCol1 = (int)point.pt.y + delta;
			
			int startRow2 = (int)corrpoint.pt.x - delta;
			int endRow2 = (int)corrpoint.pt.x + delta;
			int startCol2 = (int)corrpoint.pt.y - delta;
			int endCol2 = (int)corrpoint.pt.y + delta;
			
			Point leftCorner1 = new Point(startRow1,startCol1);
			Point rightCorner1 = new Point(endRow1, endCol1);

			Point leftCorner2 = new Point(startRow2,startCol2);
			Point rightCorner2 = new Point(endRow2, endCol2);
			
			Mat image1GrayScale = Highgui.imread(imageData.get(0).path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			Mat image2GrayScale = Highgui.imread(imageData.get(1).path, Highgui.CV_LOAD_IMAGE_GRAYSCALE);
			
			if ((leftCorner1.x < 0) || (leftCorner1.y < 0) || (leftCorner2.x < 0) || (leftCorner2.y < 0)) {
				continue;
			}
			if ((rightCorner1.x > image1GrayScale.cols() - 1) || (rightCorner1.y > image1GrayScale.rows() - 1) ||
				(rightCorner2.x > image2GrayScale.cols() - 1) || (rightCorner2.y > image2GrayScale.rows() - 1)){
				continue;
			}
			
			Mat rectangle1Grayscale = image1GrayScale.submat(new Rect(leftCorner1, rightCorner1)).clone();
			Mat rectangle2Grayscale = image2GrayScale.submat(new Rect(leftCorner2, rightCorner2)).clone();
			
			opticalFlow(rectangle1Grayscale, rectangle2Grayscale, keypointShiftDirection, keypoint_angle, leftCorner1, leftCorner2);
			
			if (points1 == null || points2 == null){
				continue;
			}
			ArrayList<Point> p1 = points1; 
			ArrayList<Point> p2 = points2;
			
			int num_of_triangles_from_list = p1.size();
			int num_of_vertices_from_list = num_of_triangles_from_list * 3;
			float[] vertices_from_list = new float[num_of_vertices_from_list*3];
			float[] colors_from_list = new float[num_of_vertices_from_list*4];
			
			index_of_vertices = 0;
			index_of_colors = 0;
			
			for(int n = 0; n < p1.size(); n++){
				double x1 = p1.get(n).x + leftCorner1.x;
				double y1 = p1.get(n).y + leftCorner1.y;
				double x2 = p2.get(n).x + leftCorner2.x;
				double y2 = p2.get(n).y + leftCorner2.y;
				
				double distance = Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
				double[] color = getColor(inputImage, (int)keypoint.keypoint.pt.x, (int)keypoint.keypoint.pt.y);
				
				float c1 = (float)color[2]/255f;
				float c2 = (float)color[1]/255f;
				float c3 = (float)color[0]/255f;
				
				Point t1 = new Point(start_x + k*(float)x1, start_y - k*(float)y1 + triangle_height/2);
				Point t2 = new Point(start_x + k*(float)x1 - triangle_width/2, start_y - k*(float)y1 - triangle_height/2);
				Point t3 = new Point(start_x + k*(float)x1 + triangle_width/2, start_y - k*(float)y1 - triangle_height/2);
				
				float z;
				if (distance > averageDistance){
					z = 0 + (float)(Math.abs(distance - averageDistance))/30f;
				}else if (distance < averageDistance){
					z = 0 - (float)(Math.abs(distance - averageDistance))/30f;
				}else{
					z = 0;
				}
				
				vertices_from_list[index_of_vertices++] = (float)t1.x;
				vertices_from_list[index_of_vertices++] = (float)t1.y;
				vertices_from_list[index_of_vertices++] = z;
				
				vertices_from_list[index_of_vertices++] = (float)t2.x;
				vertices_from_list[index_of_vertices++] = (float)t2.y;
				vertices_from_list[index_of_vertices++] = z;
				
				vertices_from_list[index_of_vertices++] = (float)t3.x;
				vertices_from_list[index_of_vertices++] = (float)t3.y;
				vertices_from_list[index_of_vertices++] = z;
				//the same color for each vertex of the added trangle:
				for (int t = 0; t < 3; t++){
					colors_from_list[index_of_colors++] = c1;
					colors_from_list[index_of_colors++] = c2;
					colors_from_list[index_of_colors++] = c3;
					colors_from_list[index_of_colors++] = 1.0f;
				}
			}
			float[] new_vertices = new float[(real_num_of_triangles-1)*9 + vertices_from_list.length];
			float[] new_colors = new float[(real_num_of_triangles-1)*12 + colors_from_list.length];
			index_of_vertices = 0;
			index_of_colors = 0;
			int index_of_old_vertices = 0;
			int index_of_old_colors = 0;
			//update data for 3d visalization:
			for (int n = 0; n < real_num_of_triangles; n++){
				if (n < index_in_array){
					//copy the tringle from float[] vertices
					for (int t = 0; t < 3; t++){
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
					}
				}else if (n == index_in_array){
					index_of_old_vertices += 9;
					index_of_old_colors += 12;
					int indexv = 0;
					int indexc = 0;
					//copy new optical-flow triangles 
					for (int t = 0; t < vertices_from_list.length/3; t++){
						new_vertices[index_of_vertices++] = vertices_from_list[indexv++];
						new_vertices[index_of_vertices++] = vertices_from_list[indexv++];
						new_vertices[index_of_vertices++] = vertices_from_list[indexv++];
						new_colors[index_of_colors++] = colors_from_list[indexc++];
						new_colors[index_of_colors++] = colors_from_list[indexc++];
						new_colors[index_of_colors++] = colors_from_list[indexc++];
						new_colors[index_of_colors++] = colors_from_list[indexc++];
					}
					
				}else{
					//copy the rest of float[] vertices
					for (int t = 0; t < 3; t++){
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_vertices[index_of_vertices++] = vertices[index_of_old_vertices++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
						new_colors[index_of_colors++] = colors[index_of_old_colors++];
					}
				}
			}
			index_in_array += vertices_from_list.length/9;
			vertices = new_vertices;
			colors = new_colors;
			real_num_of_triangles = new_vertices.length/9;
			GalleryActivity.update3DData(vertices, colors);
		}		
		
	}
	
	private ArrayList<Point> points1;
	private ArrayList<Point> points2;
	
	/**
	 * 
	 * @param rectangle1Grayscale
	 * @param rectangle2Grayscale
	 * @param keypoint_shift_direction
	 * @param keypoint_angle
	 * @param leftCorner1
	 * @param leftCorner2
	 */
	private void opticalFlow(Mat rectangle1Grayscale, Mat rectangle2Grayscale, Point keypoint_shift_direction, double keypoint_angle, Point leftCorner1, Point leftCorner2){
		int max_count = 500;
		MatOfPoint corners = new MatOfPoint();
		Imgproc.goodFeaturesToTrack(rectangle1Grayscale, corners, max_count, 0.01, 10.0, new Mat(), 3, false, 0.4);
		MatOfByte status = new MatOfByte();
		MatOfFloat err = new MatOfFloat();
		MatOfPoint2f p1 = new MatOfPoint2f( corners.toArray() );
		MatOfPoint2f p2 = new MatOfPoint2f();
		TermCriteria criteria = new TermCriteria(TermCriteria.MAX_ITER, 20, 0.03);
		Video.calcOpticalFlowPyrLK(rectangle1Grayscale, rectangle2Grayscale, p1, p2, status, err, new Size(31, 31), 3, criteria, 0, 0.001);
		
		List<Point> list1 = p1.toList();
		List<Point> list2 = p2.toList();
		
		points1 = new ArrayList<Point>();
		points2 = new ArrayList<Point>();
		
		double minD = Double.MAX_VALUE;
		double maxD = Double.MIN_VALUE;
		double averageD;
		
		for (int i = 0; i < list1.size(); i++){
			double x1 = list1.get(i).x + leftCorner1.x;
			double y1 = list1.get(i).y + leftCorner1.y;
			double x2 = list2.get(i).x + leftCorner2.x;
			double y2 = list2.get(i).y + leftCorner2.y;
			
			double distance = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
			if (distance > maxD){
				maxD = distance;
			}
			if (distance < minD){
				minD = distance;
			}
		}
		averageD = (minD + maxD)/2;
		
		int sigma = 0;
		for (int i = 0; i < list1.size(); i++){
			double x1 = list1.get(i).x + leftCorner1.x;
			double y1 = list1.get(i).y + leftCorner1.y;
			double x2 = list2.get(i).x + leftCorner2.x;
			double y2 = list2.get(i).y + leftCorner2.y;
			
			double distance = Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
			sigma+= Math.pow((distance - averageD), 2);
		}
		sigma = sigma/list1.size();
		if(sigma > 300){
			points1 = null;
			points2 = null;
			return;
		}
		for (int i = 0; i < list1.size(); i++){
			double x1 = list1.get(i).x + leftCorner1.x;
			double y1 = list1.get(i).y + leftCorner1.y;
			double x2 = list2.get(i).x + leftCorner2.x;
			double y2 = list2.get(i).y + leftCorner2.y;
			
			if (list1.get(i).x < 0 + 0.2*rectangle1Grayscale.width() || list1.get(i).x > rectangle1Grayscale.width()-0.2*rectangle1Grayscale.width() ||
				list1.get(i).y < 0 + 0.2*rectangle1Grayscale.height() || list1.get(i).y > rectangle1Grayscale.height()-0.2*rectangle1Grayscale.height()	){
				continue;
			}
			
			Point currpointShiftDirection = new Point(x2 - x1, y2 - y1);
			double ckp_norm = Math.sqrt(Math.pow(currpointShiftDirection.x, 2) + Math.pow(currpointShiftDirection.y, 2));
			currpointShiftDirection.x = currpointShiftDirection.x/ckp_norm;
			currpointShiftDirection.y = currpointShiftDirection.y/ckp_norm;
			double current_angle = Math.atan2(currpointShiftDirection.y, currpointShiftDirection.x);
			
			if (getAngularDistance(keypoint_angle, current_angle) > 0.1){
				continue;
			}
			points1.add(list1.get(i));
			points2.add(list2.get(i));
		}
		
	}
	

	/**
	 * Returns a color for pixel (x, y) in an image img.
	 * The color is calculated as an average color of the surrounding area of 10 pixels. 
	 *
	 * @param img The image.
	 * @param x The x coordinate in the image.
	 * @param y The y coordinate in the image.
	 * @return The resulting BGR color.
	 */
	private double[] getColor(Mat img, int x, int y){
		int area_const = 10;
		int xFrom = x - area_const;
		int xTo = x + area_const;
		int yFrom = y - area_const;
		int yTo = y + area_const;
		if (xFrom < 0)	xFrom = 0;
		if (xTo > img.width() - 1)	xTo = img.width() - 1;
		if (yFrom < 0)	yFrom = 0;
		if (yTo > img.height() - 1)	yTo = img.height() - 1;
		double[] color = new double[3];
		for (int w = xFrom; w <= xTo; w++){
			for(int h = yFrom; h <= yTo; h++){
				double[] c = img.get(h, w);
				color[0] += c[0];
				color[1] += c[1];
				color[2] += c[2];
			}
		}
		color[0] /= (xTo - xFrom)*(yTo - yFrom);
		color[1] /= (xTo - xFrom)*(yTo - yFrom);
		color[2] /= (xTo - xFrom)*(yTo - yFrom);
		return color;
	}
	
	/**
	 * Method for calculating the angular distance of two angles.  
	 * @param a The first angle.
	 * @param b The second angle.
	 * @return The resulting double value of the distance.
	 */
	private double getAngularDistance(double a, double b) {
		double min = Math.min(a, b); 
		double max = Math.max(a, b);
		double r = Math.min( max - min, 2*Math.PI + min - max );
		return r;
	}
	

}
