package com.lyoko.smartlock;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.lyoko.smartlock.Models.BLE_Device;
import com.lyoko.smartlock.Services.Find_Lock;
import com.lyoko.smartlock.Utils.Request;

import java.util.ArrayList;
import java.util.HashMap;


public class FindLockActivity extends AppCompatActivity {
    private static String MAC_LOCK = "24:62:AB:D7:D9:A6";
    private static final int FIND_LOCK_COLOR = Color.parseColor("#3498db");
    private Request request = new Request(this);
    private Find_Lock bleScanService;
    private Button btn_find_device;
    private BLE_Device ble_device;
    private HashMap<String, BLE_Device> bleDeviceHashMap = new HashMap<>();
    private ArrayList<BLE_Device> bleDeviceList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_scanner);
        btn_find_device = findViewById(R.id.btn_find_device);
        getWindow().setStatusBarColor(FIND_LOCK_COLOR);
        bleScanService = new Find_Lock(FindLockActivity.this);


        request.startCheckPermission();
        if (request.BLESupport()) {
            bleScanService.bluetoothEnable();
        }


        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bleDeviceHashMap.clear();
                bleDeviceList.clear();
                Log.d("before scan: ", String.valueOf(bleDeviceList.size()));
                bleScanService.startScan();


            }
        });


    }

    public void checkafterscan() {
        Log.d("after scan: ", String.valueOf(bleDeviceList.size()));
        if( bleDeviceHashMap.containsKey(MAC_LOCK)){
            Toast.makeText(this, "Founded", Toast.LENGTH_SHORT).show();
        }

    }

    public void addDevice(BluetoothDevice device, int rssi) {
        String address = device.getAddress();
        if (!bleDeviceHashMap.containsKey(address)) {
            ble_device = new BLE_Device(device);
            ble_device.setRssi(rssi);
            Log.d("device: ", address + "\tRSSI: " + rssi);
            bleDeviceHashMap.put(address, ble_device);
            bleDeviceList.add(ble_device);
            Log.d("list scan: ", String.valueOf(bleDeviceList.size()));

        } else bleDeviceHashMap.get(address).setRssi(rssi);

    }

}
