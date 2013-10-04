package com.vasova.bachelorproject;

import java.util.ArrayList;

import org.opencv.core.Point;
import org.opencv.features2d.KeyPoint;

/**
 * KeyPointStructure is a structure representing one keypoint of an image.
 * In this structure is stored the KeyPoint (keypoint definition of org.opencv.features2d package of OpenCV library), 
 * descriptor as an ArrayList of Doubles and KeyPointStructure as the corresponding point 
 * in the second image. 
 * 
 * @author viktorievasova
 *
 */
public class KeyPointStructure {
	KeyPoint keypoint;
	ArrayList<Double> descriptor;
	KeyPointStructure correspondingKeypoint;
	Point shift;
	
	/**
	 * 
	 * @param point The keypoint.
	 * @param desc The descriptor of the keypoint in an ArrayList<Double>
	 */
	public KeyPointStructure(KeyPoint point, ArrayList<Double> desc){
		this.keypoint = point;
		this.descriptor = desc;
	}
}
