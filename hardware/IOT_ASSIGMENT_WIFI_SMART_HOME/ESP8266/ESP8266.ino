#define BLYNK_PRINT Serial
#include <Wire.h>
#include<SoftwareSerial.h> //Included SoftwareSerial Library
SoftwareSerial Send_data(3, 1); //Started SoftwareSerial at RX and TX pin of ESP8266/NodeMCU
#include <I2C_Anything.h>
#include <ESP8266WiFi.h>
#include <BlynkSimpleEsp8266.h>
// You should get Auth Token in the Blynk App.
// Go to the Project Settings (nut icon).
// Your WiFi credentials.
// Set password to "" for open networks.
char auth[] = "e54be1a28b7e462fafbbf4e1be769819";
char ssid[] = "HuyMinh";
char pass[] = "0981410403";
int doorState = LOW;
int kitchenState = LOW;
int singleState = LOW;
int livingRomState = LOW;
int masterState = LOW;
int outSideState = LOW;
int AllState = LOW;

BlynkTimer timer;
BLYNK_CONNECTED() {
  Blynk.syncVirtual(V0, V1, V2, V3, V4, V5, V6);
}
// Use of syncAll() will cause this function to be called
// Parameter holds last slider value
BLYNK_WRITE(V0) {
  doorState = param.asInt();
  if (doorState == HIGH) {
    data_show(111);
  }
}
BLYNK_WRITE(V1) {
  kitchenState = param.asInt();
  if (kitchenState == HIGH) {
    data_show(31);
  } else data_show(30);
}
BLYNK_WRITE(V2) {
  singleState = param.asInt();
  if (singleState == HIGH) {
    data_show(81);
  } else data_show(80);
}
BLYNK_WRITE(V3) {
  livingRomState = param.asInt();
  if (livingRomState == HIGH) {
    data_show(51);
  } else data_show(50);
}
BLYNK_WRITE(V4) {
  masterState = param.asInt();
  if (masterState == HIGH) {
    data_show(71);
  } else data_show(70);
}
BLYNK_WRITE(V5) {
  outSideState = param.asInt();
  if (outSideState == HIGH) {
    data_show(61);
  } else data_show(60);
}
BLYNK_WRITE(V6) {
  AllState = param.asInt();
  if (AllState == HIGH) {
    Blynk.virtualWrite(V1, AllState); Blynk.virtualWrite(V6, AllState);
    Blynk.virtualWrite(V2, AllState); Blynk.virtualWrite(V3, AllState);
    Blynk.virtualWrite(V4, AllState); Blynk.virtualWrite(V5, AllState);

    data_show(1);
  } else {
    Blynk.virtualWrite(V1, AllState); Blynk.virtualWrite(V6, AllState);
    Blynk.virtualWrite(V2, AllState); Blynk.virtualWrite(V3, AllState);
    Blynk.virtualWrite(V4, AllState); Blynk.virtualWrite(V5, AllState);

    data_show(0);
  }

}
void data_show(int msg) {
  Send_data.write(msg);
  delay(300);
}

void setup()
{
  // Debug console

  Send_data.begin(9600);
  Blynk.begin(auth, ssid, pass);
  Wire.begin(D1, D2); /* join i2c bus with SDA=D1 and SCL=D2 of NodeMCU */
  // You can also specify server:
  //Blynk.begin(auth, ssid, pass, "blynk-cloud.com", 80);
  //Blynk.begin(auth, ssid, pass, IPAddress(192,168,1,100), 8080);
}
String dataIncoming;
void loop()
{
  Blynk.run();
  if (Send_data.available()) {
    char c = Send_data.read();
    if (c == '\n') {
      getData(dataIncoming);
      dataIncoming = "";
    } else dataIncoming += c;
  }
}
void getData(String data) {
  //H88.00T99.55S1
  float humi;
  float temp;
  int stateV5;
  humi = data.substring(1, data.indexOf("T")).toFloat();
  temp = data.substring(data.indexOf("T")+1,  data.indexOf("S")).toFloat();
  stateV5 = data.substring(data.indexOf("S") + 1).toInt();
  Blynk.virtualWrite(V7, humi);
  Blynk.virtualWrite(V8, temp);
 // Blynk.virtualWrite(V5, stateV5);
  delay(300);
}
