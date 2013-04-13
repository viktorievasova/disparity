package com.vasova.bachelorproject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Range;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import android.os.Environment;
import android.util.Log;

public class Registration {
	
	public int SCALE4 = 4;
	public int SCALE8 = 8;
	public int SCALE16 = 16;
	public int SCALE32 = 32;
	public int SCALE80 = 80;
	public int SCALE160 = 160;
	public int SCALE320 = 320;
	
	
	public ArrayList<int[]> register(ArrayList<String> pictures){
		int[] scales = {SCALE80, SCALE160};
		
		ArrayList<int[]> overlaps = new ArrayList<int[]>();
		for (int i = 0; i < pictures.size(); i++){
			Mat img = Highgui.imread(pictures.get(i));
			for (int j = i; j < pictures.size(); j++){
				if(j == i){
					continue;
				}
				System.out.println("matching picture " + i + " with picture " + j);
				
				Mat img1 = Highgui.imread(pictures.get(j));
				int[] overlap = findOverlap(img, img1, scales);
				overlaps.add(overlap);
			}
		}
		return overlaps;
	}
	
	public Mat createBrightnessMat(Mat mat){
		Mat m = new Mat(new Size(mat.width(), mat.height()), mat.type());
		for(int i = 0; i < mat.height(); i++){
			for(int j = 0; j < mat.width(); j++){
				double[] v = mat.get(i, j);
				double b = getBrightness(v);
				double[] vector = {b, b, b};
				m.put(i, j, vector);
			}
		}
		return m;
	}
	  

	
	private double getBrightness(double[] v){
		double [] vector = v;
		double brightness = Math.sqrt( (0.241 * Math.pow(vector[0], 2)) + 
				(0.691 * Math.pow(vector[1], 2)) + 
				(0.068 * Math.pow(vector[2], 2)) );
		
		return brightness;
		//return v[0];
	}
	
	/*
	 * matching two pictures over each other
	 * scales are in ascending order 
	 */
	public int[] findOverlap(Mat m1, Mat m2, int[] scales){
		double[] A;
		double[] B;
		double[] C;
		double[] D;
		int X1 = -1; int Y1 = -1; int X2 = -1; int Y2 = -1; 
		int U1 = -1; int V1 = -1; int U2 = -1; int V2 = -1;
		//for each scale find the best matching:
		for(int s = 0; s < scales.length; s++){
			int scale = scales[s];
			//m1 nescaleju, m2 scaluju
			int minX1 = 0;
			int minX2 = 0;
			
			int minY1 = 0;
			int minY2 = 0;

			int minU1 = 0;
			int minV1 = 0;
			
			int minU2 = 0;
			int minV2 = 0;
			
			double minValue = 1000;
			
			Mat mat1 = new Mat();
			Imgproc.integral(m1, mat1);
			
			Mat mat2 = new Mat();
			Imgproc.integral(m2, mat2);
			
			int d = m2.width()/scale;	//d*d = pocet pixelu v jednom vzorku
			int newWidth = scale;
			int newHeight= m1.height()/d;
			//posouvani obrazu po sobe
			for(int i = 1; i < 2*newWidth; i++){
				for(int j = 1; j < 2*newHeight; j++){
					//souradnice udavajici obdelnik prekryvane plochy na prvnim obrazku:
					//[x1,y1] - pravy dolni roh, [x2, y2] - levy horni roh
					int x1 = mat2.width() - 1;
					int y1 = mat2.height() - 1;
					int x2;
					int y2;
					
					//souradnice udavajici obdelnik prekryvane plochy na druhem obrazku:
					//[u1,v1] - pravy dolni roh, [u2, v2] - levy horni roh
					int u1 = i*d;
					int v1 = j*d;
					int u2;
					int v2;
					
					if(i - newWidth < 0){
						x2 = ((newWidth - i)*d)+1;
						u2 = 1;
					}else if (i > newWidth){
						x1 = (2*newWidth - i)*d;
						x2 = 1;//i - newWidth;
						u1 = newWidth*d;
						u2 = (i-newWidth)*d+1;
					}else{
						x2 = 1;
						u2 = (i - newWidth)*d + 1;
					}
					
					if(j - newHeight < 0){
						y2 = ((newHeight - j)*d)+1;
						v2 = 1;
					}else if (j > newHeight){
						y1 = (2*newHeight - j)*d;
						y2 = 1;//j - newHeight;
						v1 = newHeight*d;
						v2 = (j-newHeight)*d+1;
					}else{
						y2 = 1;
						v2 = (j - newHeight)*d + 1;
					}
					
					int isCovered = (Math.abs(x1 - x2)+1)*(Math.abs(y1 - y2)+1);
					//!!! integralMat.width = original,width+1 !!!
					double brightness1;
					double brightness2;
					A = mat1.get(v1, u1);
					B = mat1.get(v2-1, u1);
					C = mat1.get(v1, u2-1);
					D = mat1.get(v2-1, u2-1);
					double[] vector1 = {A[0]-B[0]-C[0]+D[0], A[1]-B[1]-C[1]+D[1], A[2]-B[2]-C[2]+D[2]};
					brightness1 = (getBrightness(vector1));
					
					A = mat2.get(y1, x1);
					B = mat2.get(y2-1, x1);
					C = mat2.get(y1, x2-1);
					D = mat2.get(y2-1, x2-1);
					double[] vector2 = {(A[0]-B[0]-C[0]+D[0]), (A[1]-B[1]-C[1]+D[1]), (A[2]-B[2]-C[2]+D[2])};
					brightness2 = getBrightness(vector2);
					
					double diff = (brightness1 - brightness2)/(isCovered);
					if(Math.abs(diff) < minValue){
						minValue = Math.abs(diff);
						//b1 = brightness1;
						minX1 = x1;
						minX2 = x2;
						minY1 = y1;
						minY2 = y2;
						//b2 = brightness2;
						minU1 = u1;
						minU2 = u2;
						minV1 = v1;
						minV2 = v2;
						
					}
				}
				}
			
			
			if(U1 != -1){
				int[] shift = shift(U1, V1, U2, V2, minU1, minV1, minU2, minV2, scale/scales[s-1], m1.width(), m1.height());
				//int[] shiftXY = shift(X1, Y1, X2, Y2, minX1, minY1, minX2, minY2, scale/scales[s-1], m1.width(), m1.height());
				U1 = shift[0];
				V1 = shift[1];
				U2 = shift[2];
				V2 = shift[3];
			}else{
				X1 = minX1; Y1 = minY1;
				X2 = minX2; Y2 = minY2;
				U1 = minU1; V1 = minV1;
				U2 = minU2; V2 = minV2;
			}
		}
		
		int[] result = {U1, V1, U2, V2, X1, Y1, X2, Y2};
		return result;
		
	}
	
	private int[] shift(int x1, int y1, int x2, int y2, int newx1, int newy1, int newx2, int newy2, int deltaScale, int w, int h){
		int deltaWidth = x1 - x2;
		int deltaHeight = y1 - y2;
		
		
		
		deltaScale -= 1;
		if ((x1 + deltaScale*deltaWidth < w) && (y1 + deltaScale*deltaHeight < h)){
			x1 += (deltaScale * deltaWidth);
			y1 += (deltaScale * deltaHeight);
		}else if ((x2 + deltaScale*deltaWidth < w) && (y2 + deltaScale*deltaHeight < h)){
			x2 -= (deltaScale * deltaWidth);
			y2 -= (deltaScale * deltaHeight);
		}else{
			x1 += (deltaScale * deltaWidth)/2;
			x2 -= (deltaScale * deltaWidth)/2;
			y1 += (deltaScale * deltaHeight)/2;
			y2 -= (deltaScale * deltaHeight)/2;
		}
		deltaScale++;
		int m = Math.max(x1, newx1);
		int rx1 = m - (Math.abs(x1 - newx1)/2);
		m = Math.max(y1, newy1);
		int ry1 = m - (Math.abs(y1 - newy1)/2);
		m = Math.max(x1, newx1);
		int rx2 = rx1 - deltaScale*deltaWidth;
		System.out.println("scale * deltaW: " + deltaScale*deltaWidth);
		m = Math.max(y1, newy1);
		int ry2 = ry1 - deltaScale*deltaHeight;
		System.out.println("scale * deltaH: " + deltaScale*deltaHeight);
		System.out.println("got: [" + x1 + "," + y1 + ";" + x2 +"," + y2 +"] [" + newx1 + "," + newy1 + ";" + newx2 +"," + newy2 +"]");
		int[] result = {rx1, ry1, rx2, ry2};
		System.out.println("result is: [" + rx1 + "," + ry1 + ";" + rx2 +"," + ry2 +"] ");
		
		return result;
	}
	
	
	public Mat scale(int scale, Mat mat){
		int width = mat.width();
		int height = mat.height();
		//width...x...320
		//height....y...240
		int d = width/scale;
		
		int newWidth = scale;
		int newHeight= height/d;// pro sirku 8 bude y = 6
		
		
		int numofBlocksWidth = newWidth;
		int numofBlocksHeight = newHeight;
		
		Mat integralImage = new Mat();
		Imgproc.integral(mat, integralImage);
		
		Mat scaledMat = new Mat(new Size(newWidth, newHeight), CvType.CV_32FC3);
		
		for (int i = 1; i <= numofBlocksWidth; i++){
			for (int j = 1; j <= numofBlocksHeight; j++){
				double[] A = integralImage.get(j*d, i*d);
				double[] B = integralImage.get(j*d, i*d - d);
				double[] C = integralImage.get(j*d - d, i*d);
				double[] D = integralImage.get(j*d - d, i*d - d);
				
				double[] v = {A[0]-B[0]-C[0]+D[0], A[1]-B[1]-C[1]+D[1], A[2]-B[2]-C[2]+D[2]};
				double b = getBrightness(v);// /3;
				//double b = getBrightness(integralImage.get((j+1)*d, (i+1)*d))/((i+1)*(j+1));
				double k = (d-1)*(d-1) * 4;
				float [] data = {(float)(v[0]/k), (float)(v[1]/k), (float)(v[2]/k)};// {b};
				//float [] data = {(float)b};
				scaledMat.put(j-1, i-1, data);
			}
		}
		return scaledMat;	
		
	}
	
}

