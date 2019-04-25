package com.blackphoenix.haptimoto.game;

import java.util.ArrayList;
import java.util.List;

import com.blackphoenix.haptigo.NavGuide;
import com.blackphoenix.haptigo.VestController;

import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PointF;

public class DemoController {

	private static final int MAXANIMATIONSTEP = 10000;
	
	private List<PointF> routeAnimationPoints;
	private List<Waypoint> routePoints;
	private Path routeAnimationPath = new Path();
	private PathMeasure routePM;
	private float routeSegmentLength;
	private int currentStep = 0;
	
	public DemoController(){
		
		loadRouteAnimationPoints();
		loadRoutePoints();
		loadRouteAnimation();
		
		NavGuide.setRoute(routePoints);
		NavGuide.setThresholds(routeSegmentLength);
	}
	
	private void loadRouteAnimation(){
		
       //init smooth curve
       PointF point = routeAnimationPoints.get(0);
       routeAnimationPath.moveTo(point.x, point.y);
       for(int i = 0; i < routeAnimationPoints.size() - 1; i++){
           point = routeAnimationPoints.get(i);
           PointF next = routeAnimationPoints.get(i+1);
           routeAnimationPath.lineTo(next.x, next.y);
       }
       routePM = new PathMeasure(routeAnimationPath, false);
       routeSegmentLength = routePM.getLength() / MAXANIMATIONSTEP;//20 animation steps
		
	}
	
	private void loadRouteAnimationPoints(){
		
		routeAnimationPoints = new ArrayList<PointF>();
		routeAnimationPoints.add(new PointF(409f, 518f));
		routeAnimationPoints.add(new PointF(393f, 535f));
		routeAnimationPoints.add(new PointF(378f, 563f));
		routeAnimationPoints.add(new PointF(343f, 535f));
		
		routeAnimationPoints.add(new PointF(437f, 397f));
		routeAnimationPoints.add(new PointF(292f, 221f));
		routeAnimationPoints.add(new PointF(235f, 291f));
		routeAnimationPoints.add(new PointF(160.4f, 152.3f));
		
		routeAnimationPoints.add(new PointF(175.5f, 126f));
		routeAnimationPoints.add(new PointF(172.6f, 85f));
		routeAnimationPoints.add(new PointF(141.4f, 62f));
		routeAnimationPoints.add(new PointF(122f, 67f));
		routeAnimationPoints.add(new PointF(105f, 80f));
	}
	
	private void loadRoutePoints(){
		
		routePoints = new ArrayList<Waypoint>();
		routePoints.add(new Waypoint(409f, 518f, VestController.RIGHT));
		routePoints.add(new Waypoint(343f, 535f, VestController.RIGHT));
		
		routePoints.add(new Waypoint(437f, 397f,VestController.LEFT));
		routePoints.add(new Waypoint(292f, 221f, VestController.LEFT));
		routePoints.add(new Waypoint(235f, 291f, VestController.RIGHT));
		routePoints.add(new Waypoint(160.4f, 152.3f, VestController.RIGHT));
		
//		routePoints.add(new PointF(175.5f, 126f));
//		routePoints.add(new PointF(172.6f, 85f));
//		routePoints.add(new PointF(141.4f, 62f));
//		routePoints.add(new PointF(122f, 67f));
		routePoints.add(new Waypoint(105f, 80f, VestController.DESTINATION));
	}
	
	public float[] nextPoint(){
		
		float[] pos = new float[2];
    	float[] tan = new float[2];
    	
		//animate the sprite
//        Matrix mxTransform = new Matrix();
        if (currentStep <= MAXANIMATIONSTEP) {
            /*routePM.getMatrix(routeSegmentLength * currentStep, mxTransform,
                    PathMeasure.POSITION_MATRIX_FLAG + PathMeasure.TANGENT_MATRIX_FLAG);
            mxTransform.preTranslate(-marker.getWidth(), -marker.getHeight());
            canvas.drawBitmap(marker, mxTransform, null);*/
            
        	
            routePM.getPosTan(currentStep * routeSegmentLength, pos, tan);
            
            if(currentStep % 100 == 0)
            	NavGuide.guideDriver(new PointF(pos[0], pos[1]));
            
            currentStep++; //advance to the next step
        } else {
        	
            resetDemo();
        }
        return pos;
	}
	
	public void resetDemo(){
		
		currentStep = 0;
		
		loadRoutePoints();
		NavGuide.setRoute(routePoints);
		NavGuide.setThresholds(routeSegmentLength);
	}
}




