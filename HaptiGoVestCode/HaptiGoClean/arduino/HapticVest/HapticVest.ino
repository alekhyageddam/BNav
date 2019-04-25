//Code modified from NewPing '15 Sensors' Example Sketch
#include <MeetAndroid.h>
#include <NewPing.h>

#define LeftPinIdentifier 0
#define MiddlePinIdentifier 1
#define RightPinIdentifier 2
#define LeftObstacleIdentifier 3
#define RightObstacleIdentifier 4
#define SensorCount 2
#define Max_Distance 200                        //Max Distance in cm
#define Ping_Interval 32
bool Test_Mode = false;

int intensities[5];
int pins[5];
int leftPin = 6;
int rightPin = 7;
int middlePin = 8;
int LeftSensorPin = A2;
int RightSensorPin = A0;
int LeftObstaclePin = 12;
int RightObstaclePin = 11;
int VibeFrequency = 0;
float ObstacleThreshold = 2.5;                  //Distance in feet before Android is notified

uint8_t CurrentSensor = 0;
float Distances[SensorCount];			//Stores distances in ft
float PreviousDistances[SensorCount];
unsigned long PingTimer[SensorCount]; 
float AttenuationConstant = 0.70;

MeetAndroid meetAndroid;
NewPing sonar[SensorCount] = 
{
	NewPing(A2, A2, Max_Distance),
	NewPing(A0, A0, Max_Distance)
};

void setup()
{

  for(int i =0; i < 5; i++)
  {
    intensities[i] = 0;
  }
  Serial.begin(57600);
 
 //Configure Output Pins
 pinMode(middlePin, OUTPUT);
 pins[MiddlePinIdentifier] = middlePin;
 pinMode(rightPin, OUTPUT);
 pins[RightPinIdentifier] = rightPin;
 pinMode(leftPin, OUTPUT); 
 pins[LeftPinIdentifier] = leftPin;
 pinMode(LeftObstaclePin, OUTPUT);
 pins[LeftObstacleIdentifier] = LeftObstaclePin;
 pinMode(RightObstaclePin, OUTPUT);
 pins[RightObstacleIdentifier] = RightObstaclePin;

 //Initialize intensity values
 for (int i = 0; i<5; i++)
 	intensities[i] = 0;

//Receive intensity and frequency levels for directional haptic actuators
meetAndroid.registerFunction(setIntensities, 'i');
meetAndroid.registerFunction(setFrequency, 'f');
//Enables or disable test-mode
meetAndroid.registerFunction(setMode, 'm');
//Victory Dance once Destination reached
meetAndroid.registerFunction(VictoryDance, 'v');

//Setup starting time for each ultrasonic sensor
PingTimer[0] = millis() + 75;						//First sensor fires at 75ms
for(uint8_t i = 1; i< SensorCount; i++)
	PingTimer[i] = PingTimer[i-1] + Ping_Interval;

}

void loop()
{  
  //Check ultrasonic sensors
  
  CurrentSensor = 0;

    for (uint8_t i = 0; i < SensorCount; i++)
    {
  	  if (millis() >= PingTimer[i])
  	 {
  	 	  PingTimer[i]+= Ping_Interval * SensorCount;
  		  //if (i == 0 && CurrentSensor == SensorCount -1) 
  			   //PrintDistances();
  		  sonar[CurrentSensor].timer_stop();				//Disable previous sensor
  		  CurrentSensor = i;						//Enable current sensor
          	//Initialize distance to zero
  		  sonar[CurrentSensor].ping_timer(PulseCheck);
          
  	 }
         delay(10);
  
//            Serial.print("Lp:");
//            Serial.print(Distances[0]);
//            Serial.print("Rp:"); 
//            Serial.println(Distances[1]);
    }
     setObstacleIntensities();
     doObstacleVibration();
  
  meetAndroid.receive();

  //doVibrate();
  CheckIntensities();
  delay(500);
} 

//This function sends the initial ultrasonic pulse and waits for response
void PulseCheck()
{
	if(sonar[CurrentSensor].check_timer())
	{
		unsigned int Distance_cm = sonar[CurrentSensor].ping_result/ US_ROUNDTRIP_CM;
		PreviousDistances[CurrentSensor] = Distances[CurrentSensor];
                Distances[CurrentSensor] = Distance_cm * 0.0328084;		//Convert cm to feet
	}
}

//This function tests the Amarino connection by implementing a feedback ping loop
void ping(byte flag, byte numOfValues)
{
  meetAndroid.send(meetAndroid.getInt());
}

void setObstacleIntensities()
{
  if(!Test_Mode){
  for(int i = 3; i< 5; i++)
  {  
    if((PreviousDistances[i-3] == 0) || (Distances[i-3] == 0))
      intensities[i] = 0;
    else 
    {
      if((PreviousDistances[i-3] - Distances[i-3]) > 0.5 )
      intensities[i] = 255;
    else
      intensities[i] = 0;
    }
  }  
}
}


void doObstacleVibration()
{
  //if((!Test_Mode) && (Distances[1] < 4))
  if(!Test_Mode)
  {
    //Serial.print("Obstacle Intensity");
    //Serial.println(intensities[4]);
    
    analogWrite(RightObstaclePin,intensities[4]);
    analogWrite(LeftObstaclePin,intensities[3]);
  }
  Serial.print("L Sensor");
  Serial.print("=");
  Serial.print(Distances[0]);
  Serial.println(" ft");
  Serial.print("R Sensor");
  Serial.print("=");
  Serial.print(Distances[1]);
  Serial.println(" ft");
  //  //delay(500);
//  digitalWrite(LeftObstaclePin, LOW);
//  digitalWrite(RightObstaclePin, LOW);
//  
//  intensities[3] = 0;
//  intensities[4] = 0;
//  delay(500);
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
  		if (i < 3)		//Retrieve data for navigation
  			intensities[i] = data[i];
  		else
  			intensities[i] = 255*pow(AttenuationConstant, Distances[i-3]);
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
    CheckIntensities();
    
    //meetAndroid.send(numOfValues); 
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
  for(int i = 0; i<3;i++)
    analogWrite(pins[i], 255);
  delay(500);
  
  for(int i = 0; i<3; i++)
    analogWrite(pins[i], 0);
  delay(250);

  for(int i = 0; i<3;i++)
    analogWrite(pins[i], 255);
  delay(500);

  for(int i = 0; i<3; i++)
    analogWrite(pins[i], 0);
}

//This resets all intensity values -- This implements the pulsing action
void ResetIntensities()
{
  for (int i=0; i<3; i++)
  {
    //intensities[i] = 0;
    digitalWrite(pins[i],LOW);
  }
  return;
}

void CheckIntensities()
{
  if((Distances[0] <= ObstacleThreshold) || (Distances[1] <= ObstacleThreshold))
  {  
    meetAndroid.send(Distances[0]);
    meetAndroid.send(Distances[1]);
    
  }
   return; 
}

//This function vibrates the haptic tactors
void doVibrate()
{
  //Vibrate obstacle indicator tactors
  if(Test_Mode)
  {
    analogWrite(pins[3], intensities[3]);
    analogWrite(pins[4], intensities[4]);
  }

  //Vibrate navigational tactors
  
  for (int j = 0; j<3; j++)
  {     
    for(int i=0; i<3; i++)
    {
      if (intensities[i]){
        //digitalWrite(pins[i],HIGH);
        analogWrite(pins[i],intensities[i]);
      }
    }
    delay(500);
    ResetIntensities();
    delay(100);
  }
  //delay(VibeFrequency);
  Serial.print("Vibrating...:");
  for(int i = 0; i<5; i++)
  {
   Serial.print(intensities[i]);
   Serial.print(" "); 
  }
  Serial.println();
  digitalWrite(13,LOW);
}

void PrintDistances()
{
	for (uint8_t i = 0; i < SensorCount; i++)
	{
		Serial.print(i);
		Serial.print("=");
		Serial.print(Distances[i]);
		Serial.print(" ft");
	}
	Serial.println();
}
 
