#include <NewPing.h>

#define SensorCount 2
#define Max_Distance 200                        //Max Distance in cm
#define Ping_Interval 64                       //Pulse wait-time is every 64ms (was previously 32)

//Hardware Configuration Variables
int LeftSensorPin = A2; 
int RightSensorPin = 4; 
int LeftObstaclePin = A3;                       
int RightObstaclePin = A1;                      
int Intensities[2];                             //Intensities of obstacle notification pins
int VibeFrequency;                               //Frequency of obstacle notification pins
int count = 0;

//Obstacle Calculation Variables
uint8_t CurrentSensor = 0;                      //Currently polled sensor
float Distances[SensorCount];			              //Stores detected distances in ft
float PreviousDistances[SensorCount];           //Previously detected distances in ft
unsigned long PingTimer[SensorCount];           //Timer that serves to multiplex between sensors
float AttenuationConstant = 0.65;

NewPing sonar[SensorCount] = 
{
	NewPing(LeftSensorPin, LeftSensorPin, Max_Distance),
	NewPing(RightSensorPin, RightSensorPin, Max_Distance)
};

void setup()
{
	
  //Initialize all relevant variables
  for(int i = 0; i< SensorCount; i++)
	{
		Intensities[i] = 0;
	}
  Serial.begin(57600);

	//Configure Output Pins
	pinMode(LeftObstaclePin, OUTPUT);
	pinMode(RightObstaclePin, OUTPUT);
 
	//Setup starting time for each ultrasonic sensor
	PingTimer[0] = millis() + 64;						  //First sensor fires at 64ms
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
  		  sonar[CurrentSensor].timer_stop();		        //Disable previous timer
  		  CurrentSensor = i;						                
                  Distances[i] = 0;                             //In case there's no ping echo for sensor  
                  sonar[CurrentSensor].ping_timer(PulseCheck);  //Send Ping
        }
    delay(Ping_Interval);
  }
  count = count +1;
  if (count % 5 == 0)
    ResetVibration();
  PrintDistances();
}

//This function waits for echo response after initial ping is sent
void PulseCheck()
{
	if(sonar[CurrentSensor].check_timer()) //Check if ping echo returned
	{
            unsigned int Distance_cm = sonar[CurrentSensor].ping_result/ US_ROUNDTRIP_CM;
	    PreviousDistances[CurrentSensor] = Distances[CurrentSensor];
            Distances[CurrentSensor] = Distance_cm * 0.0328084;		//Convert cm to feet
            NotifyWearer();
            count = 1;
 	}
}

//This function vibrates tactors at an appropriate frequency to warn wearer of impending obstacle
void NotifyWearer() 
{
  if (Distances[CurrentSensor] <= 4)  //Notify users of obstacles within 4 feet
  {
    Intensities[CurrentSensor] = 255;
    VibeFrequency = 200 + Distances[CurrentSensor] * 255;
  }
  else
    Intensities[CurrentSensor] = 0;
  doObstacleVibration();
}

void doObstacleVibration()
{
  if (CurrentSensor)    //Right Obstacle Sensor is currently active
  {
    analogWrite(LeftObstaclePin, Intensities[CurrentSensor]);
//    delay(VibeFrequency);
    Intensities[CurrentSensor] = 0;
//    digitalWrite(LeftObstaclePin,LOW);
  }
  else
  {
    analogWrite(RightObstaclePin, Intensities[CurrentSensor]);
//    delay(VibeFrequency);
    Intensities[CurrentSensor] = 0;
//    digitalWrite(RightObstaclePin,LOW);
  }
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
  Serial.print(Intensities[0]);
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
  Serial.print(Intensities[1]);
  Serial.print(",");
  Serial.print(Distances[1]);
  Serial.print(",");
  Serial.print(PreviousDistances[1]);
  Serial.print(",");
  Serial.println(PreviousDistances[1] - Distances[1]);
  Serial.print("Vibe Freq:");
  Serial.println(VibeFrequency);

 
}





