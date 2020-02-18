package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.lyoko.smartlock.Dialogs.SignUpDialog;

public class CheckSignActivity extends AppCompatActivity {

    public EditText et_UID_csign;
    public Button btn_Continue_csign;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sign);
        UIRegister();


        btn_Continue_csign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uid = et_UID_csign.getText().toString();
                if (uid_check_exist(uid)){
                    Intent intent = new Intent(CheckSignActivity.this, LoginActivity.class);
                    intent.putExtra("UID", uid);
                    startActivity(intent);
                } else openDialog(uid);
            }
        });
    }

    private void UIRegister() {
        et_UID_csign = findViewById(R.id.et_UID_csign);
        btn_Continue_csign = findViewById(R.id.btn_Continue_csign);
    }

    private boolean uid_check_exist(String uid ) {
        return false;
    }


    public void openDialog(String uid){
        SignUpDialog dialogLogin = new SignUpDialog();
        Bundle bundle = new Bundle();
        bundle.putString("UID",uid);
        dialogLogin.setArguments(bundle);
        dialogLogin.show((CheckSignActivity.this).getSupportFragmentManager(), "thong bao");
    }


}
