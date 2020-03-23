package com.lyoko.smartlock.Services;

import android.bluetooth.BluetoothDevice;

public interface IFindLock {
    void onFound(BluetoothDevice device, int rssi);
}
