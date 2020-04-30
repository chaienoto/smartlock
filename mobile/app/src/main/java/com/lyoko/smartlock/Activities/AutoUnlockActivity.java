package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lyoko.smartlock.Adapters.UnknownDevicesAdapter;
import com.lyoko.smartlock.Models.BLE_Device;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Helper;

import java.util.ArrayList;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_EVEN_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_ODD_POSITION;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_PHONE_NUMBER;

public class AutoUnlockActivity extends AppCompatActivity implements UnknownDevicesAdapter.OnUnknownBLEDeviceClickedListener {
    private static final int REQUEST_ENABLE_BT = 1;
    Toolbar auto_unlock_toolbar;
    RecyclerView trusted_device_recycle_view, unknown_device_recycle_view;
    Button btn_add_trusted_device;
    String device_count = "0";
    ArrayList<BLE_Device> unknownList = new ArrayList<>();
    ArrayList<String> _unknownList = new ArrayList<>();
    BluetoothAdapter bluetoothAdapter;
    BluetoothManager bluetoothManager;
    RelativeLayout trusted_device_layout;
    TextView tv8;
    UnknownDevicesAdapter unknownDevicesAdapter;
    private boolean mScanning;
    private Handler handler;
    private static final long SCAN_PERIOD = 3000;
    private String current_device_address, owner_phone_number;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_unlock);
        auto_unlock_toolbar = findViewById(R.id.auto_unlock_toolbar);
        trusted_device_recycle_view = findViewById(R.id.trusted_device_recycle_view);
        unknown_device_recycle_view = findViewById(R.id.unknown_device_recycle_view);
        btn_add_trusted_device = findViewById(R.id.btn_add_trusted_device);
        trusted_device_layout = findViewById(R.id.trusted_device_layout);
        tv8 = findViewById(R.id.tv8);
        auto_unlock_toolbar.setTitle("Quản lý thiết bị Auto Unlock");
        setSupportActionBar(auto_unlock_toolbar);
        Bundle bundle = getIntent().getExtras();
        current_device_address = bundle.getString(DEVICE_ADDRESS);
        owner_phone_number = bundle.getString(OWNER_PHONE_NUMBER);
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        handler = new Handler();
        mScanning = false;
        btn_add_trusted_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                _unknownList.clear();
                unknownList.clear();
                scanLeDevice(true);
            }
        });

    }
    private BluetoothAdapter.LeScanCallback leScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!_unknownList.contains(device.getAddress()) && rssi > -69){
                                _unknownList.add(device.getAddress());
                                unknownList.add(new BLE_Device(device.getName(),device.getAddress().toLowerCase()));
                            }
                        }
                    });
                }
            };

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    bluetoothAdapter.stopLeScan(leScanCallback);
                    showUnknownDevice();
                }
            }, SCAN_PERIOD);

            mScanning = true;
            bluetoothAdapter.startLeScan(leScanCallback);
        } else {
            mScanning = false;
            bluetoothAdapter.stopLeScan(leScanCallback);
        }
    }

    private void showUnknownDevice() {
        tv8.setVisibility(View.VISIBLE);
        if (unknownList.size() % 2 != 0){
            trusted_device_layout.setBackgroundColor(COLOR_ODD_POSITION);
        } else trusted_device_layout.setBackgroundColor(COLOR_EVEN_POSITION);
        unknownDevicesAdapter = new UnknownDevicesAdapter(this, unknownList);
        unknownDevicesAdapter.setOnUnknownBLEDeviceClickedListener(this);
        unknown_device_recycle_view.setAdapter(unknownDevicesAdapter);
        unknown_device_recycle_view.setLayoutManager(new LinearLayoutManager(this));
    }

    @Override
    public void onUnknownBLEDeviceClickedListener(final String ble_name, final String ble_address) {
        AlertDialog alertDialog = new AlertDialog.Builder(AutoUnlockActivity.this).create();
        alertDialog.setTitle("THÔNG BÁO");
        alertDialog.setMessage("Bạn có muốn thêm "+ ble_name+" làm thiết bị mở khóa chứ ");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        new Database_Helper().addTrustedDevice(current_device_address, owner_phone_number, device_count, ble_name, ble_address);
                        tv8.setVisibility(View.INVISIBLE);

                    }
                });
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "KHÔNG",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
