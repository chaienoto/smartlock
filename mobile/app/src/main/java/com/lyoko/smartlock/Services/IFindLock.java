package com.lyoko.smartlock.Services;

import android.bluetooth.BluetoothDevice;

public interface IFindLock {
    void onfound(BluetoothDevice device, int rssi);
}
