package com.vasova.bachelorproject;

import java.util.ArrayList;

import org.opencv.features2d.KeyPoint;

public class KeyPointStructure {
	KeyPoint keypoint;
	ArrayList<Double> descriptor;
	KeyPointStructure correspondingKeypoint;
	
	public KeyPointStructure(KeyPoint point, ArrayList<Double> desc){
		this.keypoint = point;
		this.descriptor = desc;
	}
}
