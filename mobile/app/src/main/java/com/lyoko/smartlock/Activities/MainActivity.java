package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.lyoko.smartlock.Adapters.TotalDevicesAdapter;
import com.lyoko.smartlock.LyokoActivity;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.Database_Helper;
import com.lyoko.smartlock.Interface.iDeviceList;
import com.lyoko.smartlock.Utils.DialogShow;
import com.lyoko.smartlock.Utils.LoadingDialog;
import com.lyoko.smartlock.Utils.SetupStatusBar;
import com.lyoko.smartlock.Utils.SuccessDialog;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.phone_login;
import static com.lyoko.smartlock.Utils.LyokoString.phone_name;

public class MainActivity extends LyokoActivity implements iDeviceList, TotalDevicesAdapter.OnDeviceClickedListener  {
    Database_Helper db = new Database_Helper();
    RecyclerView device_list_recycle_view;
    ArrayList<Device_info> ownList = new ArrayList<>();
    ArrayList<Device_info> canRemoteList = new ArrayList<>();
    TextView tv6;
    LoadingDialog loadingDialog;
    SuccessDialog successDialog;
    Toolbar devices_toolbar;
    RelativeLayout device_list_layout;
    ImageView img_find_device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SetupStatusBar.setup(MainActivity.this);
        tv6 = findViewById(R.id.tv6);
        device_list_layout = findViewById(R.id.device_list_layout);
        img_find_device = findViewById(R.id.img_find_device);
        devices_toolbar = findViewById(R.id.devices_toolbar);
        loadingDialog = new LoadingDialog(this);
        successDialog = new SuccessDialog(this);
        devices_toolbar.setTitle(phone_name.substring(0,1).toUpperCase() + phone_name.substring(1).toLowerCase()+"'s"+" home");
        setSupportActionBar(devices_toolbar);
        getFCMToken();
        device_list_recycle_view = findViewById(R.id.device_list_recycle_view);
        loadingDialog.startLoading("Đang Lấy dữ liệu");
        db.getOwnDevices(this);
        db.getRemoteDevice(this);
    }

    private void getFCMToken() {
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("TOKEN", "getInstanceId failed", task.getException());
                            return;
                        }
                        // Get new Instance ID token
                        String token = task.getResult().getToken();
                        new Database_Helper().updateToken(phone_login,token);
                    }
                });

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
                DialogShow.showAddDeviceMethods(this);
                return true;
                case R.id.menu_my_account:
                    Intent intent = new Intent(MainActivity.this, MyAccountActivity.class);
                    startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void showOwnDevices(ArrayList<Device_info> list) {
        ownList = list;
        if (canRemoteList.size()!=0)
        list.addAll(canRemoteList);
        TotalDevicesAdapter adapter = new TotalDevicesAdapter(this, list);
        device_list_recycle_view.setAdapter(adapter);
        adapter.setOnDeviceClickedListener(this);
        device_list_recycle_view.setLayoutManager(new GridLayoutManager(this,2));
        device_list_recycle_view.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRemoteDevices(ArrayList<Device_info> list) {
        loadingDialog.stopLoading();
        successDialog.startLoading("Lấy dữ liệu thành công",1000);
        canRemoteList= list;
        if (ownList.size()!=0)
        list.addAll(ownList);
        TotalDevicesAdapter adapter = new TotalDevicesAdapter(this, list);
        device_list_recycle_view.setAdapter(adapter);
        adapter.setOnDeviceClickedListener(this);
        device_list_recycle_view.setLayoutManager(new GridLayoutManager(this,2));
    }

    @Override
    public void notThingToShow() {
        img_find_device.setVisibility(View.VISIBLE);
        tv6.setVisibility(View.VISIBLE);
        img_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddDeviceActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }


    @Override
    public void onDeviceClickedListener(String owner, String device_address, String device_name) {
        boolean master;
        if (phone_login.equals(owner)) master = true; else master = false;
        DialogShow.showLockFunction(this, owner, device_address, device_name, master );
    }
}
