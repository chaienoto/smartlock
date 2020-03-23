package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Utils.LyokoString;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.lyoko.smartlock.Utils.LyokoString.PHONE_LOGIN;

public class AuthenticationActivity extends AppCompatActivity {
    TextView tv_phoneNumForVerify, tv_change_phoneNum, tv_resend_otp;
    EditText ed_otp_code;
    Button btn_Continue_auth;
    String verificationId;
    FirebaseAuth mAuth;
    Boolean isExist;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentical);
        tv_phoneNumForVerify = findViewById(R.id.tv_phoneNumForVerify);
        tv_change_phoneNum = findViewById(R.id.tv_change_phoneNum);
        tv_resend_otp = findViewById(R.id.tv_resend_otp);
        ed_otp_code = findViewById(R.id.ed_otp_code);
        btn_Continue_auth = findViewById(R.id.btn_verify);

        mAuth = FirebaseAuth.getInstance();
        mAuth.setLanguageCode("vi");

        Bundle bundle = getIntent().getExtras();
        isExist = bundle.getBoolean("isExist");

        tv_phoneNumForVerify.setText("0"+PHONE_LOGIN);
        sendVerificationCode(PHONE_LOGIN);


        tv_change_phoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AuthenticationActivity.this, CheckPhoneNumberActivity.class);
                startActivity(intent);
                finish();
            }
        });

        tv_resend_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationCode(PHONE_LOGIN);
            }
        });

        btn_Continue_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = ed_otp_code.getText().toString();
                if (otp.isEmpty() || otp.length() < 6) {
                    ed_otp_code.setError("Enter otp");
                    ed_otp_code.requestFocus();
                    return;
                }
                    verifyCode(otp);
            }
        });
    }
    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            if (isExist){
                                Intent intent = new Intent(AuthenticationActivity.this, LoginActivity.class);
//                                intent.putExtra("phoneNumber", phoneNumber);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(AuthenticationActivity.this, RegisterActivity.class);
                                startActivity(intent);
                            }
                        } else {
                            Toast.makeText(AuthenticationActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }


    private void sendVerificationCode(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + phoneNumber,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {


        @Override
        public void onVerificationCompleted(final PhoneAuthCredential credential) {
            signInWithPhoneAuthCredential(credential);
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(AuthenticationActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
        }

    };

}
