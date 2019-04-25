#include <NewPing.h>
#define Max_Distance 200                        //Max Distance in cm
#define Ping_Interval 32
#define SensorCount 2
int LeftObstaclePin = 12;
int RightObstaclePin = 11;
uint8_t CurrentSensor = 0;
float Distances[SensorCount];			//Stores distances in ft
unsigned long PingTimer[SensorCount]; 
float AttenuationConstant = 0.70;
int intensities[2];

NewPing sonar[SensorCount] = 
{
	NewPing(A2, A2, Max_Distance),
	NewPing(A0, A0, Max_Distance)
};

void setup()
{
  Serial.begin(57600);
  //Setup starting time for each ultrasonic sensor
PingTimer[0] = millis() + 75;						//First sensor fires at 75ms
for(uint8_t i = 1; i< SensorCount; i++)
	PingTimer[i] = PingTimer[i-1] + Ping_Interval;

 pinMode(LeftObstaclePin, OUTPUT);
 pinMode(RightObstaclePin, OUTPUT);
}

void loop()
{
  for (uint8_t i = 0; i < SensorCount; i++)
    {
  	  if (millis() >= PingTimer[i])
  	 {
  	 	  PingTimer[i]+= Ping_Interval * SensorCount;
  		  if (i == 0 && CurrentSensor == SensorCount -1) 
  			   PrintDistances();
  		  sonar[CurrentSensor].timer_stop();				//Disable previous sensor
  		  CurrentSensor = i;						//Enable current sensor
  		  Distances[CurrentSensor] = 0;				   	//Initialize distance to zero
  		  sonar[CurrentSensor].ping_timer(PulseCheck);
  	 }
    }
    doVibrate();
}

void PulseCheck()
{
	if(sonar[CurrentSensor].check_timer())
	{
		unsigned int Distance_cm = sonar[CurrentSensor].ping_result/ US_ROUNDTRIP_CM;
		Distances[CurrentSensor] = Distance_cm * 0.0328084;		//Convert cm to feet
	}
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
void doVibrate()
{
  for(int i = 0; i<2; i++)
  {
    intensities[i] = 255*pow(AttenuationConstant, Distances[i]);
  }
  analogWrite(LeftObstaclePin,intensities[0]);
  analogWrite(RightObstaclePin,intensities[1]);
  
}
