package com.lyoko.smartlock;


import android.content.Intent;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.lyoko.smartlock.Activities.SplashActivity;

public class LyokoMessage extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Intent intent = new Intent(this, SplashActivity.class);
        startActivity(intent);
    }
}
