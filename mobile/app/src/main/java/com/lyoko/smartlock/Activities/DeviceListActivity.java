package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lyoko.smartlock.Adapters.DevicesAdapter;
import com.lyoko.smartlock.Models.BLE_Device;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Services.IDeviceList;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;

public class DeviceListActivity extends AppCompatActivity implements IDeviceList,DevicesAdapter.OnDeviceClickedListener {
    Database_Service db_service = new Database_Service();
    RecyclerView device_list_recycle_view;
    TextView tv6;
    Toolbar devices_toolbar;
    RelativeLayout device_list_layout;
    ImageView img_find_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);

        device_list_layout = findViewById(R.id.device_list_layout);
        img_find_device = findViewById(R.id.img_find_device);
        devices_toolbar = findViewById(R.id.devices_toolbar);
        devices_toolbar.setTitle("Thiết bị đã đăng kí");
        setSupportActionBar(devices_toolbar);
        getWindow().setStatusBarColor(COLOR_BLUE);

        device_list_recycle_view = findViewById(R.id.device_list_recycle_view);
        tv6 = findViewById(R.id.tv6);
        db_service.getRegisteredDevices(this);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_devices, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_device:
                Intent intent = new Intent(DeviceListActivity.this, AddDeviceActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showDevices(ArrayList<Device_info> list) {
        if (list.size() % 2 != 0){
            device_list_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else device_list_layout.setBackgroundColor(COLOR_EVEN_POSITION);

        DevicesAdapter adapter = new DevicesAdapter(this, list);
        device_list_recycle_view.setAdapter(adapter);
        adapter.setOnDeviceClickedListener(this);
        device_list_recycle_view.setLayoutManager(new LinearLayoutManager(this));
        img_find_device.setVisibility(View.INVISIBLE);
        device_list_recycle_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void notThingToShow() {
        Toast.makeText(this, "Bạn chưa đăng kí thiết bị nào", Toast.LENGTH_SHORT).show();
        img_find_device.setVisibility(View.VISIBLE);
        tv6.setVisibility(View.VISIBLE);
        img_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceListActivity.this, AddDeviceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    public void onItemClick(String address, String name) {
        Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
        intent.putExtra("address", address);
        intent.putExtra("name", name);
        startActivity(intent);
    }
}
