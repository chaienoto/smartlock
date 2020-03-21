package com.lyoko.smartlock.Services;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.lyoko.smartlock.Activities.AddLockActivity;



public class Find_Lock {
    private static String MAC_LOCK = "24:62:AB:D7:D9:A6";
    public static final int REQUEST_ENABLE_BT = 1;
    private AddLockActivity addLockActivity;
    private BluetoothAdapter bluetoothAdapter;
    private IFindLock iFindLock;
    private Boolean isFound;
    private static final long SCAN_PERIOD = 1000;
    private static final int SIGNAL_STRENGTH = -60;
    public boolean mScanning;
    private Handler handler;


    public Find_Lock(AddLockActivity addLockActivity, IFindLock iFindLock) {
        this.addLockActivity = addLockActivity;
        this.iFindLock = iFindLock;
        mScanning = false;
        isFound = false;
        handler = new Handler();
        final BluetoothManager bluetoothManager =
                (BluetoothManager) addLockActivity.getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();

    }


    private final BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
//            if (rssi >= SIGNAL_STRENGTH) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (device.getAddress().equalsIgnoreCase(MAC_LOCK) && !isFound){
                            isFound = true;
                            iFindLock.onfound(device, rssi);
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

    public void bluetoothEnable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            addLockActivity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }
}
