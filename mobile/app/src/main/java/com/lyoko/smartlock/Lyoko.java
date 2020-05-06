package com.lyoko.smartlock;

import android.app.Application;
import android.util.Log;

import com.lyoko.smartlock.Interface.iTimeOut;

import java.util.Timer;
import java.util.TimerTask;

public class Lyoko extends Application {
    private iTimeOut timeOut;
    private Timer timer;

    public void startLoginSession() {
        if (timer != null) timer.cancel();
        Log.d("timer", "bắt đầu");
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                timeOut.onLoginSessionTimeOut();
            }
        },120000);
    }

    public void onUserInteracted() {
        startLoginSession();
    }

    public void registerLoginSession(iTimeOut iTimeOut) {
        this.timeOut = iTimeOut;
    }
}
