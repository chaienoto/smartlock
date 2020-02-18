package com.lyoko.smartlock.Services;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.lyoko.smartlock.R;

public class HistoryService extends AppCompatActivity {

    private static final String TAG = "HistoryService";

    public static final String KEY_COVERNAME = "cover_name";
    public static final String KEY_STATE= "state";
    public static final String KEY_TIME = "time";
    public static final String KEY_UNLOCKTYPE = "unlocktype";

    private TextView tvCover_Name, tvState, tvTime, tvUnlock_Type;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = firebaseFirestore.document("/door/history/files");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_history);

        tvCover_Name = findViewById(R.id.tvCover_Name);
        tvState = findViewById(R.id.tvState);
        tvTime = findViewById(R.id.tvTime);
        tvUnlock_Type = findViewById(R.id.tvUnlock_Type);

    }

    @Override
    protected void onStart() {
        super.onStart();

        //lay du lieu
        noteRef.addSnapshotListener(this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@javax.annotation.Nullable DocumentSnapshot documentSnapshot, @javax.annotation.Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    Toast.makeText(HistoryService.this, "Error Loading", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, e.toString());
                    return;
                }
                if (documentSnapshot.exists()) {
                    String Cover_Name = documentSnapshot.getString(KEY_COVERNAME);
                    String State = documentSnapshot.getString(KEY_STATE);
                    String Time = documentSnapshot.getString(KEY_TIME);
                    String UnlockType = documentSnapshot.getString(KEY_UNLOCKTYPE);

//                            Map<String, Object> note = documentSnapshot.getData();

                    tvCover_Name.setText("Title: " + Cover_Name);
                    tvState.setText("Description: " + State);
                    tvTime.setText("Title: " + Time);
                    tvUnlock_Type.setText("Description: " + UnlockType);

                }
            }
        });
    }

}
