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


arduno code for "working 6 buttons" commit:


#include <Servo.h>
#include <SoftwareSerial.h>

Servo tilt_servo;  // create servo object to control a servo, // twelve servo objects can be created on most boards
Servo pan_servo;

int pos = 0;    // variable to store the servo position

SoftwareSerial BTSerial(2, 3); //pins of bluetooth RX and TX

void setup() {
 //tilt_servo.attach(9,500,2500);  // attaches the servo on pin 9 to the servo object
 //pan_servo.attach(8);
  pinMode(5, OUTPUT);
  pinMode(6, OUTPUT);
 Serial.begin(9600);      //usb serial
 digitalWrite(5, LOW);
 digitalWrite(6, LOW);
}


void loop() {
  if (Serial.available() > 0) {
    char incoming = Serial.read();

    //PAN LEFT
    if (incoming == '2') {
      pan_servo.attach(8);
      delay(5);
      pan_left();
      pan_servo.detach();
      delay(500);
    }
    
    //PAN RIGHT
    else if (incoming == '0') {
      pan_servo.attach(8);
      delay(5);
      pan_right();
      pan_servo.detach();
      delay(500);
    }

    //TILT CONFIG
    else if (incoming == '1') {
      tilt_servo.attach(9,500,2500);
      delay(5);
      //tilt_servo.write(adjust(50));
      tilt();
      tilt_servo.detach();
      delay(500);
    } 

    //STRAIGHTEN TILT
    else if (incoming == '3') {
      digitalWrite(5, LOW);
      tilt_servo.attach(9,500,2500);
      delay(5);
      //tilt_servo.write(adjust(50));
      default_tilt();
      delay(500);
      tilt_servo.detach();
      delay(500);
    }
    //MOVE UP
    else if (incoming == '5') {
      digitalWrite(5, HIGH);  // Send signal
      delay(1000);
      digitalWrite(5, LOW);
    }
    //MOVE DOWN
    else if (incoming == '6') {
      digitalWrite(6, HIGH);  // Send signal
      delay(1000);
      digitalWrite(6, LOW);
    }
  }


// delay(5);
// tilt_servo.attach(9,500,2500);
// delay(5);
// tilt();
// tilt_servo.detach();
// delay(5);

//  if (Serial.available() > 0) {
//     char incoming = Serial.read();

//     if (incoming == '1') {
//       tilt();
//     } 
//     else if (incoming == '0') {
//       pan();
//     }
//   }

}
int adjust(int angle){
  return (angle*2)/3 + 90;
}

void default_tilt() {
  tilt_servo.write(adjust(0));
}

void tilt() {
    for (pos = -80; pos <= 80; pos += 1) { // goes from 0 degrees to 180 degrees
    // in steps of 1 degree  // Maps 0-270 to 0-180
    tilt_servo.write(adjust(pos));       // tell servo to go to position in variable 'pos'
    delay(15);                       // waits 15ms for the servo to reach the position
    }
    for (pos = 80; pos >= -80; pos -= 1) { // goes from 180 degrees to 0 degrees
      tilt_servo.write(adjust(pos));           // tell servo to go to position in variable 'pos'   
      delay(15);                       // waits 15ms for the servo to reach the position
  }
}

void pan_right() {

  // Spin clockwise (fast)
  pan_servo.write(85);
  delay(2000);
  
  // STOP
  pan_servo.write(90);
  delay(1000);
  
}

void pan_left() {
  
  // Spin counter-clockwise (fast)
  pan_servo.write(100);
  delay(2000);
  
  // STOP
  pan_servo.write(90);
  delay(1000);
}
