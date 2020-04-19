package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.PhoneAdvertise_Service;

public class AutoUnlockActivity extends AppCompatActivity {
    Toolbar auto_unlock_toolbar;
    RecyclerView auto_unlock_device_recycle_view;
    Button btn_used_my_phone, btn_used_another_device;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_unlock);
        auto_unlock_toolbar = findViewById(R.id.auto_unlock_toolbar);
        auto_unlock_device_recycle_view = findViewById(R.id.auto_unlock_device_recycle_view);
        btn_used_my_phone = findViewById(R.id.btn_used_my_phone);
        btn_used_another_device = findViewById(R.id.btn_used_another_device);
        auto_unlock_toolbar.setTitle("Quản lý thiết bị Auto Unlock");
        setSupportActionBar(auto_unlock_toolbar);

        btn_used_my_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if( !BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported() ) {
                    Toast.makeText( AutoUnlockActivity.this, "Điện Thoại của bạn ko hỗ trợ tính năng này", Toast.LENGTH_SHORT ).show();
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    new PhoneAdvertise_Service().starAdvertise();
                    Toast.makeText( AutoUnlockActivity.this, "Kích hoạt thành công", Toast.LENGTH_SHORT ).show();
                } else Toast.makeText( AutoUnlockActivity.this, "Điện Thoại của bạn ko hỗ trợ tính năng này", Toast.LENGTH_SHORT ).show();
            }
        });
    }
}
