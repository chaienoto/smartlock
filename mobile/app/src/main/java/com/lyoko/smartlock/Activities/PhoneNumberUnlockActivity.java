package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.lyoko.smartlock.R;

import java.util.Random;

import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_AUTH_OTP;

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

        final FirebaseDatabase database = FirebaseDatabase.getInstance();


        btn_sendOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Random random = new Random();
                random.nextInt(999999 - 100000);
                String.valueOf(random.nextInt(999999 - 100000));
                String ngaunhien = String.valueOf(random.nextInt(999999 - 100000));

                DatabaseReference myRef = database.getReference(PATH_C_AUTH_OTP);

                myRef.setValue(ngaunhien);

                Log.d("LOG Ngau nhiuen", ngaunhien);

                Intent sentIntent = new Intent(Intent.ACTION_SEND);
                sentIntent.putExtra(Intent.EXTRA_TEXT, ngaunhien);
                sentIntent.setType("text / plain");

                Intent chooser = Intent.createChooser(sentIntent, "Chia se ma mo khoa: ");
                if (sentIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(chooser);
                }

            }
        });
    }

}
