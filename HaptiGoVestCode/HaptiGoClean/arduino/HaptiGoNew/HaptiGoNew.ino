#include <MeetAndroid.h>

#define LeftPinIdentifier 0
#define MiddlePinIdentifier 1
#define RightPinIdentifier 2
#define BuzzLengthIdentifier 3
#define NumOfPulsesIdentifier 4

#define LONGBUZZ 450
#define SHORTBUZZ 300
#define OFFTIME 150

int leftPin = 3;
int rightPin = A1;
int middlePin = 8;

int intensities[3];
int VibeFrequency = 0;
int buzzLength = SHORTBUZZ;
int numOfPulses = 1;

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
 
  pinMode(LED_BUILTIN, OUTPUT); //To show it received a command
        
	//Receive intensity and frequency levels for directional haptic actuators
	meetAndroid.registerFunction(setIntensities, 'i');
  meetAndroid.registerFunction(setFrequency, 'f');
  meetAndroid.registerFunction(resetIntensities, 'r');

  meetAndroid.registerFunction(testFunction, 't');
}

void loop()
{
	meetAndroid.receive();
}	

void testFunction(byte flag, byte numOfValues) {
  while(true){
    delay(300);
    digitalWrite(13, LOW);
    delay(300);
  }
}


//This function sets the intensities of the haptic tactors
void setIntensities(byte flag, byte numOfValues)
{
    int data[numOfValues];
    meetAndroid.getIntValues(data);
  
   	for(int i = 0; i< 3; i++)
  	{
  	  intensities[i] = data[i];
  	}

    buzzLength = data[BuzzLengthIdentifier];
    numOfPulses = data[NumOfPulsesIdentifier];

    digitalWrite(13, HIGH);
    doVibrate();
    digitalWrite(13, LOW);
}

//This resets all intensity values -- This implements the pulsing action
void resetIntensities()
{
  digitalWrite(leftPin, LOW);
  digitalWrite(rightPin, LOW);
  digitalWrite(middlePin, LOW);
}

void resetIntensities(byte flag, byte numOfValues){  
  resetIntensities();
}

//This function sets the frequency of vibration of the haptic tactors
void setFrequency(byte flag, byte numOfValues)
{
  VibeFrequency = meetAndroid.getInt(); 
}

//This function sets the haptic tactors in vibe mode
void doVibrate()
{
  //Vibrate navigational tactors
  
  for (int j = 0; j<numOfPulses; j++)
  {     
    analogWrite(leftPin, intensities[0]);
    analogWrite(middlePin, intensities[1]);
    analogWrite(rightPin, intensities[2]);
  
    delay(buzzLength);
    resetIntensities();
    delay(OFFTIME);
  }
}


