package com.lyoko.smartlock.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;


public class Find_Device {
    private String device_address ;
    private BluetoothAdapter bluetoothAdapter;
    private com.lyoko.smartlock.Interface.iFindLock iFindLock;
    private Context context;
    private Boolean isFound;
    private static final long SCAN_PERIOD = 1000;
    private static final int SIGNAL_STRENGTH = -60;
    private boolean mScanning;
    private Handler handler;

    public Find_Device(Context context, String device_address, BluetoothAdapter adapter, com.lyoko.smartlock.Interface.iFindLock iFindLock) {
        this.context = context;
        this.iFindLock = iFindLock;
        this.device_address = device_address;
        this.mScanning = false;
        isFound = false;
        handler = new Handler();
        this.bluetoothAdapter = adapter;

    }

    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (device.getAddress().equalsIgnoreCase(device_address) && !isFound){
                            isFound = true;
                            iFindLock.onDeviceFound(device, rssi);
                        }
                    }
                });
            }
//        }
    };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);

        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    public void startScan() {
        scanLeDevice(true);
    }

    public void stopScan() {
        scanLeDevice(false);
    }



}
