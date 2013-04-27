package com.vasova.bachelorproject;

import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 

import javax.microedition.khronos.opengles.GL10;

import org.opencv.core.Mat;


public class MeshModel {
	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	private FloatBuffer colorBuffer;	// buffer holding the colors
	private float vertices[];
	private float colors[];

	private void makeVertices(Mat original, Mat disparity){
		
		int width = 320;//original.width();
		int heigth = 240;//original.height();
		
		int numOfSquares = width*heigth;
		int numOfTriangles = numOfSquares * 2;
		int numOfVertices = numOfTriangles * 3;
		
		vertices = new float[numOfVertices*3];
		colors = new float[numOfVertices*4];
		
		float k = 8f/width;
		
		float start_x = -4f;
		float start_y = 3f;
		
		float position_x = start_x;
		float position_y = start_y;
		int verticesIndex = 0;
		int colorsIndex = 0;
		
		float z;
		for (int h = 0; h < heigth; h++){
			for (int w = 0; w < width; w++){
				double[] v = disparity.get(h, w);
				z = (float)(v[0]/500);
				
				//first triangle
				vertices[verticesIndex++] = position_x;	// V1 - bottom left
				vertices[verticesIndex++] = position_y - k;
				vertices[verticesIndex++] = z;
				
				vertices[verticesIndex++] = position_x;	// V2 - top left
				vertices[verticesIndex++] = position_y;
				vertices[verticesIndex++] = z;
				
				vertices[verticesIndex++] = position_x + k;	//V3 - top right
				vertices[verticesIndex++] = position_y;
				vertices[verticesIndex++] = z;
				
				
				//second triangle
				vertices[verticesIndex++] = position_x;	//V1 - bottom left
				vertices[verticesIndex++] = position_y - k;
				vertices[verticesIndex++] = z;
				
				vertices[verticesIndex++] = position_x + k; //V3 - top right
				vertices[verticesIndex++] = position_y;
				vertices[verticesIndex++] = z;
				
				vertices[verticesIndex++] = position_x + k; //V4 - bottom right
				vertices[verticesIndex++] = position_y - k;
				vertices[verticesIndex++] = z;
				
				//colors for each vertex of the triangles (6 vertices)
				double[] color = original.get(h, w);//{255.0, 0.0, 0.0, 1};
				
				for(int i = 0; i < 6; i++){
					colors[colorsIndex++] = (float)(color[2]/255);
					colors[colorsIndex++] = (float)(color[1]/255);
					colors[colorsIndex++] = (float)(color[0]/255);
					colors[colorsIndex++] = 1f;
				}
				
				position_x += k;
				
			}
			position_y -= k;
			position_x = start_x;
		}
		
	}
	
	public MeshModel(Mat original, Mat disparity) {
		makeVertices(original, disparity);
		try{
			ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
			vertexByteBuffer.order(ByteOrder.nativeOrder());
			vertexBuffer = vertexByteBuffer.asFloatBuffer();
			vertexBuffer.put(vertices);
			vertexBuffer.position(0);
			
			
		    ByteBuffer colorByteBuffer = ByteBuffer.allocateDirect(colors.length * 4);
		    colorByteBuffer.order(ByteOrder.nativeOrder());
		    colorBuffer = colorByteBuffer.asFloatBuffer();
		    colorBuffer.put(colors);
		    colorBuffer.position(0);
			
		}catch(OutOfMemoryError me){
			// 
		}
	}

	/** The draw method for the triangle with the GL context */
	public void draw(GL10 gl) {
		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_COLOR_ARRAY);
		
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		//Point to color buffer
		gl.glColorPointer(4, GL10.GL_FLOAT, 0, colorBuffer);
		// Point to our vertex
		// Draw the vertices as triangles
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
					
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_COLOR_ARRAY);
	}
}
