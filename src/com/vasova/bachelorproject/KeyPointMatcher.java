package com.vasova.bachelorproject;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;

import android.os.Environment;
import android.util.Log;


public class KeyPointMatcher {
	
	private String TAG = "KeyPointMatcher";
	private ArrayList<ImageStructure> imageData;
	/* n - rozdeleni delky, m - rozdeleni sirky pro vytvoreni packetu*/
	private int n = 5;
	private int m = 3;
	private Mat imgForVisualization;
	
	public void setData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
	}
	
	public void match(){
		
		ImageStructure imgStr1, imgStr2;
		createPackets();
		
		for(int i = 0; i < imageData.size() - 1; i++){
			imgStr1 = imageData.get(i);
			imgStr2 = imageData.get(i+1);
			matchAPair(imgStr1, imgStr2);
		}
	}
	
	public Mat getImgForVis(){
		return imgForVisualization;
	}
	private void matchAPair(ImageStructure img1, ImageStructure img2){
		double[] overlap = img1.relativePositionWithNeighbour;
		Point topCorner;
		Point bottomCorner;
		
		topCorner = new Point(overlap[0]- (overlap[4] - 1), overlap[1] - (overlap[5] - 1));
		bottomCorner = new Point(overlap[0], overlap[1]);
		ArrayList<Packet> overlapPackets1 = new ArrayList<Packet>();
		ArrayList<KeyPointStructure> overlapKeypoints1 = new ArrayList<KeyPointStructure>();
		getOverlapPacketsAndKeypoints(img1, topCorner, bottomCorner, overlapPackets1, overlapKeypoints1);
		
		topCorner = new Point(overlap[2], overlap[3]);
		bottomCorner = new Point(overlap[2] + (overlap[4] - 1), overlap[3] + (overlap[4] - 1));
		ArrayList<Packet> overlapPackets2 = new ArrayList<Packet>();
		ArrayList<KeyPointStructure> overlapKeypoints2 = new ArrayList<KeyPointStructure>();
		getOverlapPacketsAndKeypoints(img2, topCorner, bottomCorner, overlapPackets2, overlapKeypoints2);
		
		KeyPointStructure keypoint;
		
		double xTop, xBottom, yTop, yBottom;
		double corresponding_xTop, corresponding_xBottom, corresponding_yTop, corresponding_yBottom;
		double constantEpsilon = 15d;
		Log.d(TAG, "there are " + overlapKeypoints1.size() + " points to match");
		int matched = 0;
		Mat mat1 = Highgui.imread(img1.path);
		Mat mat2 = Highgui.imread(img2.path);
		Mat mat = new Mat(mat1.rows(), mat1.cols()*2-1, mat1.type());
		for (int i = 0; i < mat.height(); i++){
			for(int j = 0; j < mat.width(); j++){
				if (j < mat1.width()){
					mat.put(i, j, mat1.get(i, j));
				}else{
					mat.put(i, j, mat2.get(i, j-mat2.width()+1));
				}
			}
		}
		for (int i = 0; i < overlapKeypoints1.size(); i++){
			//vezmu rozmezi pixelu odpovidajici okoli +-constantEpsilon keypointu
			//spocitam tomu odpovidajici souradnice v img2
			//prochazim packety img2 a u tech, kterych se correspondujici rozmezi tyka, projdu keypointy
			//u kazdeho keypointu zase zkontroluji, jestli se nalezaji v danem okoli
			//pokud je v danem okoli, porovnam ho s drive nalezenym -> pokud je "blize" (descriptor je podobnejsi), zapamatuj si ho jako korespondujici
			
			keypoint = overlapKeypoints1.get(i);
			
			Core.circle(mat, keypoint.keypoint.pt, 5, new Scalar(0.0, 0.0, 1.0));
			
			xTop = keypoint.keypoint.pt.x - constantEpsilon;
			xBottom = keypoint.keypoint.pt.x + constantEpsilon;
			yTop = keypoint.keypoint.pt.y - constantEpsilon;
			yBottom = keypoint.keypoint.pt.y + constantEpsilon;
			
			double deltaTopX = xTop - overlap[0] + (overlap [4] - 1);
			double deltaBottomX = xBottom - overlap[0] + (overlap [4] - 1);
			double deltaTopY = yTop - overlap[1] + (overlap [5] - 1);
			double deltaBottomY = yBottom - overlap[1] + (overlap [5] - 1);
			
			corresponding_xTop = overlap[2] + deltaTopX;
			corresponding_xBottom = overlap[2] + deltaBottomX;
			corresponding_yTop = overlap[3] + deltaTopY;
			corresponding_yBottom = overlap[3] + deltaBottomY;
			
			Point a = new Point(corresponding_xTop, corresponding_yTop);
			Point b = new Point(corresponding_xBottom, corresponding_yTop);
			Point c = new Point(corresponding_xBottom, corresponding_yBottom);
			Point d = new Point(corresponding_xTop, corresponding_yBottom);
			
			
			Packet p;
			double distance = 1000;
			KeyPointStructure correspondingKeypoint = null;
			/*projdu vsechny packety druheho obrazku*/
			for (int j = 0; j < overlapPackets2.size(); j++){
				p = overlapPackets2.get(j);
				topCorner = p.topCorner;
				bottomCorner = p.bottomCorner;
				/* pokud alespon jeden roh korespondujici oblasti spada do packetu, musim ho prosetrit */
				if (isPointIn(topCorner, bottomCorner, a) || isPointIn(topCorner, bottomCorner, b) ||
					isPointIn(topCorner, bottomCorner, c) || isPointIn(topCorner, bottomCorner, d)){
					
					/* prochazim vsechny keypointy nachazejici se uvnitr aktualniho packetu */
					for (int k = 0; k < p.keypointsData.size(); k++){
						KeyPointStructure kp = p.keypointsData.get(k);
						/* pokud bod kp lezi uvnitr korespondujici oblasti, uvazim ho jako potencionalni korespondujici bod */
						if (isPointIn(a, c, kp.keypoint.pt)){
							//vzdalenost od keypoint
							double current_distance = getKeyPointDistance(keypoint.descriptor, kp.descriptor);
							if (current_distance < distance){
								correspondingKeypoint = kp;
								//Log.d(TAG, "corresponding point");
								distance = current_distance;
							}
						}
					}
					
				}
			}
			keypoint.correspondingKeypoint = correspondingKeypoint;
			if (correspondingKeypoint != null){
				matched++;
				Point point = new Point(correspondingKeypoint.keypoint.pt.x + mat1.width(), correspondingKeypoint.keypoint.pt.y);
				Core.circle(mat, point, 5, new Scalar(0.0, 0.0, 1.0));
				Core.line(mat, keypoint.keypoint.pt, point, new Scalar(0.0, 0.0, 1.0));
			}
		}
		Log.d(TAG, matched + " points were matched");
		imgForVisualization = mat;
		String dirPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/" + "Gallery";// "ScaledData";
		String filePath = dirPath + "/imgKeyPoints.jpg";
		if (Highgui.imwrite(filePath, mat)){
			Log.d(TAG, "img was written");
		}

	}
	
	private double getKeyPointDistance(ArrayList<Double> keypointDescriptor, ArrayList<Double> correspondingKeypointDescriptor){
		double distance;
		if (keypointDescriptor.size() != correspondingKeypointDescriptor.size()){
			return 1000d;
		}
		double sum = 0;
		for (int i = 0; i < keypointDescriptor.size(); i++){
			sum += Math.pow( (keypointDescriptor.get(i) - correspondingKeypointDescriptor.get(i)), 2);
		}
		distance = Math.sqrt(sum);
		return distance;
	}
	
	/*
	 * This method picks all packets and keypoints lying in the overlap.
	 */
	private void getOverlapPacketsAndKeypoints(ImageStructure imgStr, Point topOverlapCorner, Point bottomOverlapCorner, ArrayList<Packet> overlapPackets, ArrayList<KeyPointStructure> overlapKeypoints){
		ArrayList<Packet> allPackets = imgStr.packets;
		Packet p;
		Point a, b, c, d;
		for (int i = 0; i < allPackets.size(); i++){
			p = allPackets.get(i);
			/* corners of current packet */
			a = new Point(p.topCorner.x, p.topCorner.y);
			b = new Point(p.topCorner.x, p.bottomCorner.y);
			c = new Point(p.bottomCorner.x, p.bottomCorner.y);
			d = new Point(p.bottomCorner.x, p.bottomCorner.y);
			/* if at least one of them lies in the overlap, we will need to test it */
			if (isPointIn(topOverlapCorner, bottomOverlapCorner, a) ||
				isPointIn(topOverlapCorner, bottomOverlapCorner, b) ||
				isPointIn(topOverlapCorner, bottomOverlapCorner, c) ||
				isPointIn(topOverlapCorner, bottomOverlapCorner, d)){
				
				overlapPackets.add(p);
				
				for (int k = 0; k < p.keypointsData.size(); k++){
					overlapKeypoints.add(p.keypointsData.get(k));
				}
				
			}
		}
	}
	
	
	private boolean isPointIn(Point topCorner, Point bottomCorner, Point p){
		return ( topCorner.x <= p.x && p.x <= bottomCorner.x &&
				 topCorner.y <= p.y && p.y <= bottomCorner.y);
	}
	
	private void createPackets(){
		ImageStructure imgStr;
		ArrayList<KeyPointStructure> keypoints = new ArrayList<KeyPointStructure>(); 
		for(int i = 0; i < imageData.size(); i++){
			keypoints = new ArrayList<KeyPointStructure>();
			
			imgStr = imageData.get(i);
			
			List<KeyPoint> listofkeypoints = imgStr.keyPoints.toList();
			Mat descriptors = imgStr.descriptors;
			
			for (int j = 0; j < listofkeypoints.size(); j++){
				Mat descriptor_row = descriptors.row(j);
				ArrayList<Double> descriptor = new ArrayList<Double>();
				for (int v = 0; v < descriptor_row.width(); v++){
					descriptor.add(descriptor_row.get(0, v)[0]);
				}
				keypoints.add(new KeyPointStructure(listofkeypoints.get(j), descriptor)); 
			}
			
			ArrayList<Packet> imgPackets = new ArrayList<Packet>();
			
			int deltaWidth = (imgStr.width / n);
			int deltaHeight = (imgStr.height / m);
			
			Packet packet;
			
			Point topCorner = new Point(0, 0);
			Point bottomCorner = new Point(deltaWidth-1, deltaHeight-1);
			for (int h = 0; h < m; h++){
				for (int w = 0; w < n; w++){
					if (w == n-1){
						bottomCorner.x = imgStr.width-1;
					}
					if (h == m-1){
						bottomCorner.y = imgStr.height-1;
					}
					
					//choose keypoints lying in this packet
					ArrayList<KeyPointStructure> packetKeypoints = new ArrayList<KeyPointStructure>();
					ArrayList<KeyPointStructure> rest = new ArrayList<KeyPointStructure>();
					for(int k = 0; k < keypoints.size(); k++){
						KeyPointStructure keypoint = keypoints.get(k);
						double x = keypoint.keypoint.pt.x;
						double y = keypoint.keypoint.pt.y;
						if (topCorner.x <= x && bottomCorner.x >= x && topCorner.y <= y && bottomCorner.y >= y){
							packetKeypoints.add(keypoint);
						}else{
							rest.add(keypoint);
						}
					}
					
					packet = new Packet();
					packet.topCorner = topCorner;
					packet.bottomCorner = bottomCorner;
					packet.keypointsData = packetKeypoints;
					imgPackets.add(packet);
					
					topCorner = new Point(bottomCorner.x+1, topCorner.y);
					bottomCorner = new Point(bottomCorner.x + deltaWidth, bottomCorner.y);
					keypoints = rest;
				}
				topCorner = new Point(0, bottomCorner.y + 1);
				bottomCorner = new Point(deltaWidth-1, bottomCorner.y + deltaHeight);
			}
			
			Log.d(TAG, "it remained "+keypoints.size()+" unclassified keypoints");
			imgStr.packets = imgPackets;
		}
	}
}
