package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.lyoko.smartlock.Fragment.LockedFragment;
import com.lyoko.smartlock.Fragment.UnlockFragment;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Helper;
import com.lyoko.smartlock.Interface.ILock;
import com.lyoko.smartlock.Utils.GetOTP_Helper;

import static android.widget.Toast.LENGTH_SHORT;
import static com.lyoko.smartlock.Utils.LyokoString.CLOSE_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_UNLOCK;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.OPEN_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.OWNER_PHONE_NUMBER;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class DeviceControllerActivity extends AppCompatActivity implements ILock {
    public static final int REQUEST_REMOTE_PHONE_NUMBER = 123;
    FragmentManager manager = getSupportFragmentManager();
    @SuppressLint("StaticFieldLeak")
    public static Button btn_door_lock;
    public GetOTP_Helper _Get_otpHelper = new GetOTP_Helper();
    Button btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    ImageView img_lock;
    Toolbar toolbar;
    public static String current_device_address;
    public static String current_device_name;
    public static String owner_phone_number;
    public static String otp;
    public static Boolean hold = false;
    public static Boolean clicked = false;
    public static Boolean otp_saved = false;
    public static Database_Helper db = new Database_Helper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        main_bg = findViewById(R.id.main_bg);
        toolbar = findViewById(R.id.toolbar);
        img_lock = findViewById(R.id.img_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);
        btn_door_lock = findViewById(R.id.btn_door_lock);

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        current_device_address = bundle.getString(DEVICE_ADDRESS);
        current_device_name = bundle.getString(DEVICE_NAME);
        owner_phone_number = bundle.getString(OWNER_PHONE_NUMBER);

        toolbar.setTitle(current_device_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        db.getLockState(owner_phone_number,current_device_address,this);

        btn_lock_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DeviceControllerActivity.this, HistoryActivity.class);
                intent.putExtra(DEVICE_ADDRESS, current_device_address);
                intent.putExtra(OWNER_PHONE_NUMBER, owner_phone_number);
                startActivity(intent);
                finish();
            }
        });

        btn_lock_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                showOTP();
            }
        });
    }



    private  void displayFragment(Class fragmentName) {
        try {
            if (!isFinishing()){
                if (fragmentName != null)
                    manager.beginTransaction().replace(R.id.main_content, (Fragment) fragmentName.newInstance()).commit();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void unlock() {
        db.changed_update_code(owner_phone_number, current_device_address, OPEN_LOCK);
    }

    public static void lock() {
        db.changed_update_code(owner_phone_number, current_device_address, CLOSE_LOCK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (owner_phone_number.equals(phone_login)) {
            getMenuInflater().inflate(R.menu.menu_lock_setting, menu);
        } else btn_lock_otp.setEnabled(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_remote_device:
                Intent intent = new Intent(DeviceControllerActivity.this, BarcodeScannerActivity.class);
                startActivityForResult(intent, REQUEST_REMOTE_PHONE_NUMBER);
                return true;
            case R.id.menu_lock_setting:
                Toast.makeText(this, "Đang làm", LENGTH_SHORT).show();
                return true;
            case R.id.advertise:
                Intent intent_advertise = new Intent(DeviceControllerActivity.this, AutoUnlockActivity.class );
                intent_advertise.putExtra(DEVICE_ADDRESS, current_device_address);
                intent_advertise.putExtra(OWNER_PHONE_NUMBER, owner_phone_number);
                startActivity(intent_advertise);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_REMOTE_PHONE_NUMBER) {
            if(resultCode == Activity.RESULT_OK) {
                final String remote_phone_number = data.getStringExtra(BarcodeScannerActivity.PHONE_NUM);
                db.addRemoteDevices(current_device_address,remote_phone_number);
                Toast.makeText(this, "Thêm Thành Công", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLock() {
        hold = false;
        clicked = false;
        toolbar.setBackgroundColor(COLOR_LOCK);
        getWindow().setStatusBarColor(COLOR_LOCK);
        btn_door_lock.setText("UNLOCK");
        displayFragment(LockedFragment.class);

    }

    @Override
    public void onUnlock() {
        getWindow().setStatusBarColor(COLOR_UNLOCK);
        toolbar.setBackgroundColor(COLOR_UNLOCK);
        btn_door_lock.setText("LOCK");
        clicked = true;
        displayFragment(UnlockFragment.class);
    }

    @Override
    public void onHold() {
        hold = true;
        clicked = true;
        if (UnlockFragment.hold_bg == null || UnlockFragment.tv_state_lock_info == null ){
            displayFragment(UnlockFragment.class);
            btn_door_lock.setText("LOCK");
        }else {
            UnlockFragment.hold_bg.setBackgroundResource(R.drawable.background_lock_hold);
            UnlockFragment.tv_state_lock_info.setText("HOLDING");

        }
    }

}
