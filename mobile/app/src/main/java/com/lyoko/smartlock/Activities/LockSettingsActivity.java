package com.lyoko.smartlock.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lyoko.smartlock.LyokoActivity;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Utils.Database_Helper;
import com.lyoko.smartlock.Utils.CheckView;
import com.lyoko.smartlock.Utils.SetupStatusBar;
import com.lyoko.smartlock.Utils.SuccessDialog;

import static com.lyoko.smartlock.Utils.LyokoString.DELAY;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_ADDRESS;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.NOT_EMPTY;
import static com.lyoko.smartlock.Utils.LyokoString.OPEN_DELAY;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_LIMIT_ENTRY;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_LIMIT_UPDATE;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class LockSettingsActivity extends LyokoActivity {
    String device_address;
    String device_name;
    String device_type;
    int lock_delay, lock_otp_limit_entry;
    String owner_phone_number;
    Toolbar settings_toolbar;
    TextView tv_current_device_name, tv_current_unlock_delay, tv_current_limit_entry_otp;
    LinearLayout settings_lock_name, setting_unlock_delay, setting_max_otp_entry, setting_devices_management;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_settings);
        SetupStatusBar.setup(LockSettingsActivity.this);
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;

        device_address = bundle.getString(DEVICE_ADDRESS);
        device_name = bundle.getString(DEVICE_NAME);
        lock_delay = bundle.getInt(DELAY);
        device_type = bundle.getString(DEVICE_TYPE);
        lock_otp_limit_entry = bundle.getInt(OTP_LIMIT_ENTRY);

        settings_toolbar = findViewById(R.id.settings_toolbar);
        settings_lock_name = findViewById(R.id.settings_lock_name);
        setting_unlock_delay = findViewById(R.id.setting_unlock_delay);
        setting_max_otp_entry = findViewById(R.id.setting_max_otp_entry);
        tv_current_device_name = findViewById(R.id.tv_current_device_name);
        tv_current_unlock_delay = findViewById(R.id.tv_current_unlock_delay);
        tv_current_limit_entry_otp = findViewById(R.id.tv_current_limit_entry_otp);
        settings_toolbar.setTitle("CÀI ĐẶT  " + device_name.toUpperCase());

        tv_current_device_name.setText(device_name);
        tv_current_unlock_delay.setText(lock_delay+ " giây");
        tv_current_limit_entry_otp.setText(lock_otp_limit_entry+" lần");


        settings_lock_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeName();
            }
        });
        setting_unlock_delay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeDelay();
            }
        });
        setting_max_otp_entry.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showChangeOTPLimit();
            }
        });

    }

    private void showChangeName() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_change_device_name,null);
        final TextView tv_dialog_change_name_cancel = view.findViewById(R.id.tv_dialog_change_name_cancel);
        final TextView tv_dialog_change_name_confirm = view.findViewById(R.id.tv_dialog_change_name_confirm);
        final EditText et_new_device_name = view.findViewById(R.id.et_new_device_name);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        tv_dialog_change_name_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckView.isEmpty(et_new_device_name)){
                    et_new_device_name.setError(NOT_EMPTY);
                    return;
                }
                new Database_Helper().updateDeviceName(device_address,device_type,et_new_device_name.getText().toString());
                new SuccessDialog(LockSettingsActivity.this).startLoading("Sửa tên thiết bị thành công", 800);


            }
        });

        tv_dialog_change_name_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void showChangeDelay() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_change_lock_delay,null);
        final TextView tv_dialog_change_delay_cancel = view.findViewById(R.id.tv_dialog_change_delay_cancel);
        final TextView tv_dialog_change_delay_confirm = view.findViewById(R.id.tv_dialog_change_delay_confirm);
        final EditText et_new_delay = view.findViewById(R.id.et_new_delay);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        tv_dialog_change_delay_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckView.isEmpty(et_new_delay)){
                    et_new_delay.setError(NOT_EMPTY);
                    return;
                }
                tv_current_device_name.setText(et_new_delay.getText().toString()+" giây");
                new Database_Helper().updateDelay(device_address,device_type,et_new_delay.getText().toString());
                new SuccessDialog(LockSettingsActivity.this).startLoading("Đổi thời gian mở khóa thành công", 800);


            }
        });

        tv_dialog_change_delay_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
    private void showChangeOTPLimit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.dialog_change_lock_otp_limit_entry,null);
        final TextView tv_dialog_change_limit_entry_cancel = view.findViewById(R.id.tv_dialog_change_limit_entry_cancel);
        final TextView tv_dialog_change_limit_entry_confirm = view.findViewById(R.id.tv_dialog_change_limit_entry_confirm);
        final EditText et_new_limit_entry = view.findViewById(R.id.et_new_limit_entry);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

        tv_dialog_change_limit_entry_confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CheckView.isEmpty(et_new_limit_entry)){
                    et_new_limit_entry.setError(NOT_EMPTY);
                    return;
                }
                new Database_Helper().updateOTPLimitEntry(device_address,device_type,et_new_limit_entry.getText().toString());

                new SuccessDialog(LockSettingsActivity.this).startLoading("Đổi giới hạn nhập otp thành công", 800);
            }
        });

        tv_dialog_change_limit_entry_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}
