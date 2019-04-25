package edu.tamu.haptigo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import edu.tamu.haptigo.JsonDirectionsParser.GoogleLocation;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.text.format.Time;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;
import at.abraxas.amarino.AmarinoIntent;

//This class implements the turn-by-turn Navigation functionality of the Nav. systems
public class NavigationService extends Service
{
	//Connection-specific variables.
	private String DeviceAddress;
	private Context context;
	private VestBinder binder = new VestBinder();

	//Navigation-specific variables
	private final int WaypointThreshold = 15; //in meters
	private final int UpdateFrequency = 3000; //in milli-seconds
	private final int UpdateDistance = 10;    //in meters
	
	private long StartTime;
	private int nav_system;
	private String destination;
	private Location currentLocation;
	private HapticNavigationBackend navigatorLibrary;
	private Queue<Location> Waypoints = new LinkedList<Location>();
	
	//Sensor-specific variables
	private double DeviceBearing;
	private double NewDeviceBearing;
	private float[] magValues;
	private float[] accelValues;
	private Sensor magSensor;
	private Sensor accelSensor;
	private double WaypointBearing;
	
	//Log-file specific variables
	File HistorylogFile;
	File ElapsedTimelogFile;

	private void PopulateRoute(List<GoogleLocation> InputPoints)
	{
		for (GoogleLocation tempWaypoint: InputPoints)
		{
			Location tempLocation = new Location("");
			tempLocation.setLatitude(tempWaypoint.getlat());
			tempLocation.setLongitude(tempWaypoint.getlng());
			Waypoints.add(tempLocation);
		}
	}
	
	private final SensorEventListener SensorListener = new SensorEventListener()
	{

		//When the Accelerometer or Magnetometer detects a change
		public void onSensorChanged(SensorEvent event)
		{
			int matrix_size = 16;
			float[] R = new float[matrix_size];
			float[] outR = new float[matrix_size];
			float[] I = new float[matrix_size];
			float[] values = new float[3];
			if (event.accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE)
			{
				Log.v("Sensor","Unreliable Sensor");
				return;
			}

			switch (event.sensor.getType())
			{
			case Sensor.TYPE_MAGNETIC_FIELD:
				magValues = event.values.clone();
				break;	
			case Sensor.TYPE_ACCELEROMETER:
				accelValues = event.values.clone();
				break;
			}

			if (magValues != null && accelValues != null)
			{

				SensorManager.getRotationMatrix(R, I, accelValues, magValues);
				// Correct if screen is in Landscape
				SensorManager.remapCoordinateSystem(R, 
						SensorManager.AXIS_X,
						SensorManager.AXIS_Z, outR);

				SensorManager.getOrientation(R, values);

				DeviceBearing = (float)(180*values[0]/Math.PI);

			}

		}

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// Do nothing

		}

	};

	private final LocationListener listener = new LocationListener() 
	{
		@Override
		public void onLocationChanged(Location InputLocation)
		{
			Toast.makeText(getApplicationContext(),"LocationChanged" , Toast.LENGTH_SHORT).show();
			if(currentLocation == null){
				currentLocation = InputLocation;
			}
			if (InputLocation != null)
			{
				//float Distance = currentLocation.distanceTo(InputLocation);
				//NewDeviceBearing = (double) InputLocation.getBearing();

				NewDeviceBearing = currentLocation.bearingTo(InputLocation);
				//if (NewDeviceBearing < 0)
					//NewDeviceBearing = (NewDeviceBearing + 360)% 360;
				
				
				if(NewDeviceBearing > 180){
					NewDeviceBearing = NewDeviceBearing - 360;
				}
				
				if(NewDeviceBearing < -180){
					NewDeviceBearing += 360;
					
				}				
				//if(NewDeviceBearing< 0){
					//NewDeviceBearing = NewDeviceBearing + 180;
					
				//}
				//Toast.makeText(getApplicationContext(),"NewDeviceBearing" , Toast.LENGTH_SHORT).show();
				Toast.makeText(getApplicationContext(),String.valueOf(NewDeviceBearing) , Toast.LENGTH_SHORT).show();
				//Toast.makeText(getApplicationContext(),String.valueOf(Distance) , Toast.LENGTH_SHORT).show();
			}
			currentLocation = InputLocation;
			//Debugging!
			//Toast.makeText(getApplicationContext(), "Location update!", Toast.LENGTH_SHORT).show();
			navigate(NewDeviceBearing);
			//navigate();
		}

		@Override
		public void onProviderDisabled(String arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status,
				Bundle extras) {
			// TODO Auto-generated method stub

		}
	};

	private void navigate(double NewDeviceBearing)
	//private void navigate()
	{
		//Add updated location to history
		//LogLocationHistory(currentLocation);
		
		//Record elapsed time to reach way-point
		long ElapsedTime = SystemClock.elapsedRealtime() - StartTime;
		
		//Acquire way point
		Location nextWaypoint = Waypoints.peek();
		if (nextWaypoint == null)
		{
			NavigationComplete();
			return;
		}

		//Calculate distance to way-point
		float Distance = currentLocation.distanceTo(nextWaypoint);
		
		if (Distance <= WaypointThreshold)
		{
			Waypoints.remove();
			//LogElapsedTimes(ElapsedTime);
			Toast.makeText(context, "Waypoint reached!", Toast.LENGTH_SHORT).show();
		}
		
		//Calculate navigation direction
		if(Waypoints.isEmpty())
			NavigationComplete();
		else
		{
			WaypointBearing = currentLocation.bearingTo(Waypoints.peek());
			WalkingNavigation(NewDeviceBearing,WaypointBearing);
		}
		
		return;

	}

	private void NavigationComplete()
	{
		switch(nav_system)
		{
			case 0:
				VestController.DestinationReached();
				break;
			case 1:
				PhoneController.victory_dance();
				break;
		}
		//stopSelf();
	}

	@Override
	public void onCreate()
	{

		//Load in Way-points
		PopulateRoute(StudyNavigation.WayPoints);
		
		// Service Managers
		LocationManager locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		SensorManager  sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		if(sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).size()>0)
			magSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
		if(sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).size()>0)
			accelSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);

		//Pick Location Provider
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UpdateFrequency,UpdateDistance,listener);

		sensorManager.registerListener(SensorListener, magSensor, SensorManager.SENSOR_DELAY_UI);
		sensorManager.registerListener(SensorListener, accelSensor, SensorManager.SENSOR_DELAY_UI);
		
		//Create Log Files
		//CreateLogFiles();
		Toast.makeText(this.getApplicationContext(), "Log Files created!", Toast.LENGTH_SHORT).show();
	}

	@Override
	public IBinder onBind(Intent callingIntent)
	{
		context = this.getApplicationContext();
		Toast.makeText(context, "Bind success!", Toast.LENGTH_LONG).show();
		DeviceAddress = context.getString(R.string.mainbt);
		switch (nav_system)
		{
			case 0:
				VestController.initialize(this.getApplicationContext(), DeviceAddress);
				VestController.setTestMode(false);
				break;
			case 1:
				PhoneController.initialize(this.getApplicationContext());
				SecondDeviceController.initialize(this.getApplicationContext());
				break;
		}
		StartTime = SystemClock.elapsedRealtime();
		navigatorLibrary = new HapticNavigationBackend(context);
		return binder;
	}

	@Override
	public boolean onUnbind(Intent callingIntent)
	{
		switch(nav_system)
		{
			case 0:
				VestController.setVibrationIntensities(new int[]{0,0,0});
				VestController.DestroyConnection();
				break;
			case 1:
				PhoneController.terminate_connection();
				break;
		}
		
		//Remove listeners
		LocationManager locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		locationManager.removeUpdates(listener);
		
		SensorManager  sensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
		sensorManager.unregisterListener(SensorListener);
		
		Toast.makeText(context, "Service Stopped!", Toast.LENGTH_SHORT).show();
		return false;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId)
	{
		//Get input parameters
		Bundle inputs = intent.getExtras();
		if (inputs == null)
		{
			Toast.makeText(getApplicationContext(), "No Service Configuration Received", Toast.LENGTH_LONG).show();
			return START_FLAG_REDELIVERY;
		}
		else
		{
			nav_system = Integer.parseInt(inputs.getString("nav_system").trim());
			destination = inputs.getString("destination").trim();
			return START_STICKY;
		}
	}

	public class VestBinder extends Binder
	{
		public NavigationService getService()
		{
			return NavigationService.this;
		}
	}

	//Update current location
	public void updateDeviceLocation(Location InputLocation)
	{
		navigatorLibrary.UpdateLocation(InputLocation);
	}
	//Update Destination Location
	public void updateDestinationLocation(Location InputDestination)
	{
		navigatorLibrary.UpdateDestination(InputDestination);
	}
	//Calculate directions and update Arduino
	public void WalkingNavigation(Location InputDestination, double CurrentBearing)
	{
		if(!navigatorLibrary.WalkingNavigator(InputDestination, CurrentBearing))
			Toast.makeText(this.getApplicationContext(), "HaptigoVest is not connected!", Toast.LENGTH_SHORT).show();
	}
	public void WalkingNavigation(double DeviceBearing, double WaypointBearing)
	{
		//Toast.makeText(this.getApplicationContext(),"HaptigoVest is called!: " + DeviceBearing + " : " + WaypointBearing, Toast.LENGTH_SHORT).show();
		if(!navigatorLibrary.WalkingNavigator(DeviceBearing, WaypointBearing))
			Toast.makeText(this.getApplicationContext(),"HaptigoVest is not connected!", Toast.LENGTH_SHORT).show();
	}

	public void CreateLogFiles()
	{
		String LocationHistoryFilename = "storage/sdcard0/Android/data/edu.tamu.haptigo/HaptigoStats/HaptigoLocationHistory/";
		String ElapsedTimeFilename =  "storage/sdcard0/Android/data/edu.tamu.haptigo/HaptigoStats/HaptigoElapsedTimes/";

		Time CurrentTime = new Time();
		CurrentTime.setToNow();

		LocationHistoryFilename = LocationHistoryFilename + CurrentTime.format2445() + ".txt";
		ElapsedTimeFilename = ElapsedTimeFilename +  CurrentTime.format2445() + ".txt";

		HistorylogFile = new File(LocationHistoryFilename);
		//HistorylogFile.mkdirs();
		ElapsedTimelogFile = new File(ElapsedTimeFilename);
		//ElapsedTimelogFile.mkdirs();

		if(!HistorylogFile.exists())
		{
			try
			{
				HistorylogFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		if(!ElapsedTimelogFile.exists())
		{
			try
			{
				ElapsedTimelogFile.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return;
	}
	public void LogLocationHistory(Location InputLocation)
	{
		String Input = "";
		double Latitude = InputLocation.getLatitude();
		double Longitude = InputLocation.getLongitude();

		Input = "(" + String.valueOf(Latitude) + "," + String.valueOf(Longitude) + ")\n";
		Input = Input + "Device Bearing:" + DeviceBearing + "," + "Waypoint Bearing:" + WaypointBearing + "\n";
		
		if (destination == "A")
			Input = Input + "Dest.A\n";
		else if (destination == "B")
			Input = Input + "Dest.B\n";
		else if (destination == "C")
			Input = Input + "Dest.C\n";
		else
			Input = Input + "Custom Destination\n";
		
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buffer = new BufferedWriter(new FileWriter(HistorylogFile, true)); 
			buffer.append(Input);
			buffer.newLine();
			buffer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return;
	}
	public void LogTestLocation(Location InputLocation)
	{
		String Input = "";
		double Latitude = InputLocation.getLatitude();
		double Longitude = InputLocation.getLongitude();

		Input = "(" + String.valueOf(Latitude) + "," + String.valueOf(Longitude) + ")\n";
		
		if (destination == "A")
			Input = Input + "Dest.A\n";
		else if (destination == "B")
			Input = Input + "Dest.B\n";
		else if (destination == "C")
			Input = Input + "Dest.C\n";
		else
			Input = Input + "Custom Destination\n";
		
		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buffer = new BufferedWriter(new FileWriter(HistorylogFile, true)); 
			buffer.append(Input);
			buffer.newLine();
			buffer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		return;
	}

	public void LogElapsedTimes(long ElapsedTime)
	{
		String Input = String.valueOf(ElapsedTime) + "milli-seconds";

		try
		{
			//BufferedWriter for performance, true to set append to file flag
			BufferedWriter buffer = new BufferedWriter(new FileWriter(ElapsedTimelogFile, true)); 
			buffer.append(Input);
			buffer.newLine();
			buffer.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return;
	}
	public Location LogCurrentLocation()
	{
		if(currentLocation != null)
		{
			LogTestLocation(currentLocation);
			return currentLocation;
		}
		else
			return null;
	}

}
