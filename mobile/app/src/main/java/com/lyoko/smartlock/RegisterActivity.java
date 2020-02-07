package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class RegisterActivity extends AppCompatActivity {

    private RadioGroup toggle;
    private RadioButton rdSoDienThoai, rdEmail;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        toggle = findViewById(R.id.toggle);
        rdSoDienThoai = findViewById(R.id.rdSoDienThoai);
        rdEmail = findViewById(R.id.rdEmail);

        rdSoDienThoai.setOnCheckedChangeListener(new RadioButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            }
        });
    }
}
