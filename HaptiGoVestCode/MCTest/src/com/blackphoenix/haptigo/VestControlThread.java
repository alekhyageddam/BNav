package com.blackphoenix.haptigo;

import android.content.Context;
import android.util.Log;
import at.abraxas.amarino.Amarino;

public class VestControlThread implements Runnable {

	//Thread sleep time or inter stimulus time
	public static final int ISI_TIME = 3000;
	// constants that define the length of the vibes and off time
	public static final int SHORT_BUZZ = 400, LONG_BUZZ = 600,
			BUZZ_OFF_TIME = 150;

	// constants that define the amplitude of vibes and index of vibes in
	// intensity array
	public static final int VIBE_HIGH = 255, VIBE_LOW = 0;
	public static final int VIBE_LEFT_INDEX = 0, VIBE_RIGHT_INDEX = 2,
			VIBE_CENTER_INDEX = 1;

	// constants defining the possible direction commands to the vest
	public static final int STRAIGHT = 0, BACK = 1, LEFT = 2, RIGHT = 4,
			DESTINATION = 8, STOP = 16;

	// intensity array to be sent to haptigo vest
	private int[] vibe_intensities = { 0, 0, 0 };
	private static int[] vibe_off_intensities = { VIBE_LOW, VIBE_LOW, VIBE_LOW };

	// current direction signal, number of pulses and buzz duration
	private int myDirectionSignal = STOP;
	private int myNumberOfPulses = 1;
	private int myBuzzLength = SHORT_BUZZ;

	// Application context for Amarino and Haptigo Vest Bluetooth Address
	private Context context;
	private String deviceAddress;
	private boolean vestConnected = false;
	private boolean shouldContinue = true;

	public VestControlThread(Context context, final String bluetoothAddress) {

		this.context = context;
		deviceAddress = bluetoothAddress;
		Amarino.connect(context, deviceAddress);
	}

	/**
	 * check if the haptigo vest is connected checking the bluetooth connection
	 * 
	 * @return
	 */
	public boolean isVestConnected() {
		return vestConnected;
	}

	/**
	 * set if the bluetooth connection with haptigo vest has been made. use the
	 * Arduino receiver intent to set this.
	 * 
	 * @param vestConnected
	 */
	public void setVestConnected(boolean vestConnected) {
		this.vestConnected = vestConnected;
	}

	@Override
	public void run() {

		while (shouldContinue) {
			if (!vestConnected)
				continue;

			assignVibeIntensities();
			
			for (int pulseCount = 0; pulseCount < myNumberOfPulses; pulseCount++) {
				
				try {
					setVibrationIntensities(vibe_intensities);
					Thread.sleep(myBuzzLength);

					setVibrationIntensities(vibe_off_intensities);
					Thread.sleep(BUZZ_OFF_TIME);

				} catch (InterruptedException e) {
					
					Log.e("Vest Control", e.getMessage());
				}
			}
			
			try {
				Thread.sleep(ISI_TIME);
				
			} catch (InterruptedException e) {
				Log.e("Vest Control", e.getMessage());
			}
		}
	}

	/**
	 * Set the instructions for the vest - direction signal , number of pulses
	 * and the length of the pulse
	 * 
	 * @param direction
	 *            - STRAIGHT | LEFT | RIGHT | BACK | DESTINATION | STOP
	 * @param numberOfPulses
	 *            - 1 | 2 | 3
	 * @param buzzLength
	 *            - SHORT_BUZZ | LONG_BUZZ
	 */
	public synchronized void setInstruction(int direction, int numberOfPulses,
			int buzzLength) {

		this.myDirectionSignal = direction;
		this.myBuzzLength = buzzLength;
		this.myNumberOfPulses = numberOfPulses;
	}

	/**
	 * Function to assign vibe intensities for tactors on the haptigo vest based
	 * on the direction signal
	 */
	private void assignVibeIntensities() {

		switch (myDirectionSignal) {
		case STRAIGHT:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_HIGH;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_LOW;
			break;

		case LEFT:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_HIGH;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_LOW;
			break;

		case RIGHT:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_HIGH;
			break;

		case BACK:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_HIGH;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_HIGH;
			break;

		case DESTINATION:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_HIGH;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_HIGH;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_HIGH;
			break;

		default:
		case STOP:
			vibe_intensities[VIBE_LEFT_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_CENTER_INDEX] = VIBE_LOW;
			vibe_intensities[VIBE_RIGHT_INDEX] = VIBE_LOW;
			break;

		}
	}

	// Set the vibration intensity of directional tactors on vest
	private void setVibrationIntensities(int[] intensities) {
		Amarino.sendDataToArduino(context, deviceAddress, 'i', intensities);
	}

	//Disconnect from Arduino
	public synchronized void destroyConnection() {
		Amarino.disconnect(context, deviceAddress);
		vestConnected = false;
	}
	
	//connect from Arduino
	public synchronized void connect() {
		
		if(!vestConnected)
			Amarino.connect(context, deviceAddress);
	}
	
	public synchronized void stop(){
		
		shouldContinue = false;
		destroyConnection();
	}
}
