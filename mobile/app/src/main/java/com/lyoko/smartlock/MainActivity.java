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
    TextView tv_lh,tv_otp,tv_find_info,tv_state_lock_info;
    private boolean lock_found = false;
    private boolean door_state = false; //fasle : close | true: open
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
                if (!lock_found){
                    if(find_lock()){
                        lock_found = true;
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
        tv_state_lock_info = findViewById(R.id.tv_state_lock_info);
    }

    private boolean find_lock() {

        return true;
    }
    @SuppressLint("ResourceAsColor")
    private void setToFixWRT() {
        if(!lock_found){
            getWindow().setStatusBarColor(find_lock_color);
            btn_door_lock.setText("FIND");
            main_bg.setBackgroundResource(R.drawable.find_lock_background);
            img_find.setBackgroundResource(R.drawable.ic_add_lock);
            inviHOTP(true);
        } else
            if (!door_state){
                door_state = true;
                getWindow().setStatusBarColor(lock_color);
                btn_door_lock.setText("UNLOCK");
                tv_state_lock_info.setText("LOCKED");
                tv_state_lock_info.setTextColor(lock_color);
                main_bg.setBackgroundResource(R.drawable.lock_background);
                img_find.setBackgroundResource(R.drawable.ic_locked);
            } else {
                door_state = false ;
                btn_door_lock.setText("LOCK");
                tv_state_lock_info.setText("UNLOCKED");
                tv_state_lock_info.setTextColor(unlock_color);
                getWindow().setStatusBarColor(unlock_color);
                main_bg.setBackgroundResource(R.drawable.unlock_background);
                img_find.setBackgroundResource(R.drawable.ic_unlocked);
            }
    }

    private void inviHOTP(boolean DoUwantToInvi) {
        if (DoUwantToInvi){
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
