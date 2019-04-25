package edu.tamu.haptigo;

import android.content.Context;
import android.widget.Toast;
import at.abraxas.amarino.Amarino;

//This class implements an interface to the tactors on the Haptigo Vest
public class VestController 
{

	public static final int VIBE_LEFT = 1, VIBE_RIGHT = 3, VIBE_CENTER = 2;
	public static final int VIBE_OBSTACLE_LEFT = 4, VIBE_OBSTACLE_RIGHT = 5;

	private static Context context;
	private static String deviceAddress;
	private static boolean Connected = false;

	//Initialize variables and connect to Arduino
	public static void initialize(Context context,final String bluetoothAddress)
	{
		VestController.context = context;
		VestController.deviceAddress = bluetoothAddress;
		Amarino.connect(context, deviceAddress);
		Connected = true;
	}

	//Set the vibration intensity of directional tactors on vest
	public static void setVibrationIntensities(int[] intensities)
	{
		Amarino.sendDataToArduino(context, deviceAddress, 'i', intensities);
	}

	//Set the vibration frequency of directional tactors on vest
	public static void setVibrationFrequency(int Frequency)
	{
		Amarino.sendDataToArduino(context, deviceAddress, 'f', Frequency);
	}

	//Set the operational mode of the vest -- TestMode/NavigationMode
	public static void setTestMode(boolean InputMode)
	{
		Amarino.sendDataToArduino(context, deviceAddress, 'm', InputMode); 
		Toast.makeText(context, "Set Navigation Mode", Toast.LENGTH_SHORT).show();
	}

	//Indicate via tactors that destination has been reached
	public static void DestinationReached()
	{
		Amarino.sendDataToArduino(context,deviceAddress, 'v', 0);
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

}
