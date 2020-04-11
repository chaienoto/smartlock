package com.lyoko.smartlock.Models;

import android.bluetooth.BluetoothDevice;

public class BLE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;
    private Boolean found;
    String name,address;

    public BLE_Device(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }
    public BLE_Device(String name,String address ){
        this.address = address;
        this.name = name;
    }

    public String getAddress() {
        return bluetoothDevice.getAddress();
    }

    public String getName() {
        return bluetoothDevice.getName();
    }

    public void setRSSI(int rssi) {
        this.rssi = rssi;
    }

    public int getRSSI() {
        return rssi;
    }

    public Boolean getFound() {
        return found;
    }

    public void setFound(Boolean found) {
        this.found = found;
    }


}
