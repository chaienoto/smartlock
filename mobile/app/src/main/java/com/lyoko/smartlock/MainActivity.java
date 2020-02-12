package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView img_find ;
    Button btn_door_lock,btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    byte lock_found = 0;
    byte door_state = 1;
    int lock_color= Color.parseColor("#f95843");
    int unlock_color= Color.parseColor("#2ecc71");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_bg = findViewById(R.id.main_bg);
        img_find = findViewById(R.id.img_find);
        btn_door_lock = findViewById(R.id.btn_door_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);

        btn_lock_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Đang chuẩn bị", Toast.LENGTH_SHORT).show();
            }
        });
        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                setToFixWRT();
                return true;
            }
        });
        setToFixWRT();



    }

    @SuppressLint("ResourceAsColor")
    private void setToFixWRT() {
        if (door_state == 1){
            door_state = 0;
            getWindow().setStatusBarColor(lock_color);
            btn_door_lock.setText("UNLOCK");
            main_bg.setBackgroundResource(R.drawable.lock_background);
            img_find.setBackgroundResource(R.drawable.ic_locked);
        } else {
            door_state = 1;
            btn_door_lock.setText("LOCK");
            getWindow().setStatusBarColor(unlock_color);
            main_bg.setBackgroundResource(R.drawable.unlock_background);
            img_find.setBackgroundResource(R.drawable.ic_unlocked);
        }
    }
}
