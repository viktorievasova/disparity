package com.vasova.bachelorproject;

import java.util.ArrayList;
import org.opencv.core.Mat;

/**
 * A structure for storing information of an image for image processing.
 * It holds the path of the image, scales of the image, width and height, 
 * an ArrayList of keypoints detected in the image, an ArrayList of the 
 * square Buckets of the image and the relative position with the neighbouring 
 * image. The relative position is defined as an array of doubles in the format 
 * [BottomPoint.x, BottomPoint.y, TopPoint.x, TopPoint.y, width_of_the_overlap, 
 * heght_of_the_overlap], where BottomPoint is a bottom right corner of the 
 * overlap in the first image and TopPoint is a top left corner of the overlap 
 * in the second image.
 *  
 * @author viktorievasova
 *
 */
public class ImageStructure {
	String path;	
	ArrayList<Mat> scales;
	int width;
	int height;
	ArrayList<KeyPointStructure> keyPoints;
	ArrayList<Bucket> buckets;
	double[] relative_position_with_neighbour;
}
