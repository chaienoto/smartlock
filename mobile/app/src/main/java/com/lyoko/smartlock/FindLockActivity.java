package com.lyoko.smartlock;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
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

import static com.lyoko.smartlock.MainActivity.REQUEST_ENABLE_BT;
import static com.lyoko.smartlock.Utils.REQUEST_LOCATION_PERMISSION;


public class FindLockActivity extends AppCompatActivity {
    private static String MAC_LOCK = "24:62:AB:D7:D9:A6";
    private static final int FIND_LOCK_COLOR = Color.parseColor("#3498db");
    private static final long SCAN_PERIOD = 1000;
    private static final int SIGNAL_TRENGTH = -60;
    public Utils utils = new Utils(this);
    private Button btn_find_device;
    private BluetoothAdapter bluetoothAdapter;
    public boolean mScanning = false;
    public boolean mfounded = false;


    private Handler handler;
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (rssi >= SIGNAL_TRENGTH) {
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        show(device, rssi);
                                    }
                                });

                            }
                        }
                    });
                }
            };


    private void show(final BluetoothDevice device, final int rssi) {

        String new_mac = device.getAddress();
        if (new_mac.equalsIgnoreCase(MAC_LOCK)) {
            Toast.makeText(this, "Tìm Thấy Thành công", Toast.LENGTH_SHORT).show();
            Log.d("Founded", "Device: " + device.getName() + "\t" + "Address: " + new_mac + "\tRSSI: " + String.valueOf(rssi) + "\tLength: " + new_mac.length());

            scanLeDevice(false);
        }
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
            Toast.makeText(FindLockActivity.this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        } else if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // setUpBLE adapter_scanner
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(FindLockActivity.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
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
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (permissions.length == 1
                        && permissions[0] == Manifest.permission.ACCESS_FINE_LOCATION
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                }
                return;
            }
        }
    }

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

}
