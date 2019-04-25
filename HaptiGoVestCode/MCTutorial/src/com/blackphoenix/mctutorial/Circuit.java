package com.blackphoenix.mctutorial;

import java.util.ArrayList;
import java.util.List;

import com.blackphoenix.haptigo.VestController;

import android.location.Location;

public class Circuit {

	// type of the circuits constants
	public static final int IDEALLEADDISTANCE = 0, REACTIONTIME = 1,
			QUICKLEFT = 2, QUICKRIGHT = 3, THREESIGNALS = 4,
			MULTIPLEINTERSECTIONS1 = 5, MULTIPLEINTERSECTIONS2 = 6;;

	/**
	 * get the route for the circuit based on the type of route and direction
	 * 
	 * @param routeType
	 *            - type of route
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return
	 */
	public static List<Waypoint> getRoute(int routeType, int direction) {

		List<Waypoint> route = null;
		switch (routeType) {
		case IDEALLEADDISTANCE:
		default:
			route = getLDRoute(direction);
			break;
			
		case REACTIONTIME:
			route =getRTRoute(direction);
			break;
			
		case MULTIPLEINTERSECTIONS1:
			route = getMI1Route(direction);
			break;

		case MULTIPLEINTERSECTIONS2:
			route = getMI2Route(direction);
			break;

		case THREESIGNALS:
			route = getThreeSigRoute(direction);
			break;

		case QUICKLEFT:
			route = getQLRoute(direction);
			break;

		case QUICKRIGHT:
			route = getQRRoute(direction);
			break;

		}

		return route;
	}

	/**
	 * Route for lead distance and reaction time tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getLDRoute(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("LDRT_start");
		startPoint.myLoc.setLongitude(-96.33651);
		startPoint.myLoc.setLatitude(30.62138);
		startPoint.direction = VestController.STRAIGHT;

		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("LDRT_turn");
		turnPoint.myLoc.setLongitude(-96.33765);
		turnPoint.myLoc.setLatitude(30.62237);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(turnPoint);
		return route;
	}

	/**
	 * Route for reaction time tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getRTRoute(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("LDRT_start");
		startPoint.myLoc.setLongitude(-96.33651);
		startPoint.myLoc.setLatitude(30.62138);
		startPoint.direction = VestController.STRAIGHT;

		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("LDRT_turn");
		turnPoint.myLoc.setLongitude(-96.33731311);
		turnPoint.myLoc.setLatitude(30.62200928);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(turnPoint);
		return route;
	}

	
	/**
	 * Route for three signals tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getThreeSigRoute(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("3S_start");
		startPoint.myLoc.setLongitude(-96.33651);
		startPoint.myLoc.setLatitude(30.62138);
		startPoint.direction = VestController.STRAIGHT;

		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("3S_turn");
		turnPoint.myLoc.setLongitude(-96.33882);
		turnPoint.myLoc.setLatitude(30.62329);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(turnPoint);
		return route;
	}

	/**
	 * Route for multiple intersections first point turn tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getMI1Route(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("MI1_start");
		// TODO set the start point for multiple intersection circuit
		startPoint.myLoc.setLongitude(-96.33590);
		startPoint.myLoc.setLatitude(30.62395);
		startPoint.direction = VestController.STRAIGHT;

		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("MI1_turn");
		turnPoint.myLoc.setLongitude(-96.33726);
		turnPoint.myLoc.setLatitude(30.62270);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(turnPoint);
		return route;
	}

	/**
	 * Route for multiple intersections second point turn tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getMI2Route(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("MI2_start");
		// TODO set the start point for multiple intersection circuit
		startPoint.myLoc.setLongitude(-96.33590);
		startPoint.myLoc.setLatitude(30.62395);
		startPoint.direction = VestController.STRAIGHT;

		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("MI2_turn");
		turnPoint.myLoc.setLongitude(-96.33767);
		turnPoint.myLoc.setLatitude(30.62235);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(turnPoint);
		return route;
	}

	/**
	 * Route for quickleft tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getQLRoute(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();

		// enter start point details
		Waypoint startPoint = new Waypoint();
		startPoint.myLoc = new Location("ql_start");
		startPoint.myLoc.setLongitude(-96.336114);
		startPoint.myLoc.setLatitude(30.62182);
		startPoint.direction = VestController.STRAIGHT;

		//Left turn point details
		Waypoint leftTurnPoint = new Waypoint();
		leftTurnPoint.myLoc = new Location("ql_left");
		leftTurnPoint.myLoc.setLongitude(-96.33726);
		leftTurnPoint.myLoc.setLatitude(30.62270);
		leftTurnPoint.direction = VestController.LEFT;
		
		// enter turn point details
		Waypoint turnPoint = new Waypoint();
		turnPoint.myLoc = new Location("ql_turn");
		turnPoint.myLoc.setLongitude(-96.33767);
		turnPoint.myLoc.setLatitude(30.62235);
		turnPoint.direction = direction;

		route.add(startPoint);
		route.add(leftTurnPoint);
		route.add(turnPoint);

		return route;
	}

	/**
	 * Route for quick right tutorial
	 * 
	 * @param direction
	 *            - direction of turn at waypoint
	 * @return - route with waypoints and direction
	 */
	private static List<Waypoint> getQRRoute(int direction) {

		List<Waypoint> route = new ArrayList<Waypoint>();
		
		// enter start point details
				Waypoint startPoint = new Waypoint();
				startPoint.myLoc = new Location("qr_start");
				startPoint.myLoc.setLongitude(-96.33651);
				startPoint.myLoc.setLatitude(30.62138);
				startPoint.direction = VestController.STRAIGHT;

				//Left turn point details
				Waypoint rightTurnPoint = new Waypoint();
				rightTurnPoint.myLoc = new Location("qr_right");
				rightTurnPoint.myLoc.setLongitude(-96.33767);
				rightTurnPoint.myLoc.setLatitude(30.62235);
				rightTurnPoint.direction = VestController.RIGHT;
				
				// enter turn point details
				Waypoint turnPoint = new Waypoint();
				turnPoint.myLoc = new Location("qr_turn");
				turnPoint.myLoc.setLongitude(-96.33726);
				turnPoint.myLoc.setLatitude(30.62270);
				turnPoint.direction = direction;

				route.add(startPoint);
				route.add(rightTurnPoint);
				route.add(turnPoint);


		return route;
	}
}
