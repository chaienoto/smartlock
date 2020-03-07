package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.lyoko.smartlock.R;

public class AuthenticationActivity extends AppCompatActivity {
    TextView tv_UID_auth,tv_change_uid,tv_OTP_resend;
    EditText ed_OTP;
    Button btn_Continue_auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentical);
        UIRegister();

        Bundle bundle = getIntent().getExtras();
        final String uid = bundle.getString("UID","");
        tv_UID_auth.setText(uid);

        btn_Continue_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = ed_OTP.getText().toString();
                if(opt_check(otp)){
                    Intent intent = new Intent(AuthenticationActivity.this, RegisterActivity.class);
                    intent.putExtra("UID", uid);
                    startActivity(intent);
                }
            }
        });
    }

    private boolean opt_check(String otp) {
        return  true;
    }

    private void UIRegister() {
        tv_UID_auth= findViewById(R.id.tv_UID_auth);
        tv_change_uid= findViewById(R.id.tv_change_uid);
        tv_OTP_resend= findViewById(R.id.tv_OTP_resend);
        ed_OTP= findViewById(R.id.ed_OTP);
        btn_Continue_auth= findViewById(R.id.btn_Continue_auth);

    }

}
