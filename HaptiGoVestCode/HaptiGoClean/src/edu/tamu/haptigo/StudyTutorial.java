package edu.tamu.haptigo;

import edu.tamu.haptigo.SurfaceControlView.ValuesChangeListener;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import at.abraxas.amarino.AmarinoIntent;

//This activity implements the tutorial for the Pocket Navigator and HaptiGo Nav. Systems.
public class StudyTutorial extends Activity
{
	//Environment Variables
	private boolean Connect_Device = true;
	private static final String PREFS_NAME = "HaptigoTestSettings";
	private SharedPreferences settings;
	private String deviceAddress;
	private Context context;

	//UI Controls
	private SurfaceControlView surfaceControl;
	private Button reconnect;
	private Button startStudy;
	private Button forward;
	private Button left;
	private Button right;
	private Button turnaround;
	
	//private static final String BT_ADDRESS="00:06:66:42:22:7C";
	private static final String BT_ADDRESS="00:06:66:4F:94:8B";

	//Output Data
	private TextView OutputIntensities;
	public static int[] intensities = new int[]{0,0,0};


	//Create receiver for Arduino events
	private BroadcastReceiver ArduinoReceiver = new BroadcastReceiver()
	{
		@Override
		public void onReceive(Context context, Intent intent)
		{
			String Connected_Devices = null;
			Connected_Devices = intent.getStringExtra(AmarinoIntent.EXTRA_DEVICE_ADDRESS);
			if(deviceAddress != Connected_Devices)
			{
				Connect_Device = true;
				Toast toast = Toast.makeText(context, "Connecting...", Toast.LENGTH_SHORT);
				toast.show();
			}
			else
			{
				Connect_Device = false;
				Toast toast = Toast.makeText(context, "Device already connected", Toast.LENGTH_SHORT);
				toast.show();
			}

			String Input = null;
			int Test_Input = -1;

			// Extract data sent by Arduino
			final int dataType = intent.getIntExtra(AmarinoIntent.EXTRA_DATA_TYPE, -1);
			if (dataType == AmarinoIntent.STRING_EXTRA)
			{
				Input =  intent.getStringExtra(AmarinoIntent.EXTRA_DATA);
			}
			else if (dataType == AmarinoIntent.INT_EXTRA)
			{
				Test_Input = intent.getIntExtra(AmarinoIntent.EXTRA_DATA, -1);
			}

			if ((Input != null) || (Test_Input >= 0))
			{
				//Send intensities to Arduino
				VestController.setVibrationIntensities(StudyTutorial.intensities);
				//View number of items sent to arduino
				Toast toast;
				if (Input != null)
				{	
					toast = Toast.makeText(context, Input, Toast.LENGTH_SHORT);
				}
				else
				{
					toast = Toast.makeText(context,String.valueOf(Test_Input), Toast.LENGTH_SHORT);
				}
				toast.show();
			}
			else
			{
				Toast toast = Toast.makeText(context,"Received Input, could not display", Toast.LENGTH_SHORT);
				toast.show();
			}
		}
	};



	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		context = this.getApplicationContext();
		settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

		//Initialize settings and UI elements
		deviceAddress = settings.getString("deviceAddress", BT_ADDRESS);
		OutputIntensities = (TextView)findViewById(R.id.txtIntensities);
		//reconnect = (Button)findViewById(R.id.reconnectButton);
		startStudy = (Button)findViewById(R.id.BtnTutorial);
		forward = (Button)findViewById(R.id.BtnForward);
		left = (Button)findViewById(R.id.BtnLeft);
		right = (Button)findViewById(R.id.BtnRight);
		turnaround = (Button)findViewById(R.id.BtnBack);

		//Initialize Surface Control and Device Address
		surfaceControl = (SurfaceControlView)findViewById(R.id.surfaceControl);
		surfaceControl.setXRange(0, 3);
		surfaceControl.setXLabel("Shirt Side");
		surfaceControl.setYRange(0, 1);
		surfaceControl.setYLabel("Intensity");
		
		//SecondDeviceController.initialize(context);
		//Create Listeners
		OnClickListener ButtonClickListener = new OnClickListener()
		{
			public void onClick(View Element)
			{
				if(Element == reconnect)
				{
					//deviceAddress = addressText.getText().toString();
					//Toast.makeText(getApplicationContext(),"SecondDeviceController!",Toast.LENGTH_SHORT).show();
					//SecondDeviceController.initialize(context, deviceAddress);
					VestController.initialize(context, deviceAddress);
				}
				else if (Element == left)
				{
					//Toast.makeText(getApplicationContext(),"SecondDeviceController!",Toast.LENGTH_SHORT).show();
					PhoneController.vibrate_left();
					//SecondDeviceController.vibrate_left();
				}
				else if (Element == right)
				{
					PhoneController.vibrate_right();
					//SecondDeviceController.vibrate_right();
				}
				else if (Element == forward)
				{
					PhoneController.vibrate_straight();
					//SecondDeviceController.vibrate_straight();
				}
				else if (Element == turnaround)
				{
					PhoneController.vibrate_backwards();
					//SecondDeviceController.vibrate_backwards();
				}
				else if (Element == startStudy)
				{
					try
					{
						//Start Configuration Activity (MENUSCREEN intent-filter was previously set up in Manifest file)
						startActivity(new Intent("edu.tamu.haptigo.CONFIGURATION")); 
					} 
					catch (ActivityNotFoundException e)
					{
						Toast.makeText(getApplicationContext(),"Could not find Configuration Activity!",Toast.LENGTH_SHORT).show();
					}
					finally
					{
						finish();
					}
				}
			}
		};


		ValuesChangeListener SurfaceControlListener = new ValuesChangeListener()
		{
			public void onValuesChanged(float xValue, float yValue)
			{
				//Configure Vibration Intensity
				float intensity = 1 -yValue;
				if (intensity < 0.25)
					intensity = 0;
				else //if (intensity > 0.75)
					intensity = 1;
//				else
//					intensity = 2*(intensity - 0.25f);
				surfaceControl.setYLabel("Intensity:"+String.format("%.2f", intensity));

				//Configure Vibration direction
				if(xValue < 1)
				{
					//Far left, set values on left tactor
					intensities[0] = 255;
					surfaceControl.setXLabel("Left");
				}
				else if (xValue > 2)
				{
					//Far right, set values on right tactor
					intensities[2] = 255;
					surfaceControl.setXLabel("Right");
				}
				else
				{
					//In center
					surfaceControl.setXLabel("Center");
					intensities[1] = 255;
//					if(xValue < 1.5)
//					{
//						//to the left of center
//						float dx = (float) (xValue - .5);
//						intensities[1] = (int)(dx*255f);
//						intensities[0] = 255-intensities[1];
//						surfaceControl.setXLabel("Left");
//					}
//					else
//					{
//						//to the right of center
//						float dx = (float) (xValue - 1.5);
//						intensities[2] = (int)(dx*255f);
//						intensities[1] = 255-intensities[2];
//						surfaceControl.setXLabel("Right");
//					}
//					if(xValue > 1 && xValue < 2)
//						surfaceControl.setXLabel("Center");
				}

				//Output intensities to screen
				String output = "";
				for(int i=0; i< 3; i++)
				{
					intensities[i] = (int)(intensities[i]*intensity);
					output += intensities[i]+" ";
				}
				setOutputText("Intensities: "+output);

				//Send output to Arduino
				VestController.setVibrationIntensities(intensities);
			}
		};

		//Assign listeners
		//reconnect.setOnClickListener(ButtonClickListener);
		startStudy.setOnClickListener(ButtonClickListener);
		forward.setOnClickListener(ButtonClickListener);
		left.setOnClickListener(ButtonClickListener);
		right.setOnClickListener(ButtonClickListener);
		turnaround.setOnClickListener(ButtonClickListener);
		surfaceControl.addValuesChangeListener(SurfaceControlListener);

	}

	private void setOutputText(String text)
	{
		OutputIntensities.setText(text);
	}

	@Override
	protected void onStart()
	{
		super.onStart();
		registerReceiver(ArduinoReceiver, new IntentFilter(AmarinoIntent.ACTION_CONNECTED));
		
		PhoneController.initialize(getApplicationContext());
		if(Connect_Device)
			VestController.initialize(this.getApplicationContext(), deviceAddress);
		
		//Send initial intensity seed
		surfaceControl.setValues(1.5f, .75f);
		VestController.setVibrationIntensities(intensities);

		// --> Debugging! Toast toast = Toast.makeText(this.getApplicationContext(), "Initial seed", Toast.LENGTH_SHORT);
		//toast.show();
	}

	@Override
	protected void onStop()
	{
		super.onStop();

		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("deviceAddress", deviceAddress);
		editor.commit(); 	// Commit the edits!
		
		//Terminate vest and phone connections
		PhoneController.terminate_connection();
		VestController.DestroyConnection();
		unregisterReceiver(ArduinoReceiver);
	}

}