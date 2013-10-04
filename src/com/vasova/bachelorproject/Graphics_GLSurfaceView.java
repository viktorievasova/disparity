package com.vasova.bachelorproject;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * A class extending GLSurfaceView. 
 * Implementation of surface for displaying OpenGL rendering.
 * This implementation enables to rotate the rendered model in the x-direction.
 *  
 * @author viktorievasova
 *
 */
public class Graphics_GLSurfaceView extends GLSurfaceView{

	private Graphics_Renderer a_renderer;
	private float previousX;
		
	public Graphics_GLSurfaceView(Context context){
		super(context);
	}
	
	/**
	 * Sets the data needed for rendering the scene.
	 * 
	 * @param vertices an array of coordinates of vertices. Each sequence of three floats represent the x, y and z coordinates of one point.
	 * Each sequence of nine floats represents one triangle in a space. 
	 * @param colors an array of RGBA values of a color of a vertex. A sequence of four floats corresponds to a RGBA color of one vertex in float[] vertices.
	 */
	public void set3DData(float[] vertices, float[]colors){
		if (a_renderer == null){
			System.out.println("renderer is null");
			a_renderer = new Graphics_Renderer(vertices, colors);
			setRenderer(a_renderer);
		}else{
			a_renderer.setNewData(vertices, colors);
		}
	}
	
	/** 
	 * {@inheritDoc}
	 * This implementation of onTouchEvent reacts on the ACTION_MOVE event.
	 * It rotates the rendered model in the x-direction.
	 * */
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if (event != null){
			float x = event.getX();			
			if(event.getAction() == MotionEvent.ACTION_MOVE){
				if(a_renderer != null){
					float deltaX = (x - previousX) / 2f;
					a_renderer.angleY += deltaX;
				}
			}
			previousX = x;
			return true;
		}else{
			return super.onTouchEvent(event);
		}
	}
	
}
