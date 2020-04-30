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
static BLEAddress *pScanAddress;

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
int otpTypingDelay = 5000;
int availableToUnlockTime = 10000;
int devicesScanDelay = 10000;
int updateDataDelay = 500;
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
boolean otpTyping = false;
boolean readyToUnlock = false;
boolean dataSendinng = false;
int error_count = 0;
int update_code = -99;
int deviceFound = -1;
int available_devices_count = 0;

// speaker setting
int freq = 16000;
int channel = 0;
int resolution = 8;
int successSound = 3;
int touchSound = 1;
int otp_null = 2;
FirebaseData firebaseData;
FirebaseJson json;

void stateCheck() {
  if (state == _state) return;
  state = _state;
  switch (state) {
    case 1: openTheGate(); break;
    case 2: holdOn(); break;
    default: lockTheGate(); break;
  }
  feedbackUpdate();
}
void stateChange(int _s) {
  if (_s != 2) {
    digitalWrite(ledPin, _s);
    digitalWrite(relayPin, _s);
  }
  Firebase.setInt(firebaseData, lock_state_path, _s);
}
void lockTheGate() {
  Serial.println("Đóng chặt lắm rồi này");
  state = 0;
  lasttouched = 0;
  stateChange(state);
}

void openTheGate() {
  Serial.println("Mở rồi này");
  state = 1;
  stateUnlockPrevMillis = millis();
  sound(successSound) ;
  stateChange(state);
}

void holdOn() {
  stateChange(2);
}

void historySave(String unlock_name, String unlock_type) {
  char timeStringBuff[50];
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) return;
  strftime(timeStringBuff, sizeof(timeStringBuff), "%A, %B %d %Y %H:%M:%S", &timeinfo);
  json.add("unlock_name", unlock_name); json.add("unlock_type", unlock_type); json.add("unlock_time", String(timeStringBuff));

  Serial.println("Saving History.....");
  Firebase.pushJSON(firebaseData, histories_path, json);
  json.clear();
}

void sound(int soundCode) {
  for (int i = 0; i < soundCode; i++ ) {
    ledcWrite(channel, 255);
    delay(25);
    ledcWrite(channel, 0);
    delay(25);
  }
}

void ConfirmOTP() {
  if (otp.equals(_otp)) {
    openTheGate(); otp = ""; error_count = 0;
    historySave("Người có mã otp", "otp");
    Firebase.setString(firebaseData, lock_otp_path, "");
  } else
    error_count++;
  _otp = "";
}

//-------------------------------------------------------------------------------------
class wifiCredentailCallBack: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *wifiCredential) {
      std::string rxValue = wifiCredential->getValue();
      String wifiCredentail;
      if (rxValue.length() > 0) {
        for (int i = 0; i <= rxValue.length() - 1; i++)  wifiCredentail = wifiCredentail + rxValue[i];
        EEPROM.writeString(wifiAddress, wifiCredentail); EEPROM.commit();
        process = 1;
        response->setValue(&process, 1);
      }
    }
};

class ownerPhoneNumberCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *ownerPhoneNumber) {
      std::string value = ownerPhoneNumber->getValue();
      String ownerData;
      if (value.length() > 0) {
        for (int i = 0; i <= value.length() - 1; i++) ownerData = ownerData + value[i];
        EEPROM.writeString(onwerPhoneNumberAddress, ownerData); EEPROM.commit();
        process = 2; response->setValue(&process, 1);
      }
    }
};

class deviceChipIDCallback: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *deviceChipID) {
      std::string value = deviceChipID->getValue();
      String chipIDData;
      if (value.length() > 0) {
        for (int i = 0; i <= value.length() - 1; i++) chipIDData = chipIDData + value[i];
        EEPROM.writeString(chipIdAddress, chipIDData);
        EEPROM.write(modeAddress, 1); EEPROM.commit();
        process = 3; response->setValue(&process, 1);

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
  while (WiFi.status() != WL_CONNECTED ) {}
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  getAllData();
}

class ScanDevicesCallbacks: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      pScanAddress = new BLEAddress(advertisedDevice.getAddress());
      for (int i = 0; i < available_devices_count ; i++)
        if (strcmp(pScanAddress->toString().c_str(), trusted_devices_address[i].c_str()) == 0)
          if (advertisedDevice.getRSSI() > -60) {
            deviceFound = i;
            pBLEScan->stop();
          }
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
    pBLEScan->setAdvertisedDeviceCallbacks(new ScanDevicesCallbacks());
    pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
    pBLEScan->setInterval(151);
    pBLEScan->setWindow(150);
  }
}
void updateUnlockDelay() {
  Firebase.getInt(firebaseData, lock_delay_path);
  stateUnlockDelay = firebaseData.intData();
  Serial.print("UPDATE DELAY: ");
  Serial.println(stateUnlockDelay);
  feedbackUpdate();
}

void updateOTP() {
  Firebase.getString(firebaseData, lock_otp_path);
  otp = firebaseData.stringData();
  Serial.print("UPDATE OTP: ");
  Serial.println(otp);
  feedbackUpdate();
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
    Serial.print("UPDATE DEVICE NO.");
    Serial.print(i);
    Serial.print(": ");
    Serial.print(trusted_devices_name[i]);
    Serial.print("---");
    Serial.println(trusted_devices_address[i]);
  }
  available_devices_count = arrName.size();
  feedbackUpdate();
}
void feedbackUpdate() {
  Firebase.setInt(firebaseData, update_code_path, -99);
}
void getAllData() {
  Serial.println("DATA GET THE FIRST TIME");
  otpTyping = false;
  updateUnlockDelay();
  updateOTP();
  updateTrustedDevices();
}
void listenForUpdateData() {
  int checkNullUpdate = 0;
  if (Firebase.getInt(firebaseData, update_code_path)) {
    switch (firebaseData.intData()) {
      case 0: _state = 0; stateCheck(); break;
      case 1: _state = 1; stateCheck(); break;
      case 2: _state = 2; stateCheck(); break;
      case 3: updateUnlockDelay(); break;
      case 4: updateOTP(); break;
      case 5: updateTrustedDevices(); break;
      default: checkNullUpdate = 1;
    }
    if (checkNullUpdate) {
      Serial.print("DATA UP TO DATE TIME: ");
      Serial.println(millis() / 1000);
    }
    updateDataPrevMillis = millis();
  } else ESP.restart();
}
void offGettingOTP() {
  Serial.print("ERROR COUNT: ");
  Serial.println(error_count);
  Serial.print("otpTyping: ");
  Serial.println(otpTyping);
}
void notifyToPhone() {
  offGettingOTP();
  Serial.println("không cho nhập OTP...");
}
void getTypingOtp() {
  otpTypingPrevMillis = millis(); sound(successSound);
  while (millis() - otpTypingPrevMillis < otpTypingDelay) {
    Serial.println(_otp);
    currtouched = cap.touched();
    while (_otp.length() == 6) ConfirmOTP();
    if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) ) {
      sound(touchSound); if (_otp != "") _otp =  _otp.substring(0, _otp.length() - 1 );
    }
    for (uint8_t i = 1; i < 12; i++)
      if (i != 8)
        if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) )
          if (_otp.length() < 6) {
            _otp = _otp + keypad[i];
            Serial.print("otp: ");

            sound(touchSound);
          }
    lasttouched = currtouched;
  }
  if (_otp.length() < 6) error_count++;
  offGettingOTP(); _otp = "";
}

void scanTrustedDevices() {
  Serial.println("SCANNING DEVICES");
  deviceFound = -1;
  pBLEScan->start(1);
  devicesScanPrevMillis = millis();
}
void loop() {
  while (modeIndex == 0) {
    response->notify();
    if (process == 3) ESP.restart();
    delay(200);
  }
  if (millis() / 1000 > 1000) ESP.restart();

  while (state == 1) {
    Serial.println("Đang Mở.....");
    if (millis() - stateUnlockPrevMillis >= stateUnlockDelay) lockTheGate();
    Firebase.getInt(firebaseData, update_code_path);
    if (firebaseData.intData() == 2) {
      Serial.println("Hold này.....");
      stateUnlockPrevMillis = millis();
      holdOn();
      Firebase.setInt(firebaseData, update_code_path, -99);
    } else  Serial.println(firebaseData.intData());
    delay (200);
  }
  currtouched = cap.touched();
  if (!(currtouched & _BV(0)) && (lasttouched & _BV(0)) )
      if (otp != "" && error_count < 3 )  getTypingOtp(); else  sound(otp_null);
  if (!(currtouched & _BV(8)) && (lasttouched & _BV(8)) )
    if (deviceFound != -1) {
      sound(touchSound); openTheGate(); historySave(trusted_devices_name[deviceFound], "trusted_devices");
    }
  lasttouched = currtouched;

  if (millis() - updateDataPrevMillis > updateDataDelay) listenForUpdateData();
  if (millis() - devicesScanPrevMillis > devicesScanDelay) scanTrustedDevices();

}
