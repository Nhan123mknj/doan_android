package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.newsapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class SettingActivity extends AppCompatActivity {
    LinearLayout logout, changePass, sendMail;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setting);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initViews();
        setupClickListeners();
    }

    private void initViews() {
        logout = findViewById(R.id.logout);
        btnBack = findViewById(R.id.btnBack);
        changePass = findViewById(R.id.changePass);
        sendMail = findViewById(R.id.sendMail);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        
        sendMail.setOnClickListener(v -> {
            Intent intent = new Intent(this, SendMailActivity.class);
            startActivity(intent);
        });
        
        changePass.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePassActivity.class);
            startActivity(intent);
        });
        
        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}