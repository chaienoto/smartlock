package com.lyoko.smartlock.Models;

import android.bluetooth.BluetoothDevice;

public class BLE_device {
    private BluetoothDevice bluetoothDevice;
    private int rssi;

    public BLE_device(BluetoothDevice bluetoothDevice, int rssi) {
        this.bluetoothDevice = bluetoothDevice;
        this.rssi = rssi;
    }
}
