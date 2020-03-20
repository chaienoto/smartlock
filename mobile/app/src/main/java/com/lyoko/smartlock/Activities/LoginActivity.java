package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.lyoko.smartlock.R;

public class LoginActivity extends AppCompatActivity {
    EditText et_login_password;
    TextView tv_change_login_phoneNumber;
    Button btn_login;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login.findViewById(R.id.btn_login);
        et_login_password.findViewById(R.id.et_login_password);
        tv_change_login_phoneNumber.findViewById(R.id.tv_change_login_phoneNumber);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_login_password.getText().toString();
                //check password với pass trên database
                // nếu đúng thì chuyển qua MainActivity
            }
        });

    }
}
