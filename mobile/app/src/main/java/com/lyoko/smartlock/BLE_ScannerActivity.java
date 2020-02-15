package com.lyoko.smartlock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

import static com.lyoko.smartlock.Utils.REQUEST_LOCATION_PERMISSION;
import static com.lyoko.smartlock.MainActivity.REQUEST_ENABLE_BT;


public class BLE_ScannerActivity extends AppCompatActivity {
    //private static final UUID uuid = UUID.nameUUIDFromBytes("4fafc201-1fb5-459e-8fcc-c5c9c331914b");
    private static final int FIND_LOCK_COLOR = Color.parseColor("#3498db");
    private static final long SCAN_PERIOD = 1000;
    private static final int SIGNAL_TRENGTH = -60;
    public Utils utils = new Utils(this);
    private Button btn_find_device;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    public boolean mScanning = false;


    private Handler handler;
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final int new_rssi = rssi;
                            if (rssi >= SIGNAL_TRENGTH){
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        show(device,new_rssi);
                                    }
                                });

                            }
                        }
                    });
                }
            };


    private void show(BluetoothDevice device, int rssi) {
        Log.d("Founded","Device: "+device.getName()+"\t"+"Address: "+device.getAddress()+ "\tRSSI: "+String.valueOf(rssi));

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scanner);
        btn_find_device = findViewById(R.id.btn_find_device);
        getWindow().setStatusBarColor(FIND_LOCK_COLOR);

        utils.startCheckPermission();



        // Check if BLE is supported on the device. enable bluetooth if !enable
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(BLE_ScannerActivity.this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // setUpBLE adapter_scanner
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(BLE_ScannerActivity.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mScanning = false;
        handler = new Handler();

        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (permissions.length == 1
                        && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    Toast.makeText(this, "Vui lòng bật Location Trong App Info ", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            Toast.makeText(BLE_ScannerActivity.this, "Starting...", Toast.LENGTH_SHORT).show();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(BLE_ScannerActivity.this, "Stopping...", Toast.LENGTH_SHORT).show();
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                }
            }, SCAN_PERIOD);
            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
//
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);


        }
    }

}
