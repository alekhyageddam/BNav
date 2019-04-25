package edu.tamu.haptigo;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.widget.Toast;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;

//This class implements an interface to the Navigation backend to Second Device
public class SecondDeviceController 
{
	private static BluetoothAdapter mBluetoothAdapter = null;
	private static BluetoothDevice mDevice = null;
	private static BluetoothServerSocket mBluetoothServerSocket = null;
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
	private static Context context;
	private static BluetoothSocket connectedSocket;
	private static OutputStream out = null;
	public static final int VIBE_LEFT = 1, VIBE_RIGHT = 3, VIBE_CENTER = 2;
	public static final int VIBE_OBSTACLE_LEFT = 4, VIBE_OBSTACLE_RIGHT = 5;

	//private static Context context;
	private static String deviceAddress;
	private static boolean connected = false;
	//private static BluetoothAdapter mBluetoothAdapter = null;

	//Initialize variables and connect to Arduino
	public static void initialize(Context context)
	{
        // Get local Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Toast.makeText(context, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            //finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(context,
                    "Please Enable Bluetooth",Toast.LENGTH_LONG).show();
            //Add code to enable bluetooth from inside app
            return;
        }
        
		SecondDeviceController.context = context;
		//VestController.deviceAddress = bluetoothAddress;
		SecondDeviceController.deviceAddress = "F8:DB:7F:0E:BA:12";
		
        try {
			mBluetoothServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("HaptiGo",MY_UUID);
		} catch (IOException e) {
			Toast.makeText(context, "Unable to create Socket!", Toast.LENGTH_SHORT).show();
		}
        
        try {
        	Toast.makeText(context, "Ready to accept connection!", Toast.LENGTH_SHORT).show();
        	connectedSocket = mBluetoothServerSocket.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Toast.makeText(context, "Unable to accept connection!", Toast.LENGTH_SHORT).show();
			return;
		}
        if(connectedSocket != null){
        	try {
				mBluetoothServerSocket.close();
			} catch (IOException e) {
				Toast.makeText(context, "Error in Closing the server socket!", Toast.LENGTH_SHORT).show();
				// TODO Auto-generated catch block
				//e.printStackTrace();
			}
 	
        }
        else{
        	Toast.makeText(context, "Something is wrong!", Toast.LENGTH_SHORT).show();
        	
        }
		connected = true;
		
	}

	public static boolean is_connected(){
		Toast.makeText(context,String.valueOf(connected), Toast.LENGTH_SHORT).show();
		return connected;
		}
	public static void vibrate_left(){
		try {
			out = connectedSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		byte[] stringByte = "left".getBytes();
		try {
			out.write(stringByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void vibrate_right(){
		try {
			out = connectedSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		byte[] stringByte = "right".getBytes();
		try {
			out.write(stringByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void vibrate_straight(){
		try {
			out = connectedSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		byte[] stringByte = "straight".getBytes();
		try {
			out.write(stringByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void vibrate_backwards(){
		try {
			out = connectedSocket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		byte[] stringByte = "backwards".getBytes();
		try {
			out.write(stringByte);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	//Set the vibration intensity of directional tactors on vest
	public static void setVibrationIntensities(int[] intensities)
	{
		
	}

	//Set the vibration frequency of directional tactors on vest
	public static void setVibrationFrequency(int Frequency)
	{
		
	}

	//Set the operational mode of the vest -- TestMode/NavigationMode
	public static void setTestMode(boolean InputMode)
	{
		
		Toast.makeText(context, "Set Navigation Mode", Toast.LENGTH_SHORT).show();
	}

	//Indicate via tactors that destination has been reached
	public static void DestinationReached()
	{
		
	}

	//Check if connected to Arduino
	public static boolean IsConnected()
	{
		return connected;
	}

	//Disconnect from Arduino
	public static void DestroyConnection()
	{
		
		connected = false;
	}

}
