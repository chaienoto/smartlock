package com.lyoko.smartlock.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firestore.v1.WriteResult;
import com.lyoko.smartlock.Models.History;
import com.lyoko.smartlock.R;
import com.lyoko.smartlock.Services.IHistory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

//    private static final String TAG = "MainActivity";
//    public static final String KEY_COVERNAME = "cover_name";
//    public static final String KEY_UNLOCKTYPE = "unlock_tyoe";
//    public static final String KEY_TIME = "time";

    ArrayList<History> list = new ArrayList<>();

    ImageView img_find;
    Button btn_door_lock, btn_lock_otp, btn_lock_history;
    RelativeLayout main_bg;
    TextView tv_lh, tv_otp, tv_find_info, tv_state_lock_info;
    Toolbar toolbar;
    private boolean lock_found = false;
    //    false : close | true: open
    private boolean door_state = false;
    int lock_color = Color.parseColor("#f95843");
    int unlock_color = Color.parseColor("#2ecc71");

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    CollectionReference collection = db.collection("/door/history/files");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UIRegister();
        setSupportActionBar(toolbar);

        btn_door_lock.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (find_lock()) {
                    setToFixWRT();
                }
                return true;
            }
        });
        setToFixWRT();

        btn_lock_history.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, HistoryActivity.class);
                startActivity(i);
            }
        });


    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    private void UIRegister() {
        main_bg = findViewById(R.id.main_bg);
        toolbar = findViewById(R.id.toolbar);
        img_find = findViewById(R.id.img_find);
        btn_door_lock = findViewById(R.id.btn_door_lock);
        btn_lock_otp = findViewById(R.id.btn_lock_otp);
        btn_lock_history = findViewById(R.id.btn_lock_history);
        tv_lh = findViewById(R.id.tv_lh);
        tv_otp = findViewById(R.id.tv_otp);
        tv_find_info = findViewById(R.id.tv_find_info);
        tv_state_lock_info = findViewById(R.id.tv_state_lock_info);


    }

    private boolean find_lock() {

        return true;
    }

    @SuppressLint("ResourceAsColor")
    private void setToFixWRT() {
        if (!door_state) {
            door_state = true;
            toolbar.setBackgroundColor(lock_color);
            getWindow().setStatusBarColor(lock_color);
            btn_door_lock.setText("UNLOCK");
            tv_state_lock_info.setText("LOCKED");
            tv_state_lock_info.setTextColor(lock_color);
            main_bg.setBackgroundResource(R.drawable.lock_background);
            img_find.setBackgroundResource(R.drawable.ic_locked);
        } else {
            door_state = false;
            toolbar.setBackgroundColor(unlock_color);
            btn_door_lock.setText("LOCK");
            tv_state_lock_info.setText("UNLOCKED");
            tv_state_lock_info.setTextColor(unlock_color);
            getWindow().setStatusBarColor(unlock_color);
            main_bg.setBackgroundResource(R.drawable.unlock_background);
            img_find.setBackgroundResource(R.drawable.ic_unlocked);
        }

    }

    public void saveHistory(View view) {
//        String cover_name = btn_door_lock.getText().toString();
//        String time = btn_door_lock.getText().toString();
////        String unlock_type = btn_door_lock.getText().toString();
////
////        final Map<String, Object> HistoryInf = new HashMap<>();
////        HistoryInf.put(KEY_COVERNAME, cover_name);
////        HistoryInf.put(KEY_TIME, time);
////        HistoryInf.put(KEY_UNLOCKTYPE, unlock_type);
////
////        DocumentReference docRef = db.collection("files").document("time");
////// Update the timestamp field with the value from the server
////        ApiFuture<WriteResult> writeResult = docRef.update("timestamp", FieldValue.serverTimestamp());
////        System.out.println("Update time : " + writeResult.get());

//        list.clear();
//
//        Map<String, Object> sHis = new HashMap<>();
//        sHis.put("cover_name", .getText().toString());
//        house.put("house_phone", house_phone.getText().toString());
//        house.put("house_address", house_address.getText().toString());
//
//        UUID hID = UUID.randomUUID();
//        list.add()

    }


}
