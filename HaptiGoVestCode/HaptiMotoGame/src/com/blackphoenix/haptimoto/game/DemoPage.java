package com.blackphoenix.haptimoto.game;

import com.blackphoenix.haptigo.VestController;
import com.blackphoenix.haptimotogame.R;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.Toast;
import at.abraxas.amarino.AmarinoIntent;
import android.graphics.*;

public class DemoPage extends Activity implements OnTouchListener {

	public static final String HAPTIGO_BT_ADDRESS = "00:06:66:4F:94:8B";
//	public static final String HAPTIGO_BT_ADDRESS = "00:06:66:4F:94:79";

	float myLocX = 0, myLocY = 0; 
	MapView map;


	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.haptimoto_demo_layout);

		map = new MapView(this);
		map.setZOrderOnTop(true);
		map.setOnTouchListener(this);

		LinearLayout layoutMap = (LinearLayout) findViewById(R.id.layoutMap);
		layoutMap.addView(map);


		VestController.initialize(getApplicationContext(), HAPTIGO_BT_ADDRESS);

		// register to receive haptigo connection events
		registerReceiver(ConnectedArduinoReceiver, new IntentFilter(
				AmarinoIntent.ACTION_CONNECTED));


		// register to receive haptigo disconnection or failed connection
		IntentFilter disconnectionFilter = new IntentFilter(
				AmarinoIntent.ACTION_DISCONNECTED);
		disconnectionFilter.addAction(AmarinoIntent.ACTION_CONNECTION_FAILED);
		registerReceiver(DisConnectedArduinoReceiver, disconnectionFilter);


		//add surface view to the view 

	}



	//What to do when the play button was clicked
	public void onPlayClick(View v){

		map.resume();
	}

	public void onStopClick(View v){

		map.pause();
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

				VestController.Connected = false;

				Toast toast = Toast.makeText(context, "Connecting...",
						Toast.LENGTH_SHORT);
				toast.show();
			} else {

				VestController.Connected = false;

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
			VestController.Connected = false;

			Toast toast = Toast.makeText(context, "Connecting...",
					Toast.LENGTH_SHORT);
			toast.show();
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.menu_connect_vest:

			// vestController.connect();
			VestController.MakeConnection();
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

	@Override
	public void onDestroy() {

		super.onDestroy();

		// unregisterReceiver(ConnectedArduinoReceiver);
		// unregisterReceiver(DisConnectedArduinoReceiver);
		VestController.DestroyConnection();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.haptimoto_demo_menu, menu);
		return true;
	}

	@Override
	protected void onPause(){

		super.onPause();
		map.pause();
	}


	@Override
	protected void onResume(){

		super.onResume();
		map.resume();
	}

	@Override
	public boolean onTouch(View view, MotionEvent me) {

		
		try{
			Thread.sleep(50);
		}catch (InterruptedException e) {

		}
		
		if(me.getAction() == MotionEvent.ACTION_UP){
			myLocX = me.getX();
			myLocY = me.getY();
			
			Toast toast = Toast.makeText(this,
					"x::" + myLocX + "y::" + myLocY , Toast.LENGTH_SHORT);
			toast.show();
		}
		return true;
	}

	public class MapView extends SurfaceView implements Runnable, Callback {

		boolean playDemo = true;
				
		private Bitmap mapPath;
		private Bitmap marker;

		private Thread demoThread = null;
		private SurfaceHolder demoSurfaceHolder = null;
		private Context myContext;
		
		private DemoController controller;

		public MapView(Context context) {
			super(context);
			demoSurfaceHolder = getHolder();
			
			controller = new DemoController();
			demoSurfaceHolder.addCallback(this);
			loadBackgroundImage();
			
		}

		private void loadBackgroundImage(){
			
			Bitmap background = BitmapFactory.decodeResource(getResources(), R.drawable.housetobright);
		    float scalex = (float) 1.6;// (float)background.getHeight()/(float)getHeight();
		    float scaley = (float) 1.2;
		    int newWidth = Math.round(background.getWidth()/scalex);
		    int newHeight = Math.round(background.getHeight()/scaley);
		    mapPath = Bitmap.createScaledBitmap(background, newWidth, newHeight, true);
		    
		    Bitmap mark = BitmapFactory.decodeResource(getResources(), R.drawable.marker);
		    float scale = (float) 40;
		    newWidth = Math.round(mark.getWidth()/scale);
		    newHeight = Math.round(mark.getHeight()/scale);
		    marker = Bitmap.createScaledBitmap(mark, newWidth, newHeight, true);
		    
		}
		
		
		
		@Override
		public void run() {

			while(playDemo){

				if(!demoSurfaceHolder.getSurface().isValid()) continue;
				
				
				Canvas mapCanvas = demoSurfaceHolder.lockCanvas();
				//playDemoAnimation(mapCanvas);
//				mapCanvas.drawBitmap(mapPath, 0,0 , null);
//			    mapCanvas.drawBitmap(marker, myLocX - (marker.getWidth()/2), myLocY - (marker.getHeight()/2) , null);
			    
			    playDemoAnimation(mapCanvas);
			    demoSurfaceHolder.unlockCanvasAndPost(mapCanvas);
				
			}
		}

		private void playDemoAnimation(Canvas canvas){
			
			canvas.drawBitmap(mapPath, 0,0 , null);
			
			float[] pos = controller.nextPoint();
			canvas.drawBitmap(marker, pos[0], pos[1], null);
		}
		public void pause(){

			playDemo = false;

			if(demoThread == null) return;
			while(true){

				try{

					demoThread.join();
				}catch (InterruptedException e) {

					Log.e("HaptiMoto", "Demo Thread Error \n" + e.getMessage());
				}

				break;
			}

			demoThread = null;
		}

		public void resume(){

			playDemo = true;
			demoThread = new Thread(this);
			demoThread.start();
			
		}

		public void restart(){
			
			playDemo = true;
			demoThread = new Thread(this);
			demoThread.start();
			
			controller.resetDemo();
		}
		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int width,
				int height) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {

			Canvas mapCanvas = holder.lockCanvas();
			
			if(mapCanvas == null) return;
		    mapCanvas.drawBitmap(mapPath, 0, 0 , null);
		    holder.unlockCanvasAndPost(mapCanvas);
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			// TODO Auto-generated method stub
			
		}

	}

}
