arduino code for intial blinking light commit, BEFORE UPLOADING TO ARDUINO, DISCONNECT TX AND RX, then plug back in when done


void setup() {
  Serial.begin(9600);
  pinMode(7, OUTPUT);
}

void loop() {
  // Just listen for incoming data
  if (Serial.available() > 0) {
    char incoming = Serial.read();

  if (incoming == '1') {
      digitalWrite(7, HIGH);
    } else if (incoming == '0') {
      digitalWrite(7, LOW);
    }
  }
}


arduno code for "working pan/tilt buttons" commit:


#include <Servo.h>
#include <SoftwareSerial.h>

Servo tilt_servo;  // create servo object to control a servo, // twelve servo objects can be created on most boards
Servo pan_servo;

int pos = 0;    // variable to store the servo position

SoftwareSerial BTSerial(2, 3); //pins of bluetooth RX and TX

void setup() {
 tilt_servo.attach(9,500,2500);  // attaches the servo on pin 9 to the servo object
 pan_servo.attach(8);

 Serial.begin(9600);      //usb serial
}


void loop() {

 if (Serial.available() > 0) {
    char incoming = Serial.read();

    if (incoming == '1') {
      tilt();
    } 
    else if (incoming == '0') {
      pan();
    }
  }

}
int adjust(int angle){
  return (angle*2)/3 + 90;
}

void tilt() {

    for (pos = -135; pos <= 135; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree  // Maps 0-270 to 0-180
    tilt_servo.write(adjust(pos));       // tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15ms for the servo to reach the position
    }
    for (pos = 135; pos >= -135; pos -= 1) { // goes from 180 degrees to 0 degrees
      tilt_servo.write(adjust(pos));           // tell servo to go to position in variable 'pos'   
      delay(15);                       // waits 15ms for the servo to reach the position
  }
}

void pan() {
  pan_servo.write(100);
  delay(15);
  pan_servo.write(100);
  delay(15);
  //   for (pos = 0; pos <= 360; pos += 1) { // goes from 0 degrees to 180 degrees
  //     // in steps of 1 degree
  //     pan_servo.write(pos/2);              // tell servo to go to position in variable 'pos'
  //     delay(15);                       // waits 15ms for the servo to reach the position
  //   }
  //   for (pos = 360; pos >= 0; pos -= 1) { // goes from 180 degrees to 0 degrees
  //   pan_servo.write(pos/2);              // tell servo to go to position in variable 'pos'
  //   delay(15);                       // waits 15ms for the servo to reach the position
  // } 
}
