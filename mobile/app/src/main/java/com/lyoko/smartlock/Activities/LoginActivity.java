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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.lyoko.smartlock.R;

public class LoginActivity extends AppCompatActivity {
    EditText et_login_password;
    TextView tv_change_login_phoneNumber;
    Button btn_login;
    String loginPassword;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        btn_login = findViewById(R.id.btn_login);
        et_login_password = findViewById(R.id.et_login_password);
        tv_change_login_phoneNumber = findViewById(R.id.tv_change_login_phoneNumber);

        final String phoneNumberFake = "098123123";
        final String passwordFake = "123456";

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference documentReference = firebaseFirestore.document("/door/phoneNumber/list/" + phoneNumberFake);
                loginPassword = et_login_password.getText().toString();



                documentReference.addSnapshotListener(LoginActivity.this, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        String password = documentSnapshot.getString("password");

                        //check password với pass trên database
                        // nếu đúng thì chuyển qua MainActivity
                        if (loginPassword.equals(password)) {
                            Log.d("Tag", password);
                            Toast.makeText(LoginActivity.this, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        } else {
                            Toast.makeText(LoginActivity.this, "Server đang bận, bạn hãy thử lại sau", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}
