package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String uid = getIntent().getStringExtra("UID");
        Toast.makeText(this, uid, Toast.LENGTH_SHORT).show();
    }
}
