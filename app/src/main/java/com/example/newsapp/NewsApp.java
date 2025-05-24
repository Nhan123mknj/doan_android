package com.example.newsapp;

import android.app.Application;

import com.cloudinary.android.MediaManager;
import com.google.firebase.FirebaseApp;

import java.util.HashMap;
import java.util.Map;

public class NewsApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        Map config = new HashMap();
        config.put("cloud_name", "dsnsgdqkb");
        config.put("api_key", "976173493414541");
        config.put("api_secret", "EsJ_MekMzCxJjF9A6b9ntsGB5kw");
        MediaManager.init(this, config);
    }
}
