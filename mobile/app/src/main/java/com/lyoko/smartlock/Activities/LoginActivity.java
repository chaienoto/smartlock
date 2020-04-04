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
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.ILogin;


public class LoginActivity extends AppCompatActivity implements ILogin {
    EditText et_login_password;
    TextView tv_change_login_phoneNumber;
    Button btn_login;
    Database_Service service = new Database_Service();
    String loginPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        et_login_password = findViewById(R.id.et_login_password);
        tv_change_login_phoneNumber = findViewById(R.id.tv_change_login_phoneNumber);

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPassword = et_login_password.getText().toString();
                if (loginPassword == null || loginPassword ==""){
                    return;
                }
                service.checkPassword(loginPassword,LoginActivity.this);

            }
        });

    }

    @Override
    public void onPasswordMatched() {
        Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
        Intent i = new Intent(LoginActivity.this, DeviceListActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public void onPasswordNotMatch() {
        Toast.makeText(LoginActivity.this, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
        et_login_password.setText("");
    }

}
