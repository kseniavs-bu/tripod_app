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
