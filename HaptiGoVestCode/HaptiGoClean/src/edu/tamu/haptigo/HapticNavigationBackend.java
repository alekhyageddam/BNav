package edu.tamu.haptigo;

import android.content.Context;
import android.location.Location;
import android.widget.Toast;

//This class implements all the back-end Navigation functionality of the Navigation Systems.
public class HapticNavigationBackend
{
	//Direction constants
	private final int Left = 0;
	private	final int Straight = 1;
	private	final int Right = 2;
	private final int Backwards = 3;
	
	private Location CurrentLocation = new Location("current");
	private Location NextDestination = new Location("next");
	private double NextBearing;
    private int TurnDirection;
    Context context;
    
	public void UpdateLocation(Location InputLocation)
	{
		CurrentLocation = InputLocation;
	}

	public void UpdateDestination(Location InputDestination)
	{
		NextDestination = InputDestination;
	}
	public int GetDirection()
	{
		return TurnDirection;
		
	}
	public HapticNavigationBackend(Context appContext){
		context = appContext;
	}
	
	// Calculates required walking direction and relays this info to Arduino
	public boolean WalkingNavigator(Location NextDestination, double DeviceBearing)
	{
		Toast.makeText(context, "Calling walking navigator", Toast.LENGTH_LONG).show();
	    if(!SecondDeviceController.is_connected()){
	    	Toast.makeText(context, "Not connected to device", Toast.LENGTH_LONG).show();
	    	SecondDeviceController.initialize(context);
	    }
		//Acquire the bearing for the shortest path to the next location
		NextBearing = CurrentLocation.bearingTo(NextDestination);
		
		//Return value
		boolean VestStatus = false;
		
		// Convert Bearings to range in [0, 360)
		if (NextBearing < 0)
			NextBearing = NextBearing + 360;

		if (DeviceBearing < 0)
			DeviceBearing = DeviceBearing + 360;
		  
		// Calculate the data to send to Arduino
		int Frequency = VibeFrequency(DeviceBearing, NextBearing);
		
		if(PhoneController.enabled)
		{
			int TurnDirection = PocketNavigatorVibeDirection(DeviceBearing, NextBearing);
			switch(TurnDirection)
			{
				case Left:
					PhoneController.vibrate_left();
					SecondDeviceController.vibrate_left();
					break;
				case Straight:
					PhoneController.vibrate_straight();
					SecondDeviceController.vibrate_straight();
					break;
				case Right:
					PhoneController.vibrate_right();
					SecondDeviceController.vibrate_right();
					break;
				case Backwards:
					PhoneController.vibrate_backwards();
					SecondDeviceController.vibrate_backwards();
			}
		}
		else if(VestController.IsConnected()) //Update Arduino vibe boards
		{
			int TurnDirection = VibeDirection(DeviceBearing, NextBearing);
			int Intensities[] = VibeIntensities(TurnDirection);
			VestController.setVibrationIntensities(Intensities);
			VestController.setVibrationFrequency(Frequency);
			VestStatus = true;
		}
		else
			VestStatus = false;
		return VestStatus;
	}
	
	// Calculates required walking direction and relays this info to Arduino
	//Ashish change for left from board and right form phone will be done here.
	public boolean WalkingNavigator(double DeviceBearing, double WaypointBearing)
	{

		//Return value
		boolean VestStatus = false;

		//if (DeviceBearing < 0)
			//DeviceBearing = (DeviceBearing + 360)% 360;

		// Calculate the data to send to Arduino
		int Frequency = VibeFrequency(DeviceBearing, WaypointBearing);
		
		//Update Arduino vibe boards

		if(PhoneController.enabled)
		{
			int TurnDirection = PocketNavigatorVibeDirection(DeviceBearing, WaypointBearing);
			switch(TurnDirection)
			{
				case Left:
					PhoneController.vibrate_left();
					SecondDeviceController.vibrate_left();
					break;
				case Straight:
					PhoneController.vibrate_straight();
					SecondDeviceController.vibrate_straight();
					break;
				case Right:
					PhoneController.vibrate_right();
					SecondDeviceController.vibrate_right();
					break;
				case Backwards:
					PhoneController.vibrate_backwards();
					SecondDeviceController.vibrate_backwards();
			}
			return true;
		}
		else if(VestController.IsConnected())
		{
			int TurnDirection = VibeDirection(DeviceBearing, WaypointBearing);
			int Intensities[] = VibeIntensities(TurnDirection);
			VestController.setVibrationIntensities(Intensities);
			VestController.setVibrationFrequency(Frequency);
			VestStatus = true;
		}
		else
			VestStatus = false;
		return VestStatus;
	}

	//Calculates the angle between two input bearings
	private double CalculateAngle(double CurrentBearing, double NextBearing)
	{
		double Difference;
		double Angle;
		   
		Difference = Math.abs(NextBearing - CurrentBearing);
		if (Difference > 180)			//Give shortest angle -- No need for user to turn completely around
			Angle = Math.abs(360 - Difference);
		else
			Angle = Difference;
		return Angle;
	}
	//Calculates the required movement direction
	private int VibeDirection(double DeviceBearing, double WaypointBearing)
	{
		
		double TurnThreshold = 30;
		double deflection = WaypointBearing - (DeviceBearing);
				
		if(deflection > 180){
			deflection = deflection - 360;
		}
		
		if(deflection < -180){
			deflection += 360;
			
		}
		if(deflection < -TurnThreshold)
		{
			TurnDirection = Left;
		}
		else if ((deflection >= -TurnThreshold) && (deflection < TurnThreshold))
		{
			TurnDirection = Straight;
		}
		else
		{
			TurnDirection = Right;
		}
		
		return TurnDirection;
   }
	//Calculates the required movement direction
	private int PocketNavigatorVibeDirection(double DeviceBearing, double WaypointBearing)
	{
		double TurnThreshold = 45;
		double LeftThreshold = -135;
		double RightThreshold = 135;
		double deflection = WaypointBearing - (DeviceBearing);
				
		if(deflection > 180){
			deflection = deflection - 360;
		}
		
		if(deflection < -180){
			deflection += 360;
			
		}
		if((deflection < -TurnThreshold) && (deflection >= LeftThreshold))
		{
			TurnDirection = Left;
		}
		else if ((deflection >= -TurnThreshold) && (deflection < TurnThreshold))
		{
			TurnDirection = Straight;
		}
		else if ((deflection >= TurnThreshold) && (deflection <= RightThreshold))
		{
			TurnDirection = Right;
		}
		else
		{
			TurnDirection = Backwards;
		}
		
		return TurnDirection;
	}
	
   //Calculates the vibration frequency of Arduino vibe boards
   private int VibeFrequency(double CurrentBearing, double NextBearing)
	{
		/* Delay between vibrations in milliseconds, Smaller freq = more frequent vibrations */
		double TurnAngle = CalculateAngle(CurrentBearing, NextBearing);
		
		int Frequency;
		
		// **Could address turnAngle <= 10 in place where method is called
		if (TurnAngle <= 20)
			Frequency = 1000;
		else if (TurnAngle > 20 && TurnAngle <= 30)
			Frequency = 200;
		else if (TurnAngle > 30 && TurnAngle <= 60)
			Frequency = 350;
		else if (TurnAngle > 60 && TurnAngle <= 90)
			Frequency = 500;
		else if (TurnAngle > 90 && TurnAngle <= 135)
			Frequency = 750;
		else
			Frequency = 1000;
		
		return Frequency;
	}

	//Calculates the intensities of all tactors on the Haptic Vest
	private int[] VibeIntensities(int InputDirection)
	{
		final int Intensity = 255;
		int results[] = new int[]{0,0,0};

		if(InputDirection == Left)
			results[Left] = Intensity;
		else if (InputDirection == Straight)
			results[Straight] = Intensity;
		else if (InputDirection == Right)
			results[Right] = Intensity;

		return results;
	}
}