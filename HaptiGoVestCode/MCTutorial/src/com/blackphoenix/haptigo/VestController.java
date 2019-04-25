package com.blackphoenix.haptigo;

import android.content.Context;
import at.abraxas.amarino.Amarino;

//This class implements an interface to the tactors on the Haptigo Vest
public class VestController 
{
	
	//Thread sleep time or inter stimulus time
	public static final int ISI_TIME = 3000;
	// constants that define the length of the vibes and off time
	public static final int SHORT_BUZZ = 400, LONG_BUZZ = 600,
			BUZZ_OFF_TIME = 150;

	// constants that define the amplitude of vibes and index of tactors on vest 
	public static final int VIBE_HIGH = 255, VIBE_LOW = 0;
	public static final int VIBE_LEFT_INDEX = 0, VIBE_RIGHT_INDEX = 2, VIBE_CENTER_INDEX = 1;
	private static final int NUM_PULSES_INDEX = 4, BUZZ_LENGTH_INDEX = 3;
	
	// constants defining the possible direction commands to the vest
	public static final int STRAIGHT = 0, BACK = 1, LEFT = 2, RIGHT = 3,
			DESTINATION = 4, STOP = 5;
	
	private static Context context;
	private static String deviceAddress;
	public static boolean Connected = false;

	//Initialize variables and connect to Arduino
	public static void initialize(Context context,final String bluetoothAddress)
	{
		VestController.context = context;
		VestController.deviceAddress = bluetoothAddress;
		Amarino.connect(context, deviceAddress);
		Connected = true;
	}

	//Set the vibration intensity of directional tactors on vest
	public static void setVibrationIntensities(int[] intensities, int buzzLength, int numOfPulses)
	{
		int data[] = new int[5];
		data[VIBE_LEFT_INDEX] = intensities[VIBE_LEFT_INDEX];
		data[VIBE_RIGHT_INDEX] = intensities[VIBE_RIGHT_INDEX];
		data[VIBE_CENTER_INDEX] = intensities[VIBE_CENTER_INDEX];
		data[BUZZ_LENGTH_INDEX] = buzzLength;
		data[NUM_PULSES_INDEX] = numOfPulses;
		
		Amarino.sendDataToArduino(context, deviceAddress, 'i', data);
	}
	
	public static void resetVibrationIntensities(){
		Amarino.sendDataToArduino(context, deviceAddress, 'r', 0);
	}
	//Check if connected to Arduino
	public static boolean IsConnected()
	{
		return Connected;
	}

	//Disconnect from Arduino
	public static void DestroyConnection()
	{
		Amarino.disconnect(context,deviceAddress);
		Connected = false;
	}
	
	public static void MakeConnection(){
		
		if(!Connected)
			Amarino.connect(context, deviceAddress);
	}

}
