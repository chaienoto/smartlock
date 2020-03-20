package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lyoko.smartlock.R;

public class RegisterActivity extends AppCompatActivity {
    EditText et_password, et_password_confirm, et_CoverName;
    Button btn_register;
    String phoneNumber,password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_CoverName = findViewById(R.id.et_CoverName);
        btn_register = findViewById(R.id.btn_register);

        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");  // lấy phoneNumber chỗ này để push lên database

        password = et_password.getText().toString();

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               // check password vs confirm password
                // bắn dũ liệu lên database bao gồm cover name với pass
                registerWithPhoneNumber();
            }
        });
    }

    private void registerWithPhoneNumber() {

    }

}
