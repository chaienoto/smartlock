package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lyoko.smartlock.R;

public class RegisterActivity extends AppCompatActivity {
    EditText et_PWD, et_PWD_confirm;
    Button btn_Continue_register;
    String UID,PWD;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        UIRegister();
        Bundle bundle = getIntent().getExtras();
        UID = bundle.getString("UID","");
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
        btn_Continue_register = findViewById(R.id.btn_Continue_register);

    }
}
