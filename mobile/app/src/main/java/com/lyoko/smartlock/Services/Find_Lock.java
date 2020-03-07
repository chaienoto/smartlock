package com.lyoko.smartlock.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Handler;

import com.lyoko.smartlock.Activities.FindLockActivity;

public class Find_Lock {
    public static final int REQUEST_ENABLE_BT = 1;
    private FindLockActivity findLockActivity;
    private BluetoothAdapter bluetoothAdapter;
    public boolean mScanning;
    private Handler handler;
    private static final long SCAN_PERIOD = 1000;
    private static final int SIGNAL_STRENGTH = -60;


    public Find_Lock(FindLockActivity findLockActivity) {
        this.findLockActivity = findLockActivity;
        mScanning = false;
        handler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) findLockActivity.getSystemService(findLockActivity.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

    }


    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
            if (rssi >= SIGNAL_STRENGTH) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        findLockActivity.addDevice(device, rssi);
                    }
                });
            }
        }
    };


    private void scanLeDevice(final boolean enable) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    findLockActivity.checkafterscan();
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

    private void stopScan() {
        scanLeDevice(false);
    }

    public void bluetoothEnable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            findLockActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
