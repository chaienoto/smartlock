package com.lyoko.smartlock.Services;

        import android.bluetooth.BluetoothDevice;

public interface IFindLock {
    void onDeviceFound(BluetoothDevice device, int rssi);
    void onNotOwner(String address);
    void onAsOwner(String address);
    void onConnected();
    void onComplete();
    void onReadyToAddDevice(String address);

}
