package com.lyoko.smartlock.Activities;

import android.annotation.SuppressLint;
import android.graphics.Color;
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
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.Permission;

import static com.lyoko.smartlock.Utils.LyokoString.COLOR_BLUE;

public class AddDeviceActivity extends AppCompatActivity implements BarcodeScannerFragment.OnGetDeviceAddress {
    private Permission permission;
    static FragmentManager manager ;
    @SuppressLint("StaticFieldLeak")
    static TextView add_lock_description;
    @SuppressLint("StaticFieldLeak")
    static TextView add_lock_step;
    @SuppressLint("StaticFieldLeak")
    public static Button btn_next_step;
    EditText add_device_name;
    public static String device_name;
    public static String device_mac_address;
    public static String wifi_ssid;
    public static String wifi_password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_lock);
        btn_next_step = findViewById(R.id.btn_next_step);
        add_lock_description = findViewById(R.id.add_lock_description);
        add_lock_step = findViewById(R.id.add_lock_step);
        add_device_name = findViewById(R.id.add_device_name);

        getWindow().setStatusBarColor(Color.parseColor(COLOR_BLUE));
        permission = new Permission(this);
        checkPermission();

        manager = getSupportFragmentManager();
        displayFragment(GetDeviceNameFragment.class);

        btn_next_step.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gotoNextStep(1);

            }
        });
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

    private static void displayFragment(Class fragmentName) {
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
        device_mac_address = address;
        Log.d("QR Scan", device_mac_address);
        gotoNextStep(2);

    }

    @Override
    public void onDeviceAddressUnSuitable() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "Mã QR không phù hợp", Toast.LENGTH_SHORT).show();

            }
        });

    }


}
