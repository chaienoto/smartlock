package com.lyoko.smartlock.Models;

import android.bluetooth.BluetoothDevice;

public class BLE_Device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BLE_Device(BluetoothDevice bluetoothDevice) {
        this.bluetoothDevice = bluetoothDevice;
    }

    public String getAddess() {
        return bluetoothDevice.getAddress();
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
