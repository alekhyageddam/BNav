#include <NewPing.h>

#define SensorCount 2
#define Max_Distance 200                        //Max Distance in cm
#define Ping_Interval 100 //32

int LeftSensorPin = 4; 
int RightSensorPin = A2; 
int LeftObstaclePin = A3;                       //Was previously pin 11
int RightObstaclePin = 8;                      //Was previously pin 12
int intensities[2];
int intensity;
int VibeFrequency = 1000;
float ObstacleThreshold = 2.5;                  //Distance in feet before Android is notified

uint8_t CurrentSensor = 0;
float Distances[SensorCount];			//Stores distances in ft
float PreviousDistances[SensorCount];
unsigned long PingTimer[SensorCount]; 
float AttenuationConstant = 0.65;

NewPing sonar[SensorCount] = 
{
	NewPing(LeftSensorPin, LeftSensorPin, Max_Distance),
	NewPing(RightSensorPin, RightSensorPin, Max_Distance)
};

void setup()
{
	for(int i = 0; i< 2; i++)
	{
		intensities[i] = 0;
	}
        intensity = 0;
	Serial.begin(57600);

	//Configure Output Pins
	pinMode(LeftObstaclePin, OUTPUT);
	pinMode(RightObstaclePin, OUTPUT);
 
	//Setup starting time for each ultrasonic sensor
	PingTimer[0] = millis() + 64;						//First sensor fires at 75ms
	for(uint8_t i = 1; i< SensorCount; i++)
		PingTimer[i] = PingTimer[i-1] + Ping_Interval;

  CurrentSensor = 0;
}

void loop()
{
	//Check ultrasonic sensors
	for (uint8_t i = 0; i < SensorCount; i++)
    {
  	  if (millis() >= PingTimer[i])
  	 {
  	 	  PingTimer[i]+= Ping_Interval * SensorCount;
  		  sonar[CurrentSensor].timer_stop();		//Disable previous sensor
  		  CurrentSensor = i;						//Enable current sensor
          
          //Initialize distance to zero
  		  sonar[CurrentSensor].ping_timer(PulseCheck);
        //PrintDistances();
     }
     delay(100);
	}
 ResetVibration();
    PrintDistances();
    //delay(80);
   
}

//This function sends the initial ultrasonic pulse and waits for response
void PulseCheck()
{
	if(sonar[CurrentSensor].check_timer())
	{
                unsigned int Distance_cm = sonar[CurrentSensor].ping_result/ US_ROUNDTRIP_CM;
		PreviousDistances[CurrentSensor] = Distances[CurrentSensor];
                Distances[CurrentSensor] = Distance_cm * 0.0328084;		//Convert cm to feet
                setObstacleIntensities();
                doObstacleVibration();
	}
       
    
}

void setObstacleIntensities()
{
  boolean obstacle = false;
  for(int i = 0; i< 2; i++)
  {
    
      if ((Distances[i] <= 4)) //&& (PreviousDistances[i] - Distances[i]) > 0.2)
      {
        intensities[i] = 255;
        intensity = 255;
        VibeFrequency = 200 + Distances[i] * 225;
        obstacle = true;
        //VibeFrequency = map(Distances[i], 0, 4, 500, 1000);
      }
      else
      {
        intensities[i] = 0;
        //VibeFrequency = 1000;
      }
      //intensities[i] = 255*pow(AttenuationConstant, Distances[i]);
  }
    
    if(!obstacle)
    {
      intensity = 0;
    }
}

void doObstacleVibration()
{
  analogWrite(RightObstaclePin,intensity);  
  analogWrite(LeftObstaclePin,intensity);
//  delay(200);
//  intensities[0] = 0;
//  intensities[1] = 0;
//  VibeFrequency = 200;
//  digitalWrite(LeftObstaclePin,LOW);
//  digitalWrite(RightObstaclePin,LOW);
 
}

void ResetVibration()
{
  digitalWrite(LeftObstaclePin,LOW);
  digitalWrite(RightObstaclePin,LOW);
}

void PrintDistances()
{
 
  Serial.print("L Sensor");
  Serial.print(" =");
  Serial.print(intensities[0]);
  Serial.print(",");
  Serial.print(Distances[0]);
  Serial.print(",");
  Serial.print(PreviousDistances[0]);
  Serial.print(",");
  Serial.println(PreviousDistances[0] - Distances[0]);
  Serial.print("Vibe Freq:");
  Serial.println(VibeFrequency);
   
  Serial.print("R Sensor");
  Serial.print(" =");
  Serial.print(intensities[1]);
  Serial.print(",");
  Serial.print(Distances[1]);
  Serial.print(",");
  Serial.print(PreviousDistances[1]);
  Serial.print(",");
  Serial.println(PreviousDistances[1] - Distances[1]);
  Serial.print("Vibe Freq:");
  Serial.println(VibeFrequency);

 
}






