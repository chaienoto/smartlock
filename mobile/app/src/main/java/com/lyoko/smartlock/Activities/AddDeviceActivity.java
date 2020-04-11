package com.lyoko.smartlock.Activities;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lyoko.smartlock.Fragment.AutoSetupDeviceFragment;
import com.lyoko.smartlock.Fragment.BarcodeScannerFragment;
import com.lyoko.smartlock.Fragment.GetDeviceNameFragment;
import com.lyoko.smartlock.Fragment.GetWifiFragment;
import com.lyoko.smartlock.Interface.IQRCheck;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Utils.Permission;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;

public class AddDeviceActivity extends AppCompatActivity implements IQRCheck,BarcodeScannerFragment.OnGetDeviceAddress {
    private Permission permission;
    public static final int REQUEST_ENABLE_BT = 1;
    public static BluetoothAdapter bluetoothAdapter;
    private BluetoothManager bluetoothManager;
    public static FragmentManager manager ;
    public static TextView add_lock_description;
    public static TextView add_lock_step;
    public static Button btn_next_step;
    public EditText add_device_name;
    public static String device_name;
    public static String device_mac_address;
    public static String wifi_ssid;
    public static String wifi_password;
    Database_Service db_service = new Database_Service();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        btn_next_step = findViewById(R.id.btn_next_step);
        add_lock_description = findViewById(R.id.add_lock_description);
        add_lock_step = findViewById(R.id.add_lock_step);
        add_device_name = findViewById(R.id.add_device_name);

        getWindow().setStatusBarColor(COLOR_BLUE);
        permission = new Permission(this);
        checkBluetoothEnable();
        checkPermission();

        bluetoothManager = (BluetoothManager) getApplicationContext().getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        manager = getSupportFragmentManager();
        displayFragment(GetDeviceNameFragment.class);

        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextStep(1);

            }
        });
    }

    public void checkBluetoothEnable() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    public static void gotoNextStep(int step) {
        Log.d("step", step+"");
        switch (step){
            case 1:
                device_name = GetDeviceNameFragment.add_device_name.getText().toString();
                add_lock_description.setText(R.string.STEP_DESCRIPTION_2);
                add_lock_step.setText(R.string.STEP_2);
                displayFragment(BarcodeScannerFragment.class);
                break;
            case 2:
                add_lock_description.setText(R.string.STEP_DESCRIPTION_3);
                add_lock_step.setText(R.string.STEP_3);
                displayFragment(GetWifiFragment.class);
                break;
            case 3:
                add_lock_description.setText(R.string.STEP_DESCRIPTION_4);
                add_lock_step.setText(R.string.STEP_4);
                displayFragment(AutoSetupDeviceFragment.class);
                break;
            case 4: break;
        }
    }

    public static void displayFragment(Class fragmentName) {
        try {
            if (fragmentName != null)
                manager.beginTransaction().replace(R.id.add_lock_content, (Fragment) fragmentName.newInstance()).commit();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void checkPermission() {
        permission.getPermission();
}

    @Override
    public void onAttachFragment(final Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof BarcodeScannerFragment) {
            BarcodeScannerFragment barcodeScannerFragment = (BarcodeScannerFragment) fragment;
            barcodeScannerFragment.setOnGetDeviceAddress(this);
        }
    }

    @Override
    public void onDeviceAddressSuitable(String address) {
        Log.d("QR Scan", address);
        db_service.checkAuthenticDevice(address,this);

    }

    @Override
    public void onDeviceAddressUnSuitable() {
        Toast.makeText(AddDeviceActivity.this, "Mã QR không phù hợp", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNotOwner(String address) {
        Toast.makeText(AddDeviceActivity.this, "Ổ Khóa đã được đăng kí bởi số điện thoại khác", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onAsOwner(String address) {
        Toast.makeText(AddDeviceActivity.this, "Bạn đã đăng kí ổ khóa này rồi", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReadyToAddDevice(String address) {
        device_mac_address = address;
        gotoNextStep(2);
    }

}
