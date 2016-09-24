/*
 Pi_Serial_test.cpp - SerialProtocol library - demo
 Copyright (c) 2014 NicoHood.  All right reserved.
 Program to test serial communication
 
 Compile with:
 sudo gcc -o Pi_Serial_Test.o Pi_Serial_Test.cpp -lwiringPi -DRaspberryPi -pedantic -Wall
 sudo ./Pi_Serial_Test.o
 */

// sudo gcc rasp_client.c -o test -l wiring
 
// just that the Arduino IDE doesnt compile these files.
#ifdef RaspberryPi 
 
//include system librarys
#include <stdio.h> //for printf
#include <stdint.h> //uint8_t definitions
#include <stdlib.h> //for exit(int);
#include <string.h> //for errno
#include <errno.h> //error output
#include <unistd.h>
#include <arpa/inet.h>
#include <sys/types.h>
#include <sys/socket.h>
 
//wiring Pi
#include <wiringPi.h>
#include <wiringSerial.h>
 
#define  BUFF_SIZE   1024

// Find Serial device on Raspberry with ~ls /dev/tty*
// ARDUINO_UNO "/dev/ttyACM0"
// FTDI_PROGRAMMER "/dev/ttyUSB0"
// HARDWARE_UART "/dev/ttyAMA0"
char device[]= "/dev/ttyACM0";
// filedescriptor
int fd;
unsigned long baud = 9600;
unsigned long time=0;
int   client_socket;
struct sockaddr_in   server_addr;
char   buff[BUFF_SIZE+5];
 
//prototypes
int main(void);
void loop(void);
void setup(void);
 
void setup(){
  client_socket  = socket( PF_INET, SOCK_STREAM, 0);
   if( -1 == client_socket)
   {
      printf( "socket 생성 실패\n");
      exit( 1);
   }
   memset( &server_addr, 0, sizeof( server_addr));
   server_addr.sin_family     = AF_INET;
   server_addr.sin_port       = htons(9999);
   server_addr.sin_addr.s_addr= inet_addr( "165.194.17.214");
   if( -1 == connect( client_socket, (struct sockaddr*)&server_addr, sizeof( server_addr) ) )
   {
      printf( "접속 실패\n");
      exit( 1);
   }

  printf("%s \n", "Raspberry Startup!");
  fflush(stdout);
 
  //get filedescriptor
  if ((fd = serialOpen (device, baud)) < 0){
    fprintf (stderr, "Unable to open serial device: %s\n", strerror (errno)) ;
    exit(1); //error
  }
 
  //setup GPIO in wiringPi mode
  if (wiringPiSetup () == -1){
    fprintf (stdout, "Unable to start wiringPi: %s\n", strerror (errno)) ;
    exit(1); //error
  }
 
  	char bytess[6];
	bytess[0] = 'R';
	bytess[1] = 'A';
	bytess[2] = 'S';
	bytess[3] = 'P';
	bytess[4] = 'B';
	bytess[5] = 'R';
  	write( client_socket, bytess, 6);
  	printf("%s\n",bytess);
  	fflush(stdout);
}

void loop(){
  // read signal
  if(serialDataAvail (fd)){
	char bytes[29];
	char sensor1;
    char sensor2;
	char check = serialGetchar (fd);
	if(check == '3' || check == '2' || check == '1' || check == '0'){

	if(check == '3'){
		sensor1 = '1';
		sensor2 = '1';
	}else if(check == '2'){
		sensor1 = '1';
		sensor2 = '0';
	}else if(check == '1'){
		sensor1 = '0';
		sensor2 = '1';
	}else{
		sensor1 = '0';
		sensor2 = '0';
	}

	printf("check : %c\n", check);
    printf("sensor1 : %c, sensor2 : %c\n", sensor1, sensor2);

	bytes[0] = 'C';
	bytes[1] = 'A';
	bytes[2] = 'U';
	bytes[3] = '2';
	bytes[4] = '0';
	bytes[5] = '8';
	bytes[6] = ',';
	bytes[7] = '5';
	bytes[8] = '0';
	bytes[9] = '1';
	bytes[10] = ',';
	bytes[11] = 'M';
	bytes[12] = ',';
	bytes[13] = sensor1;
	bytes[14] = '/';
	bytes[15] = 'C';
	bytes[16] = 'A';
	bytes[17] = 'U';
	bytes[18] = '2';
	bytes[19] = '0';
	bytes[20] = '8';
	bytes[21] = ',';
	bytes[22] = '5';
	bytes[23] = '0';
	bytes[24] = '2';
	bytes[25] = ',';
	bytes[26] = 'M';
	bytes[27] = ',';
	bytes[28] = sensor2;
 	write( client_socket, bytes, 29);
	printf("%s\n",bytes);
    fflush(stdout);
	}
   }
}
 
// main function for normal c++ programs on Raspberry
int main(){
  setup();
  while(1) loop();
  close(client_socket);
  return 0;
}
 
#endif //#ifdef RaspberryPi