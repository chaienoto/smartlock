package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthSettings;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.protobuf.SourceContextProto;
import com.lyoko.smartlock.R;

import org.w3c.dom.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_AUTH_OTP;

public class OtpUnlockActivity extends AppCompatActivity {

    Button btn_OtpUnlock;
    EditText et_OtpUnlock;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseAuthSettings firebaseAuthSettings = mAuth.getFirebaseAuthSettings();
    FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    String phoneNumOtpUnlock, verificationId;
//    String otpFake = "123123";

    DocumentReference documentReference = firebaseFirestore.document("/door/otp");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_unlock);
        mAuth.setLanguageCode("vi");

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference(PATH_C_AUTH_OTP);

        myRef.setValue("Hello, World!");

        btn_OtpUnlock = findViewById(R.id.btn_OtpUnlock);
        et_OtpUnlock = findViewById(R.id.et_OtpUnlock);
        String otpUnlock = et_OtpUnlock.getText().toString();

        Bundle bundle = getIntent().getExtras();
        phoneNumOtpUnlock = bundle.getString("phoneNumOtpUnlock");
        Log.d("TAG","mess" + phoneNumOtpUnlock);
        sendVerificationCode(phoneNumOtpUnlock);



//        otpUnlock = et_OtpUnlock.getText().toString();

//        sendVerificationCode(phoneNumOtpUnlock);

        btn_OtpUnlock.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String otpUnlock = et_OtpUnlock.getText().toString();
                if (otpUnlock.isEmpty() || otpUnlock.length() < 6) {
                    et_OtpUnlock.setError("Enter otp");
                    et_OtpUnlock.requestFocus();

                } else {

                    Map<String, Object> data = new HashMap<>();
                    data.put("otp", otpUnlock);
                    documentReference.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            Toast.makeText(OtpUnlockActivity.this, "Thành công!" + otpUnlock, Toast.LENGTH_SHORT).show();
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(OtpUnlockActivity.this, "Thử lại sau!", Toast.LENGTH_SHORT).show();
                                }
                            });
//                verifyCode(otpUnlock);
                }
            }
        });


    }

    //Xac thuc va dang nhap
    private void verifyCode(String otpUnlock) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otpUnlock);
        signInWithPhoneAuthCredential(credential);
    }

    private void signInWithPhoneAuthCredential(final PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(OtpUnlockActivity.this, "Success!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(OtpUnlockActivity.this,  task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void sendVerificationCode(String phoneNumOtpUnlock) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84" + phoneNumOtpUnlock,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks
            mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onVerificationCompleted(final PhoneAuthCredential credential) {
            //nhap opt dung vs otp da gui
            String code = credential.getSmsCode();
            if (code != null) {
                et_OtpUnlock.setText(code);
                verifyCode(code);
            }
            Log.d("TAG", "code" + credential);
//            verifyCode(credential);


        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            Toast.makeText(OtpUnlockActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            //code da gui thanh cong
            super.onCodeSent(s, forceResendingToken);
            verificationId = s;
//            Log.d("LOG: ", "code da gui " + verificationId);
//            OtpUnlockActivity.this.ena
        }

    };

}
