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
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    ImageView img_find ;
    Button btn_door_lock,btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    TextView tv_lh,tv_otp,tv_find_info;
    byte lock_found = 0;
    byte door_state = 1;
    int lock_color= Color.parseColor("#f95843");
    int unlock_color= Color.parseColor("#2ecc71");
    int find_lock_color= Color.parseColor("#3498db");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UIRegister();




        btn_lock_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Đang chuẩn bị", Toast.LENGTH_SHORT).show();
            }
        });
        btn_door_lock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lock_found == 0){
                    if(find_lock()){
                        lock_found = 1;
                        inviHOTP(false);
                        setToFixWRT();
                    }
                }
            }
        });
        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (find_lock()){
                    setToFixWRT();
                }
                return true;
            }
        });
        setToFixWRT();



    }

    private void UIRegister() {
        main_bg = findViewById(R.id.main_bg);
        img_find = findViewById(R.id.img_find);
        btn_door_lock = findViewById(R.id.btn_door_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);
        tv_lh = findViewById(R.id.tv_lh);
        tv_otp = findViewById(R.id.tv_otp);
        tv_find_info = findViewById(R.id.tv_find_info);
    }

    private boolean find_lock() {

        return true;
    }
    @SuppressLint("ResourceAsColor")
    private void setToFixWRT() {
        if(lock_found == 0){
            getWindow().setStatusBarColor(find_lock_color);
            btn_door_lock.setText("FIND"); 
            main_bg.setBackgroundResource(R.drawable.find_lock_background);
            img_find.setBackgroundResource(R.drawable.ic_add_lock);
            inviHOTP(true);
        } else {
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

    private void inviHOTP(boolean a) {
        if (a){
            btn_lock_history.setVisibility(View.INVISIBLE);
            btn_lock_otp.setVisibility(View.INVISIBLE);
            tv_lh.setVisibility(View.INVISIBLE);
            tv_otp.setVisibility(View.INVISIBLE);
            tv_find_info.setVisibility(View.VISIBLE);
        } else {
            btn_lock_history.setVisibility(View.VISIBLE);
            btn_lock_otp.setVisibility(View.VISIBLE);
            tv_lh.setVisibility(View.VISIBLE);
            tv_otp.setVisibility(View.VISIBLE);
            tv_find_info.setVisibility(View.INVISIBLE);

        }

    }
}
