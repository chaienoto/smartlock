#include "EEPROM.h"
#include "time.h"
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <FirebaseESP32.h>
#include <WiFi.h>
#include <Wire.h>
#include "Adafruit_MPR121.h"
#define EEPROM_SIZE 256
#define FIREBASE_HOST "lyoko-smartlock.firebaseio.com"
#define FIREBASE_AUTH "OiYuKBdHNenNFVtrb4Ws4ieaNgD3Z6rE2KGQhSiS"
#define ledPin 2
#define relayPin 4
#define holdLed 15
#define speaker 5
#define SERVICE_LYOKO_UUID                      "7060da7f-1ce6-43d1-b58c-2c595f8f9a56"
#define CHARACTERISTIC_WIFI_CREDENTIAL_UUID     "b82150b1-48e9-4a1b-a18c-3f2c140a8104"
#define CHARACTERISTIC_RESPONSE_UUID            "459d013b-061c-430b-a4fe-734cc22012cb"
#define CHARACTERISTIC_OWNER_PHONE_NUMBER_UUID  "e7608c36-76e7-4e9c-9263-1786f7a2f854"
#define CHARACTERISTIC_CHIP_ID_UUID             "95374a93-101c-4f2c-a590-2e8fcb3dfd37"

Adafruit_MPR121 cap = Adafruit_MPR121();

// for rtc
const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 7 * 3600;
const int   daylightOffset_sec = 0;

// new device characteristic
BLECharacteristic* wifiCredential = NULL;
BLECharacteristic* ownerPhoneNumber = NULL;
BLECharacteristic* deviceChipID = NULL;
BLECharacteristic* response = NULL;
uint8_t process = 0;

// keypad state save
uint16_t lasttouched = 0;
uint16_t currtouched = 0;
String keypad[] = {"*", "7", "4", "1", "0", "8", "5", "2", "#", "9", "6", "3"};

// device eeprom info address
const int onwerPhoneNumberAddress = 1;
const int chipIdAddress = 11;
const int wifiAddress = 30;
const int modeAddress = 0;

// device eeprom info
String wifi_ssid;
String wifi_password;
String ownerPNumber;
String chipID;

// db path
String parentPath;
String lock_state_path;
String lock_otp_path;
String histories_path;

// device info
int modeIndex;
int lockDelay;
int state, _state;
String otp, _otp;
String trusted_devices[4];
boolean otpGetting;
boolean leaving;
String unlock_type;
int error_count = 0;
unsigned long saveUnlockTime;

// speaker setting
int freq = 16000;
int channel = 0;
int resolution = 8;

FirebaseData firebaseData;
FirebaseData _firebaseData;
FirebaseJson json;

void connectToDB() {
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  parentPath = "/lyoko_users/" + ownerPNumber + "/own_devices/" + chipID + "/";
  lock_otp_path = parentPath + "lock/otp";
  lock_state_path = parentPath + "lock/state";
  histories_path = parentPath + "histories";
  if (!Firebase.beginStream(firebaseData, parentPath + "lock")) {
    Serial.println("------------------------------------");
    Serial.println("Can't begin stream connection...");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
  Firebase.setStreamCallback(firebaseData, streamCallback, streamTimeoutCallback);
}

void streamCallback(StreamData data) {
  Serial.println(data.dataPath());
  if (data.dataPath() == "/") {
    FirebaseJson *json = data.jsonObjectPtr();
    size_t len = json->iteratorBegin();
    String key, value = "";
    int type = 0;
    for (size_t i = 0; i < len; i++) {
      json->iteratorGet(i, type, key, value);
      if (type == JSON_OBJECT) {
        if (key == "otp") otp = value;
        else if (key == "state") _state = value.toInt();
        else if (key == "delay") lockDelay = value.toInt();
      } else trusted_devices[i - 3] = value;
    }
    logInfo();
  } else if (data.dataPath() == "/state") {
    _state = data.intData();
    stateCheck();
  } else if (data.dataPath() == "/otp") {
    otp = data.stringData();
  } else if (data.dataPath() == "/delay") {
    lockDelay = data.intData();
  } else {
    int posi = data.dataPath().substring(data.dataPath().length() - 1 ).toInt();
    trusted_devices[posi] = data.stringData();
  }
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println();
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
}

class wifiCredentailCallBack: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *wifiCredential) {
      std::string rxValue = wifiCredential->getValue();
      String data;
      if (rxValue.length() > 0) {
        for (int i = 0; i <= rxValue.length() - 1; i++)
          data = data + rxValue[i];
        Serial.print("wifi: ");
        Serial.println(data);
        EEPROM.writeString(wifiAddress, data);
        EEPROM.commit();
        process = 1;
        response->setValue(&process, 1);
        Serial.print("process: ");
        Serial.println(process);
      }
    }
};

class ownerPhoneNumberCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *ownerPhoneNumber) {
      std::string value = ownerPhoneNumber->getValue();
      String ownerData;
      if (value.length() > 0) {
        for (int i = 0; i <= value.length() - 1; i++)
          ownerData = ownerData + value[i];
        Serial.print("owner: ");
        Serial.println(ownerData);
        EEPROM.writeString(onwerPhoneNumberAddress, ownerData);
        EEPROM.commit();
        process = 2;
        response->setValue(&process, 1);
        Serial.print("process: ");
        Serial.println(process);
      }
    }
};

class deviceChipIDCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *deviceChipID) {
      std::string value = deviceChipID->getValue();
      String chipIDData;
      if (value.length() > 0) {
        for (int i = 0; i <= value.length() - 1; i++)
          chipIDData = chipIDData + value[i];
        Serial.print("chipID: ");
        Serial.println(chipIDData);
        EEPROM.writeString(chipIdAddress, chipIDData);
        EEPROM.write(modeAddress, 1);
        EEPROM.commit();
        process = 3;
        response->setValue(&process, 1);
        Serial.print("process: ");
        Serial.println(process);

      }
    }
};

void getDataCredentail() {
  ownerPNumber = EEPROM.readString(onwerPhoneNumberAddress);
  chipID = EEPROM.readString(chipIdAddress);
  String wifiData = EEPROM.readString(wifiAddress);
  if (wifiData.length() > 0) {
    wifi_ssid = wifiData.substring(0, wifiData.indexOf("|"));
    wifi_password = wifiData.substring(wifi_ssid.length() + 1 );
  }
  Serial.print("ownerData: ");
  Serial.println(ownerPNumber);
  Serial.print("chipID: ");
  Serial.println(chipID);
  Serial.print("wifiData: ");
  Serial.println(wifiData);
}

void wifiTask() {
  WiFi.begin(wifi_ssid.c_str(), wifi_password.c_str());
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED ) {
    Serial.print(".");
    delay(300);
  }
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
}

void bleTask() {
  BLEDevice::init("Lyoko Smart Lock");                                        // Create Device
  BLEServer* server = BLEDevice::createServer();                              // Create Server
  BLEService* lyoko_service = server->createService(SERVICE_LYOKO_UUID);

  // Create a BLE Characteristic
  wifiCredential = lyoko_service->createCharacteristic(
                     CHARACTERISTIC_WIFI_CREDENTIAL_UUID,
                     BLECharacteristic::PROPERTY_WRITE |
                     BLECharacteristic::PROPERTY_READ
                   );

  ownerPhoneNumber = lyoko_service->createCharacteristic(
                       CHARACTERISTIC_OWNER_PHONE_NUMBER_UUID,
                       BLECharacteristic::PROPERTY_WRITE
                     );

  deviceChipID = lyoko_service->createCharacteristic(
                   CHARACTERISTIC_CHIP_ID_UUID,
                   BLECharacteristic::PROPERTY_WRITE
                 );

  response = lyoko_service->createCharacteristic(
               CHARACTERISTIC_RESPONSE_UUID,
               BLECharacteristic::PROPERTY_READ |
               BLECharacteristic::PROPERTY_NOTIFY
             );

  // Create a BLE Descriptor
  response->addDescriptor(new BLE2902());

  wifiCredential->setCallbacks(new wifiCredentailCallBack());
  ownerPhoneNumber->setCallbacks(new ownerPhoneNumberCallback());
  deviceChipID->setCallbacks(new deviceChipIDCallback());

  // Start the service
  lyoko_service->start();
  // Start advertising
  BLEAdvertising* pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(SERVICE_LYOKO_UUID);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x12);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  digitalWrite(ledPin, true);
}

void logInfo() {
  Serial.print("OTP: ");
  Serial.println(otp);
  Serial.print("State: ");
  Serial.println(state);
  Serial.print("Delay: ");
  Serial.println(lockDelay);
  for (int i = 0; i <= 3; i++) {
    Serial.print("trusted_devices no.");
    Serial.print(i);
    Serial.print(": ");
    Serial.println(trusted_devices[i]);
  }
}

void stateCheck() {
  if (state == _state) return;
  state = _state;
  switch (state) {
    case 1:
      openTheGate();
      break;
    case 2:
      holdOn();
      break;
    default:
      lockTheGate();
      break;
  }
}



void lockTheGate() {
  Serial.println("Đóng chặt lắm rồi này");
  digitalWrite(ledPin, false);
  digitalWrite(holdLed, false);
  digitalWrite(relayPin, false);
  successSound();
  lasttouched = 0;
}

void openTheGate() {
  saveUnlockTime = millis();
  Serial.print("saveTime: ");
  Serial.println(saveUnlockTime);
  Serial.println("Mở rồi này");
  digitalWrite(holdLed, false);
  digitalWrite(ledPin, true);
  digitalWrite(relayPin, true);
  successSound();
}

void holdOn() {
  state = 2;
  Serial.println("Đang giữ cho cửa không đóng nè");
  digitalWrite(relayPin, true);
  digitalWrite(holdLed, true);
  digitalWrite(ledPin, false);
  touchSound();
}

void historySave() {
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }
  char timeStringBuff[50];
  strftime(timeStringBuff, sizeof(timeStringBuff), "%A, %B %d %Y %H:%M:%S", &timeinfo);
  json.clear().set("unlock_name", "Người có mã OTP");
  json.set("unlock_type", unlock_type);
  json.set("unlock_time", String(timeStringBuff));
  Firebase.pushJSON(_firebaseData, histories_path, json);
  Firebase.setInt(_firebaseData, lock_state_path, 1);
}

void setup() {
  Serial.begin(115200);
  // setup pinMode and sound
  ledcSetup(channel, freq, resolution);
  ledcAttachPin(speaker, channel);
  pinMode(ledPin, OUTPUT);
  pinMode(relayPin, OUTPUT);
  pinMode(holdLed, OUTPUT);
  //check devices mode
  if (!EEPROM.begin(EEPROM_SIZE)) {
    delay(1000);
  }
  modeIndex = EEPROM.read(modeAddress);
  if (modeIndex == 0) {
    bleTask();
  } else {
    getDataCredentail();
    wifiTask();
    connectToDB();
    lockTheGate();
  }
  // keypad begin
  otpGetting = false;
  _otp = "";
  if (!cap.begin(0x5A)) {
    Serial.println("MPR121 not found, check wiring?");
  }
  Serial.println("MPR121 found!");
}

void touchSound() {
  ledcWrite(channel, 255);
  delay(50);
  ledcWrite(channel, 0);
}

void successSound() {
  for (int i = 0; i < 2; i++ ) {
    ledcWrite(channel, 255);
    delay(50);
    ledcWrite(channel, 0);
    delay(20);
  }
}

void ConfirmOTP() {
  otpGetting = false;
  Serial.println("CHECKING OTP");
  if (otp.equals(_otp)) {
    Serial.println("SUCCESS");
    state = 1;
    openTheGate();
    unlock_type = "otp";
    historySave();
    Firebase.setString(_firebaseData, lock_otp_path, "");
  } else {
    Serial.println("DENIED");
  }
  _otp = "";

}

void loop() {
  // mode check
  while (modeIndex == 0) {
    response->notify();
    if (process == 3) ESP.restart();
    delay(200);
  }
  // lock state check
  while (state) {
    if ( millis() - saveUnlockTime >= lockDelay ) {
      state = 0;
      lockTheGate();
      Firebase.setInt(_firebaseData, lock_state_path, 0);
      Serial.println(millis());
    }
  }
  currtouched = cap.touched();
  // if * touch -> keypad on
  if (!(currtouched & _BV(0)) && (lasttouched & _BV(0)) ) {
    otpGetting = true;
    _otp = "";
    touchSound();
  }
  // run below task when keypad on

  // if OTP complete -> check
  if (_otp.length() == 6) {
    ConfirmOTP();
    return;
  }
  // if # touch -> delete last number or turn of keypad
  if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) ) {
    if (_otp != "") {
      _otp =  _otp.substring(0, _otp.length() - 1 );
      touchSound();
    } else otpGetting = false;
  }
  // get OTP
  if (otpGetting)
    for (uint8_t i = 1; i < 12; i++) {
      // it if *is* touched and *wasnt* touched before, alert!
      if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) )
        if (_otp.length() < 6) {
          _otp = _otp + keypad[i];
          Serial.print("otp: ");
          Serial.println(_otp);
          touchSound();
        }
    }
  lasttouched = currtouched;
  return;
}
