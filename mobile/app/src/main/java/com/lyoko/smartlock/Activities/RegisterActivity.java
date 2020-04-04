package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.lyoko.smartlock.Interface.IRegister;
import com.lyoko.smartlock.Models.User_Info;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;

import static com.lyoko.smartlock.Utils.LyokoString.NOT_EMPTY;
import static com.lyoko.smartlock.Utils.LyokoString.REGISTER_SUCCESSFULLY;

public class RegisterActivity extends AppCompatActivity implements IRegister {
    EditText et_password, et_password_confirm, et_ownerName;
    Button btn_register;
    String  password, password_confirm, owner_name;
    Database_Service db_service = new Database_Service();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_ownerName = findViewById(R.id.et_ownerName);
        btn_register = findViewById(R.id.btn_register);

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                owner_name = et_ownerName.getText().toString();
                password = et_password.getText().toString();
                password_confirm = et_password_confirm.getText().toString();
                checkEmpty(owner_name,1);
                checkEmpty(password,2);
                checkEmpty(password_confirm,3);

                if (password.equals(password_confirm)) {
                    User_Info userInfo = new User_Info(owner_name,password);
                    db_service.registerNewUser(RegisterActivity.this,userInfo);
                }
            }

            private void checkEmpty(String s, int index) {
                if (s == null || s ==""){
                    switch (index){
                        case 1:
                            Toast.makeText(RegisterActivity.this, NOT_EMPTY +" tên của bạn" , Toast.LENGTH_SHORT).show();
                            return;
                        case 2:
                            Toast.makeText(RegisterActivity.this, NOT_EMPTY +" mật khẩu" , Toast.LENGTH_SHORT).show();
                            return;
                        case 3:
                            Toast.makeText(RegisterActivity.this, NOT_EMPTY +" nhập lại maatk khẩu" , Toast.LENGTH_SHORT).show();
                            return;

                    }
                }
            }
        });
    }

    @Override
    public void onRegisterSuccess() {
        Toast.makeText(this, REGISTER_SUCCESSFULLY, Toast.LENGTH_SHORT).show();
        Intent i = new Intent(RegisterActivity.this, DeviceListActivity.class);
        startActivity(i);
        finish();
    }
}
