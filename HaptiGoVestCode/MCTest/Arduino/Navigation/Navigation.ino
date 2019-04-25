#include <MeetAndroid.h>

#define LeftPinIdentifier 0
#define MiddlePinIdentifier 1
#define RightPinIdentifier 2
#define BuzzLengthIdentifier 3
#define NumOfPulsesIdentifier 4
#define LONGBUZZ 450
#define SHORTBUZZ 300
#define OFFTIME 150

bool Test_Mode = false;

int numOfPulses = 1;

int leftLightPin = 3;
int rightLightPin = A1;
int middleLightPin = 10;

int leftVibePin = 3;
int rightVibePin =A1;
int middleVibePin = 8;

int intensities[3];
int outputPins[3];
int buzzLength = SHORTBUZZ;
MeetAndroid meetAndroid;

void setup()
{
	for(int i = 0; i < 3; i++)
	{
		intensities[i] = 0;
	}
	Serial.begin(9600);

	//Configure Output pins
	pinMode(leftVibePin, OUTPUT);
	pinMode(rightVibePin, OUTPUT);
	pinMode(middleVibePin, OUTPUT);

        pinMode(leftLightPin, OUTPUT);
	pinMode(rightLightPin, OUTPUT);
	pinMode(middleLightPin, OUTPUT);
        pinMode(13, OUTPUT);

        outputPins[LeftPinIdentifier] = leftVibePin;
        outputPins[MiddlePinIdentifier]= middleVibePin;
        outputPins[RightPinIdentifier] = rightVibePin;
        
	//Receive intensity and frequency levels for directional haptic actuators
	meetAndroid.registerFunction(setIntensities, 'i');
        meetAndroid.registerFunction(resetIntensities, 'r');
        
        //change the mode signals
        meetAndroid.registerFunction(setLightMode, 'l');
        meetAndroid.registerFunction(setVibeMode, 'v');
		
}

void loop()
{
	meetAndroid.receive();
	//doVibrate();
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
        
        doVibrate();
}

void resetIntensities(byte flag, byte numOfValues){
  
  int data[numOfValues];
  meetAndroid.getIntValues(data);
  
  resetTactors();
  return;
}

void resetTactors(){
  digitalWrite(outputPins[LeftPinIdentifier],LOW);
  digitalWrite(outputPins[MiddlePinIdentifier], LOW);
  digitalWrite(outputPins[RightPinIdentifier], LOW);
}

//This function sets the haptic tactors in vibe mode
void doVibrate()
{
  //Vibrate navigational tactors
  
  for (int j = 0; j<numOfPulses; j++)
  {     
    analogWrite(outputPins[LeftPinIdentifier], intensities[LeftPinIdentifier]);
    analogWrite(outputPins[MiddlePinIdentifier], intensities[MiddlePinIdentifier]);
    analogWrite(outputPins[RightPinIdentifier], intensities[RightPinIdentifier]);
  
    delay(buzzLength);
    resetTactors();
    delay(OFFTIME);
  }
}

//setting the output pins to control Vibe Motors
void setVibeMode(byte flag, byte numOfValues){
  
        outputPins[LeftPinIdentifier] = leftVibePin;
        outputPins[MiddlePinIdentifier]= middleVibePin;
        outputPins[RightPinIdentifier] = rightVibePin;
}

//setting the output pins to control leds
void setLightMode(byte flag, byte numOfValues){
  
        outputPins[LeftPinIdentifier] = leftLightPin;
        outputPins[MiddlePinIdentifier]= middleLightPin;
        outputPins[RightPinIdentifier] = rightLightPin;
}
