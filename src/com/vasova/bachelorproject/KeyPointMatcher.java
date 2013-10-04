package com.vasova.bachelorproject;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import android.util.Log;

/**
 * This class provides methods for detection and matching the SURF keypoints.
 * @author viktorievasova
 *
 */
public class KeyPointMatcher {
	
	private String TAG = "KeyPointMatcher";
	private ArrayList<ImageStructure> imageData;	
	private double n;
	private double m;
	private KeyPointStructure mostStableKeypoint = null;
	private float theStrongestResponse = -1f;
	
	/**
	 * Sets the input data-set of images.
	 * @param imgData an ArrayList of the input images.
	 */
	public void setData(ArrayList<ImageStructure> imgData){
		this.imageData = imgData;
		double width = imageData.get(0).width;
		double height = imageData.get(0).height;
		double max = Math.max(width, height);
		if (max == height){
			m = 10;
			double size = height/10;
			n = width/size;
		}else{
			n = 10;
			double size = width/10;
			m = height/size;
		}
	}
	
	/**
	 * Returns an updated version of input data-set of images.
	 * @return an updated ArrayList of input-images.
	 */
	public ArrayList<ImageStructure> getData(){
		return this.imageData;
	}
	
	/**
	 * This function starts the process of matching keyoints 
	 * between each two neighbouring ImageStructures in the imageData ArrayList.
	 * It extracts the keypoints, regularly split images into buckets and match the keypoints.  
	 */
	public void match(){
		extractKeyPoints(1000);
		ImageStructure imgStr1, imgStr2;
		createBuckets();
		for(int i = 0; i < imageData.size() - 1; i++){
			imgStr1 = imageData.get(i);
			imgStr2 = imageData.get(i+1);
			matchAPair(imgStr1, imgStr2);
		}
	}
	
	/**
	 * This method matches keypoints between two ImageStructures given as parametrs.
	 * At first, it detects the vector indicating the shift of the images (their relative position).
	 * Then for each keypoint of img1 is found the best match located in the img2 in 
	 * the corresponding surrounding of the keypoint of img1 given by the information of image shift calculated before.
	 * It uses the devision into buckets of the image. 
	 * 
	 * @param img1 first image.
	 * @param img2 second image.
	 */
	private void matchAPair(ImageStructure img1, ImageStructure img2){
		Point shiftdirection = getDirectionOfShift(img1, img2, 4000);
		if (shiftdirection == null){
			extractKeyPoints(500);
			createBuckets();
			shiftdirection = getDirectionOfShift(img1, img2, 700);
		}
		matchKeypoints(img1, img2, shiftdirection);
	}
	
	/**
	 * Returns a vector indicating the shift between the keypoint and the corresponding keypoint.
	 * @param keypoint the keypoint.
	 * @return a shift of between a point and it's corresponding point.
	 */
	private Point getShiftDirection(KeyPointStructure keypoint){
		Point vector = new Point();
		double v1 = (keypoint.correspondingKeypoint.keypoint.pt.x - keypoint.keypoint.pt.x);
		double v2 = (keypoint.correspondingKeypoint.keypoint.pt.y - keypoint.keypoint.pt.y);
		vector.x = v1;
		vector.y = v2;
		return vector;
	}
	
	/**
	 * This method iterates all keypoints of img1 with the response higher than is the threshold value,
	 * matches them and based on a match with the highest response is calculated a vector indicating 
	 * the shift of images img1 and img2.
	 * @param img1 the first image.
	 * @param img2 the second image.
	 * @param threshold the hessian threashold.
	 * @return a shift between the img1 and img2.
	 */
	private Point getDirectionOfShift(ImageStructure img1, ImageStructure img2, int threshold){
		double[] overlap = img1.relative_position_with_neighbour;
		Point topCorner;
		Point bottomCorner;
		
		topCorner = new Point(overlap[0]- (overlap[4] - 1), overlap[1] - (overlap[5] - 1));
		bottomCorner = new Point(overlap[0], overlap[1]);
		ArrayList<Bucket> overlapbuckets1 = new ArrayList<Bucket>();
		ArrayList<KeyPointStructure> overlapKeypoints1 = new ArrayList<KeyPointStructure>();
		getOverlapbucketsAndKeypoints(img1, topCorner, bottomCorner, overlapbuckets1, overlapKeypoints1);
		
		topCorner = new Point(overlap[2], overlap[3]);
		bottomCorner = new Point(overlap[2] + (overlap[4] - 1), overlap[3] + (overlap[5] - 1));
		ArrayList<Bucket> overlapbuckets2 = new ArrayList<Bucket>();
		ArrayList<KeyPointStructure> overlapKeypoints2 = new ArrayList<KeyPointStructure>();
		getOverlapbucketsAndKeypoints(img2, topCorner, bottomCorner, overlapbuckets2, overlapKeypoints2);
		
		KeyPointStructure keypoint;
		
		double corresponding_xTop, corresponding_xBottom, corresponding_yTop, corresponding_yBottom;
		double constantEpsilon = 30d;
		int matched = 0;		
		for (int i = 0; i < overlapKeypoints1.size(); i++){
			keypoint = overlapKeypoints1.get(i);
			if (keypoint.keypoint.response < threshold){
				continue;
			}
			double deltaX = keypoint.keypoint.pt.x - (overlap[0] - overlap[4]); 
			double deltaY = keypoint.keypoint.pt.y - (overlap[1] - overlap[5]);
			
			double corresponding_x = overlap[2] + deltaX;
			double corresponding_y = overlap[3] + deltaY;
			
			corresponding_xTop = corresponding_x - constantEpsilon;
			corresponding_xBottom = corresponding_x + constantEpsilon;
			corresponding_yTop = corresponding_y - constantEpsilon;
			corresponding_yBottom = corresponding_y + constantEpsilon;
			
			Point a = new Point(corresponding_xTop, corresponding_yTop);
			Point c = new Point(corresponding_xBottom, corresponding_yBottom);
			
			Bucket p;
			double descriptor_distance1 = 1000;
			double descriptor_distance2 = 1000;
			KeyPointStructure correspondingKeypoint = null;
			KeyPointStructure correspondingKeypoint2 = null;
			Point bucketCorner1, bucketCorner2, bucketCorner3, bucketCorner4;
			for (int j = 0; j < overlapbuckets2.size(); j++){
				p = overlapbuckets2.get(j);
				bucketCorner1 = p.topCorner;
				bucketCorner2 = new Point(p.topCorner.x, p.bottomCorner.y);
				bucketCorner3 = p.bottomCorner;
				bucketCorner4 = new Point(p.bottomCorner.x, p.topCorner.y);
				if (isPointInARectangle(a, c, bucketCorner1) || isPointInARectangle(a, c, bucketCorner2) ||
						isPointInARectangle(a, c, bucketCorner3) || isPointInARectangle(a, c, bucketCorner4)){
					for (int k = 0; k < p.keypointsData.size(); k++){
						KeyPointStructure kp = p.keypointsData.get(k);
						if (isPointInARectangle(a, c, kp.keypoint.pt)){
							double current_descriptor_distance = getDescriptorDistance(keypoint.descriptor, kp.descriptor);
							if (current_descriptor_distance < descriptor_distance1){
								correspondingKeypoint = kp;
								if (correspondingKeypoint2 == null){
									correspondingKeypoint2 = kp;
								}
								descriptor_distance1 = current_descriptor_distance;
							}else if (current_descriptor_distance < descriptor_distance2){
								correspondingKeypoint2 = kp;
								descriptor_distance2 = current_descriptor_distance;
							}
						}
					}
					
				}
			}
			
			if (correspondingKeypoint != null && correspondingKeypoint2 != null){
				if (descriptor_distance2 < 1.6*descriptor_distance1){
					correspondingKeypoint = null;
					correspondingKeypoint2 = null;
				}
			}
			
			if (correspondingKeypoint != null){
				if (getAngularDistance(keypoint.keypoint.angle, correspondingKeypoint.keypoint.angle) > 30){
					correspondingKeypoint = null;
					correspondingKeypoint2 = null;
				}
			}
			
			if (correspondingKeypoint != null){
				keypoint.correspondingKeypoint = correspondingKeypoint;
				double x1 = keypoint.keypoint.pt.x;
				double y1 = keypoint.keypoint.pt.y;
				double x2 = keypoint.correspondingKeypoint.keypoint.pt.x;
				double y2 = keypoint.correspondingKeypoint.keypoint.pt.y;
				keypoint.shift = new Point( x2-x1, y2-y1);
				float averageresponse = (keypoint.keypoint.response + keypoint.correspondingKeypoint.keypoint.response)/2;
				if (averageresponse > theStrongestResponse){
					mostStableKeypoint = keypoint;
					theStrongestResponse = averageresponse;
				}
				matched++;
			}
		}
		
		if (matched==0){
			return null;
		}
		return getShiftDirection(mostStableKeypoint);

	}
	
	/**
	 * This method finds the best match for every keypoint of img1 in img2.
	 * @param img1 the first image.
	 * @param img2 the second image.
	 * @param shiftdirection the vector defining the relative position between img1 and img2.
	 */
	private void matchKeypoints(ImageStructure img1, ImageStructure img2, Point shiftdirection){
		double[] overlap = img1.relative_position_with_neighbour;
		Point topCorner;
		Point bottomCorner;
		
		topCorner = new Point(overlap[0]- (overlap[4] - 1), overlap[1] - (overlap[5] - 1));
		bottomCorner = new Point(overlap[0], overlap[1]);
		ArrayList<Bucket> overlapbuckets1 = new ArrayList<Bucket>();
		ArrayList<KeyPointStructure> overlapKeypoints1 = new ArrayList<KeyPointStructure>();
		getOverlapbucketsAndKeypoints(img1, topCorner, bottomCorner, overlapbuckets1, overlapKeypoints1);
		
		topCorner = new Point(overlap[2], overlap[3]);
		bottomCorner = new Point(overlap[2] + (overlap[4] - 1), overlap[3] + (overlap[5] - 1));
		ArrayList<Bucket> overlapbuckets2 = new ArrayList<Bucket>();
		ArrayList<KeyPointStructure> overlapKeypoints2 = new ArrayList<KeyPointStructure>();
		getOverlapbucketsAndKeypoints(img2, topCorner, bottomCorner, overlapbuckets2, overlapKeypoints2);
		
		KeyPointStructure keypoint;
		
		for (int i = 0; i < overlapKeypoints1.size(); i++){
			keypoint = overlapKeypoints1.get(i);
			double corresponding_x = keypoint.keypoint.pt.x	+ shiftdirection.x;
			double corresponding_y = keypoint.keypoint.pt.y + shiftdirection.y;
			
			double widthOfRect = 60;
			double heightOfRect = 3*widthOfRect;
			
			Point V1, V2, V3, V4;
			Point iV1, iV2, iV3, iV4; // inner
			
			double norm = Math.sqrt(Math.pow(shiftdirection.x, 2) + Math.pow(shiftdirection.y, 2));
			
			Point h = new Point(shiftdirection.x/norm * heightOfRect, shiftdirection.y/norm * heightOfRect);
			Point w = new Point(-1 * shiftdirection.y/norm * widthOfRect, shiftdirection.x/norm * widthOfRect);
			double constw = 0.5;
			double consth = 0.5;
			V1 = new Point(corresponding_x + 	constw*w.x + consth*h.x, corresponding_y + constw*w.y + consth*h.y);
			//cV1 = new Point(V1.x + orientedrectangles.cols()/2, V1.y);
			
			V2 = new Point(corresponding_x - constw*w.x + consth*h.x, corresponding_y - constw*w.y + consth*h.y);
			//cV2 = new Point(V2.x + orientedrectangles.cols()/2, V2.y);
			
			V3 = new Point(corresponding_x - constw*w.x - consth*h.x, corresponding_y - constw*w.y - consth*h.y );
			//cV3 = new Point(V3.x + orientedrectangles.cols()/2, V3.y);
			
			V4 = new Point(corresponding_x + constw*w.x - consth*h.x, corresponding_y + constw*w.y - consth*h.y);
			//cV4 = new Point(V4.x + orientedrectangles.cols()/2, V4.y);
			
			//inner
			constw = 0.1;
			//constw = 0.2;
			consth = 0.2;
			//consth = 0.35;
			iV1 = new Point(corresponding_x + 	constw*w.x + consth*h.x, corresponding_y + constw*w.y + consth*h.y);
			//ciV1 = new Point(iV1.x + orientedrectangles.cols()/2, iV1.y);
			
			iV2 = new Point(corresponding_x - constw*w.x + consth*h.x, corresponding_y - constw*w.y + consth*h.y);
			//ciV2 = new Point(iV2.x + orientedrectangles.cols()/2, iV2.y);
			
			iV3 = new Point(corresponding_x - constw*w.x - consth*h.x, corresponding_y - constw*w.y - consth*h.y );
			//ciV3 = new Point(iV3.x + orientedrectangles.cols()/2, iV3.y);
			
			iV4 = new Point(corresponding_x + constw*w.x - consth*h.x, corresponding_y + constw*w.y - consth*h.y);
			//ciV4 = new Point(iV4.x + orientedrectangles.cols()/2, iV4.y);
			
			
			Bucket p;
			double descriptor_distance1 = 1000;
			double descriptor_distance2 = 1000;
			KeyPointStructure correspondingKeypoint = null;
			KeyPointStructure correspondingKeypoint2 = null;
			Point bucketCorner1, bucketCorner2, bucketCorner3, bucketCorner4;
			double[] values;
			values = new double[]{V1.x, V2.x, V3.x, V4.x};
			double minX = findMin(values);
			double maxX = findMax(values);
			values = new double[]{V1.y, V2.y, V3.y, V4.y};
			double minY = findMin(values);
			double maxY = findMax(values);
			Point boundingBoxTopCorner = new Point(minX, minY);
			Point boundingBoxBottomCorner = new Point(maxX, maxY);
			
			/*projdu vsechny buckety druheho obrazku*/
			for (int j = 0; j < overlapbuckets2.size(); j++){
				p = overlapbuckets2.get(j);
				bucketCorner1 = p.topCorner;
				bucketCorner2 = new Point(p.topCorner.x, p.bottomCorner.y);
				bucketCorner3 = p.bottomCorner;
				bucketCorner4 = new Point(p.bottomCorner.x, p.topCorner.y);
				/* pokud alespon jeden roh boundingBoxu spada do bucketu, musim ho prosetrit */
				if (isPointInARectangle(boundingBoxTopCorner, boundingBoxBottomCorner, bucketCorner1) || isPointInARectangle(boundingBoxTopCorner, boundingBoxBottomCorner, bucketCorner2) ||
						isPointInARectangle(boundingBoxTopCorner, boundingBoxBottomCorner, bucketCorner3) || isPointInARectangle(boundingBoxTopCorner, boundingBoxBottomCorner, bucketCorner4)){
					/* prochazim vsechny keypointy nachazejici se uvnitr aktualniho bucketu */
					for (int k = 0; k < p.keypointsData.size(); k++){
						KeyPointStructure kp = p.keypointsData.get(k);
						// test point vs inner oriented rectangle
						/* pokud bod kp lezi uvnitr korespondujici oblasti, uvazim ho jako potencionalni korespondujici bod */
						if (isPointInAnOrientedRectangle(new Point[] {iV1, iV2, iV3, iV4}, kp.keypoint.pt)){
							//vzdalenost od keypoint
							//Core.circle(matching, new Point(kp.keypoint.pt.x + matching.cols()/2, kp.keypoint.pt.y), 2,  color);
							double current_descriptor_distance = getDescriptorDistance(keypoint.descriptor, kp.descriptor);
							if (current_descriptor_distance < descriptor_distance1){
								correspondingKeypoint = kp;
								if (correspondingKeypoint2 == null){
									correspondingKeypoint2 = kp;
								}
								descriptor_distance1 = current_descriptor_distance;
							}else if (current_descriptor_distance < descriptor_distance2){
								correspondingKeypoint2 = kp;
								descriptor_distance2 = current_descriptor_distance;
							}
						}
					}
					
				}
			
				// if the keypoint is not an obvious winer, do not accept the keypoint
				if (correspondingKeypoint != null && correspondingKeypoint2 != null){
					if (descriptor_distance2 < 1.6*descriptor_distance1){
						correspondingKeypoint = null;
						correspondingKeypoint2 = null;
					}
				}
				// if the angle differs too much, do not accept the keypoint
				if (correspondingKeypoint != null){
					if (getAngularDistance(keypoint.keypoint.angle, correspondingKeypoint.keypoint.angle) > 30){
						correspondingKeypoint = null;
						correspondingKeypoint2 = null;
					}
				}
				
				if (correspondingKeypoint != null){
					keypoint.correspondingKeypoint = correspondingKeypoint;
					double x1 = keypoint.keypoint.pt.x;
					double y1 = keypoint.keypoint.pt.y;
					
					double x2 = keypoint.correspondingKeypoint.keypoint.pt.x;
					double y2 = keypoint.correspondingKeypoint.keypoint.pt.y;
					keypoint.shift = new Point( x2-x1, y2-y1);
				}
			}
		}
	}
	
	/**
	 * Returns the distance between two descriptors. 
	 * It uses the Euclidean metric.
	 * @param keypointDescriptor the first descriptor.
	 * @param correspondingKeypointDescriptor the second descriptor.
	 * @return the distance between the descriptors.
	 */
	private double getDescriptorDistance(ArrayList<Double> keypointDescriptor, ArrayList<Double> correspondingKeypointDescriptor){
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
	
	/**
	 * Returns the angular distance of two angles in degrees.
	 * @param a the first angle.
	 * @param b the second angle.
	 * @return the distance between angle a and b. 
	 */
	private double getAngularDistance(double a, double b) {
		double min = Math.min(a, b); 
		double max = Math.max(a, b);
		double r = Math.min( max - min, 360 + min - max );
		return r;
	}
	
	/**
	 * Finds a maximal value in the array.
	 * @param values array of double values.
	 * @return the maximal value.
	 */
	private double findMax(double [] values){
		if (values.length == 0){
			return -1000;
		}
		double max = values[0];
		for (int i = 1; i < values.length; i++){
			double v = values[i];
			if (v > max){
				max = v;
			}
		}
		return max;
	}
	/**
	 * Finds a minimal value in the array.
	 * @param values array of double values.
	 * @return the minimal value.
	 */
	private double findMin(double[] values){
		if (values.length == 0){
			return 1000;
		}
		double min = values[0];
		for (int i = 1; i < values.length; i++){
			double v = values[i];
			if (v < min){
				min = v;
			}
		}
		return min;
	}


	
	/**
	 * This method picks all buckets and keypoints lying in the overlap.
	 * @param imgStr the image.
	 * @param topOverlapCorner the top left corner defining the rectangle of the overlap.
	 * @param bottomOverlapCorner the bottom right corner defining the rectangle of the overlap. 
	 * @param overlapbuckets the buckets lying in the overlap.
	 * @param overlapKeypoints all keypoints lying in the overlap.
	 */
	 
	private void getOverlapbucketsAndKeypoints(ImageStructure imgStr, Point topOverlapCorner, Point bottomOverlapCorner, ArrayList<Bucket> overlapbuckets, ArrayList<KeyPointStructure> overlapKeypoints){
		ArrayList<Bucket> allbuckets = imgStr.buckets;
		Bucket p;
		Point a, b, c, d;
		for (int i = 0; i < allbuckets.size(); i++){
			p = allbuckets.get(i);
			/* corners of current bucket */
			a = new Point(p.topCorner.x, p.topCorner.y);
			b = new Point(p.topCorner.x, p.bottomCorner.y);
			c = new Point(p.bottomCorner.x, p.bottomCorner.y);
			d = new Point(p.bottomCorner.x, p.topCorner.y);
			/* if at least one of the corners lies in the overlap, we will need to test it */
			if (isPointInARectangle(topOverlapCorner, bottomOverlapCorner, a) ||
				isPointInARectangle(topOverlapCorner, bottomOverlapCorner, b) ||
				isPointInARectangle(topOverlapCorner, bottomOverlapCorner, c) ||
				isPointInARectangle(topOverlapCorner, bottomOverlapCorner, d)){
				
				overlapbuckets.add(p);
				
				for (int k = 0; k < p.keypointsData.size(); k++){
					overlapKeypoints.add(p.keypointsData.get(k));
				}
				
			}
		}
	}
	
	
	/**
	 * Returns true if the point p lies in the rectangle defined by the top and bottom corner.
	 * @param topCorner the top left corner of the rectangle.
	 * @param bottomCorner the bottom right corner of the rectangle.
	 * @param p the point.
	 * @return true if p lies in the rectangle, false if does not.
	 */
	private boolean isPointInARectangle(Point topCorner, Point bottomCorner, Point p){
		return ( topCorner.x <= p.x && p.x <= bottomCorner.x &&
				 topCorner.y <= p.y && p.y <= bottomCorner.y);
	}
	
	/**
	 * Returns true if the point p lies inside of the oriented rectangle defined by the top and bottom corner.
	 * @param rectangle an array of points representing the corners of the oriented rectangle.
	 * @param p the point.
	 * @return true if p lies in the rectangle, false if does not.
	 */
	private boolean isPointInAnOrientedRectangle(Point[] rectangle, Point p){
			Point a, b;
			int ontheleftside = 0;
			int ontherighttside = 0;
			for (int i = 0; i < 4; i++){
				a = rectangle[i];
				if (i == rectangle.length - 1){
					b = rectangle[0];
				}else{
					b = rectangle[i + 1];
				}
				// line defined by points a and b
				double x1, x2, y1, y2;
				x1 = a.x;
				x2 = b.x;
				y1 = a.y;
				y2 = b.y;
				
				double A = -(y2 - y1);
				double B = x2 - x1;
				double C = -(A * x1 + B * y1);
				
				double D = A * p.x + B * p.y + C;
				
				if (D > 0){
					//is on the left side ontheleftside
					ontheleftside ++;
				}else{
					//is on the right side
					ontherighttside ++;
				}
			}
			if (ontheleftside == 4 || ontherighttside == 4){
				return true;
			}
			return false;

		}
	
	/**
	 * For each imageStructure in the data-set of images it subdivides the image into n x m buckets.
	 * Each bucket is stored in Bucket structure which is defined by the top left and bottom right 
	 * and all keypoints lying in the region of the bucket are stored.  
	 */
	private void createBuckets(){
		ImageStructure imgStr;
		ArrayList<KeyPointStructure> keypoints = new ArrayList<KeyPointStructure>(); 
		for(int i = 0; i < imageData.size(); i++){
			imgStr = imageData.get(i);
			keypoints = imgStr.keyPoints;
			ArrayList<Bucket> imgbuckets = new ArrayList<Bucket>();
			double deltaWidth = (imgStr.width / n);
			double deltaHeight = (imgStr.height / m);
			
			Bucket bucket;
			
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
					
					//choose keypoints lying in this bucket
					ArrayList<KeyPointStructure> bucketKeypoints = new ArrayList<KeyPointStructure>();
					ArrayList<KeyPointStructure> rest = new ArrayList<KeyPointStructure>();
					for(int k = 0; k < keypoints.size(); k++){
						KeyPointStructure keypoint = keypoints.get(k);
						double x = keypoint.keypoint.pt.x;
						double y = keypoint.keypoint.pt.y;
						if (topCorner.x <= x && bottomCorner.x >= x && topCorner.y <= y && bottomCorner.y >= y){
							bucketKeypoints.add(keypoint);
						}else{
							rest.add(keypoint);
						}
					}
					
					bucket = new Bucket();
					bucket.topCorner = topCorner;
					bucket.bottomCorner = bottomCorner;
					bucket.keypointsData = bucketKeypoints;
					imgbuckets.add(bucket);
					
					topCorner = new Point(bottomCorner.x, topCorner.y);
					bottomCorner = new Point(bottomCorner.x + deltaWidth, bottomCorner.y);
					keypoints = rest;
				}
				topCorner = new Point(0, bottomCorner.y);
				bottomCorner = new Point(deltaWidth-1, bottomCorner.y + deltaHeight);
			}
			imgStr.buckets = imgbuckets;
		}
	}
	
	/**
	 * This method extracts keypoints for each image in the image data set that was set with setData() function before.
	 * Keypoints are detected and described with the SURF detector/descriptor, stored in an ArrayList and assigned to
	 * the current ImageStructure. 
	 * 
	 * @param hessianThreshold the hessian threshold.
	 */
	private void extractKeyPoints(int hessianThreshold){
		FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);		
		// setting the hessian threshold:
		try{
			File tempFile = File.createTempFile("config", ".yml");
			String settings = "%YAML:1.0\nhessianThreshold:"+hessianThreshold+".\noctaves: 3\noctaveLayers: 4\nupright: 0\n";
			FileWriter writer = new FileWriter(tempFile, false);
			writer.write(settings);
			writer.close();
			detector.read(tempFile.getPath());
		}catch(Exception e){
			Log.i(TAG, "can not wirte or read yaml file");
		}
		
		DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.SURF);
		ImageStructure imgStr;
		MatOfKeyPoint cKeypoints;
		Mat cImage;
		Mat cDescriptors;
		for(int i = 0; i < this.imageData.size(); i++){
			imgStr = this.imageData.get(i);
			cKeypoints = new MatOfKeyPoint();
			cImage = Highgui.imread(imgStr.path);
			
			detector.detect(cImage, cKeypoints);
			cDescriptors = new Mat();
			descriptor.compute(cImage, cKeypoints, cDescriptors);
			
			ArrayList<KeyPointStructure> keypoints = new ArrayList<KeyPointStructure>();
			KeyPoint[] allKeypoints = cKeypoints.toArray();
			
			for (int j = 0; j < allKeypoints.length; j++){
				ArrayList<Double> desc = new ArrayList<Double>();
				Mat descriptor_row = cDescriptors.row(j);
				for (int v = 0; v < descriptor_row.width(); v++){
					desc.add(descriptor_row.get(0, v)[0]);
				}
				KeyPointStructure point = new KeyPointStructure(allKeypoints[j], desc);
				keypoints.add(point);
			}
			imgStr.keyPoints = keypoints;
		}
	}
}
