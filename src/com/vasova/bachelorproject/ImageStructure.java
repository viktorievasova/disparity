package com.vasova.bachelorproject;

import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

public class ImageStructure {
	String path;
	int orientation;//0...horizontal, 1...vertical
	
	String neighbourPath;
	ArrayList<Mat> scales;
	int width;
	int height;
	double[] relativePositionWithNeighbour;
	
	MatOfKeyPoint keyPoints;
	Mat descriptors;
	
	ArrayList<Packet> packets;
}
