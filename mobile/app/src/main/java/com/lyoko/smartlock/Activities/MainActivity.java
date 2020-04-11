package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.lyoko.smartlock.Fragment.LockedFragment;
import com.lyoko.smartlock.Fragment.UnlockFragment;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.Database_Service;
import com.lyoko.smartlock.Interface.ILock;
import com.lyoko.smartlock.Utils.CheckView;
import com.lyoko.smartlock.Utils.OTP;

import static android.widget.Toast.LENGTH_SHORT;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_LOCK;
import static com.lyoko.smartlock.Utils.LyokoString.COLOR_UNLOCK;
import static com.lyoko.smartlock.Utils.LyokoString.NOT_EMPTY;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_COPIED;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_MESSAGE;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_NOT_SAVE;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_REMOVED;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_SAVED;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_SUB_MESSAGE;
import static com.lyoko.smartlock.Utils.LyokoString.OTP_SHARE_SUB_MESSAGE_2;
import static com.lyoko.smartlock.Utils.LyokoString.UNLOCK_DELAY;
import static com.lyoko.smartlock.Utils.LyokoString.phone_login;

public class MainActivity extends AppCompatActivity implements ILock {
    public static final int REQUEST_REMOTE_PHONE_NUMBER = 143;
    ImageView img_lock;
    public static Button btn_door_lock;
    Button btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    FragmentManager manager = getSupportFragmentManager();
    Toolbar toolbar;
    public static String current_device_address;
    public static String current_device_name;
    public static String owner_phone_number;
    public static String owner_name;
    public static String otp;
    public static Boolean hold = false;
    public static Boolean clicked = false;
    public OTP otp_help = new OTP();
    public static Boolean otp_saved = false;
    public static Database_Service db_service = new Database_Service();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        main_bg = findViewById(R.id.main_bg);
        toolbar = findViewById(R.id.toolbar);
        img_lock = findViewById(R.id.img_lock);
        btn_door_lock = findViewById(R.id.btn_door_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);


        Bundle bundle = getIntent().getExtras();
        current_device_address = bundle.getString("address");
        current_device_name = bundle.getString("name");
        owner_phone_number = bundle.getString("owner");
        toolbar.setTitle(current_device_name);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        otp_help.getOTPDetail(current_device_address);
        db_service.getLockState(owner_phone_number,current_device_address,this);

        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (!clicked){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if(!hold){
                                lock();
                            } return;
                        }
                    },UNLOCK_DELAY);
                    Toast.makeText(MainActivity.this, "Cửa tự động đóng sau "+UNLOCK_DELAY/1000+ " giây", LENGTH_SHORT).show();
                    unlock();
                    db_service.saveHistory(owner_phone_number,current_device_address,owner_name);
                }
                return true;
            }
        });

        btn_lock_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
                intent.putExtra("address", current_device_address);
                intent.putExtra("owner", owner_phone_number);
                startActivity(intent);
                finish();
            }
        });

        btn_lock_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOTP();
            }
        });
    }

    private void showOTP(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        final View view = inflater.inflate(R.layout.otp_show_dialog,null);
        final ImageView img_copy = view.findViewById(R.id.img_copy);
        final ImageView img_share = view.findViewById(R.id.img_share);
        final ImageView img_remove_otp = view.findViewById(R.id.img_remove_otp);
        final TextView tv_otp = view.findViewById(R.id.tv_otp);
        final TextView tv_generate_new_otp = view.findViewById(R.id.tv_generate_new_otp);
        final TextView tv_confirm_otp_changed = view.findViewById(R.id.tv_confirm_otp_changed);
        final TextView tv_close_otp_dialog = view.findViewById(R.id.tv_close_otp_dialog);
        builder.setView(view);
        final AlertDialog dialog = builder.create();
//        if (otp.length()<6){
//            tv_otp.setText("Không có mã");
//        }

        tv_otp.setText(otp);
        img_copy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("OTP Copied", tv_otp.getText().toString().trim());
                clipboard.setPrimaryClip(clip);
                Toast.makeText(MainActivity.this, OTP_SHARE_COPIED, LENGTH_SHORT).show();
            }
        });

        img_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!otp_saved){
                    Toast.makeText(MainActivity.this, OTP_SHARE_NOT_SAVE, LENGTH_SHORT).show();
                } else {
                    Intent sentIntent = new Intent(Intent.ACTION_SEND);
                    sentIntent.putExtra(Intent.EXTRA_TEXT,
                            OTP_SHARE_MESSAGE+ tv_otp.getText().toString()+OTP_SHARE_SUB_MESSAGE_2);
                    sentIntent.setType("text/plain");
                    Intent chooser = Intent.createChooser(sentIntent, "Chia sẻ OTP: ");
                    if (sentIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(chooser);
                    }
                }

            }
        });

        img_remove_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp_help.setOtp("");
                tv_otp.setText(null);
            }
        });

        tv_confirm_otp_changed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tv_otp.getText().length() != 0){
                    Toast.makeText(MainActivity.this, OTP_SHARE_SAVED, LENGTH_SHORT).show();
                    otp_saved = true;
                    otp_help.confirmOTPChanged(current_device_address);
                } else {
                    Toast.makeText(MainActivity.this, OTP_SHARE_REMOVED, LENGTH_SHORT).show();
                    otp_help.removeOTP(current_device_address);
                }

            }
        });
        tv_generate_new_otp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                otp_saved = false;
                tv_otp.setText(otp_help.generateOTP());
            }
        });

        tv_close_otp_dialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private  void displayFragment(Class fragmentName) {
        try {
            if (!isFinishing()){
                if (fragmentName != null)
                    manager.beginTransaction().replace(R.id.main_content, (Fragment) fragmentName.newInstance()).commit();
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static void unlock() {
        db_service.changeLockState(owner_phone_number,current_device_address,1);
    }

    public static void lock() {
        db_service.changeLockState(owner_phone_number,current_device_address,0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (owner_phone_number.equals(phone_login)){
            db_service.getOwnerName(phone_login,this);
            getMenuInflater().inflate(R.menu.menu_lock_setting, menu);
        } else{
            db_service.getOwnerName(phone_login,this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_remote_device:
                Intent intent = new Intent(MainActivity.this, BarcodeScannerActivity.class);
                startActivityForResult(intent, REQUEST_REMOTE_PHONE_NUMBER);
                return true;
            case R.id.menu_lock_setting:
                Toast.makeText(this, "Đang làm", LENGTH_SHORT).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_REMOTE_PHONE_NUMBER) {
            if(resultCode == Activity.RESULT_OK) {
                final String remote_phone_number = data.getStringExtra(BarcodeScannerActivity.PHONE_NUM);
                db_service.addRemoteDevices(current_device_address,remote_phone_number);
                Toast.makeText(this, "Thêm Thành Công", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLock() {
        hold = false;
        clicked = false;
        toolbar.setBackgroundColor(COLOR_LOCK);
        getWindow().setStatusBarColor(COLOR_LOCK);
        btn_door_lock.setText("UNLOCK");
        displayFragment(LockedFragment.class);

    }

    @Override
    public void onUnlock() {
        getWindow().setStatusBarColor(COLOR_UNLOCK);
        toolbar.setBackgroundColor(COLOR_UNLOCK);
        btn_door_lock.setText("LOCK");
        clicked = true;
        displayFragment(UnlockFragment.class);
    }

    @Override
    public void onGetOwnerName(String ownerName) {
        owner_name = ownerName;
    }

    @Override
    public void onHold() {
        hold = true;
        clicked = true;
        if (UnlockFragment.hold_bg == null || UnlockFragment.tv_state_lock_info == null ){
            displayFragment(UnlockFragment.class);
            btn_door_lock.setText("LOCK");
        }else {
            UnlockFragment.hold_bg.setBackgroundResource(R.drawable.background_lock_hold);
            UnlockFragment.tv_state_lock_info.setText("HOLDING");

        }
    }

    @Override
    protected void onDestroy() {
        lock();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        lock();
        super.onResume();
    }

    @Override
    protected void onPause() {
        lock();
        super.onPause();
    }
}
