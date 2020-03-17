/*
    Video: https://www.youtube.com/watch?v=oCMOYS71NIU
    Based on Neil Kolban example for IDF: https://github.com/nkolban/esp32-snippets/blob/master/cpp_utils/tests/BLE%20Tests/SampleNotify.cpp
    Ported to Arduino ESP32 by Evandro Copercini
    updated by chegewara

   Create a BLE server that, once we receive a connection, will send periodic notifications.
   The service advertises itself as: 4fafc201-1fb5-459e-8fcc-c5c9c331914b
   And has a characteristic of: beb5483e-36e1-4688-b7f5-ea07361b26a8

   The design of creating the BLE server is:
   1. Create a BLE Server
   2. Create a BLE Service
   3. Create a BLE Characteristic on the Service
   4. Create a BLE Descriptor on the characteristic
   5. Start the service.
   6. Start advertising.
   7. test git

   A connect hander associated with the server starts a background task that performs notification
   every couple of seconds.
*/
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>

BLEServer* pServer = NULL;
bool deviceConnected = false;
bool oldDeviceConnected = false;
uint32_t value = 0;
uint8_t level = 57;

// See the following for generating UUIDs:
// https://www.uuidgenerator.net/

#define Battery_Service BLEUUID((uint16_t)0x180F)
BLECharacteristic pBattery_Level (BLEUUID((uint16_t)0x2A19),
                                  BLECharacteristic::PROPERTY_READ |
                                  BLECharacteristic::PROPERTY_NOTIFY);



class MyServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
      deviceConnected = true;
      BLEDevice::startAdvertising();
    };

    void onDisconnect(BLEServer* pServer) {
      deviceConnected = false;
    }
};



void setup() {
  Serial.begin(115200);

  // Create the BLE Device
  BLEDevice::init("Lyoko Smart Lock");

  // Create the BLE Server
  pServer = BLEDevice::createServer();
  pServer->setCallbacks(new MyServerCallbacks());

  // Create the BLE Service
  BLEService *pBattery_Service = pServer->createService(Battery_Service);

  // Create a BLE Characteristic
  pBattery_Service->addCharacteristic(&pBattery_Level);

  // https://www.bluetooth.com/specifications/gatt/viewer?attributeXmlFile=org.bluetooth.descriptor.gatt.client_characteristic_configuration.xml
  // Create a BLE Descriptor
  pBattery_Level.addDescriptor(new BLE2902());

  // Start the service
  pBattery_Service->start();

  // Start advertising
  BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
  pAdvertising->addServiceUUID(Battery_Service);
  pAdvertising->setScanResponse(false);
  pAdvertising->setMinPreferred(0x0);  // set value to 0x00 to not advertise this parameter
  BLEDevice::startAdvertising();
  Serial.println("Waiting a client connection to notify...");
}

void loop() {
//  // notify changed value 
//  if (deviceConnected) {
//    pCharacteristic->setValue((uint8_t*)&value, 4);
//    pCharacteristic->notify();
//    value++;
//    delay(10); // bluetooth stack will go into congestion, if too many packets are sent, in 6 hours test i was able to go as low as 3ms
//  }
//  // disconnecting
//  if (!deviceConnected && oldDeviceConnected) {
//    delay(500); // give the bluetooth stack the chance to get things ready
//    pServer->startAdvertising(); // restart advertising
//    Serial.println("start advertising");
//    oldDeviceConnected = deviceConnected;
//  }
//  // connecting
//  if (deviceConnected && !oldDeviceConnected) {
//    // do stuff here on connecting
//    oldDeviceConnected = deviceConnected;
//  }

  pBattery_Level.setValue(&level, 1);
  pBattery_Level.notify();
  delay(5000);

  level++;
  Serial.println(int(level));

  if (int(level) == 100)
    level = 0;



}
