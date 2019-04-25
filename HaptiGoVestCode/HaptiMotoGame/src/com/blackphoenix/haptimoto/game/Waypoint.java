package com.blackphoenix.haptimoto.game;

import com.blackphoenix.haptigo.VestController;
import android.graphics.PointF;

public class Waypoint {

	private PointF myLoc = null;
	public int direction = VestController.LEFT;
	
	public Waypoint(float x, float y, int direction){
		
		myLoc = new PointF(x , y);
		this.direction = direction;
	}
	
	public int getDirection(){
		
		return direction;
	}
	
	public double distanceTo(float x, float y){
		double distance;
		
		float dx = x - myLoc.x;
		float dy = y - myLoc.y;
		
		distance = Math.sqrt(dx * dx + dy * dy);
		
		return distance;
	}
}