package com.blackphoenix.stopwatch;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class StopWatchActivity extends Activity {

	List<DataEntry> logData = null;
	DataEntry currentDataPoint = null;
	String username = "test";

	Button startButton, stopButton, newRoutineButton, logButton;
	TextView statusText;
	EditText usernameText;
	Spinner routineList, directionList;

	long startTime, endTime;
	String currentRoutine, currentDirection;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_stop_watch);

		// access the buttons and ui elements
		accessUIElements();
		listenToItemsSelected();

		logData = new ArrayList<DataEntry>();
		currentDataPoint = new DataEntry();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stop_watch, menu);
		return true;
	}

	/**
	 * Routine to start the watch for logging time
	 * 
	 * @param v
	 */
	public void startWatch(View v) {

		// note the start time and timestamp it
		startTime = System.currentTimeMillis();

		//block changing username and spinners
		usernameText.setEnabled(false);
		routineList.setEnabled(false);
		directionList.setEnabled(false);

		//enable stop button and disable start button
		startButton.setEnabled(false);
		logButton.setEnabled(true);
		stopButton.setEnabled(true);

		//change the status text
		statusText.setText("Watch Running ...");
	}

	public void logWatch(View v) {

		//note the username, routine and direction
		username = usernameText.getText().toString();
		currentDataPoint.routine = currentRoutine;
		currentDataPoint.direction = "log";
		currentDataPoint.timestamp = startTime;

		// note the start time and timestamp it
		endTime = System.currentTimeMillis();
		currentDataPoint.timeTaken = endTime - startTime;
		
		//log the data and create new data point
		logData.add(currentDataPoint);
		currentDataPoint = new DataEntry();

		//change the status text
		statusText.setText("Watch Running ... logged time");
	}

	/**
	 * Routine to stop the watch for logging time
	 * 
	 * @param v
	 */
	public void stopWatch(View v) {

		//note the username, routine and direction
		username = usernameText.getText().toString();
		currentDataPoint.routine = currentRoutine;
		currentDataPoint.direction = currentDirection;
		currentDataPoint.timestamp = startTime;

		endTime = System.currentTimeMillis();
		currentDataPoint.timeTaken = endTime - startTime;
		
		logData.add(currentDataPoint);

		//enable new routine 
		logButton.setEnabled(false);
		stopButton.setEnabled(false);
		newRoutineButton.setEnabled(true);

		//change the status text
		statusText.setText("Start New Routine or Click menu to change user");
	}

	/**
	 * Routine to start new routine details
	 * 
	 * @param v
	 */
	public void newRoutine(View v) {

		//log the data and create new data point
		currentDataPoint = new DataEntry();

		//enable start button to start the watch
		newRoutineButton.setEnabled(false);
		startButton.setEnabled(true);
		
		//enable the spinners to set new routine
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
			saveLoggedData();
			this.finish();
			break;

		case R.id.menu_change_user:

			//enable the edit box to change the user name
			usernameText.setEnabled(true);

			saveLoggedData();

			//empty the log data
			logData.clear();
			break;
		}

		return false;
	}

	private void saveLoggedData() {
		String filename = username + System.currentTimeMillis() + ".txt";
		File root = getExternalFilesDir(null);
		File resultsFile = new File(root, filename);

		try {

			FileOutputStream resultsStream = new FileOutputStream(resultsFile);

			for (DataEntry dataPoint : logData) {

				String dataString = dataPoint.timestamp + ","
						+ dataPoint.routine + "," + dataPoint.direction + ","
						+ dataPoint.timeTaken + "\n";

				resultsStream.write(dataString.getBytes());
			}

			resultsStream.close();
		} catch (Exception e) {
		}
	}

	private void accessUIElements() {

		// access buttons
		startButton = (Button) findViewById(R.id.btn_start);
		stopButton = (Button) findViewById(R.id.btn_stop);
		newRoutineButton = (Button) findViewById(R.id.btn_new_routine);
		logButton = (Button) findViewById(R.id.btn_log);

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

			currentRoutine = routineList.getSelectedItem().toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {

			currentRoutine = routineList.getItemAtPosition(0)
					.toString();
		}
	};

	/**
	 * Listening to direction selected from the spinner
	 */
	private AdapterView.OnItemSelectedListener directionSelectedListener = new OnItemSelectedListener() {

		@Override
		public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {

			currentDirection = directionList.getSelectedItem()
					.toString();
		}

		@Override
		public void onNothingSelected(AdapterView<?> arg0) {
			currentDirection = directionList.getItemAtPosition(0)
					.toString();
		}
	};
}
