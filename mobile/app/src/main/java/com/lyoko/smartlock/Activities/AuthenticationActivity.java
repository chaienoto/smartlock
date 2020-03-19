package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.gms.tasks.TaskExecutors;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AuthenticationActivity extends AppCompatActivity {
    TextView tv_UID_auth, tv_change_uid, tv_OTP_resend;
    EditText ed_OTP;
    Button btn_Continue_auth;
    String verificationId;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    BigInteger uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentical);
        UIRegister();
        Bundle bundle = getIntent().getExtras();
        //ngon ngu cua tin nhan gui code
        mAuth.setLanguageCode("vi");

        uid = BigInteger.valueOf(Integer.parseInt(bundle.getString("UID", "")));
        tv_UID_auth.setText(uid.toString());
//        FirebaseAuthSettings firebaseAuthSettings = mAuth.getFirebaseAuthSettings();
//        firebaseAuthSettings.setAutoRetrievedSmsCodeForPhoneNumber("+84"+uid,124356+"" );  //neu co cai nay thi no auto vao luon ma ko can xac nhận nhưng tuyệt đối ko sử dung cai này nhé

        sendVerificationCode(uid.toString());

        btn_Continue_auth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String otp = ed_OTP.getText().toString();
                if (otp.isEmpty() || otp.length() < 6) {
                    ed_OTP.setError("Enter otp");
                    ed_OTP.requestFocus();
                    return;
                }
                verifyCode(otp);
            }
        });

    }

    private void UIRegister() {
        tv_UID_auth = findViewById(R.id.tv_UID_auth);
        tv_change_uid = findViewById(R.id.tv_change_uid);
        tv_OTP_resend = findViewById(R.id.tv_OTP_resend);
        ed_OTP = findViewById(R.id.ed_OTP);
        btn_Continue_auth = findViewById(R.id.btn_Continue_auth);

    }

    private void verifyCode(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithCredential(credential);

    }

    private void signInWithCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Database_Service database_service = new Database_Service();
                            database_service.getPhoneNumbers(AuthenticationActivity.this);
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
        public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
//            signInWithCredential(phoneAuthCredential);
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

    public void checkPhoneNumber(ArrayList<String> list) {

        Byte check = 0;

        for (String phoneNumber : list) {
            if (phoneNumber.equals(uid.toString())) {
                check = 1;
                Intent intent = new Intent(AuthenticationActivity.this, LoginActivity.class); // cái này sai rồi, phải check xem thằng này muốn đăng kí/nhập đã nha
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }

        }
        if (check == 0) {
            Intent intent = new Intent(AuthenticationActivity.this, RegisterActivity.class); // cái này sai rồi, phải check xem thằng này muốn đăng kí/nhập đã nha
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }

}
