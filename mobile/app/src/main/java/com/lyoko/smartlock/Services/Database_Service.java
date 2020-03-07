package com.lyoko.smartlock.Services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lyoko.smartlock.Models.History;

import java.util.ArrayList;

public class Database_Service {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getHistories(final IHistory iHistory) {

        CollectionReference collection = db.collection("/door/history/files");
        collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                final ArrayList<History> list = new ArrayList<>();
                if (e == null) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots ) {
                        String cover_name = document.getString("cover_name");
                        String unlock_type = document.getString("unlock_type");
                        Boolean state = document.getBoolean("state");
                        Timestamp time = document.getTimestamp("time");
                        list.add(new History(cover_name,state,time,unlock_type));
                    }
                    iHistory.show_history(list);
//                    list.clear();
                } else{
                    Log.w("get History", "Listen failed.", e);
                    return;
                }

            }
        });

    }

};

