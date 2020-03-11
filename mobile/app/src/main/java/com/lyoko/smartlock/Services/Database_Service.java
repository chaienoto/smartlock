package com.lyoko.smartlock.Services;

import android.util.Log;
import androidx.annotation.Nullable;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.lyoko.smartlock.Models.History;
import java.util.ArrayList;
import java.util.Date;

public class Database_Service {
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getHistories(final IHistory iHistory) {

        CollectionReference collection = db.collection("/door/history/files");
        collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                final ArrayList<History> list = new ArrayList<>();
                if (e == null) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cover_name = document.getString("cover_name");
                        String unlock_type = document.getString("unlock_type");
                        Date date = document.getTimestamp("time").toDate();
                        list.add(new History(cover_name, date, unlock_type));
                    }
                    iHistory.show_history(list);
                } else {
                    Log.w("get History", "Listen failed.", e);
                    return;
                }

            }

        });
    }

}