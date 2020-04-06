package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.lyoko.smartlock.Adapters.OwnDevicesAdapter;
import com.lyoko.smartlock.Adapters.RemoteDevicesAdapter;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.Models.Remote_device;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.IDeviceList;
import com.lyoko.smartlock.Utils.QRGenerate;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class DeviceListActivity extends AppCompatActivity implements IDeviceList, OwnDevicesAdapter.OnOwnDeviceClickedListener, RemoteDevicesAdapter.OnRemoteDeviceClickedListener  {
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
        devices_toolbar.setTitle("Thiết bị có thể điều khiển");
        setSupportActionBar(devices_toolbar);
        getWindow().setStatusBarColor(COLOR_BLUE);

        device_list_recycle_view = findViewById(R.id.device_list_recycle_view);
        tv6 = findViewById(R.id.tv6);
        db_service.getOwnDevices(phone_login,this);
        db_service.getRemoteList(this);
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

    private void invi(){
        img_find_device.setVisibility(View.INVISIBLE);
        tv6.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showOwnDevices(ArrayList<Device_info> list) {
        if (list.size() % 2 != 0){
            device_list_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else device_list_layout.setBackgroundColor(COLOR_EVEN_POSITION);
        OwnDevicesAdapter adapter = new OwnDevicesAdapter(this, list);
        device_list_recycle_view.setAdapter(adapter);
        adapter.setOnOwnDeviceClickedListener(this);
        device_list_recycle_view.setLayoutManager(new LinearLayoutManager(this));
        invi();
        device_list_recycle_view.setVisibility(View.VISIBLE);

    }

    @Override
    public void showRemoteDevices(ArrayList<Remote_device> list) {
        invi();
        if (list.size() % 2 != 0){
            device_list_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else {
            device_list_layout.setBackgroundColor(COLOR_EVEN_POSITION);
        }
        RemoteDevicesAdapter adapter = new RemoteDevicesAdapter(this, list);
        device_list_recycle_view.setAdapter(adapter);
        adapter.setOnRemoteDeviceClickedListener(this);
        device_list_recycle_view.setLayoutManager(new LinearLayoutManager(this));
        device_list_recycle_view.setVisibility(View.VISIBLE);


    }

    @Override
    public void onGetRemoteList(ArrayList<Remote_device> list) {
        db_service.getRemoteDevice(this, list);
    }

    @Override
    public void notThingToShow(String list) {
        if (list.equals("own")){
            db_service.getRemoteList(this);
        }
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
    public void onOwnDeviceItemClick(String address, String name) {
       moveToControl(phone_login,address,name);
    }

    @Override
    public void onRemoteDeviceItemClick(String owner, String address, String name) {
        Log.d("device_name",name);
        moveToControl(owner,address,name);
    }
    private void moveToControl(String owner, String address, String name){
        Intent intent = new Intent(DeviceListActivity.this, MainActivity.class);
        intent.putExtra("address", address);
        intent.putExtra("name", name);
        intent.putExtra("owner", owner);
        startActivity(intent);
    }
}
