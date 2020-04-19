#include "EEPROM.h"
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

BLECharacteristic* wifiCredential = NULL;
BLECharacteristic* ownerPhoneNumber = NULL;
BLECharacteristic* deviceChipID = NULL;
BLECharacteristic* response = NULL;

uint16_t lasttouched = 0;
uint16_t currtouched = 0;
const int onwerPhoneNumberAddress = 1;
const int chipIdAddress = 11;
const int wifiAddress = 30;
const int modeAddress = 0;

String wifi_ssid;
String wifi_password;
String ownerPNumber;
String chipID;
String parentPath;
String childPath[3] = {"otp", "state", "trusted_devices"};
size_t childPathSize = 3;
String lock_state_path;
String lock_otp_path;

int modeIndex;
int knowDeviceIndex;
int lock_state;
int lock_otp;
uint8_t process = 0;

String keypad[] = {"*", "7", "4", "1", "0", "8", "5", "2", "#", "9", "6", "3"};
String trusted_devices[4];
int freq = 16000;
int channel = 0;
int resolution = 8;
boolean otpGetting;
int state;
int lockDelay = 5000;
unsigned long saveUnlockTime;
String OTP, _otp;

FirebaseData firebaseData;
FirebaseJson json;

void connectToDB() {
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  parentPath = "/lyoko_users/" + ownerPNumber + "/own_devices/" + chipID + "/lock";

  if (!Firebase.beginStream(firebaseData, parentPath))
  {
    Serial.println("------------------------------------");
    Serial.println("Can't begin stream connection...");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
  Firebase.setStreamCallback(firebaseData, streamCallback, streamTimeoutCallback);
}

class wifiCredentailCallBack: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *wifiCredential) {
      std::string rxValue = wifiCredential->getValue();
      String data;
      if (rxValue.length() > 0) {
        for (int i = 0; i <= rxValue.length() - 1; i++) {
          data = data + rxValue[i];
        }
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
        for (int i = 0; i <= value.length() - 1; i++) {
          ownerData = ownerData + value[i];
        }
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
        for (int i = 0; i <= value.length() - 1; i++) {
          chipIDData = chipIDData + value[i];
        }
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
  Serial.print("ownerData: ");
  Serial.println(ownerPNumber);

  chipID = EEPROM.readString(chipIdAddress);
  Serial.print("chipID: ");
  Serial.println(chipID);

  String wifiData = EEPROM.readString(wifiAddress);
  Serial.print("wifiData: ");
  Serial.println(wifiData);

  if (wifiData.length() > 0) {
    wifi_ssid = wifiData.substring(0, wifiData.indexOf("|"));
    wifi_password = wifiData.substring(wifi_ssid.length() + 1 );
  }

}

void wifiTask() {
  WiFi.begin(wifi_ssid.c_str(), wifi_password.c_str());
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED ) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
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
  Serial.println("Characteristic defined! Now you can read it in your phone!");

}

void getInitialData(StreamData &data) {
  FirebaseJson *json = data.jsonObjectPtr();
  Serial.println("Iterate JSON data:");
  size_t len = json->iteratorBegin();
  String key, value = "";
  int type = 0;
  for (size_t i = 0; i < len; i++) {
    json->iteratorGet(i, type, key, value);
    Serial.print(i);
    Serial.print(", ");
    Serial.print("Type: ");
    Serial.print(type == JSON_OBJECT ? "object" : "array");
    if (type == JSON_OBJECT) {
      if (key == "otp") {
        OTP = value;
        Serial.print(", OTP: ");
        Serial.println(OTP);
      } else if (key == "state") {
        state = value.toInt();
        Serial.print(", State: ");
        Serial.println(state);
      }
    } else {
      trusted_devices[i - 3] = value;
      Serial.print(", trusted_devices no.");
      Serial.print(i - 2);
      Serial.print(": ");
      Serial.println(trusted_devices[i - 3]);
    }
  }
  json->iteratorEnd();
}

void getTrustedDevices(StreamData data) {
  String posi = data.dataPath().substring(data.dataPath().length() - 1 );
  trusted_devices[posi.toInt()] = data.stringData();
  Serial.print("UPDATE TRUSTED DEVICE NO. ");
  Serial.print(posi.toInt()+1);
  Serial.print(": ");
  Serial.println(trusted_devices[posi.toInt()]);
}

void streamCallback(StreamData data) {
  int _state = 0;
  String event_path = data.dataPath();
  switch (event_path.length()) {
    case 6:
      _state = data.intData();
      break;
    case 4:
      OTP = data.stringData();
      Serial.println("OTP UPDATE");
      break;
    case 1:
      getInitialData(data);
      break;
    default:
      getTrustedDevices(data);
      break;
  }
  if (state == _state) {
    return;
  } else state = _state;

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
  json.clear();
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
  Serial.println("Đang giữ cho cửa không đóng nè");
  digitalWrite(relayPin, true);
  digitalWrite(holdLed, true);
  digitalWrite(ledPin, false);
  touchSound();
}

void streamTimeoutCallback(bool timeout) {
  if (timeout) {
    Serial.println();
    Serial.println("Stream timeout, resume streaming...");
    Serial.println();
  }
}

void setup() {
  Serial.begin(115200);
  ledcSetup(channel, freq, resolution);
  ledcAttachPin(speaker, channel);
  pinMode(ledPin, OUTPUT);
  pinMode(relayPin, OUTPUT);
  pinMode(holdLed, OUTPUT);
  if (!EEPROM.begin(EEPROM_SIZE)) {
    delay(1000);
  }
  modeIndex = EEPROM.read(modeAddress);
  Serial.print("mode: ");
  if (modeIndex == 0) {
    digitalWrite(ledPin, true);
    process = 0;
    bleTask();
  } else {
    Serial.println("Registered Device");
    digitalWrite(ledPin, false);
    getDataCredentail();
    wifiTask();
    connectToDB();
  }
  otpGetting = false;
  _otp = "";
  if (!cap.begin(0x5A)) {
    Serial.println("MPR121 not found, check wiring?");
    while (1);
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
  Serial.print("Confirming OTP: ");
  Serial.println(_otp);
  otpGetting = false;
  if (OTP.equals(_otp)) {
    _otp = "";
    Serial.println("SUCCESS");
    Firebase.setString(firebaseData, parentPath + "/otp", "");
    Firebase.setInt(firebaseData, parentPath + "/state", 1);
  } else {
    Serial.println("OTP DENIED");
  }

}

void loop() {
  while (state == 1) {
    if ( millis() - saveUnlockTime >= lockDelay ) {
      Firebase.setInt(firebaseData, parentPath + "/state", 0);
      state = -1 ;
    }
  }
  if (modeIndex == 0) {
    if (process == 3) {
      response->notify();
      Serial.print("Reset sau 3s");
      delay(1000);
      ESP.restart();
    }
    response->notify();
    delay(10);
    return;
  }
  currtouched = cap.touched();
  if (!(currtouched & _BV(0)) && (lasttouched & _BV(0)) ) {
    otpGetting = true;
    _otp = "";
    touchSound();
  }
  if (_otp.length() == 6) {
    ConfirmOTP();
    return;
  }
  if (otpGetting) {
    for (uint8_t i = 1; i < 12; i++) {
      if (i == 8) {
        if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) ) {
          if (_otp != "") {
            _otp =  _otp.substring(0, _otp.length() - 1 );
            touchSound();
          } else {
            otpGetting = false;
          }
        }
      } else {
        // it if *is* touched and *wasnt* touched before, alert!
        if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) ) {
          if (_otp.length() < 6) {
            _otp = _otp + keypad[i];
            Serial.print("OTP: ");
            Serial.println(_otp);
            touchSound();
          }
        }
      }
    }
  }
  lasttouched = currtouched;
  return;

}
