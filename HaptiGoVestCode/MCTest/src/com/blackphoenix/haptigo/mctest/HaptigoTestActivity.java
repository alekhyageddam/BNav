package com.blackphoenix.haptigo.mctest;

import com.blackphoenix.haptigo.VestControlThread;
import com.blackphoenix.haptigo.VestController;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.AmarinoIntent;

public class HaptigoTestActivity extends Activity implements OnSeekBarChangeListener {

//	public static final String HAPTIGO_BT_ADDRESS = "00:06:66:4F:94:79";
	public static final String HAPTIGO_BT_ADDRESS = "00:06:66:4F:94:8B";

	private boolean connectDevice = true;

//	private VestControlThread vestController;
	private TextView statusText;
	private SeekBar signalLengthSlider;
	private TextView signalLengthText;
	
	// Dialog showed to change the light/vibe mode
	AlertDialog changeModeDialog;
	
	private int buzzLength = VestController.SHORT_BUZZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_haptigo_test);

		VestController.initialize(getApplicationContext(), HAPTIGO_BT_ADDRESS);

		statusText = (TextView) findViewById(R.id.text_status);
		signalLengthText = (TextView) findViewById(R.id.text_signal_length_value);

		// register to receive haptigo connection events
		registerReceiver(ConnectedArduinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_CONNECTED));

		// register to receive haptigo disconnection or failed connection
		IntentFilter disconnectionFilter = new IntentFilter(
				AmarinoIntent.ACTION_DISCONNECTED);
		disconnectionFilter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
		registerReceiver(DisConnectedArduinoReceiver, disconnectionFilter);
		
		//adding a listener to signal length slider
		signalLengthSlider = (SeekBar) findViewById(R.id.slider_signal_length);
		signalLengthSlider.setOnSeekBarChangeListener(this);
		
		//setting the default buzz length
		buzzLength = this.getResources().getInteger(R.integer.default_signal_length);
		
		
		//Create the dialog to change the mode light/vibe
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
		dialogBuilder.setTitle(R.string.change_mode_title)
			.setSingleChoiceItems(R.array.modes, 1, new OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					//changing the based on the option clicked 
					if(which == 0){
						
						VestController.setLightMode();
						statusText.setText("Light Mode");
						
					}else{
						
						VestController.setVibeMode();
						statusText.setText("Vibe Mode");
					}
					
					dialog.dismiss();
				}
			});
		
		changeModeDialog =  dialogBuilder.create();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.haptigo_test, menu);
		return true;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();

		// unregisterReceiver(ConnectedArduinoReceiver);
		// unregisterReceiver(DisConnectedArduinoReceiver);
		VestController.DestroyConnection();
	}

	public void onInstructionClicked(View v) {

		if (connectDevice == false)
			statusText
					.setText("Connect to Haptigo Vest. Click Menu -> Connect / Disconnect.");

		switch (v.getId()) {

		case R.id.bt_b1:
			conveyInstructions(VestController.BACK, 1);
			break;

		case R.id.bt_b2:
			conveyInstructions(VestController.BACK, 2);
			break;

		case R.id.bt_b3:
			conveyInstructions(VestController.BACK, 3);
			break;

		case R.id.bt_f1:
			conveyInstructions(VestController.STRAIGHT, 1);
			break;

//		case R.id.bt_f2:
//			conveyInstructions(VestController.STRAIGHT, 1);
//			break;
//
//		case R.id.bt_f3:
//			conveyInstructions(VestController.STRAIGHT, 1);
//			break;

		case R.id.bt_l1:
			conveyInstructions(VestController.LEFT, 1);
			break;

		case R.id.bt_l2:
			conveyInstructions(VestController.LEFT, 2);
			break;

		case R.id.bt_l3:
			conveyInstructions(VestController.LEFT, 3);
			break;

		case R.id.bt_r1:
			conveyInstructions(VestController.RIGHT, 1);
			break;

		case R.id.bt_r2:
			conveyInstructions(VestController.RIGHT, 2);
			break;

		case R.id.bt_r3:
			conveyInstructions(VestController.RIGHT, 3);
			break;

		case R.id.bt_stop:
		default:
			conveyInstructions(VestController.DESTINATION, 3);
			break;
		}
	}

	private void conveyInstructions(int direction, int numberOfPulses) {

		int[] intensities;

		switch (direction) {
		case VestController.LEFT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);
			intensities = new int[] { VestController.VIBE_HIGH,
					VestController.VIBE_LOW, VestController.VIBE_LOW };

			VestController.setVibrationIntensities(intensities,
						buzzLength, numberOfPulses);
			statusText.setText("LEFT ::" + buzzLength
					+ " :: " + numberOfPulses);
			break;

		case VestController.RIGHT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			intensities = new int[] { VestController.VIBE_LOW,
					VestController.VIBE_LOW, VestController.VIBE_HIGH };

			VestController.setVibrationIntensities(intensities,
					buzzLength, numberOfPulses);
			statusText.setText("RIGHT ::" + buzzLength
					+ " :: " + numberOfPulses);
			break;

		case VestController.BACK:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.LONG_BUZZ);

			intensities = new int[] { VestController.VIBE_HIGH,
					VestController.VIBE_LOW, VestController.VIBE_HIGH };

			VestController.setVibrationIntensities(intensities,
					buzzLength, numberOfPulses);
			statusText.setText("BOTH ::" + buzzLength + " :: "
					+ numberOfPulses);
			break;

		case VestController.STRAIGHT:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			intensities = new int[] { VestController.VIBE_LOW,
					VestController.VIBE_HIGH, VestController.VIBE_LOW };

			VestController.setVibrationIntensities(intensities,
					buzzLength, numberOfPulses);
			statusText.setText("STRAIGHT ::" + buzzLength
					+ " :: " + numberOfPulses);
			break;

		default:
		case VestController.STOP:
			// vestController.setInstruction(direction, numberOfPulses,
			// VestControlThread.SHORT_BUZZ);

			VestController.resetVibrationIntensities();
			statusText.setText("STOP ::" + VestControlThread.SHORT_BUZZ
					+ " :: " + numberOfPulses);
			break;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_connect_vest:

			// vestController.connect();
			VestController.MakeConnection();
			break;

		case R.id.menu_change_mode:
			
			//Show the dialog for changing the mode
			changeModeDialog.show();
			break;
		case R.id.menu_exit:

			unregisterReceiver(ConnectedArduinoReceiver);
			unregisterReceiver(DisConnectedArduinoReceiver);

			// vestController.stop();
			VestController.DestroyConnection();

			this.finish();
			break;
		}

		return false;
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
//			String Connected_Devices = null;

			connectDevice = true;
			//miConnectVest.setEnabled(true);
			// vestController.setVestConnected(false);
			VestController.Connected = false;

			Toast toast = Toast.makeText(context, "Connecting...",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// Change the buzz length the text view to show the changed buzz length
		buzzLength = progress;
		signalLengthText.setText(buzzLength+"");
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Nothing done here
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Nothing done here
		
	}
}
