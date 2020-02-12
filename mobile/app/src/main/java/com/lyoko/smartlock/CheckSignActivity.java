package com.lyoko.smartlock;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class CheckSignActivity extends AppCompatActivity {

    public EditText etSDT;
    public Button btnTiepTuc;
//    private DialogLoginListener listener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_sign);

        etSDT = findViewById(R.id.etSDT);
        btnTiepTuc = findViewById(R.id.btnTiepTuc);
        btnTiepTuc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sdt = etSDT.getText().toString();
                openDialog(sdt);
            }
        });

    }



    public void openDialog(String a){

        SignUpDialog dialogLogin = new SignUpDialog();
        Bundle bundle = new Bundle();
        bundle.putString("SDT",a);
        dialogLogin.setArguments(bundle);
        dialogLogin.show((CheckSignActivity.this).getSupportFragmentManager(), "thong bao");


    }


//    public interface DialogLoginListener{
//        void applyTexts(String sdt);
//    }
}
