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

import static com.lyoko.smartlock.Utils.LyokoString.DEVICE_OWNER;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_COVER_NAME;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_TIMESTAMP;
import static com.lyoko.smartlock.Utils.LyokoString.HISTORY_UNLOCK_TYPE;
import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_AUTH_MAC;
import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_HISTORY;
import static com.lyoko.smartlock.Utils.LyokoString.PATH_C_PHONE_NUMBER_REGISTERED;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_LOGIN;
import static com.lyoko.smartlock.Utils.LyokoString.PHONE_NUMBER;

public class Database_Service {
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    public void getHistories(final IHistory iHistory) {
        CollectionReference collection = db.collection(PATH_C_HISTORY);
        collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                final ArrayList<History> list = new ArrayList<>();
                if (e == null) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String cover_name = document.getString(HISTORY_COVER_NAME);
                        String unlock_type = document.getString(HISTORY_UNLOCK_TYPE);
                        Date date = document.getTimestamp(HISTORY_TIMESTAMP).toDate();
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

    public void checkPhoneNumber(final ICheckPhoneNumber iCheckPhoneNumber) {
        CollectionReference collection = db.collection(PATH_C_PHONE_NUMBER_REGISTERED);
        collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null) {
                    Boolean check = false;
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        if (PHONE_LOGIN.equals(document.getString(PHONE_NUMBER))&&!check){
                            check = true;
                            iCheckPhoneNumber.phoneNumExist();
                        }
                    }
                    if (!check)
                        iCheckPhoneNumber.phoneNumNotExist();

                } else {
                    Log.w("get History", "Listen failed.", e);
                    return;
                }
            }
        });
    }

    public void checkAuthenticDevice(final String address,final IFindLock iFindLock){
        CollectionReference collection = db.collection(PATH_C_AUTH_MAC);
        collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e == null){
                    for (QueryDocumentSnapshot document:queryDocumentSnapshots){
                        if (address.equalsIgnoreCase(document.getId())) {
                            final String s = document.getString(DEVICE_OWNER);
                            if (s == null){
                                iFindLock.onReadyToAddDevice(address);
                            } else {
                                if (PHONE_LOGIN == null || PHONE_LOGIN == ""){
                                    PHONE_LOGIN = "0";
                                }
                                if (PHONE_LOGIN.equalsIgnoreCase(s)) {
                                    iFindLock.onAsOwner(address);
                                } else iFindLock.onNotOwner(address);
                            }
                        }
                    }
                }
            }
        });

    }



}