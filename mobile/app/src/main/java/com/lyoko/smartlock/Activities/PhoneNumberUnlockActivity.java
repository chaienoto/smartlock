package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.lyoko.smartlock.R;

public class PhoneNumberUnlockActivity extends AppCompatActivity {

    EditText et_phoneNumOtpUnlock;
    Button btn_sendOtp;
    String phoneNumOtpUnlock;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_number_unlock);

        et_phoneNumOtpUnlock = findViewById(R.id.et_phoneNumOtpUnlock);
        btn_sendOtp = findViewById(R.id.btn_sendOtp);


        btn_sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                phoneNumOtpUnlock = et_phoneNumOtpUnlock.getText().toString();
                Intent i = new Intent(PhoneNumberUnlockActivity.this, OtpUnlockActivity.class);
                i.putExtra("phoneNumOtpUnlock", phoneNumOtpUnlock);
                Log.d("PhoneNumber", phoneNumOtpUnlock);
                startActivity(i);

            }
        });
    }

}
