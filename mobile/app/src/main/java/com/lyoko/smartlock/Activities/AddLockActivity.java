package com.lyoko.smartlock.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.BluetoothLeService;
import com.lyoko.smartlock.Services.Find_Lock;
import com.lyoko.smartlock.Services.IFindLock;
import com.lyoko.smartlock.Utils.Permission;

public class AddLockActivity extends AppCompatActivity implements IFindLock {
    private static final int FIND_LOCK_COLOR = Color.parseColor("#3498db");
    private static final int REQUEST_DEVICE_ADDRESS = 99;
    String DEVICE_ADDRESS ;
    private Permission permission;
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
        permission = new Permission(this);

        permission.getPermission();
        if (permission.checkBLESupport()) {
            findLock.bluetoothEnable();
        }

        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AddLockActivity.this,BarcodeScannerActivity.class);
                startActivityForResult(intent,REQUEST_DEVICE_ADDRESS);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DEVICE_ADDRESS){
            if(resultCode == Activity.RESULT_OK){
                DEVICE_ADDRESS =data.getStringExtra("address");
                Log.d("QR_scan",DEVICE_ADDRESS);
                findLock.startScan(DEVICE_ADDRESS);

            }
        }
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
