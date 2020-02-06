#include <Wire.h>
#include <DHT.h>
#include <DHT_U.h>
#include <Adafruit_GFX.h>
#include <Adafruit_SSD1306.h>
#include <Adafruit_Sensor.h>
#define DHTPIN 2     // what pin we're connected to
#define DHTTYPE DHT11   // DHT 11
#define OLED_RESET -1
#define LDR_PIN                   0   // Here Analog pin 0
#define MAX_ADC_READING           1023  // 10bit adc 2^10 == 1023
#define ADC_REF_VOLTAGE           5.0 // 5 volts
#define REF_RESISTANCE            10030 // 10k resistor 
#define LUX_CALC_SCALAR           12518931 // Formula 
#define LUX_CALC_EXPONENT         -1.405  // exponent first calculated with calculator 
Adafruit_SSD1306 display(OLED_RESET);
DHT_Unified dht(DHTPIN, DHTTYPE);
constexpr uint8_t den_phong_khach = 5;
constexpr uint8_t den_nha_bep = 3;
constexpr uint8_t den_ngoai_san = 6;
constexpr uint8_t phong_master = 7;     // Configurable, see typical pin layout above
constexpr uint8_t phong_sub = 8;
constexpr uint8_t nha_ve_sinh = 9;
constexpr uint8_t anh_sang = A0;
constexpr uint8_t chuyendong = 10;
char sanState = "0";
float lux = 0.00, ADC_value = 0.0048828125, LDR_value;
void setup() {
  for (int i = 3; i < 10; i++) {
    pinMode(i, OUTPUT);
  }
  pinMode(chuyendong, INPUT);
  Serial.begin(9600);
  display.begin(SSD1306_SWITCHCAPVCC, 0x3C ); //
  display.display();
  dht.begin();

}

void displayTempHumid() {
  sensors_event_t event;
  dht.humidity().getEvent(&event);
  float h = event.relative_humidity;
  dht.temperature().getEvent(&event);
  float t = event.temperature;

  int   ldrRawData;
  float resistorVoltage, ldrVoltage;
  float ldrResistance;
  float ldrLux;
  ldrRawData = analogRead(LDR_PIN);
  resistorVoltage = (float)ldrRawData / MAX_ADC_READING * ADC_REF_VOLTAGE;
  // voltage across the LDR is the 5V supply minus the 5k resistor voltage
  ldrVoltage = ADC_REF_VOLTAGE - resistorVoltage;
  // LDR_RESISTANCE_CONVERSION
  // resistance that the LDR would have for that voltage
  ldrResistance = ldrVoltage / resistorVoltage * REF_RESISTANCE;
  // LDR_LUX
  // Change the code below to the proper conversion from ldrResistance to
  // ldrLux
  ldrLux = LUX_CALC_SCALAR * pow(ldrResistance, LUX_CALC_EXPONENT);

  char buffer[6];
  String humi = dtostrf(h, 5, 2, buffer);
  String temp = dtostrf(t, 5, 2, buffer);
  Serial.print("H" + humi + "T" + temp);
  Serial.print("\n");
  delay(150);

  //kiem tra bat ky lan doc that bai va thoat
  if (isnan(h) || isnan(t)) {
    display.clearDisplay(); // clearing the display
    display.setTextColor(WHITE); //setting the color
    display.setTextSize(1); //set the font size
    display.setCursor(5, 0); //set the cursor coordinates
    display.print("Failed to read from DHT sensor!");
    return;
  }
  if (ldrLux <= 10) {
    //if (sanState == "0") {
    //      digitalWrite(den_ngoai_san, LOW);
    //      sanState = "0";
    //} else {
    digitalWrite(den_ngoai_san, HIGH);
    sanState = "1";
  }
  else {
    //    if (sanState == "0") {
    //digitalWrite(den_ngoai_san, LOW);
    //    } else {
    //      digitalWrite(den_ngoai_san, HIGH);
    //      sanState = "1";
    //
    //    }
  }
  display.clearDisplay();
  display.setTextColor(WHITE);
  display.setTextSize(1);
  display.setCursor(0, 0);
  display.print("Do am: ");
  display.print(h);
  display.print(" %\t");
  display.setCursor(0, 10);
  display.print("Nhiet do: ");
  display.print(t);
  display.print(" C");
  display.setCursor(0, 20);
  display.print("LUX: ");
  display.print(ldrLux);

}
void loop() {
  displayTempHumid();
  display.display();
  delay(200);
  receiveEvent();
  long state = digitalRead(chuyendong);
  if (state == HIGH) {
    digitalWrite(nha_ve_sinh, HIGH);
  }
  else {
    digitalWrite(nha_ve_sinh, LOW);

  }

}
void receiveEvent() {
  int data;
  if (Serial.available() > 0) {
    data = Serial.read();
    delay(200);
    //Serial.println(data);
    if (data == 31) {
      digitalWrite(den_nha_bep, HIGH);
    }
    if (data == 30) {
      digitalWrite(den_nha_bep, LOW);
    }
    if (data == 81) {
      digitalWrite(phong_sub, HIGH);
    }
    if (data == 80) {
      digitalWrite(phong_sub, LOW);
    }
    if (data == 51) {
      digitalWrite(den_phong_khach, HIGH);
    }
    if (data == 50) {
      digitalWrite(den_phong_khach, LOW);
    }
    if (data == 71) {
      digitalWrite(phong_master, HIGH);
    }
    if (data == 70) {
      digitalWrite(phong_master, LOW);
    }
    if (data == 61) {
      digitalWrite(den_ngoai_san, HIGH);
      sanState = "0";
    }
    if (data == 60) {
      digitalWrite(den_ngoai_san, LOW);
      sanState = "1";
    }
    if (data == 1) {
      digitalWrite(den_nha_bep, HIGH);
      digitalWrite(phong_sub, HIGH);
      digitalWrite(den_phong_khach, HIGH);
      digitalWrite(phong_master, HIGH);
      digitalWrite(den_ngoai_san, HIGH);
    }
    if (data == 0) {
      digitalWrite(den_ngoai_san, LOW);
      digitalWrite(phong_master, LOW);
      digitalWrite(den_phong_khach, LOW);
      digitalWrite(phong_sub, LOW);
      digitalWrite(den_nha_bep, LOW);
    }
  }


}
