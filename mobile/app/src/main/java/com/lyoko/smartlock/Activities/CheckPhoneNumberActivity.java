package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Services.ICheckPhoneNumber;
import java.math.BigInteger;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_GRAY;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_UNLOCK;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_LOGIN;

public class CheckPhoneNumberActivity extends AppCompatActivity implements ICheckPhoneNumber {
    Database_Service service = new Database_Service();
    public EditText et_phoneNumForCheck;
    public Button btn_checkPhoneNum;
    String phoneNumber;
    AlertDialog.Builder builder;
    LayoutInflater inflater;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sign);
        et_phoneNumForCheck = findViewById(R.id.et_phoneNumForCheck);
        btn_checkPhoneNum = findViewById(R.id.btn_checkPhoneNum);
        builder = new AlertDialog.Builder(this);
        inflater = getLayoutInflater();

        btn_checkPhoneNum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String s = et_phoneNumForCheck.getText().toString().trim();
                if (s == null || s == "" || s.length()<10){
                    return;
                }
                phoneNumber = String.valueOf(BigInteger.valueOf(Long.parseLong(s)));
                service.checkPhoneNumber(phoneNumber, CheckPhoneNumberActivity.this);
            }
        });

    }


    @Override
    public void phoneNumExist() {
        Log.d("phoneNumExist: ",phoneNumber);
        PHONE_LOGIN = phoneNumber;
        View view = inflater.inflate(R.layout.sign_in_dialog,null);
        TextView dialogPhone = view.findViewById(R.id.tv_sign_in_dialog);
        dialogPhone.setText("0"+ phoneNumber);
        builder = new AlertDialog.Builder(this);
        inflater = getLayoutInflater();
        builder.setView(view)
                .setNegativeButton("Không", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setPositiveButton("Tiếp tục", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(CheckPhoneNumberActivity.this, AuthenticationActivity.class);
                        intent.putExtra("isExist", true);
                        startActivity(intent);
                        finish();
                    }
                });
        builder.create().show();
    }

    @Override
    public void phoneNumNotExist() {
        PHONE_LOGIN = phoneNumber;
        Log.d("phoneNumNotExist: ",PHONE_LOGIN);
        final View view = inflater.inflate(R.layout.register_dialog,null);
        TextView dialogPhone = view.findViewById(R.id.tv_register_dialog);
        final TextView tv_dialogBack = view.findViewById(R.id.tv_back_dialog);
        final TextView tv_dialogContinue = view.findViewById(R.id.tv_continue_dialog);
        final CheckBox checkBox = view.findViewById(R.id.cb_checkbox);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialogPhone.setText("0"+ PHONE_LOGIN);
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!checkBox.isChecked()){
                    tv_dialogContinue.setEnabled(false);
                    tv_dialogContinue.setTextColor(COLOR_GRAY);
                } else{
                    tv_dialogContinue.setEnabled(true);
                    tv_dialogContinue.setTextColor(COLOR_UNLOCK);
                }
            }
        });
        tv_dialogBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        tv_dialogContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CheckPhoneNumberActivity.this, RegisterActivity.class);
                intent.putExtra("isExist", false);
                startActivity(intent);
                finish();
            }
        });
        dialog.show();
    }
}
