package com.vasova.bachelorproject;

import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 

import javax.microedition.khronos.opengles.GL10;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLUtils;

public class Square {
	private FloatBuffer vertexBuffer;	// buffer holding the vertices
	private FloatBuffer colorBuffer;	// buffer holding the colors
	private float vertices[];
	private float colors[];

	private FloatBuffer textureBuffer;  // buffer holding the texture coordinates
	private float texture[];

	private Mat originalImage;
	private Mat disparityImage;
	
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
		float end_x = -4f + (k*width);
		float end_y = 3f - (k*heigth);
		
		float position_x = start_x;
		float position_y = start_y;
		int verticesIndex = 0;
		int colorsIndex = 0;
		
		float z;
		for (int h = 0; h < heigth; h++){
			for (int w = 0; w < width; w++){
				double[] v = disparity.get(h, w);
				z = (float)(v[0]/100);
				double[] color = original.get(h, w);
				
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
				
				//colors for each vertex of first triangle
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
				
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
				
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
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
				
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
				
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
				
				colors[colorsIndex++] = (float)(color[2]/255);
				colors[colorsIndex++] = (float)(color[1]/255);
				colors[colorsIndex++] = (float)(color[0]/255);
				colors[colorsIndex++] = 1f;
				position_x += k;
				
			}
			position_y -= k;
			position_x = start_x;
		}
		
		texture = new float[8];
		texture[0] = start_x;
		texture[1] = start_y;     // top left    
		
		texture[2] = start_x; 
		texture[3] = end_y;     // bottom left 
		
		texture[4] = end_x;
		texture[5] = start_y;     // top right
		
		texture[6] = end_x; 
		texture[7] = end_y;      // bottom right
		
	}
	
	public Square(Mat original, Mat disparity) {
		this.disparityImage = disparity;
		this.originalImage = original;
		makeVertices(original, disparity);
		try{
			// a float has 4 bytes so we allocate for each coordinate 4 bytes
			ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
			vertexByteBuffer.order(ByteOrder.nativeOrder());
			
			// allocates the memory from the byte buffer
			vertexBuffer = vertexByteBuffer.asFloatBuffer();
			
			// fill the vertexBuffer with the vertices
			vertexBuffer.put(vertices);
			
			// set the cursor position to the beginning of the buffer
			vertexBuffer.position(0);
			
		    ByteBuffer byteBuffer = ByteBuffer.allocateDirect(texture.length * 4);
		    byteBuffer.order(ByteOrder.nativeOrder());
		    textureBuffer = byteBuffer.asFloatBuffer();
		    textureBuffer.put(texture);
		    textureBuffer.position(0);

		}catch(OutOfMemoryError me){
			// 
		}
	}

	private int[] textures = new int[1];
	
	public void loadGLTexture(GL10 gl, Context context) {
	    // loading textures
		Mat mMat = this.originalImage;
		//Mat result = new Mat();
		//Imgproc.cvtColor(mMat, result, Imgproc.COLOR_RGB2BGRA);
		Bitmap bitmap = Bitmap.createBitmap(mMat.cols(), mMat.rows(), Bitmap.Config.ARGB_8888);
		Utils.matToBitmap(mMat, bitmap);


		// generate one texture pointer
	    gl.glGenTextures(1, textures, 0);
	    // ...and bind it to our array
	    gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
	    // create nearest filtered texture
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_NEAREST);
	    gl.glTexParameterf(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);
	    // Use Android GLUtils to specify a two-dimensional texture image from our bitmap 
	    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, bitmap, 0);
	    // Clean up
	    bitmap.recycle();
	}

	
	/** The draw method for the triangle with the GL context */
	public void draw(GL10 gl) {
		
		// bind the previously generated texture
		gl.glBindTexture(GL10.GL_TEXTURE_2D, textures[0]);
		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);
		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		
/*		
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// set the colour for the background
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);
		
		// to show the color (paint the screen) we need to clear the color buffer
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		
		// set the colour for the square
		
		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLES, 0, vertices.length / 3);
		
		//Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
*/ 
	}
}



/**
 * A vertex shaded square.
 */

/*
class Square {
	private FloatBuffer mFVertexBuffer; 
	private FloatBuffer mColorBuffer; 
	private ByteBuffer mIndexBuffer;
	
	private float k = 0.2f;
	private float startPosition_x1 = -3.0f;
	private float startPosition_y1 = 1.3f;

	private float z = -1.0f;
	
	public Square() {
		float startPosition_x2 = startPosition_x1 + k;
		float startPosition_y2 = startPosition_y1 + k; 
		
		float vertices[] ={
		    startPosition_x1, startPosition_y1, z, 
		    startPosition_x2, startPosition_y1, z, 
		    startPosition_x1, startPosition_y2, z, 
		    startPosition_x2, startPosition_y2, z

		};

		float color1 = (float)(Math.random()*255/100);
		float color2 = (float)(Math.random()*255/100);
		float color3 = (float)(Math.random()*255/100);
		
		System.out.println("colors " + color1 + "; " + color2 + ";" + color3);
		float colors[] = {
		    color1, color2, color3, 1,
		    color1, color2, color3, 1,
		    color1, color2, color3, 1,
		    color1, color2, color3, 1 
		};
		
		byte indices[] = {
					0, 3, 1,
					0, 2, 3
		};
		
		
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4); 
		vbb.order(ByteOrder.nativeOrder());
		mFVertexBuffer = vbb.asFloatBuffer();
		mFVertexBuffer.put(vertices);
		mFVertexBuffer.position(0);
		
		mColorBuffer = ByteBuffer.allocateDirect(colors.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
		 
		mColorBuffer.put(colors);
		mColorBuffer.position(0);
		
		mIndexBuffer = ByteBuffer.allocateDirect(indices.length); 
		mIndexBuffer.put(indices);
		mIndexBuffer.position(0);
	}
	
	public void setConstantValue(float c){
		this.k = c;
		float startPosition_x2 = startPosition_x1 + k;
		float startPosition_y2 = startPosition_y1 + k; 
		
		mFVertexBuffer.put(0, startPosition_x1);
		mFVertexBuffer.put(1, startPosition_y1);
		
		mFVertexBuffer.put(3, startPosition_x2);
		mFVertexBuffer.put(4, startPosition_y1);
		
		mFVertexBuffer.put(6, startPosition_x1);
		mFVertexBuffer.put(7, startPosition_y2);
		
		mFVertexBuffer.put(9, startPosition_x2);
		mFVertexBuffer.put(10, startPosition_y2);
	}
	
	public void setColor(double[] color){
		double r = color[0]; 
		double g = color[1];
		double b = color[2];
		for (int i = 0; i < 4; i++){
			mColorBuffer.put(0+(i*4), ((float)b/255.0f));
			mColorBuffer.put(1+(i*4), ((float)g/255.0f));
			mColorBuffer.put(2+(i*4), ((float)r/255.0f));
		}
	}
	
	public void draw(GL10 gl){
		gl.glFrontFace(GL11.GL_CW);
		
		gl.glVertexPointer(3, GL11.GL_FLOAT, 0, mFVertexBuffer);
		gl.glColorPointer(4, GL11.GL_FLOAT, 0, mColorBuffer); 
		gl.glDrawElements(GL11.GL_TRIANGLES, 6, GL11.GL_UNSIGNED_BYTE, mIndexBuffer);
		gl.glFrontFace(GL11.GL_CCW);
	}
	
}*/