package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.ILock;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_UNLOCK;
import static com.lyoko.smartlock.Utils.LyokoString.UNLOCK_DELAY;

public class MainActivity extends AppCompatActivity implements ILock {
    public static final int REQUEST_REMOTE_PHONE_NUMBER = 143;
    ImageView img_lock;
    Button btn_door_lock, btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    TextView tv_state_lock_info;
    Toolbar toolbar;
    String current_device_address;
    String current_device_name;
    String owner_name;
    Database_Service db_service = new Database_Service();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_bg = findViewById(R.id.main_bg);
        toolbar = findViewById(R.id.toolbar);
        img_lock = findViewById(R.id.img_lock);
        btn_door_lock = findViewById(R.id.btn_door_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);
        tv_state_lock_info = findViewById(R.id.tv_state_lock_info);

        Bundle bundle = getIntent().getExtras();
        current_device_address = bundle.getString("address");
        current_device_name = bundle.getString("name");
        toolbar.setTitle(current_device_name);

        db_service.getLockState(current_device_address,this);
        db_service.getOwnerName(this);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        lock();
                    }
                },UNLOCK_DELAY);
                unlock();
                db_service.saveHistory(current_device_address,owner_name);
                return true;
            }
        });

        btn_lock_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.putExtra("address", current_device_address);;
                startActivity(intent);
            }
        });

    }

    private void unlock() {
        db_service.changeLockState(current_device_address,1);
    }
    private void lock() {
        db_service.changeLockState(current_device_address,0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_lock_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_remote_device:
                Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
                startActivityForResult(intent, REQUEST_REMOTE_PHONE_NUMBER);
                return true;
            case R.id.menu_lock_setting:
                Toast.makeText(this, "Đang làm", Toast.LENGTH_SHORT).show();
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
                db_service.addRemoteDevices(current_device_address,remote_phone_number);
                Toast.makeText(this, "Thêm Thành Công", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLock() {
        Toast.makeText(this, "Đã Đóng", Toast.LENGTH_SHORT).show();
        toolbar.setBackgroundColor(COLOR_LOCK);
        getWindow().setStatusBarColor(COLOR_LOCK);
        btn_door_lock.setText("UNLOCK");
        tv_state_lock_info.setText("LOCKED");
        tv_state_lock_info.setTextColor(COLOR_LOCK);
        main_bg.setBackgroundResource(R.drawable.lock_background);
        img_lock.setBackgroundResource(R.drawable.ic_locked);
    }

    @Override
    public void onUnlock() {
        Toast.makeText(this, "Cửa đóng sau 5s", Toast.LENGTH_SHORT).show();
        toolbar.setBackgroundColor(COLOR_UNLOCK);
        btn_door_lock.setText("LOCK");
        tv_state_lock_info.setText("UNLOCKED");
        tv_state_lock_info.setTextColor(COLOR_UNLOCK);
        getWindow().setStatusBarColor(COLOR_UNLOCK);
        main_bg.setBackgroundResource(R.drawable.unlock_background);
        img_lock.setBackgroundResource(R.drawable.ic_unlocked);
    }

    @Override
    public void onGetOwnerName(String ownerName) {
        owner_name = ownerName;
    }
}
