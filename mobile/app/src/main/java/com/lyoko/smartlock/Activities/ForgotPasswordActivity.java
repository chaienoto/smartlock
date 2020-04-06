package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Utils.CheckView;

import static com.lyoko.smartlock.Utils.LyokoString.LOGGED_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.LOGGED_PHONE;
import static com.lyoko.smartlock.Utils.LyokoString.LOGIN_SAVED;
import static com.lyoko.smartlock.Utils.LyokoString.NOT_EMPTY;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;
import static com.lyoko.smartlock.Utils.LyokoString.phone_name;

public class ForgotPasswordActivity extends AppCompatActivity {
    TextView tv_forgot_login_name, tv_forgot_login_phoneNumber;
    EditText et_forgot_login_password, et_forgot_login_password_confirm;
    Button btn_forgot_login_password_confirm;
    private String loginPhoneNumber, loginName, pw, pwc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        tv_forgot_login_name = findViewById(R.id.tv_forgot_login_name);
        tv_forgot_login_phoneNumber = findViewById(R.id.tv_forgot_login_phoneNumber);
        et_forgot_login_password = findViewById(R.id.et_forgot_login_password);
        et_forgot_login_password_confirm = findViewById(R.id.et_forgot_login_password_confirm);
        btn_forgot_login_password_confirm = findViewById(R.id.btn_forgot_login_password_confirm);

        Bundle bundle = getIntent().getExtras();
        loginPhoneNumber = bundle.getString(LOGGED_PHONE);
        loginName = bundle.getString(LOGGED_NAME);

        tv_forgot_login_name.setText(loginName);
        tv_forgot_login_phoneNumber.setText("0"+loginPhoneNumber);

        btn_forgot_login_password_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckView.isEmpty(et_forgot_login_password)){
                    et_forgot_login_password.setError(NOT_EMPTY);
                    return;
                }
                if (CheckView.isEmpty(et_forgot_login_password_confirm)){
                    et_forgot_login_password.setError(NOT_EMPTY);
                    return;
                }
                pwc = et_forgot_login_password_confirm.getText().toString().trim();
                pw = et_forgot_login_password.getText().toString().trim();
                if (pw.equals(pwc)){
                    new Database_Service().changePassword(pw);
                    showNoti();
                } else {
                    et_forgot_login_password_confirm.setError("Nhập lại mật khẩu không đúng");
                }
            }
        });
    }

    private void showNoti() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.reset_password_dialog,null);
        TextView tv_back_to_login = view.findViewById(R.id.tv_back_to_login);
        final AlertDialog dialog = builder.setView(view).create();
        tv_back_to_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                intent.putExtra(LOGGED_PHONE, phone_login);
                intent.putExtra(LOGGED_NAME, phone_name);
                intent.putExtra(LOGIN_SAVED, true);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();

    }
}
