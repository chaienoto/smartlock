package com.lyoko.smartlock.Activities;

import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.BluetoothLeService;
import com.lyoko.smartlock.Services.Find_Lock;
import com.lyoko.smartlock.Services.IFindLock;
import com.lyoko.smartlock.Utils.Request;

public class AddLockActivity extends AppCompatActivity implements IFindLock {
    private static final int FIND_LOCK_COLOR = Color.parseColor("#3498db");
    private Request request;
    ImageView img_find_device;
    private Find_Lock findLock;
    private Button btn_find_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        btn_find_device = findViewById(R.id.btn_find_device);
        img_find_device = findViewById(R.id.img_find_device);
        getWindow().setStatusBarColor(FIND_LOCK_COLOR);

        findLock = new Find_Lock(this,this);
        request = new Request(this);

        request.checkPermission();
        if (request.checkBLESupport()) {
            findLock.bluetoothEnable();
        }

        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findLock.startScan();
            }
        });
    }

    @Override
    public void onfound(final BluetoothDevice device, int rssi) {
        final BluetoothLeService bluetoothLeService = new BluetoothLeService(this,false,device);
        btn_find_device.setText("CONNECT");
        bluetoothLeService.connectDevice();

        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }
}
