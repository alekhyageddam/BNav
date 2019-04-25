package com.blackphoenix.mctutorial;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.blackphoenix.haptigo.NavGuide;
import com.blackphoenix.haptigo.VestController;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import at.abraxas.amarino.AmarinoIntent;

public class Haptigo_Tutorial_Activity extends Activity {

	public static final String HAPTIGO_BT_ADDRESS = "00:06:66:4F:94:8B";
	private boolean connectDevice = true;

	View routineDetails = null;
	private Button startButton;
	private Button stopButton;
	private Button newRoutineButton;
	private TextView statusText;
	private EditText usernameText;
	private Spinner routineList;
	private Spinner directionList;

	private int currentDirection = VestController.STRAIGHT;
	private int currentRoutine = Circuit.IDEALLEADDISTANCE;
	private String currentDirectionType = "Straight";
	private String currentRoutineType = "LD";

	private boolean startNavGuide = false;
	List<DataEntry> logData = null;
	DataEntry currentDataPoint = null;

	private String username = "test";
	private long startTime = 0;

	private LocationManager myLocationManager;

	private long previousLocationUpdateTime = 0;
	private long currentLocationUpdateTime = 0;
	
	// location manager and location update listener

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_haptigo_tutorial);

		// get the group of ui elements used for changing routine details
		accessUIElements();
		listenToItemsSelected();

		// create log data list
		logData = new ArrayList<DataEntry>();
		currentDataPoint = new DataEntry();

		// Connect and listen to vest broadcast receiver
		connectVest();

		// create the location manager
		myLocationManager = (LocationManager) this
				.getSystemService(Context.LOCATION_SERVICE);

		// register location updates for navigation
		myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				NavGuide.LOCATION_UPDATE_INTERVAL, NavGuide.WAYPOINT_REACHED_THRESHOLD, locationListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.haptigo__tutorial_, menu);
		return true;
	}

	/**
	 * Routine to start the watch for logging time
	 * 
	 * @param v
	 */
	public void startRoutineLog(View v) {

		// note the start time and timestamp it
		startTime = previousLocationUpdateTime = System.currentTimeMillis();
		startNavGuide = true;

		username= usernameText.getText().toString();
		// set Route
		NavGuide.setRoute(Circuit.getRoute(currentRoutine, currentDirection));

		createNewDataPoint();

		// // register location updates for navigation
		// myLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
		// 3000, 0, locationListener);

		// block changing username and spinners
		usernameText.setEnabled(false);
		routineList.setEnabled(false);
		directionList.setEnabled(false);

		// enable stop button and disable start button
		startButton.setEnabled(false);
		stopButton.setEnabled(true);

		// change the status text
		statusText.setText("Routine log started ...");
		Toast message = Toast.makeText(getApplicationContext(),
				"Circuit log started", Toast.LENGTH_SHORT);
		message.show();
	}

	/**
	 * Routine to stop the routine and log the data
	 * 
	 * @param v
	 */
	public void stopRoutineLog(View v) {

		startNavGuide = false;

		// save the log data
		saveLoggedData();

		// clear the log data
		logData.clear();

		// unregister location updates for navigation
		// myLocationManager.removeUpdates(locationListener);

		// enable new routine
		stopButton.setEnabled(false);
		newRoutineButton.setEnabled(true);

		// change the status text
		statusText.setText("Start New Routine or Click menu to change user ::" + logData.size() );
	}

	/**
	 * Routine to start new routine details
	 * 
	 * @param v
	 */
	public void newRoutine(View v) {

		// enable start button to start the watch
		newRoutineButton.setEnabled(false);
		startButton.setEnabled(true);

		// enable the spinners to set new routine
		routineList.setEnabled(true);
		directionList.setEnabled(true);
	}

	/**
	 * Perform exit and change user menu item functions
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case R.id.menu_exit:

			myLocationManager.removeUpdates(locationListener);
			
			// break the connection to vest
			unregisterReceiver(ConnectedArduinoReceiver);
			unregisterReceiver(DisConnectedArduinoReceiver);

			VestController.DestroyConnection();

			this.finish();
			break;

		case R.id.menu_change_user:

			// enable the edit box to change the user name
			usernameText.setEnabled(true);

			// change the user name
			statusText.setText("Change the username");
			// empty the log data
			logData.clear();
			break;

		case R.id.menu_connect_vest:
			// connect to vest
			if (!VestController.IsConnected()) {
				VestController.MakeConnection();
			}
		}

		return false;
	}

	private void saveLoggedData() {
		String filename = username + "_" + currentRoutineType + "_"
				+ currentDirectionType + "_" + System.currentTimeMillis()
				+ ".txt";
		File root = getExternalFilesDir(null);
		File resultsFile = new File(root, filename);

		FileOutputStream resultsStream = null;

		try {

			resultsStream = new FileOutputStream(resultsFile);

			for (DataEntry dataPoint : logData) {

				resultsStream.write(dataPoint.getStringVersion().getBytes());
			}

		} catch (Exception e) {

			Toast errorMessage = Toast.makeText(getApplicationContext(),
					"Error in logging", Toast.LENGTH_SHORT);
			errorMessage.show();
		}

		try {
			if (resultsStream != null)
				resultsStream.close();

		} catch (IOException e) {
			Toast errorMessage = Toast.makeText(getApplicationContext(),
					"Error in closing log file", Toast.LENGTH_SHORT);
			errorMessage.show();
		}
	}

	private void connectVest() {

		// register to receive haptigo connection events
		registerReceiver(ConnectedArduinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_CONNECTED));

		// register to receive haptigo disconnection or failed connection
		IntentFilter disconnectionFilter = new IntentFilter(
				AmarinoIntent.ACTION_DISCONNECTED);
		disconnectionFilter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
		registerReceiver(DisConnectedArduinoReceiver, disconnectionFilter);

		// Connect to vest
		VestController.initialize(getApplicationContext(), HAPTIGO_BT_ADDRESS);
	}

	/**
	 * Access the UI elements to enable or disable them
	 */
	private void accessUIElements() {

		// access the routine Details view
		routineDetails = (View) findViewById(R.id.layout_routine_values);

		// access buttons
		startButton = (Button) findViewById(R.id.btn_start);
		stopButton = (Button) findViewById(R.id.btn_stop);
		newRoutineButton = (Button) findViewById(R.id.btn_new_routine);

		// access textview and edittext
		statusText = (TextView) findViewById(R.id.status_text);
		usernameText = (EditText) findViewById(R.id.value_username);

		// access spinners
		routineList = (Spinner) findViewById(R.id.value_routine);
		directionList = (Spinner) findViewById(R.id.value_direction);
	}

	private void listenToItemsSelected() {

		routineList.setOnItemSelectedListener(routineSelectedListener);
		directionList.setOnItemSelectedListener(directionSelectedListener);
	}

	/**
	 * Listening to routine selected from the spinner
	 */
	private AdapterView.OnItemSelectedListener routineSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			currentRoutine = routineList.getSelectedItemPosition();

			currentRoutineType = routineList.getSelectedItem().toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

			currentRoutine = 0;

			currentRoutineType = routineList.getItemAtPosition(0).toString();
		}
	};

	/**
	 * Listening to direction selected from the spinner
	 */
	private AdapterView.OnItemSelectedListener directionSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			currentDirection = directionList.getSelectedItemPosition();

			currentDirectionType = directionList.getSelectedItem().toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			currentDirection = 0;

			currentDirectionType = directionList.getItemAtPosition(0)
					.toString();
		}
	};

	private void createNewDataPoint() {

		currentDataPoint = new DataEntry();
		currentDataPoint.timestamp = System.currentTimeMillis();
		currentDataPoint.username = username;
		currentDataPoint.directionType = currentDirectionType;
		currentDataPoint.routineType = currentRoutineType;
	}

	// Create receiver for Arduino events
	private BroadcastReceiver ConnectedArduinoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String Connected_Devices = null;
			Connected_Devices = intent
					.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			if (Connected_Devices == null
					|| !HAPTIGO_BT_ADDRESS.equals(Connected_Devices)) {
				connectDevice = true;
				// miConnectVest.setEnabled(true);
				// vestController.setVestConnected(false);
				VestController.Connected = false;

				Toast toast = Toast.makeText(context, "Connecting...",
						Toast.LENGTH_SHORT);
				toast.show();
			} else {
				connectDevice = false;
				// vestController.setVestConnected(true);
				VestController.Connected = false;
				// miConnectVest.setEnabled(false);

				Toast toast = Toast.makeText(context,
						"Device already connected", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};

	// Create receiver for Arduino events
	private BroadcastReceiver DisConnectedArduinoReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// String Connected_Devices = null;

			connectDevice = true;
			// vestController.setVestConnected(false);
			VestController.Connected = false;

			Toast toast = Toast.makeText(context, "Connecting...",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	};

	private LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {

			// TODO: UNCOMMENT after debug session
			// if(!VestController.IsConnected())
			// {
			// statusText.setText("Connect to Vest");
			// return;
			// }

//			currentLocationUpdateTime = System.currentTimeMillis();
//			
//			if(currentLocationUpdateTime - previousLocationUpdateTime < NavGuide.LOCATION_UPDATE_INTERVAL){
//				
//				return;
//			}
//			
//			previousLocationUpdateTime = currentLocationUpdateTime;
			if (startNavGuide) {

				NavGuide.guideDriver(location, getApplicationContext());

				if (NavGuide.canLogData()) {

					NavGuide.logData(currentDataPoint);

					statusText.setText("Next Turn Signal ::"
							+ currentDataPoint.nextDirection + "," +currentDataPoint.distanceToWayPoint);
					logData.add(currentDataPoint);

					createNewDataPoint();
				}else{
					
					statusText.setText("Stopped logging");
				}
			}
		}
	};
}
