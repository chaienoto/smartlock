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
#include <Adafruit_NeoPixel.h>
#include "Adafruit_MPR121.h"
#define EEPROM_SIZE 256
#define FIREBASE_HOST "lyoko-smartlock.firebaseio.com"
#define FIREBASE_AUTH "85viJDSNb1ClLOu5yULeAorEXhaEXfebxJroE2dy"
#define FIREBASE_FCM_SERVER_KEY "AAAAVh-iOpk:APA91bFCpQgGhwc-YQsoUAqR57zRMa29wB11XJ2M5XnfG2LVqI9Fx4dLNBgV0lBaJWWFUibESwl9wbN2-Q3Uxzw7HJaSwdUl6HCCIXFTAThcnhYAEit731WJGxJD1mSbyWtZQGAskkJr"

#define LED_GREEN_PIN 2
#define LED_RED_PIN 4
#define SPEAKER_PIN 5
#define LED_BLUE_PIN 15
#define RELAY_PIN 23

#define SERVICE_LYOKO_UUID                      "7060da7f-1ce6-43d1-b58c-2c595f8f9a56"
#define CHARACTERISTIC_WIFI_CREDENTIAL_UUID     "b82150b1-48e9-4a1b-a18c-3f2c140a8104"
#define CHARACTERISTIC_RESPONSE_UUID            "459d013b-061c-430b-a4fe-734cc22012cb"
#define CHARACTERISTIC_OWNER_PHONE_NUMBER_UUID  "e7608c36-76e7-4e9c-9263-1786f7a2f854"
#define CHARACTERISTIC_CHIP_ID_UUID             "95374a93-101c-4f2c-a590-2e8fcb3dfd37"

#ifndef _BV
#define _BV(bit) (1 << (bit))
#endif
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
String fcm_path;
String otp_limit_entry_path;


// TIME DELAYTION
int stateUnlockDelay;

int otpTypingDelay = 10000;
int availableToUnlockTime = 10000;
int devicesScanDelay = 10000;
int updateDataDelay = 1000;
unsigned long devicesScanPrevMillis;
unsigned long otpTypingPrevMillis;
unsigned long updateDataPrevMillis;
unsigned long stateUnlockPrevMillis;
unsigned long ledMillis;

// device info
int modeIndex;
int otp_limit_entry;
int state, _state;
String otp;
String owner_fcm;
String trusted_devices_name[4];
String trusted_devices_address[4];
boolean otpTyping = false;
boolean readyToUnlock = false;
boolean otpWasConfirm = false;
int error_count = 0;
int update_code = -99;
int deviceFound = -1;
int available_devices_count = 0;

// SPEAKER_PIN setting
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
    //    digitalWrite(LED_PIN, _s);
    digitalWrite(RELAY_PIN, _s);
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
  state = 2;
  stateChange(state);
}

void historySave(String unlock_name, String unlock_type) {
  Serial.println("Saving History.....");
  char timeStringBuff[50];
  struct tm timeinfo;
  if (!getLocalTime(&timeinfo)) return;
  strftime(timeStringBuff, sizeof(timeStringBuff), "%A, %B %d %Y %H:%M:%S", &timeinfo);
  json.add("unlock_name", unlock_name); json.add("unlock_type", unlock_type); json.add("unlock_time", String(timeStringBuff));
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

void ConfirmOTP(String _otp) {
  if (otp.equals(_otp))
  {
    openTheGate(); otp = ""; error_count = 0; otpWasConfirm = true; 
    historySave("Người có mã otp", "otp");
    Firebase.setString(firebaseData, lock_otp_path, otp);
  } else  error_count++;
  otpTyping = false;
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
  digitalWrite(LED_GREEN_PIN, true);
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
  lock_otp_path = parentPath + "otp";
  lock_state_path = parentPath + "state";
  lock_delay_path = parentPath + "delay";
  histories_path = parentPath + "histories";
  trusted_devices_address_path = parentPath + "trusted_devices_address";
  trusted_devices_name_path = parentPath + "trusted_devices_name";
  update_code_path = parentPath + "update_code";
  alert_code_path = parentPath + "alert_code";
  otp_limit_entry_path = parentPath + "otp_limit_entry";
  fcm_path = "/lyoko_users/" + ownerPNumber + "/fcm";
}

void wifiBegin() {
  WiFi.begin(wifi_ssid.c_str(), wifi_password.c_str());
  while (WiFi.status() != WL_CONNECTED ) {}
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
  firebaseData.fcm.begin(FIREBASE_FCM_SERVER_KEY);
  firebaseData.fcm.setPriority("normal");
  firebaseData.fcm.setTimeToLive(1000);
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  getAllData();

}

class ScanDevicesCallbacks: public BLEAdvertisedDeviceCallbacks {
    void onResult(BLEAdvertisedDevice advertisedDevice) {
      pScanAddress = new BLEAddress(advertisedDevice.getAddress());
      for (int i = 0; i < available_devices_count ; i++)
        if (strcmp(pScanAddress->toString().c_str(), trusted_devices_address[i].c_str()) == 0 && advertisedDevice.getRSSI() >= -60) {
          openTheGate();
          historySave(trusted_devices_name[i], "trusted_devices");
          pBLEScan->stop();
          notifyInfo(3);
        }
    }
};

void updateUnlockDelay() {
  Firebase.getInt(firebaseData, lock_delay_path);
  stateUnlockDelay = firebaseData.intData();
  Serial.print("UPDATE DELAY: ");
  Serial.println(stateUnlockDelay);
  feedbackUpdate();
}

void updateOTPLimitEntry() {
  Firebase.getInt(firebaseData, otp_limit_entry_path);
  otp_limit_entry = firebaseData.intData();
  Serial.print("OTP LIMIT ENTRY: ");
  Serial.println(otp_limit_entry);
  feedbackUpdate();
}
void updateOwnerFcm() {
  Firebase.getString(firebaseData, fcm_path);
  owner_fcm = firebaseData.stringData();
  Serial.print("UPDATE FCM: ");
  Serial.println(owner_fcm);
  feedbackUpdate();
  firebaseData.fcm.clearDeviceToken();
  firebaseData.fcm.addDeviceToken(owner_fcm);
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
  updateOwnerFcm();
  updateOTPLimitEntry();
}
void listenForUpdateData() {
  if (Firebase.getInt(firebaseData, update_code_path)) {
    switch (firebaseData.intData()) {
      case 0: _state = 0; stateCheck(); break;
      case 1: _state = 1; stateCheck(); break;
      case 2: _state = 2; stateCheck(); break;
      case 3: updateUnlockDelay(); break;
      case 4: updateOTP(); break;
      case 5: updateTrustedDevices(); break;
      case 6: updateOTPLimitEntry(); break;
      case 7: updateOwnerFcm(); break;
      default: Serial.print("DATA UP TO DATE TIME: ");
        Serial.println(millis() / 1000);
    }
    updateDataPrevMillis = millis();
  } else ESP.restart();
}
void otpState() {
  Serial.print("ERROR COUNT: ");
  Serial.println(error_count);
  Serial.print("otpTyping: ");
  Serial.println(otpTyping);
}

void getTypingOtp() {
  Serial.println("Bắt đầu lấy OTP.....");
  boolean led = false;
  String _otp;
  otpTypingPrevMillis = millis();
  sound(successSound);
  while (otpTyping) 
  {
    if (millis() - ledMillis > 400 )    //blink otp getting led code
    {
      led = !led;
      digitalWrite(LED_BLUE_PIN, led);
      ledMillis = millis();
    }
    while (_otp.length() == 6) ConfirmOTP(_otp);    // Confirm otp
    currtouched = cap.touched();
    if (millis() - otpTypingPrevMillis < otpTypingDelay)
    {
      for (uint8_t i = 1; i < 12; i++)
      {
        if ((currtouched & _BV(i)) && !(lasttouched & _BV(i)) )
        {
          if (i != 8)
          {
            if (_otp.length() < 6)
            {
              _otp = _otp + keypad[i];
              Serial.print("otp: ");
              Serial.println(_otp);
            }
          }
          else
          {
            if (_otp != "")
            {
              _otp =  _otp.substring(0, _otp.length() - 1 );
            }
            else
            {
              otpTyping = !otpTyping;
              otpWasConfirm = true;
            }
          }
          sound(touchSound);
        }
      }
    }
    lasttouched = currtouched;
  }
  if (!otpWasConfirm) error_count++;
  if (error_count == otp_limit_entry - 1 ) sendMessage();
  otpState(); otpWasConfirm = false;
  Serial.println("Kết thúc lấy OTP");
}
void sendMessage() {
  firebaseData.fcm.setNotifyMessage("Cảnh báo", "Ai đó đang cố nhập mã mở khóa, chúng tôi đã khóa bàn phím giúp bạn! ");
  if (Firebase.sendMessage(firebaseData, 0))//send message to recipient index 0
  {

    Serial.println("PASSED");
    Serial.println(firebaseData.fcm.getSendResult());
    Serial.println("------------------------------------");
    Serial.println();
  }
  else
  {
    Serial.println("FAILED");
    Serial.println("REASON: " + firebaseData.errorReason());
    Serial.println("------------------------------------");
    Serial.println();
  }
}

void notifyInfo(int j) {
  for (int i = 0; i <= j; i++) {
    digitalWrite(LED_RED_PIN, 200);
    ledcWrite(channel, 255);
    delay(400);
    digitalWrite(LED_RED_PIN, 0);
    ledcWrite(channel, 0);
    delay(100);
  }
}

void scanTrustedDevices() {
  Serial.println("SCANNING DEVICES");
  deviceFound = -1;
  pBLEScan->start(1);
  devicesScanPrevMillis = millis();
}

void setup() {
  Serial.begin(115200);
  pinMode(RELAY_PIN, OUTPUT);
  pinMode(LED_RED_PIN, OUTPUT);
  pinMode(LED_GREEN_PIN, OUTPUT);
  pinMode(LED_BLUE_PIN, OUTPUT);
  if (!cap.begin(0x5A)) {
    Serial.println("NOT");
    digitalWrite(LED_RED_PIN, 1);
    delay(400);
    digitalWrite(LED_RED_PIN, 0);
  }
  else {
    Serial.println("FOUND");
    digitalWrite(LED_BLUE_PIN, 1);
    delay(400);
    digitalWrite(LED_BLUE_PIN, 0);
  }
  ledcSetup(channel, freq, resolution);
  ledcAttachPin(SPEAKER_PIN, channel);
  if (!EEPROM.begin(EEPROM_SIZE)) {
    delay(1000);
  }
  modeIndex = EEPROM.read(modeAddress);
  if (modeIndex == 0) newDeviceTask();
  else {
    getDataCredentail();
    wifiBegin();
    digitalWrite(RELAY_PIN, 0);
    Firebase.setInt(firebaseData, lock_state_path, 0);
    BLEDevice::init("");
    pBLEScan = BLEDevice::getScan(); //create new scan
    pBLEScan->setAdvertisedDeviceCallbacks(new ScanDevicesCallbacks());
    pBLEScan->setActiveScan(true); //active scan uses more power, but get results faster
    pBLEScan->setInterval(151);
    pBLEScan->setWindow(150);
  }
}

void loop() {
  while (modeIndex == 0) {
    response->notify();
    if (process == 3) ESP.restart();
    delay(200);
  }
  if (millis() / 1000 > 1000) ESP.restart();
  currtouched = cap.touched();
  // WHILE LOCK ON
  while (state == 1) {
    Serial.println("Đang Mở.....");
    if (millis() - stateUnlockPrevMillis >= stateUnlockDelay) lockTheGate();
    Firebase.getInt(firebaseData, update_code_path);
    if (firebaseData.intData() == 2) {
      Serial.println("Hold này.....");
      stateUnlockPrevMillis = millis();
      holdOn();
      Firebase.setInt(firebaseData, update_code_path, -99);
    }
    delay (500);
  }

  // IF * touched
  if (!(currtouched & _BV(0)) && (lasttouched & _BV(0)) ) {
    if ( otp != "" && error_count < otp_limit_entry ) {
      otpTyping = true;
      getTypingOtp();
    } else {
      Serial.println("KHÔNG CÓ MÃ OTP");
      notifyInfo(1);
    }
  }
  lasttouched = currtouched;
  if (millis() - updateDataPrevMillis > updateDataDelay) listenForUpdateData();
  if (millis() - devicesScanPrevMillis > devicesScanDelay) scanTrustedDevices();
}
