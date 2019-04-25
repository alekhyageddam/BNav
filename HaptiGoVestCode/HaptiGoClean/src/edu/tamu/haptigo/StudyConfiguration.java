package edu.tamu.haptigo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

//This activity configures the settings that will be used for the Navigation study.
public class StudyConfiguration extends Activity
{
	//UI Controls
	private RadioGroup Path_Selector;
	private RadioGroup Nav_Selector;
	private Button NextScreen;
	private static String nav_system;
	private static String destination;
	public static RadioButton destA;
	public static RadioButton destB;
	public static RadioButton destC;
	public static RadioButton haptigo_nav;
	public static RadioButton pocket_nav;
	public static EditText inputDestination;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.configmain);
		
		//Initialize UI elements
		Path_Selector = (RadioGroup)findViewById(R.id.path_select);
		Nav_Selector = (RadioGroup)findViewById(R.id.nav_select);
		inputDestination = (EditText)findViewById(R.id.customDestination);
		NextScreen = (Button)findViewById(R.id.btnConfigure);
		destA = (RadioButton)findViewById(R.id.DestA);
		destB = (RadioButton)findViewById(R.id.DestB);
		destC = (RadioButton)findViewById(R.id.DestC);
		haptigo_nav = (RadioButton)findViewById(R.id.Haptigo);
		pocket_nav = (RadioButton)findViewById(R.id.PocketNavigator);

		//Create Listener
		OnClickListener ButtonClickListener = new OnClickListener()
		{
			public void onClick(View Element)
			{
				if (Element == NextScreen)
				{
					try
					{
						//Start the Navigation Service (MENUSCREEN intent-filter was previously set up in Manifest file)
						parseSelection();
						Intent navIntent = new Intent("edu.tamu.haptigo.NAVIGATION");
						navIntent.putExtra("nav_system", nav_system);
						navIntent.putExtra("destination", destination);
						startActivity(navIntent); 
					} 
					catch (ActivityNotFoundException e)
					{
						Toast.makeText(getApplicationContext(),"Could not find Navigation Activity!",Toast.LENGTH_SHORT).show();
					}
//					finally
//					{
//						finish();
//					}
				}
			}
		};

		//Assign listener
		NextScreen.setOnClickListener(ButtonClickListener);
	}
	
	public void parseSelection()
	{
		int path, navsystem;
		
		path = Path_Selector.getCheckedRadioButtonId();
		navsystem = Nav_Selector.getCheckedRadioButtonId();
		
		//Parse selected nav. system
		if (navsystem == haptigo_nav.getId())
			nav_system = "0";
		else if (navsystem == pocket_nav.getId())
			nav_system = "1";
		
		//Parse selected destination
		String inputString = inputDestination.getText().toString().trim();
		if (inputString.length() > 0)
			destination = inputString;
		else if (path == destA.getId())
			destination = "A";
		else if (path == destB.getId())
			destination = "B";
		else if (path == destC.getId())
			destination = "C";
	}

}