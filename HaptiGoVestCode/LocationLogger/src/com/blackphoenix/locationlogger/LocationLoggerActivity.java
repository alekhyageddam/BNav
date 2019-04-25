package com.blackphoenix.locationlogger;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class LocationLoggerActivity extends Activity {

	private TextView tvLatValue, tvLongValue, tvDistanceValue;
	private List<Location> loggedLocations;
	private LocationManager myLocationManager;
	private Location myMarkedLocation = null;
	private Location myCurrentLocation = null;
	
	private Button btnMarkLocation, btnLogLocation;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_logger);
		
		//initiate the textview to update the location and distance values
		tvLatValue = (TextView) findViewById(R.id.tv_latitude_value);
		tvLongValue = (TextView) findViewById(R.id.tv_longitude_value);
		tvDistanceValue = (TextView) findViewById(R.id.tv_distance_value);
		
		btnLogLocation = (Button) findViewById(R.id.btn_log_location);
		btnMarkLocation = (Button) findViewById(R.id.btn_mark_location);
		
		//initializing the location logger list
		loggedLocations = new ArrayList<Location>();
		initializeLocationService();		
	}

	private void initializeLocationService(){
		
		myLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		LocationListener locationListener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String provider) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location location) {
				
				myCurrentLocation = location;
				
				if(myMarkedLocation!=null){
				
					float distanceToMarkedLoc = location.distanceTo(myMarkedLocation);
					tvDistanceValue.setText(distanceToMarkedLoc+"");					
				}
				
				btnLogLocation.setEnabled(true);
				btnMarkLocation.setEnabled(true);
			}
		};
		
		myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 10, locationListener);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.location_logger, menu);
		return true;
	}
	
	public void logMyLocation(View v){
		
		loggedLocations.add(myCurrentLocation);
	}
	
	public void markMyLocation(View v){
		
		myMarkedLocation = myCurrentLocation;
		
		tvLatValue.setText(myCurrentLocation.getLatitude() + "");
		tvLongValue.setText(myCurrentLocation.getLongitude() + "");
		tvDistanceValue.setText("0");
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.menu_exit:
			saveLoggedLocations();
			this.finish();
		}
		
		return false;
	}
	
	private void saveLoggedLocations(){
		String filename = "locations_" + System.currentTimeMillis() + ".txt";
		File root = getExternalFilesDir(null);
		File resultsDir = new File(root, "results");
		File resultsFile = new File (resultsDir, filename);

		resultsDir.mkdirs();

		try {

			FileOutputStream resultsStream = new FileOutputStream(resultsFile);
			
			for(Location l:loggedLocations){
				
				String locString = l.getLatitude() + "," +l.getLongitude() +"\n";
				resultsStream.write(locString.getBytes());
			}
			
			resultsStream.close();
		}catch(Exception e){}
	}
}
