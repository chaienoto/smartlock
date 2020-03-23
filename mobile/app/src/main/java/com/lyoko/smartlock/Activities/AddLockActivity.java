package com.lyoko.smartlock.Activities;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.UiThread;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lyoko.smartlock.Fragment.BarcodeScannerFragment;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.BluetoothLeService;
import com.lyoko.smartlock.Services.Find_Lock;
import com.lyoko.smartlock.Services.IFindLock;
import com.lyoko.smartlock.Utils.LyokoString;
import com.lyoko.smartlock.Utils.Permission;

public class AddLockActivity extends AppCompatActivity implements IFindLock, BarcodeScannerFragment.OnGetDeviceAddress {
    private Permission permission;
    FragmentManager manager;
    TextView add_lock_description, add_lock_step;
    private Find_Lock findLock;
    private Button btn_find_device;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        btn_find_device = findViewById(R.id.btn_find_device);
        add_lock_description = findViewById(R.id.add_lock_description);
        add_lock_step = findViewById(R.id.add_lock_step);

        getWindow().setStatusBarColor(Color.parseColor(LyokoString.BLUE));
        checkPermissionAndBLE();


        manager = getSupportFragmentManager();
        displayFragment(BarcodeScannerFragment.class);



//        btn_find_device.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(AddLockActivity.this,BarcodeScannerActivity.class);
//                startActivityForResult(intent,REQUEST_DEVICE_ADDRESS);
//            }
//        });
    }

    private void displayFragment(Class fragmentName) {
        try {
            if (fragmentName != null){
                manager.beginTransaction().replace(R.id.add_lock_content, (Fragment) fragmentName.newInstance()).commit();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void checkPermissionAndBLE() {
        findLock = new Find_Lock(this,this);
        permission = new Permission(this);
        permission.getPermission();
        if (permission.checkBLESupport()) {
            findLock.bluetoothEnable();
        }
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        if (fragment instanceof BarcodeScannerFragment) {
            BarcodeScannerFragment barcodeScannerFragment = (BarcodeScannerFragment) fragment;
            barcodeScannerFragment.setOnGetDeviceAddress(this);
        }
    }

    @Override
    public void onFound(final BluetoothDevice device, int rssi) {
        final BluetoothLeService bluetoothLeService = new BluetoothLeService(this,false,device);
        btn_find_device.setText("CONNECT");
        bluetoothLeService.connectDevice();

        btn_find_device.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

    }

    @Override
    public void onAddressDeviceScanned(String address) {
        if (address.contains(LyokoString.MAC_DEFAULT)){
            findLock.startScan(address);
        } else
        Log.d("QR_scan",address);

    }

    @Override
    public void onAddressScannedUnsuitable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Mã QR không phù hợp", Toast.LENGTH_SHORT).show();

            }
        });

    }

}
