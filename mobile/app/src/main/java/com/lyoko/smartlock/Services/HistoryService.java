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

    public static final String KEY_TITLE = "title";
    public static final String KEY_DESCRIPTION = "description";

    private TextView tvTitle, tvDescription;

    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private DocumentReference noteRef = firebaseFirestore.document("History/HistoryUser");

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_history);

        tvTitle = findViewById(R.id.tvTitle);
        tvDescription = findViewById(R.id.tvDescription);

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
                    String title = documentSnapshot.getString(KEY_TITLE);
                    String description = documentSnapshot.getString(KEY_DESCRIPTION);

//                            Map<String, Object> note = documentSnapshot.getData();

                    tvTitle.setText("Title: " + title);
                    tvDescription.setText("Description: " + description);

                }
            }
        });
    }

}
