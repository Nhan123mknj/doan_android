package com.example.newsapp.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Locale;

public class TTSService extends Service {
    private static final String CHANNEL_ID = "TTSServiceChannel";
    private TextToSpeech textToSpeech;
    private boolean isReading = false;
    private String contentToRead;
    private String articleTitle;

    @Override
    public void onCreate() {
        super.onCreate();
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("vi", "VN"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Toast.makeText(this, "Ngôn ngữ tiếng Việt không được hỗ trợ trên thiết bị này", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Khởi tạo TextToSpeech thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            contentToRead = intent.getStringExtra("content");
            articleTitle = intent.getStringExtra("title");
            if ("START_READING".equals(action)) {
                if (textToSpeech != null && contentToRead != null) {
                    textToSpeech.speak(contentToRead, TextToSpeech.QUEUE_FLUSH, null, "article_content");
                    isReading = true;
                }
            } else if ("PAUSE_READING".equals(action)) {
                if (textToSpeech != null && isReading) {
                    textToSpeech.stop();
                    isReading = true;
                }
            } else if ("STOP_READING".equals(action)) {
                if (textToSpeech != null) {
                    textToSpeech.stop();
                    textToSpeech.shutdown();
                    isReading = false;
                    stopSelf();
                }
            } else if ("RESUME_READING".equals(action)) {
                if (textToSpeech != null && contentToRead != null) {
                    textToSpeech.speak(contentToRead, TextToSpeech.QUEUE_FLUSH, null, "article_content");
                    isReading = true;
                }
            }
        }
        return START_STICKY;
    }

    @Nullable
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
        super.onDestroy();
    }
}