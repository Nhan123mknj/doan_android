package com.example.newsapp.Services;

import static com.example.newsapp.NewsApp.CHANNEL_ID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Helper.MyReceiver;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.Locale;

public class TSSServices extends Service {

    private TextToSpeech textToSpeech;
    public static final int ACTION_START = 0;
    public static final int ACTION_PAUSE = 1;
    public static final int ACTION_RESUME = 2;
    public static final int ACTION_CLEAR = 3;
    private String content;
    private Articles mArticle;
    private boolean isReading = false;
    private int pendingAction = -1;
    private boolean isTTSReady = false;
    private Target picassoTarget;
    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ tiếng Việt không được hỗ trợ trên thiết bị này", Toast.LENGTH_SHORT).show();
                } else {
                    isTTSReady = true;
                    if (pendingAction != -1) {
                        try {
                            handleAction(pendingAction);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        pendingAction = -1;
                    }
                }
            } else {
                Toast.makeText(this, "Khởi tạo TextToSpeech thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_NOT_STICKY;

        int actionReading = intent.getIntExtra("actionService", -1);
        Bundle bundle = intent.getExtras();

        if (bundle != null && bundle.containsKey("article")) {
            mArticle = (Articles) bundle.getSerializable("article");
            content = mArticle != null ? mArticle.getContent() : "";
        }

        if (textToSpeech == null) {
            pendingAction = actionReading;
        } if (isTTSReady) {
            try {
                handleAction(actionReading);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            pendingAction = actionReading;
        }

        return START_STICKY;
    }



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        stopSelf();
        super.onDestroy();
    }

    private void handleAction(int action) throws IOException {
        switch (action) {
            case ACTION_START:
                startReading();
                break;
            case ACTION_PAUSE:
                pauseReading();
                break;
            case ACTION_RESUME:
                resumeReading();
                break;
            case ACTION_CLEAR:
                if (textToSpeech != null) {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                    isReading = false;
                    stopForeground(true);
                    stopSelf();
                    sendActionToActivity(ACTION_CLEAR);
                }
                break;
            default:
                Log.w("TSSServices", "Hành động không hợp lệ: " + action);
        }
    }

    private void startReading() throws IOException {
        if (textToSpeech != null && content != null) {
            textToSpeech.stop();
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "article_content");
            isReading = true;
            sendActionToActivity(ACTION_START);
            sendNotification(mArticle);
        }
    }

    private void resumeReading() throws IOException {
        if (textToSpeech != null && content != null) {
            textToSpeech.speak(content, TextToSpeech.QUEUE_FLUSH, null, "article_content");
            isReading = true;
            sendNotification(mArticle);
            sendActionToActivity(ACTION_RESUME);
        }
    }

    private void pauseReading() throws IOException {
        if (textToSpeech != null && isReading) {
            textToSpeech.stop();
            isReading = false;
            sendNotification(mArticle);
            sendActionToActivity(ACTION_PAUSE);
        }
    }

    private void sendNotification(Articles article) throws IOException {
        Intent intent = new Intent(this, DetailActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.player_notification);
        remoteViews.setTextViewText(R.id.title_notification, article.getTitle());

        // Gắn nút click ở đây
        if (isReading) {
            remoteViews.setOnClickPendingIntent(R.id.miniPlayerPlayPause, getPendingIntent(this, ACTION_PAUSE));
            remoteViews.setImageViewResource(R.id.miniPlayerPlayPause, R.drawable.pause_icon);
        } else {
            remoteViews.setOnClickPendingIntent(R.id.miniPlayerPlayPause, getPendingIntent(this, ACTION_RESUME));
            remoteViews.setImageViewResource(R.id.miniPlayerPlayPause, R.drawable.play_ic);
        }
        remoteViews.setOnClickPendingIntent(R.id.miniPlayerStop, getPendingIntent(this, ACTION_CLEAR));

        // Load ảnh
        if (article.getUrlToImage() == null || article.getUrlToImage().isEmpty()) {
            remoteViews.setImageViewResource(R.id.notification_image, R.drawable.img_trending);
            buildAndShowNotification(remoteViews, pendingIntent);
        } else {
            picassoTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    remoteViews.setImageViewBitmap(R.id.notification_image, bitmap);
                    buildAndShowNotification(remoteViews, pendingIntent);
                }

                @Override
                public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                    Log.e("Picasso", "Tải ảnh thất bại: " + e.getMessage());
                    remoteViews.setImageViewResource(R.id.notification_image, R.drawable.img_trending);
                    buildAndShowNotification(remoteViews, pendingIntent);
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            };

            Picasso.get().load(article.getUrlToImage()).into(picassoTarget);
        }
    }


    @SuppressLint("ForegroundServiceType")
    private void buildAndShowNotification(RemoteViews remoteViews, PendingIntent pendingIntent) {
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.icon)
                .setContentIntent(pendingIntent)
                .setCustomContentView(remoteViews)
                .setCustomBigContentView(remoteViews)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
    }

    private PendingIntent getPendingIntent(Context context, int action) {
        Intent intent = new Intent(this, MyReceiver.class);
        intent.putExtra("action_reading", action);
        return PendingIntent.getBroadcast(context.getApplicationContext(), action, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    private void sendActionToActivity(int action) {
       Intent intent = new Intent("send_action_to_activity");
       Bundle bundle = new Bundle();
       bundle.putSerializable("articleObj", mArticle);
       bundle.putBoolean("isReading", isReading);
       bundle.putInt("action_reading", action);

       intent.putExtras(bundle);

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(intent);
    }
}