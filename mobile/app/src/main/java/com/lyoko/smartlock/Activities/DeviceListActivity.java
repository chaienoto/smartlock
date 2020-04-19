package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

import com.lyoko.smartlock.Adapters.OwnDevicesAdapter;
import com.lyoko.smartlock.Adapters.RemoteDevicesAdapter;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Helper;
import com.lyoko.smartlock.Interface.IDeviceList;
import com.lyoko.smartlock.Utils.QRGenerate;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_PHONE_NUMBER;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class DeviceListActivity extends AppCompatActivity implements IDeviceList, OwnDevicesAdapter.OnOwnDeviceClickedListener, RemoteDevicesAdapter.OnRemoteDeviceClickedListener  {
    Database_Helper db = new Database_Helper();
    RecyclerView owner_device_list_recycle_view, remote_device_list_recycle_view;
    TextView tv6, tv8;
    Toolbar devices_toolbar;
    RelativeLayout device_list_layout;
    ImageView img_find_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_list);
        tv6 = findViewById(R.id.tv6);
        device_list_layout = findViewById(R.id.device_list_layout);
        img_find_device = findViewById(R.id.img_find_device);
        devices_toolbar = findViewById(R.id.devices_toolbar);
        devices_toolbar.setTitle("Thiết bị của bạn");
        setSupportActionBar(devices_toolbar);
        getWindow().setStatusBarColor(COLOR_BLUE);

        owner_device_list_recycle_view = findViewById(R.id.owner_device_list_recycle_view);
        remote_device_list_recycle_view = findViewById(R.id.remote_device_list_recycle_view);

        db.getOwnDevices(phone_login,this);
        db.getRemoteList(this);
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
            case R.id.menu_qr_generate:
                QRGenerate.showMyQR(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void inVisible(){
        img_find_device.setVisibility(View.INVISIBLE);
        tv6.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showOwnDevices(ArrayList<Device_info> list) {
        inVisible();
        OwnDevicesAdapter adapter = new OwnDevicesAdapter(this, list);
        owner_device_list_recycle_view.setAdapter(adapter);
        adapter.setOnOwnDeviceClickedListener(this);
        owner_device_list_recycle_view.setLayoutManager(new LinearLayoutManager(this));
        owner_device_list_recycle_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRemoteDevices(ArrayList<Remote_device> list) {
        inVisible();
        if (list.size() % 2 != 0){
            device_list_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else {
            device_list_layout.setBackgroundColor(COLOR_EVEN_POSITION);
        }
        RemoteDevicesAdapter adapter = new RemoteDevicesAdapter(this, list);
        remote_device_list_recycle_view.setAdapter(adapter);
        adapter.setOnRemoteDeviceClickedListener(this);
        remote_device_list_recycle_view.setLayoutManager(new LinearLayoutManager(this));
        remote_device_list_recycle_view.setVisibility(View.VISIBLE);


    }

    @Override
    public void onGetRemoteList(ArrayList<Remote_device> list) {
        db.getRemoteDevice(this, list);
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
    public void onOwnDeviceItemClick(String device_address, String device_name) {
       moveToControl(phone_login, device_address, device_name );
    }

    @Override
    public void onRemoteDeviceItemClick(String owner_device_phoneNumber, String device_address, String device_name) {
        moveToControl(owner_device_phoneNumber, device_address, device_name);
    }

    private void moveToControl(String owner_device_phoneNumber, String device_address, String device_name){
        Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
        intent.putExtra(DEVICE_ADDRESS, device_address);
        intent.putExtra(DEVICE_NAME, device_name);
        intent.putExtra(OWNER_PHONE_NUMBER, owner_device_phoneNumber);
        startActivity(intent);
    }
}
