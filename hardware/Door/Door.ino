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

#define SERVICE_LYOKO_UUID                      "7060da7f-1ce6-43d1-b58c-2c595f8f9a56"
#define CHARACTERISTIC_WIFI_CREDENTIAL_UUID     "b82150b1-48e9-4a1b-a18c-3f2c140a8104"
#define CHARACTERISTIC_RESPONSE_UUID            "459d013b-061c-430b-a4fe-734cc22012cb"
#define CHARACTERISTIC_OWNER_PHONE_NUMBER_UUID  "e7608c36-76e7-4e9c-9263-1786f7a2f854"
#define CHARACTERISTIC_CHIP_ID_UUID             "95374a93-101c-4f2c-a590-2e8fcb3dfd37"

BLECharacteristic* wifiCredential = NULL;
BLECharacteristic* ownerPhoneNumber = NULL;
BLECharacteristic* deviceChipID = NULL;
BLECharacteristic* response = NULL;

const int onwerPhoneNumberAddress = 1;
const int chipIdAddress = 11;
const int wifiAddress = 30;
const int modeAddress = 0;

String wifi_ssid;
String wifi_password;
String ownerPNumber;
String chipID;
String childPath[2] = {"lock_state", "lock_otp"};
String parentPath;

int modeIndex;
int knowDeviceIndex;
int lock_state;
int lock_otp;
uint8_t process = 0;


FirebaseData firebaseData;
FirebaseJson json;

//void printResult(FirebaseData &data);
//void printResult(StreamData &data);

void connectToDB() {
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);

  //  /phone_number_registered/336471477/devices/24:62:AB:D7:D9:A6/lock_state;
  parentPath = "/phone_number_registered/" + ownerPNumber + "/devices/" + chipID + "/lock_state";

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

void streamCallback(StreamData data) {
  Serial.println("STREAM PATH: " + data.streamPath());
  Serial.println("EVENT TYPE: " + data.eventType());
  Serial.print("VALUE: ");
  Serial.print(data.intData());
  Serial.println();
  int result = data.intData();
  switch (result) {
    case 1:
      Serial.println("Mở rồi này");
      digitalWrite(holdLed, false);
      digitalWrite(ledPin, true);
      digitalWrite(relayPin, true);
      break;
    case 2:
      Serial.println("Đang giữ cho cửa không đóng nè");
      digitalWrite(relayPin, true);
      digitalWrite(holdLed, true);
      digitalWrite(ledPin, false);
      break;
    default:
      Serial.println("Đóng chặt lắm rồi này");
      digitalWrite(ledPin, false);
      digitalWrite(holdLed, false);
      digitalWrite(relayPin, false);
      break;
  }
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
  pinMode(ledPin, OUTPUT);
  pinMode(relayPin, OUTPUT);
  pinMode(holdLed, OUTPUT);
  if (!EEPROM.begin(EEPROM_SIZE)) {
    delay(1000);
  }
  process = 0;
  modeIndex = EEPROM.read(modeAddress);
  Serial.print("mode: ");
  if (modeIndex == 0) {
    Serial.println("New Device");
    digitalWrite(ledPin, true);
    bleTask();
  } else {
    Serial.println("Registered Device");
    digitalWrite(ledPin, false);
    getDataCredentail();
    wifiTask();
    connectToDB();
  }
}

void loop() {
  if (modeIndex == 0) {
    if (process == 3) {
      response->notify();
      Serial.print("Reset sau 3s");
      delay(1000);
      ESP.restart();
    }
    response->notify();
    delay(80);
  }

}
