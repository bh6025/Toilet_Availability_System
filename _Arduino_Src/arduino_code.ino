#include <SoftwareSerial.h>

int sensorPin1 = A0;
int sensorPin2 = A1;

void setup() 
{
  Serial.begin(9600);
}

void loop() 
{
  int check1 = 0;
  int check2 = 0;
  
  for(int i = 0; i < 6 ; i ++){
    float distance1 = 12343.85 * pow(analogRead(sensorPin1),-1.15);
    float distance2 = 12343.85 * pow(analogRead(sensorPin2),-1.15);
    delay(500);
    if(distance1 < 80) check1 ++;
    if(distance2 < 80) check2 ++;
  }
  
  if(check1 >= 4 && check2 >= 4){
    Serial.print(3);
  }else if(check1 >= 4){
    Serial.print(2);
  }else if(check2 >= 4){
    Serial.print(1);
  }else{
    Serial.print(0);
  }
}

