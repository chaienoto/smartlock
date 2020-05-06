package com.lyoko.smartlock.Interface;

        import android.bluetooth.BluetoothDevice;

public interface iFindLock {
    void onDeviceFound(BluetoothDevice device, int rssi);
    void onConnected();
    void onComplete();

}
