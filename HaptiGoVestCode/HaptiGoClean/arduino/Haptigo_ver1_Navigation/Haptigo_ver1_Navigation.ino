#include <MeetAndroid.h>

#define LeftPinIdentifier 0
#define MiddlePinIdentifier 1
#define RightPinIdentifier 2
bool Test_Mode = false;

int leftPin = 3;
int rightPin = 12;
int middlePin = 6;
int intensities[3];
float AttenuationConstant = 0.70;
int VibeFrequency = 0;
MeetAndroid meetAndroid;

void setup()
{
	for(int i = 0; i < 3; i++)
	{
		intensities[i] = 0;
	}
	Serial.begin(57600);

	//Configure Output pins
	pinMode(leftPin, OUTPUT);
	pinMode(rightPin, OUTPUT);
	pinMode(middlePin, OUTPUT);

	//Receive intensity and frequency levels for directional haptic actuators
	meetAndroid.registerFunction(setIntensities, 'i');
	meetAndroid.registerFunction(setFrequency, 'f');
	//Enables or disable test-mode
	meetAndroid.registerFunction(setMode, 'm');
	//Victory Dance once Destination reached
	meetAndroid.registerFunction(VictoryDance, 'v');	
}

void loop()
{
	meetAndroid.receive();
	//doVibrate();
}	

//This function tests the Amarino connection by implementing a feedback ping loop
void ping(byte flag, byte numOfValues)
{
  meetAndroid.send(meetAndroid.getInt());
}

//This function sets the intensities of the haptic tactors
void setIntensities(byte flag, byte numOfValues)
{
  
  if (!Test_Mode)  //Test-app is not being run
  {
  	int data[numOfValues];
  	meetAndroid.getIntValues(data);

    Serial.println("Real Mode:");
  	for(int i = 0; i< 3; i++)
  	{
          Serial.println(intensities[i]);
  	 intensities[i] = data[i];
  	}
  
    while(Serial.available())
    {
        Serial.read();
    }
        
    doVibrate();
  }
  else            //Test app is being run
  {
    meetAndroid.getIntValues(intensities);
    Serial.print("Test Mode:");
    for(int i = 0; i< 5; i++)
    {
      Serial.print(intensities[i]);
      Serial.print(" ");  
    }
    Serial.println();
    digitalWrite(13, HIGH);
    doVibrate();
  }
}

//This function sets the frequency of vibration of the haptic tactors
void setFrequency(byte flag, byte numOfValues)
{
  VibeFrequency = meetAndroid.getInt(); 
  return;
}

//This function enables or disables test mode
void setMode(byte flag, byte numOfValues)
{
  int Input = meetAndroid.getInt();
    Serial.print("Mode Change:");
    Serial.print(Input);
  if(Input)
  {
    Serial.println("  :: Test Mode");
    Test_Mode = true;

  }
  else
  {
    Serial.println("  :: Real Mode");
    Test_Mode = false;
  }
   
}

//This function indicates conclusion of navigation
void VictoryDance(byte flag, byte numOfValues)
{
  //Begin end sequence
  analogWrite(leftPin , 255);
  analogWrite(rightPin, 255);
  analogWrite(middlePin, 255);
  delay(500);
  
  analogWrite(leftPin , 0);
  analogWrite(rightPin, 0);
  analogWrite(middlePin, 0);
  delay(250);

  analogWrite(leftPin , 255);
  analogWrite(rightPin, 255);
  analogWrite(middlePin, 255);
  delay(500);

  analogWrite(leftPin , 0);
  analogWrite(rightPin, 0);
  analogWrite(middlePin, 0);
}

//This resets all intensity values -- This implements the pulsing action
void ResetIntensities()
{
    
  digitalWrite(leftPin,LOW);
  digitalWrite(rightPin, LOW);
  digitalWrite(middlePin, LOW);
  return;
}

//This function vibrates the haptic tactors
void doVibrate()
{
  //Vibrate navigational tactors
  
  for (int j = 0; j<3; j++)
  {     
    analogWrite(leftPin, intensities[0]);
    analogWrite(middlePin, intensities[1]);
    analogWrite(rightPin, intensities[2]);
  
    delay(500);
    ResetIntensities();
    delay(100);
  }
  //delay(VibeFrequency);
  Serial.print("Vibrating...:");
  for(int i = 0; i<3; i++)
  {
   Serial.print(intensities[i]);
   Serial.print(" "); 
  }
  Serial.println();
  digitalWrite(13,LOW);
}
