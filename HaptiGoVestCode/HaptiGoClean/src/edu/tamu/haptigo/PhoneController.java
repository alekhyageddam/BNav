package edu.tamu.haptigo;

import android.content.Context;
import android.os.Vibrator;

//This class implements an interface to the vibration motors on an Android Phone
public class PhoneController
{
	private static Context context;
	private static Vibrator phone_vibrator;
	private final static int no_delay = 0;
	private final static int long_pulse = 600;	//long pulse is 400ms
	private final static int short_pulse = 250;  //short pulse is 200ms
	private final static int pulse_delay = 150;   //delay between consecutive pulses
	public static boolean enabled = false;
	
	public static void initialize(Context input_context)
	{
		PhoneController.context = input_context;
		phone_vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		enabled = true;
	}
	
	public static void vibrate_left()
	{
		long[] pattern = {no_delay,long_pulse, pulse_delay, short_pulse};
		if(enabled)
			phone_vibrator.vibrate(pattern, -1);
	}
	
	public static void vibrate_right()
	{
		long[] pattern = {no_delay, short_pulse};
		if(enabled)
			phone_vibrator.vibrate(pattern, -1);
	}
	
	public static void vibrate_straight()
	{
		long[] pattern = {no_delay,short_pulse,pulse_delay,short_pulse,pulse_delay,short_pulse};
		if(enabled)
			phone_vibrator.vibrate(pattern, -1);
	}

	public static void vibrate_backwards()
	{
    	long[] pattern = {no_delay,long_pulse,pulse_delay,long_pulse,pulse_delay,long_pulse };
		if(enabled)
			phone_vibrator.vibrate(pattern, -1);
	}
	public static void victory_dance()
	{
		long[] pattern = {no_delay, short_pulse, pulse_delay, short_pulse, pulse_delay, short_pulse};
		if(enabled)
			phone_vibrator.vibrate(pattern, 0);
	}
	public static void terminate_connection()
	{
		phone_vibrator.cancel();
		enabled = false;
	}

}
