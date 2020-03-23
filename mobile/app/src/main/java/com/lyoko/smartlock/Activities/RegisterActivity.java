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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    //    public static final String KEY_PHONE_NUMBER = "phoneNumber";

    public static final String KEY_PASSWORD = "password";
    public static final String KEY_COVER_NAME = "covername";

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    //
    private CollectionReference collectionReference = firebaseFirestore.collection("/door/phoneNumber/list");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        et_password = findViewById(R.id.et_password);
        et_password_confirm = findViewById(R.id.et_password_confirm);
        et_CoverName = findViewById(R.id.et_CoverName);
        btn_register = findViewById(R.id.btn_register);

        Intent intent = this.getIntent();
        phoneNumber = intent.getStringExtra("phoneNumber");
// lấy phoneNumber chỗ này để push lên database
        final String phoneNumberFake = "098123123";

        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                covername = et_CoverName.getText().toString();
                password = et_password.getText().toString();
                password_confirm = et_password_confirm.getText().toString();

                if (password.equals(password_confirm)) {
                    Map<String, Object> saveInf = new HashMap<>();
                    saveInf.put("cover_name", covername);
                    saveInf.put("password", password);
                    DocumentReference documentReference = firebaseFirestore.document("/door/phoneNumber/list/" + phoneNumberFake);
                    if (documentReference.set(saveInf).isSuccessful()){
                        Toast.makeText(RegisterActivity.this, "Thanh cong", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(RegisterActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Server đang bận, bạn hãy thử lại sau!", Toast.LENGTH_SHORT).show();
                    }

                }

                registerWithPhoneNumber();

            }
        });
    }

    private void registerWithPhoneNumber() {

    }

}
