package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.widget.ToolbarWidgetWrapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    ImageView img_find ;
    Button btn_door_lock,btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    TextView tv_lh,tv_otp,tv_find_info,tv_state_lock_info;
    Toolbar toolbar;
    private boolean lock_found = false;
//    false : close | true: open
    private boolean door_state = false;
    int lock_color= Color.parseColor("#f95843");
    int unlock_color= Color.parseColor("#2ecc71");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UIRegister();
        setSupportActionBar(toolbar);

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

        btn_lock_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               Intent i = new Intent(MainActivity.this, HistoryActivity.class);
               startActivity(i);
            }
        });


    }
    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return true;
    }

    private void UIRegister() {
        main_bg = findViewById(R.id.main_bg);
        toolbar = findViewById(R.id.toolbar);
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
            if (!door_state){
                door_state = true;
                toolbar.setBackgroundColor(lock_color);
                getWindow().setStatusBarColor(lock_color);
                btn_door_lock.setText("UNLOCK");
                tv_state_lock_info.setText("LOCKED");
                tv_state_lock_info.setTextColor(lock_color);
                main_bg.setBackgroundResource(R.drawable.lock_background);
                img_find.setBackgroundResource(R.drawable.ic_locked);
            } else {
                door_state = false ;
                toolbar.setBackgroundColor(unlock_color);
                btn_door_lock.setText("LOCK");
                tv_state_lock_info.setText("UNLOCKED");
                tv_state_lock_info.setTextColor(unlock_color);
                getWindow().setStatusBarColor(unlock_color);
                main_bg.setBackgroundResource(R.drawable.unlock_background);
                img_find.setBackgroundResource(R.drawable.ic_unlocked);
            }
    }



}
