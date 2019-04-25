#include <NewPing.h>

#define SensorCount 2
#define Max_Distance 200                        //Max Distance in cm
#define Ping_Interval 40 //32

int LeftSensorPin = 4;
int RightSensorPin = A2;
int LeftObstaclePin = A3;                       //Was previously pin 11
int RightObstaclePin = 8;                      //Was previously pin 12
int LeftSensorPin = A1;
int RightSensorPin = A0;
int LeftObstaclePin = 9;                       //Was previously pin 11
int RightObstaclePin = 10;                      //Was previously pin 12
int intensities[2];
int VibeFrequency = 0;
float ObstacleThreshold = 2.5;                  //Distance in feet before Android is notified

uint8_t CurrentSensor = 0;
float Distances[SensorCount];			//Stores distances in ft
float PreviousDistances[SensorCount];
unsigned long PingTimer[SensorCount]; 
float AttenuationConstant = 0.70;
int NO_Pulse[SensorCount];

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
	Serial.begin(57600);

	//Configure Output Pins
	pinMode(LeftObstaclePin, OUTPUT);
	pinMode(RightObstaclePin, OUTPUT);
 
	//Setup starting time for each ultrasonic sensor
	PingTimer[0] = millis() + 75;						//First sensor fires at 75ms
	for(uint8_t i = 1; i< SensorCount; i++)
		PingTimer[i] = PingTimer[i-1] + Ping_Interval;
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
     }
     delay(10);
	}
    setObstacleIntensities();
    doObstacleVibration();
    PrintDistances();
    // delay(1000);
    delay(1000);
}

//This function sends the initial ultrasonic pulse and waits for response
void PulseCheck()
{
	if(sonar[CurrentSensor].check_timer())
	{
		unsigned int Distance_cm = sonar[CurrentSensor].ping_result/ US_ROUNDTRIP_CM;
		PreviousDistances[CurrentSensor] = Distances[CurrentSensor];
                Distances[CurrentSensor] = Distance_cm * 0.0328084;		//Convert cm to feet
	        NO_Pulse[CurrentSensor] = 0;
                //Serial.println("Pulse!");
        }
        else
        {
          NO_Pulse[CurrentSensor] = 1;
          //PreviousDistances[CurrentSensor] = 0;
          //Distances[CurrentSensor] = 6;
        }  
    
}

void setObstacleIntensities()
{
  
  for(int i = 0; i< 2; i++)
  {  
    if((PreviousDistances[i] == 0) || (Distances[i] == 0) || (NO_Pulse[i] == 1))
      intensities[i] = 0;
    else 
    {
      	if((PreviousDistances[i] - Distances[i]) > 0.5 )
    		intensities[i] = 255;
    	else
      		intensities[i] = 0;
    }
  }  
}

void doObstacleVibration()
{
  analogWrite(RightObstaclePin,intensities[0]);
  analogWrite(LeftObstaclePin,intensities[1]);
  delay(1000);
  digitalWrite(RightObstaclePin,LOW);
  digitalWrite(LeftObstaclePin,LOW);
  //PrintDistances(); 
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
  Serial.println(NO_Pulse[0]);
  
  Serial.print("R Sensor");
  Serial.print(" =");
  Serial.print(intensities[1]);
  Serial.print(",");
  Serial.print(Distances[1]);
  Serial.print(",");
  Serial.print(PreviousDistances[1]);
  Serial.print(",");
  Serial.println(PreviousDistances[1] - Distances[1]);
  Serial.println(NO_Pulse[1]);
}



