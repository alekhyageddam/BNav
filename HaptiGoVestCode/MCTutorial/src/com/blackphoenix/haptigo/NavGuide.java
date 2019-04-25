package com.blackphoenix.haptigo;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

import com.blackphoenix.mctutorial.DataEntry;
import com.blackphoenix.mctutorial.Waypoint;

/**
 * Guidance logic for the each waypoint - sends direction signals to vest
 * 
 * @author Manoj
 * 
 */
public class NavGuide {

	public static int WAYPOINT_REACHED_THRESHOLD = 15; // Waypoint is reached
														// when user is in 10m
														// radius of location
	public static double MPS_TO_KMPH_FACTOR = 3.6; // conversion factor to
													// convert meters per second
													// to kmph
	public static int FIRST_NOTIFICATION_FACTOR = 4; // ideal_distance factor at
														// which first turn
														// notification is given
	public static int SECOND_NOTIFICATION_FACTOR = 2;// ideal_distance factor at
														// which second turn
														// notification is given
	public static int FINAL_NOTIFICATION_FACTOR = 1;// ideal_distance factor at
													// which final turn
													// notification is given

	public static int TIME_TO_STOP_LOGGING = 10000; // 30 seconds after last
													// point reached time
	
	public static int LOCATION_UPDATE_INTERVAL = 2000; // time between location updates
	public static double IDEAL_LEAD_SPEED_FACTOR = 1.1973; // constants for
															// Ideal lead
															// distance formula
	public static double IDEAL_LEAD_CONSTANT_FACTOR = 21.307;

	public static int FIRST_NOTIFICATION_PULSE = 1; // number of pulses for
													// first notification
	public static int SECOND_NOTIFICATION_PULSE = 2;// number of pulses for
													// second notification
	public static int FINAL_NOTIFICATION_PULSE = 3;// number of pulses for final
													// notification

	private static Location myCurrentLocation;
	private static Waypoint nextWaypoint;
	private static List<Waypoint> route = null;
	private static boolean logging = false;
	private static boolean isStartPoint = true;
	private static int nextTurnDirection = VestController.STRAIGHT;
	private static int numberOfPulses = 1;
	private static boolean areWeThereYet = false;
	private static long lastPointTime = 0;
	private static long timeAfterLastPoint = 0;
	private static double distanceToWaypoint = 0;

	public static boolean canLogData() {
		return logging;
	}

	public static void setRoute(List<Waypoint> circuit) {

		if (circuit == null)
			return;

		route = circuit;
		nextWaypoint = route.remove(0);

		lastPointTime = 0;
		timeAfterLastPoint = 0;
		areWeThereYet = false;
		isStartPoint = true;
		logging = false;
	}

	public static boolean isWayPointReached() {

		if (nextWaypoint == null || myCurrentLocation == null)
			return false;

		return nextWaypoint.myLoc.distanceTo(myCurrentLocation) <= WAYPOINT_REACHED_THRESHOLD;
	}

	private static double calculateIdealLeadDistance() {

		double speed = myCurrentLocation.getSpeed();
		return (IDEAL_LEAD_SPEED_FACTOR * speed * MPS_TO_KMPH_FACTOR + IDEAL_LEAD_CONSTANT_FACTOR);
	}

	public static void guideDriver(Location currentLocation,
			Context toastContext) {

		if (currentLocation == null)
			return;

		myCurrentLocation = currentLocation;

		if (nextWaypoint != null)
			distanceToWaypoint = currentLocation.distanceTo(nextWaypoint.myLoc);
		else
			distanceToWaypoint = -1;

		boolean wayPointReached = isWayPointReached();

		// After reaching the first point start logging the data
		if (isStartPoint){ //&& wayPointReached) {
			isStartPoint = false;
			logging = true; // start the logging process
			nextTurnDirection = VestController.STRAIGHT;
			numberOfPulses = FIRST_NOTIFICATION_PULSE;
			conveyInstructions(); // Asking the user to move straight


			// When we have more turns note the next way point and the
			// direction
			// but do not notify the user
			nextWaypoint = route.remove(0);
			nextTurnDirection = nextWaypoint.direction;
			numberOfPulses = FIRST_NOTIFICATION_PULSE;
			
			Toast message = Toast.makeText(toastContext, "Started Circuit ::"
					+ distanceToWaypoint, Toast.LENGTH_LONG);
			message.show();
			return;
		}

//		if (isStartPoint && !wayPointReached) {
//			Toast message = Toast.makeText(toastContext,
//					"Did not reach start point yet ::" + distanceToWaypoint,
//					Toast.LENGTH_LONG);
//			message.show();
//			return;
//		}

		// for all other instances when we give directions
		if (!areWeThereYet) {

			// if the last Point time is greater than 0 it means user reached
			// last point
			if (lastPointTime > 0) {

				timeAfterLastPoint = System.currentTimeMillis() - lastPointTime;

				Toast message = Toast.makeText(toastContext, "About to End",
						Toast.LENGTH_SHORT);
				message.show();

				if (timeAfterLastPoint > TIME_TO_STOP_LOGGING) {

					areWeThereYet = true;
					logging = false;
					conveyInstructions();

					message = Toast.makeText(toastContext, "End Of Circuit",
							Toast.LENGTH_LONG);
					message.show();
				}
				
				return;
			}

			// if the waypoint is reached
			if (wayPointReached) {

				// When we are at the last turn notw the last point time
				if (route.isEmpty()) {

					lastPointTime = System.currentTimeMillis();
					nextTurnDirection = VestController.DESTINATION;
					numberOfPulses = FIRST_NOTIFICATION_PULSE;

					Toast message = Toast.makeText(toastContext,
							"Reached Last Point", Toast.LENGTH_SHORT);
					message.show();
					return;

				} else {

					// When we have more turns note the next way point and the
					// direction
					// but do not notify the user
					nextWaypoint = route.remove(0);
					nextTurnDirection = nextWaypoint.direction;
					numberOfPulses = FIRST_NOTIFICATION_PULSE;

					Toast message = Toast.makeText(toastContext,
							"Reached middle Point", Toast.LENGTH_SHORT);
					message.show();

					return;
				}

			} else {

				double distanceToWayPoint = myCurrentLocation
						.distanceTo(nextWaypoint.myLoc);
				double finalIdealLeadDistance = calculateIdealLeadDistance();
				double secondIdealLeadDistance = SECOND_NOTIFICATION_FACTOR
						* finalIdealLeadDistance;
				double firstIdealLeadDistance = FIRST_NOTIFICATION_FACTOR
						* finalIdealLeadDistance;

				if (distanceToWayPoint > firstIdealLeadDistance) {

					nextTurnDirection = VestController.STRAIGHT;
					numberOfPulses = FIRST_NOTIFICATION_PULSE;

					conveyInstructions();
				} else if (distanceToWayPoint > secondIdealLeadDistance
						&& distanceToWayPoint < firstIdealLeadDistance) {

					nextTurnDirection = nextWaypoint.direction;
					numberOfPulses = FIRST_NOTIFICATION_PULSE;

					conveyInstructions();
				} else if (distanceToWayPoint > finalIdealLeadDistance
						&& distanceToWayPoint < secondIdealLeadDistance) {

					nextTurnDirection = nextWaypoint.direction;
					numberOfPulses = SECOND_NOTIFICATION_PULSE;

					conveyInstructions();
				} else {

					nextTurnDirection = nextWaypoint.direction;
					numberOfPulses = FINAL_NOTIFICATION_PULSE;

					conveyInstructions();
				}

				Toast message = Toast.makeText(toastContext, "Turn signal :: "
						+ nextTurnDirection + " ::" + numberOfPulses,
						Toast.LENGTH_SHORT);
				message.show();
			}
		}
	}

	public static void logData(DataEntry dataPoint) {

		if (dataPoint == null || !logging)
			return;

		dataPoint.nextDirection = nextTurnDirection;
		dataPoint.myLoc = myCurrentLocation;
		dataPoint.speed = myCurrentLocation.getSpeed();
		dataPoint.numberOfPulses = numberOfPulses;
		dataPoint.waypointLocation = nextWaypoint.myLoc;
		dataPoint.distanceToWayPoint = distanceToWaypoint;
	}

	private static void conveyInstructions() {

		int[] intensities;

		switch (nextTurnDirection) {
		case VestController.LEFT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);
			intensities = new int[] { VestController.VIBE_HIGH,
					VestController.VIBE_LOW, VestController.VIBE_LOW };

			VestController.setVibrationIntensities(intensities,
					VestController.SHORT_BUZZ, numberOfPulses);

			break;

		case VestController.RIGHT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			intensities = new int[] { VestController.VIBE_LOW,
					VestController.VIBE_LOW, VestController.VIBE_HIGH };

			VestController.setVibrationIntensities(intensities,
					VestController.SHORT_BUZZ, numberOfPulses);

			break;

		case VestController.BACK:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.LONG_BUZZ);

			intensities = new int[] { VestController.VIBE_HIGH,
					VestController.VIBE_LOW, VestController.VIBE_HIGH };

			VestController.setVibrationIntensities(intensities,
					VestController.LONG_BUZZ, numberOfPulses);

			break;

		case VestController.STRAIGHT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			intensities = new int[] { VestController.VIBE_LOW,
					VestController.VIBE_HIGH, VestController.VIBE_LOW };

			VestController.setVibrationIntensities(intensities,
					VestController.SHORT_BUZZ, numberOfPulses);

			break;

		default:
		case VestController.STOP:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			VestController.resetVibrationIntensities();
			break;
		}
	}

}
