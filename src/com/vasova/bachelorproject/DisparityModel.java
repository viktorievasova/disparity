package com.vasova.bachelorproject;

import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 

import javax.microedition.khronos.opengles.GL10;

import android.util.Log;

/**
 * A class representing a 3D model of a disparity map.
 *    
 * @author viktorievasova
 *
 */
public class DisparityModel {
	
	private String TAG = "DisparityModel";
	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	private FloatBuffer colorBuffer;	// buffer holding the colors
	private float vertices[];
	private float colors[];

	private ByteBuffer vertexByteBuffer; 
    private ByteBuffer colorByteBuffer;
    
    /**
     * The constructor of this class. It fills the FloatBuffers with vertices and colors given as parameters which are needed for rendering.
     * @param v an array of vertices. Each sequence of three floats represents the x, y and z coordinates of one point in the space.
	 * Each sequence of nine floats represents one triangle in a space.
     * @param c an array of color values. To each vertex correspond four values of this array representing a RGBA color.
     */
	public DisparityModel(float[] v, float[] c) {
		this.vertices = v;
		this.colors = c;
		try{
			vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
			vertexByteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = vertexByteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);
			
			colorByteBuffer = ByteBuffer.allocateDirect(colors.length * 4);
		    colorByteBuffer.order(ByteOrder.nativeOrder());
		    colorBuffer = colorByteBuffer.asFloatBuffer();
		    colorBuffer.put(colors);
		    colorBuffer.position(0);
		}catch(OutOfMemoryError me){
			Log.i(TAG, "out of memory eception: " + me.getMessage()); 
		}
	}

	/**
	 *  The draw method for the the disparity model with the GL context.
	 *  It draws all vertices buffered in the vertexBuffer and all colors in colorBuffer corresponding to the vertices.  
	 */
	public void draw(GL10 gl) {
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		//Point to color buffer
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		// Draw the vertices as triangles
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
		
	}
}
