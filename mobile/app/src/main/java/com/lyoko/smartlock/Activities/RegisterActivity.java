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
    EditText et_PWD, et_PWD_confirm, et_CoverName;
    Button btn_Continue_register;
    String UID,PWD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UIRegister();
//        Bundle bundle = getIntent().getExtras();
//        UID = bundle.getString("phoneNumber","");
        Intent i = this.getIntent();
        UID = i.getStringExtra("phoneNumber");
//        Toast.makeText(this, "phone number: " + UID, Toast.LENGTH_SHORT).show();
        Log.d("phone :", UID);
        PWD = et_PWD.getText().toString();

        btn_Continue_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (UID_Register()){
                    Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean UID_Register() {
        return true;
    }

    private void UIRegister() {
        et_PWD = findViewById(R.id.et_PWD);
        et_PWD_confirm = findViewById(R.id.et_PWD_confirm);
        et_CoverName = findViewById(R.id.et_CoverName);
        btn_Continue_register = findViewById(R.id.btn_Continue_register);
    }
}
