package com.example.newsapp.Helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.newsapp.Services.TSSServices;

public class MyReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        int actionReading = intent.getIntExtra("action_reading", 0);

        Intent serviceIntent = new Intent(context, TSSServices.class);
        serviceIntent.putExtra("actionService", actionReading);
        context.startService(serviceIntent);
    }
}
