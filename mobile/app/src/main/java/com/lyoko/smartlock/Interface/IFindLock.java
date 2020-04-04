package com.lyoko.smartlock.Interface;

        import android.bluetooth.BluetoothDevice;

public interface IFindLock {
    void onDeviceFound(BluetoothDevice device, int rssi);
    void onConnected();
    void onComplete();

}
