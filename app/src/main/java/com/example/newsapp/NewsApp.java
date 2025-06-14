package com.example.newsapp;

import android.app.Application;
import android.os.Build;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class NewsApp extends Application {

    public static final String CHANNEL_ID = "TTSServiceChannel";
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Map config = new HashMap();
        config.put("cloud_name", "dsnsgdqkb");
        config.put("api_key", "976173493414541");
        config.put("api_secret", "EsJ_MekMzCxJjF9A6b9ntsGB5kw");
        MediaManager.init(this, config);

        createChannelNoitification();
    }

    private void createChannelNoitification() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL_ID,
                    "TTS Service Channel",
                    android.app.NotificationManager.IMPORTANCE_DEFAULT
            );

            android.app.NotificationManager manager = getSystemService(android.app.NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
