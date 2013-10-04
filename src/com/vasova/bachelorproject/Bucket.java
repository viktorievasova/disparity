package com.vasova.bachelorproject;

import java.util.ArrayList;
import org.opencv.core.Point;

/**
 * This structure represents a rectangular part of an image.
 * It is defined by the top and the bottom corner using the 
 * coordinates of the image.
 * All keypoints lying in this area are stored in an ArraList of KeypointStructure.
 */
public class Bucket {
	Point topCorner;
	Point bottomCorner;
	ArrayList<KeyPointStructure> keypointsData;
}
