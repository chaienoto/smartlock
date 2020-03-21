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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.UserData;
import com.lyoko.smartlock.R;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    EditText et_password, et_password_confirm, et_CoverName;
    Button btn_register;
    String phoneNumber, password, password_confirm, covername;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_CoverName = findViewById(R.id.et_CoverName);
        btn_register = findViewById(R.id.btn_register);

        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber"); // lấy phoneNumber chỗ này để push lên database

        final String phoneNumberFake = "112233445";

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                covername = et_CoverName.getText().toString();
                password = et_password.getText().toString();
                password_confirm = et_password_confirm.getText().toString();

                if (password.equals(password_confirm)) {
                    Map<String, Object> data = new HashMap<>();
                    data.put("cover_name", covername);
                    data.put("password", password);
                    registerWithPhoneNumber(phoneNumber,data);

                }
            }
        });
    }

    private void registerWithPhoneNumber(String phoneNumber, Map<String, Object> data ) {
        DocumentReference documentReference = db.document("/door/phoneNumber/list/" + phoneNumber );
        documentReference.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(RegisterActivity.this, "Đăng Kí Thành Công", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                startActivity(i);
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(RegisterActivity.this, "Server đang bận, bạn hãy thử lại sau!", Toast.LENGTH_SHORT).show();
            }
        });

    }

}
