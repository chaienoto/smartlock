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
import com.lyoko.smartlock.Adapters.DevicesAdapter;
import com.lyoko.smartlock.Models.Device_info;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.IDeviceList;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

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
            case R.id.menu_qr_generate:
                showMyQR();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showMyQR() {
        QRCodeWriter writer = new QRCodeWriter();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.myqr_dialog,null);
        final ImageView img_myQR = view.findViewById(R.id.img_myQR);
        final ImageView img_close_myQR_dialog = view.findViewById(R.id.img_close_myQR_dialog);
        builder.setView(view);
        try {
            BitMatrix bitMatrix = writer.encode(phone_login, BarcodeFormat.QR_CODE, 512, 512);
            Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.RGB_565);
            for (int x = 0; x<512; x++){
                for (int y=0; y<512; y++){
                    bitmap.setPixel(x,y,bitMatrix.get(x,y)? Color.BLUE : Color.WHITE);
                }
            }
            img_myQR.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        final AlertDialog dialog = builder.create();
        img_close_myQR_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
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
