package com.vasova.bachelorproject;

import java.nio.ByteBuffer; 
import java.nio.ByteOrder; 
import java.nio.FloatBuffer; 
import javax.microedition.khronos.opengles.GL10; 
import javax.microedition.khronos.opengles.GL11;

/**
 * A vertex shaded square.
 */
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
	
}