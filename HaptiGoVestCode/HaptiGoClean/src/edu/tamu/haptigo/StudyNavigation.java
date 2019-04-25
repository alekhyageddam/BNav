package edu.tamu.haptigo;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import edu.tamu.haptigo.JsonDirectionsParser.GoogleLocation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.util.Log;

/* GOOGLE REQUIREMENTS:
 * (1) Query Limit of 2,500 directions requests per day. (Transit directions will count as 4 requests)
 * (2) Individual requests for driving, walking, or cycling directions may contain up to 8 intermediate waypoints in the request.
 * (3) The Directions API may only be used in conjunction with displaying results on a Google map;using Directions data without displaying a map for which directions data was requested is prohibited.
 * (4) Calculation of directions generates copyrights and warnings which must be displayed to the user in some fashion. 
 */

public class StudyNavigation extends FragmentActivity
{
	//Environment variables
	private String nav_system;
	private String destination;
	private Context context;
	private NavigationService backgroundNavigation;
	private Location currentLocation;
	private boolean IsBound;
	private PolylineOptions nav_path;
	public static List<GoogleLocation> WayPoints = new ArrayList<GoogleLocation>();

	//UI Controls
	private Button Start;
	private Button Stop;
	private GoogleMap pathMap;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.navimain);
		context = this.getApplicationContext();
    	Toast.makeText(context, "Connecting to device", Toast.LENGTH_LONG).show();
    	//Ashish
    	//SecondDeviceController.initialize(context);
		//Get GPS fix
    	//SecondDeviceController.vibrate_left();
    	
		currentLocation = getLastBestLocation(30, 500);
		if (currentLocation == null)
			Toast.makeText(getApplicationContext(), "Could not get GPS fix!", Toast.LENGTH_LONG).show();

		//Get input parameters
		Bundle inputs = getIntent().getExtras();
		if (inputs == null)
		{
			Toast.makeText(getApplicationContext(), "No Configuration Received", Toast.LENGTH_LONG).show();
			return;
		}
		else
		{
			nav_system = inputs.getString("nav_system");
			destination = inputs.getString("destination").replace(" ", "");
			nav_path = QueryDirections();
			nav_path.color(Color.RED);
			nav_path.geodesic(true);
		}

		//Initialize UI elements
		Start = (Button)findViewById(R.id.StartNav);
		Stop = (Button)findViewById(R.id.StopNav);
//		pathMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapview)).getMap();
//		pathMap.clear();
//		
//		if (pathMap != null)
//		{
//			
//			for(int i = 0; i < nav_path.getPoints().size(); i++)
//			{
//				MarkerOptions tempMarker = new MarkerOptions();
//				tempMarker.position(nav_path.getPoints().get(i));
//				tempMarker.title("Point " + String.valueOf(i));
//				tempMarker.draggable(false);
//				pathMap.addMarker(tempMarker);
//				
//			}
//			pathMap.addPolyline(nav_path);
//
//			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(nav_path.getPoints().get(0), 17);
//			pathMap.animateCamera(cameraUpdate);
//		}
//		else
//		{
//			Toast.makeText(getApplicationContext(), "Could not intialize map", Toast.LENGTH_LONG).show();
//			return;
//		}

		//Create Listener
		OnClickListener ButtonClickListener = new OnClickListener()
		{
			public void onClick(View Element)
			{
				if (Element == Start)
					BindNavService();
				else if (Element == Stop)
				{
					UnBindNavService();
					Log.i ("info", "finish");
					finish();
				}
			}
		};

		//Assign listeners
		Start.setOnClickListener(ButtonClickListener);
		Stop.setOnClickListener(ButtonClickListener);
	}
	
	private final LocationListener listener = new LocationListener() 
	{
		@Override
		public void onLocationChanged(Location InputLocation)
		{
			currentLocation = InputLocation;
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub

		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub

		}
	};
	private ServiceConnection NavConnection = new ServiceConnection()
	{
		@Override
		public void onServiceConnected(ComponentName className, IBinder service)
		{
			NavigationService.VestBinder binder = (NavigationService.VestBinder) service;
			backgroundNavigation = binder.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name)
		{
			backgroundNavigation = null;
			Toast.makeText(getApplicationContext(), "Service connection failure!", Toast.LENGTH_LONG).show();
		}
	};

	void BindNavService()
	{
		Intent bindIntent = new Intent(this, NavigationService.class);
		bindIntent.putExtra("nav_system", nav_system);
		bindIntent.putExtra("destination", destination);
		startService(bindIntent);
		bindService(bindIntent, NavConnection, Context.BIND_AUTO_CREATE);
		IsBound = true;
		Toast.makeText(getApplicationContext(), "Navigation commencing...!", Toast.LENGTH_LONG).show();
	}

	void UnBindNavService()
	{
		Log.i ("info", "UnBindNavService");
		if (IsBound)
		{
//			if(NavConnection != null){
//			getApplicationContext().unbindService(NavConnection);
//			}
			Log.i ("info", "UnBindNavService done");
			IsBound = false;
		}
	}

	public GoogleLocation setLocation(Double input_lat, Double input_lng)
	{
		GoogleLocation tempLocation = new GoogleLocation();
		tempLocation.setlat(input_lat);
		tempLocation.setlng(input_lng);
		return tempLocation;
	}

	public PolylineOptions convertWayPointsToPath()
	{
		PolylineOptions destPath = new PolylineOptions();

		for (GoogleLocation tempLocation: WayPoints)
		{
			destPath.add(new LatLng(tempLocation.getlat(), tempLocation.getlng()));
		}
		return destPath;
	}
	
	//Adapted from code by Reto Meier
	public Location getLastBestLocation(int minDistance, long minTime)
	{
	    Location bestResult = null;
	    LocationManager locationManager =(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
	    
	    float bestAccuracy = Float.MAX_VALUE;
	    long bestTime = Long.MIN_VALUE;
	   
	    // Iterate through all the providers on the system, keeping
	    // note of the most accurate result within the acceptable time limit.
	    // If no result is found within maxTime, return the newest Location.
	    List<String> matchingProviders = locationManager.getAllProviders();
	    for (String provider: matchingProviders)
	    {
	      Location location = locationManager.getLastKnownLocation(provider);
	      if (location != null)
	      {
	        float accuracy = location.getAccuracy();
	        long time = location.getTime();
	       
	        if ((time > minTime && accuracy < bestAccuracy))
	        {
	          bestResult = location;
	          bestAccuracy = accuracy;
	          bestTime = time;
	        }
	        else if (time < minTime && bestAccuracy == Float.MAX_VALUE && time > bestTime)
	        {
	          bestResult = location;
	          bestTime = time;
	        }
	      }
	    }
	   
	    // If the best result is beyond the allowed time limit, or the accuracy of the
	    // best result is wider than the acceptable maximum distance, request a single update.
	    // This check simply implements the same conditions we set when requesting regular
	    // location updates every [minTime] and [minDistance].
	    if (listener != null && (bestTime < minTime || bestAccuracy > minDistance))
	    {
	    	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 10,listener);
	    }
	   
	    return bestResult;
	  }

	public PolylineOptions QueryDirections()
	{
		PolylineOptions navPath = new PolylineOptions();

		/* TeagueToCommons:
		(30.61689, -96.33587),(30.616234,-96.336855), (30.61575, -96.33646)
		 */
		WayPoints.clear();
		if (destination.trim().equals("B"))
		{
			WayPoints.add(setLocation(30.61437726, -96.3386965));
			WayPoints.add(setLocation(30.61468582, -96.33909722));
			WayPoints.add(setLocation(30.61435788, -96.33973344));
		}
		else if (destination.trim().equals("C"))
		{
			WayPoints.add(setLocation(30.61359819, -96.33823121));
			WayPoints.add(setLocation(30.61425184, -96.33868342));
			WayPoints.add(setLocation(30.61466632, -96.33810901));
		}
		else if (destination.trim().equals("A"))
		{
			WayPoints.add(setLocation(30.615245, -96.338386));
			WayPoints.add(setLocation(30.614834, -96.338011));
			WayPoints.add(setLocation(30.614298, -96.338799));
		}
		else
		{
			ObjectMapper mapper = new ObjectMapper();
			try
			{
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpResponse query = httpClient.execute(new HttpGet("http://maps.googleapis.com/maps/api/directions/json?origin="+ currentLocation.getLatitude() + "," + currentLocation.getLongitude() + "&destination=" + destination +"&sensor=true&mode=walking"));
				InputStream response = query.getEntity().getContent();
				JsonDirectionsParser sample_query = mapper.readValue(response, JsonDirectionsParser.class);	
				WayPoints = sample_query.getDirections();

				/*// display to console
				String temp = WayPoints.toString();
				Toast.makeText(getApplicationContext(), temp, Toast.LENGTH_LONG).show();
				System.out.println(mapper.defaultPrettyPrintingWriter().writeValueAsString(sample_query));*/
			}
			catch (JsonGenerationException e)
			{
				e.printStackTrace();
			}
			catch (JsonMappingException e)
			{
				e.printStackTrace();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		navPath = convertWayPointsToPath();
		return navPath;
	}
}