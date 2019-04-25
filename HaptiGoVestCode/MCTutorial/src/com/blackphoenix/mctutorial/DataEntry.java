package com.blackphoenix.mctutorial;

import android.location.Location;

/**
 * Details of one entry in log data
 * 
 * @author Manoj
 * 
 */
public class DataEntry {

	public String username;
	public String routineType;
	public String directionType;
	public int nextDirection;
	public double speed;
	public Location myLoc;
	public Location waypointLocation;
	public int numberOfPulses;
	public long timestamp;
	public double distanceToWayPoint;

	public String getStringVersion() {

		String results = timestamp + "," + username + "," + routineType + ","
				+ directionType + "," + nextDirection + "," + numberOfPulses + ","
				+ distanceToWayPoint + ","
				+ "," + myLoc.getLatitude() + "," + myLoc.getLongitude() + "," 
				+ waypointLocation.getLatitude() + "," + waypointLocation.getLongitude() + "\n";

		return results;
	}
}
