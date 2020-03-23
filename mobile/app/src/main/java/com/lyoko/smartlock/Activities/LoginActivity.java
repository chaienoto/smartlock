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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.lyoko.smartlock.R;

import static com.lyoko.smartlock.Utils.LyokoString.PASSWORD;
import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_PHONE_NUMBER_REGISTERED;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_LOGIN;

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

        final String passwordFake = "123456";

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DocumentReference documentReference = firebaseFirestore.document(PATH_C_PHONE_NUMBER_REGISTERED + "/" + PHONE_LOGIN);
                loginPassword = et_login_password.getText().toString();
                documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        String password = documentSnapshot.getString(PASSWORD);

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
