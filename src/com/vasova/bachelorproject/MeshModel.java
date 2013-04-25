package com.vasova.bachelorproject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Mat;

public class MeshModel
{
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mColorBuffer;
    private FloatBuffer mNormalBuffer;
    private ShortBuffer mIndexBuffer;
    
    private FloatBuffer mVertexBuffer2;
    private FloatBuffer mColorBuffer2;
    private FloatBuffer mNormalBuffer2;
    private ShortBuffer mIndexBuffer2;
        
    private int faceCount = 0;

    private float start_position_x = -3.5f;
    private float start_position_y = 2.0f;

    private float[] coords;
    private float[] colcoords;
    private short[] icoords;
    
    private int width;
    private int height;
    private float k;
    
    private Mat originalImg; 
    private Mat disparityImage;
    
    public MeshModel(Mat origImage, Mat disparImage)
    {
    	this.originalImg = origImage;
    	this.disparityImage = disparImage;
    	width = 320;//originalImg.width();
    	height = 240;//originalImg.height();
    	System.out.println("width & height: " + width + " and " + height);
    	    	
    	k = (float)(7f/(float)width);
    	
    	
    	int num_faces = width * height * 2;	//width*height = #rectangle ; #face = 2*#rectangle
        int num_vertices = width * height * 4;
        faceCount = num_faces * 3; // three vertexes per face v1,v2,v3
                
        
        //coordinates:
        coords = new float[num_vertices*3];
        //colors:
        colcoords = new float[num_vertices*4];
        //edges:
        icoords = new short[width*height*6];         
        fillCoordAndColorArray();
        fillEdgesArray();
        mVertexBuffer = makeFloatBuffer(coords);
        mColorBuffer = makeFloatBuffer(colcoords);
        //mNormalBuffer = makeFloatBuffer(ncoords);
        mIndexBuffer = makeShortBuffer(icoords);
        
    }
    
    private void fillCoordAndColorArray(){
    	int colorArrayIndex = 0;
        int coordinateIndex = 0;
        float position_x = start_position_x;
		float position_y = start_position_y; 
		float z = 0f;
        
    	for (int j = 0; j < height; j++){
        	for(int i = 0; i < width; i++){
        		double[] disparity = disparityImage.get(j, i);
        		//z = (float)(disparity[0]/(100));
        		
        		coords[coordinateIndex++] = position_x;
        		coords[coordinateIndex++] = position_y;
        		coords[coordinateIndex++] = z;
        		
        		coords[coordinateIndex++] = position_x + k;
        		coords[coordinateIndex++] = position_y;
        		coords[coordinateIndex++] = z;
        		
        		coords[coordinateIndex++] = position_x;
        		coords[coordinateIndex++] = position_y - k;
        		coords[coordinateIndex++] = z;
        		
        		coords[coordinateIndex++] = position_x + k;
        		coords[coordinateIndex++] = position_y - k;
        		coords[coordinateIndex++] = z;
        		
    			double[] color = originalImg.get(j, i);
        		
    			colcoords[colorArrayIndex++] = (float)(color[2]/255);
        		colcoords[colorArrayIndex++] = (float)(color[1]/255);
        		colcoords[colorArrayIndex++] = (float)(color[0]/255);
        		colcoords[colorArrayIndex++] = 1.0f;
        		
        		colcoords[colorArrayIndex++] = (float)(color[2]/255);
        		colcoords[colorArrayIndex++] = (float)(color[1]/255);
        		colcoords[colorArrayIndex++] = (float)(color[0]/255);
        		colcoords[colorArrayIndex++] = 1.0f;
        		
        		colcoords[colorArrayIndex++] = (float)(color[2]/255);
        		colcoords[colorArrayIndex++] = (float)(color[1]/255);
        		colcoords[colorArrayIndex++] = (float)(color[0]/255);
        		colcoords[colorArrayIndex++] = 1.0f;
        		
        		colcoords[colorArrayIndex++] = (float)(color[2]/255);
        		colcoords[colorArrayIndex++] = (float)(color[1]/255);
        		colcoords[colorArrayIndex++] = (float)(color[0]/255);
        		colcoords[colorArrayIndex++] = 1.0f;
        		
		    	if (i == width - 1){
        			position_y -= k;
        			position_x = start_position_x;
        		}else{
        			position_x += k;	
        		}
        	}
        }
    	System.out.println("num of colors: " + colorArrayIndex);
    	System.out.println("num of coords: " + coordinateIndex);
    	
    }
    
    private void fillEdgesArray(){
    	int edgeIndex = 0;
        for (int j = 0; j < width*height; j++){
        	int index = j*4;
        	icoords[edgeIndex++] = (short)(index);
        	icoords[edgeIndex++] = (short)(index+1);
        	icoords[edgeIndex++] = (short)(index+2);
        	
        	icoords[edgeIndex++] = (short)(index+1);
        	icoords[edgeIndex++] = (short)(index+2);
        	icoords[edgeIndex++] = (short)(index+3);
        }
        System.out.println("edgeIndex: " + edgeIndex);
    }

    private FloatBuffer makeFloatBuffer(float[] arr)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(arr);
        fb.position(0);
        return fb;
    }

    private ShortBuffer makeShortBuffer(short[] arr)
    {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer ib = bb.asShortBuffer();
        ib.put(arr);
        ib.position(0);
        return ib;
    }

    public void draw(GL10 gl)
    {
		gl.glFrontFace(GL10.GL_CCW);
        gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
        
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        //gl.glNormalPointer(GL10.GL_FLOAT, 0, mNormalBuffer);
        //gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, mTexBuffer);
        gl.glDrawElements(GL10.GL_TRIANGLES, faceCount, GL10.GL_UNSIGNED_SHORT, mIndexBuffer);
    	
    }
}