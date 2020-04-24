#include "EEPROM.h"
#include "time.h"
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include <BLEScan.h>
#include <BLEAdvertisedDevice.h>
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
// scan device
BLEScan* pBLEScan;

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
String lock_delay_path;
String histories_path;
String trusted_devices_address_path;
String trusted_devices_name_path;
String update_code_path;
String alert_code_path;

// TIME DELAYTION
int stateUnlockDelay;
int otpTypingDelay = 4000;
int devicesScanDelay = 5000;
int updateDataDelay = 1000;
unsigned long devicesScanPrevMillis;
unsigned long otpTypingPrevMillis;
unsigned long updateDataPrevMillis;
unsigned long stateUnlockPrevMillis;


// device info
int modeIndex;
int state, _state;
String otp;
String _otp = "";
String trusted_devices_name[4];
String trusted_devices_address[4];
boolean otpGetting = false;
boolean leaving;
boolean dataSendinng = false;
String unlock_type;
int error_count = 0;
int update_code = -1;


// speaker setting
int freq = 16000;
int channel = 0;
int resolution = 8;

FirebaseData firebaseData;
FirebaseData _firebaseData;
FirebaseJson json;

void stateCheck() {
  if (state == _state) return;
  state = _state;
  switch (state) {
    case 1: openTheGate(); break;
    case 2: holdOn(); break;
    default: lockTheGate(); break;
  }
}
void stateChange(int _s) {
  Firebase.setInt(firebaseData, lock_state_path, _s);
}
void lockTheGate() {
  Serial.println("Đóng chặt lắm rồi này");
  digitalWrite(ledPin, false);
  digitalWrite(holdLed, false);
  digitalWrite(relayPin, false);
  successSound();
  state = 0;
  lasttouched = 0;
  stateChange(state);
}

void openTheGate() {
  state = 1;
  stateUnlockPrevMillis = millis();
  Serial.print("saveTime: ");
  Serial.println(stateUnlockPrevMillis);
  Serial.println("Mở rồi này");
  digitalWrite(holdLed, false);
  digitalWrite(ledPin, true);
  digitalWrite(relayPin, true);
  successSound();
  stateChange(state);
}

void holdOn() {
  state = 2;
  touchSound();
}

void historySave() {
  char timeStringBuff[50];
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) return;
  strftime(timeStringBuff, sizeof(timeStringBuff), "%A, %B %d %Y %H:%M:%S", &timeinfo);
  json.clear().add("unlock_name", "Người có mã OTP");
  json.add("unlock_type", unlock_type);
  json.add("unlock_time", String(timeStringBuff));
  Serial.println("Saving History.....");
  Firebase.pushJSON(firebaseData, histories_path, json);
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
  if (otp.equals(_otp)) {
    openTheGate();
    otp = "";
    unlock_type = "otp";
    error_count = 0;
    Firebase.setString(firebaseData, lock_otp_path, "");
    historySave();
  } else
    Serial.println("DENIED");
  offGettingOTP();
}

//-------------------------------------------------------------------------------------
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

void newDeviceTask() {
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
//-------------------------------------------------------------------------------------
void getDataCredentail() {
  ownerPNumber = EEPROM.readString(onwerPhoneNumberAddress);
  chipID = EEPROM.readString(chipIdAddress);
  String s = EEPROM.readString(wifiAddress);
  if (s.length() > 0) {
    wifi_ssid = s.substring(0, s.indexOf("|"));
    wifi_password = s.substring(wifi_ssid.length() + 1 );
  }
  // SETTING DATABASE PATH
  parentPath = "/lyoko_users/" + ownerPNumber + "/own_devices/" + chipID + "/";
  lock_otp_path = parentPath + "lock/otp";
  lock_state_path = parentPath + "lock/state";
  lock_delay_path = parentPath + "lock/delay";
  histories_path = parentPath + "histories";
  trusted_devices_address_path = parentPath + "lock/trusted_devices_address";
  trusted_devices_name_path = parentPath + "lock/trusted_devices_name";
  update_code_path = parentPath + "lock/update_code";
  alert_code_path = parentPath + "lock/alert_code";

}

void registeredDeviceTask() {
  WiFi.begin(wifi_ssid.c_str(), wifi_password.c_str());
  while (WiFi.status() != WL_CONNECTED ) {
  }
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  getAllData();
}

class MyAdvertisedDeviceCallbacks: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      Serial.printf("Advertised Device: %s \n", advertisedDevice.toString().c_str());
    }
};

void setup() {
  Serial.begin(115200);
  if (!cap.begin(0x5A))
    Serial.println("MPR121 not found");
  else Serial.println("MPR121 found!");
  ledcSetup(channel, freq, resolution);
  ledcAttachPin(speaker, channel);
  pinMode(ledPin, OUTPUT);
  pinMode(relayPin, OUTPUT);
  pinMode(holdLed, OUTPUT);
  if (!EEPROM.begin(EEPROM_SIZE)) {
    delay(1000);
  }
  modeIndex = EEPROM.read(modeAddress);
  if (modeIndex == 0) newDeviceTask();
  else {
    getDataCredentail();
    registeredDeviceTask();
    lockTheGate();
    BLEDevice::init("");
    pBLEScan = BLEDevice::getScan(); //create new scan
    pBLEScan->setAdvertisedDeviceCallbacks(new MyAdvertisedDeviceCallbacks());
    pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
    pBLEScan->setInterval(199);
    pBLEScan->setWindow(180);
  }
}
void updateUnlockDelay() {
  Firebase.getInt(firebaseData, lock_delay_path);
  stateUnlockDelay = firebaseData.intData();
  Serial.print("UPDATE DELAY: ");
  Serial.println(stateUnlockDelay);
}

void updateOTP() {
  Firebase.getString(firebaseData, lock_otp_path);
  otp = firebaseData.stringData();
  Serial.print("UPDATE OTP: ");
  Serial.println(otp);
}
void updateTrustedDevices() {
  Firebase.get(firebaseData, trusted_devices_address_path);
  FirebaseJsonArray &arrAddress = firebaseData.jsonArray();
  for (size_t i = 0; i < arrAddress.size(); i++) {
    FirebaseJsonData &jsonData = firebaseData.jsonData();
    arrAddress.get(jsonData, i);
    trusted_devices_address[i] = jsonData.stringValue;

  }
  Firebase.get(firebaseData, trusted_devices_name_path);
  FirebaseJsonArray &arrName = firebaseData.jsonArray();
  for (size_t i = 0; i < arrName.size(); i++) {
    FirebaseJsonData &jsonData = firebaseData.jsonData();
    arrName.get(jsonData, i);
    trusted_devices_name[i] = jsonData.stringValue;
  }
  if (arrName.size() < 4) {
    for (size_t i = arrName.size(); i < 4; i++) {
      trusted_devices_name[i] = "";
      trusted_devices_address[i] = "";
    }
  }
  for (int i = 0; i < 4; i++) {
    Serial.print("UPDATE DEVICE NO.");
    Serial.print(i);
    Serial.print(": ");
    Serial.print(trusted_devices_name[i]);
    Serial.print("---");
    Serial.println(trusted_devices_address[i]);
  }
}

void getAllData() {
  Serial.println("DATA GET THE FIRST TIME");
  updateUnlockDelay();
  updateOTP();
  updateTrustedDevices();
}
void updateFeedback() {
  Firebase.setInt(firebaseData, update_code_path, -99);
}
void listenForUpdateData() {
  Firebase.getInt(firebaseData, update_code_path);
  switch (firebaseData.intData()) {
    case 0: _state = 0; stateCheck(); break;
    case 1: _state = 1; stateCheck(); break;
    case 2: _state = 2; stateCheck(); break;
    case 3: updateUnlockDelay(); break;
    case 4: updateOTP(); break;
    case 5: updateTrustedDevices(); break;
    default: Serial.println("DATA UP TO DATE"); break;
  }
  if (firebaseData.intData() != -99) updateFeedback();
  updateDataPrevMillis = millis();
}
void offGettingOTP() {
  Serial.print("ERROR COUNT: ");
  Serial.println(error_count);
  otpGetting = false;
  _otp = "";
}
void notifyToPhone(int alert_code) {
  Firebase.setInt(firebaseData, alert_code_path, alert_code);
}
void loop() {
  while (modeIndex == 0) {
    response->notify();
    if (process == 3) ESP.restart();
    delay(200);
  }
  currtouched = cap.touched();
  if (!(currtouched & _BV(0)) && (lasttouched & _BV(0)) ) {
    if (otp != "") {
      otpGetting = true;
      _otp = "";
      touchSound();
      otpTypingPrevMillis = millis();
    }
  }
  if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) ) {
    if (_otp != "")
      _otp =  _otp.substring(0, _otp.length() - 1 );
    else otpGetting = false;
    touchSound();
  }

  while (otpGetting) {
    if (error_count < 3) {
      while (millis() - otpTypingPrevMillis < otpTypingDelay) {
        while (_otp.length() == 6) {
          ConfirmOTP();
        }
        currtouched = cap.touched();
        if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) ) {
          if (_otp != "")
            _otp =  _otp.substring(0, _otp.length() - 1 );
          else otpGetting = false;
          touchSound();
        }
        for (uint8_t i = 1; i < 12; i++)
          if (i != 8)
            if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) )
              if (_otp.length() < 6) {
                _otp = _otp + keypad[i];
                Serial.print("otp: ");
                Serial.println(_otp);
                touchSound();
              }
        lasttouched = currtouched;
      }
      offGettingOTP();
      error_count++;
    } else {
      notifyToPhone(999);
      offGettingOTP();
    }
  }
  lasttouched = currtouched;
  // STOP RUNNING WHEN LOCK OPEN
  while (state) if (millis() - stateUnlockPrevMillis >= stateUnlockDelay) lockTheGate();
  // SCAN AND UPDATE DATA
  if (millis() - updateDataPrevMillis > updateDataDelay) listenForUpdateData();
  if (millis() - devicesScanPrevMillis > devicesScanDelay) {
    Serial.println("Scanning devices");
    pBLEScan->start(1, false);
    pBLEScan->clearResults();
    devicesScanPrevMillis = millis();
  }
}
