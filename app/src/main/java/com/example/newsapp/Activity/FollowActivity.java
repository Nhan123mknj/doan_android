package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.AuthorAdapter;
import com.example.newsapp.Interface.IClickItemUsersListener;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.UsersViewModel;

import java.util.ArrayList;

public class FollowActivity extends AppCompatActivity {
    ImageButton backButton;
    RecyclerView followRecyclerView;
    UsersViewModel usersViewModel;
    AuthorAdapter authorAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_follow);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        String followType = intent.getStringExtra("type");
        initViews();
        setupRecyclerView(userId, followType);
        backButton.setOnClickListener(v -> finish());
    }

    private void initViews() {
        backButton = findViewById(R.id.btnBack);
        followRecyclerView = findViewById(R.id.recyclerView);


        usersViewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()).create(UsersViewModel.class);
        authorAdapter = new AuthorAdapter(new ArrayList<>(), new IClickItemUsersListener() {
            @Override
            public void onItemClickedUsers(Users users) {
                Intent intent = new Intent(FollowActivity.this, DetailAuthorActivity.class);
                intent.putExtra("userId", users.getUserId());
                startActivity(intent);
            }

            @Override
            public void onItemClickedFollow(Users users) {

            }
        });
    }
    private void setupRecyclerView(String userId, String followType) {
        followRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        followRecyclerView.setAdapter(authorAdapter);
        if (followType.equals("follower")) {
            usersViewModel.getFollowersDetail(userId).observe(this, users -> {
                if (users != null) {
                    Log.d("FollowActivity", "Received " + users.size() + " followers");
                    authorAdapter.setAuthorList(users);
                } else {
                    Log.w("FollowActivity", "No followers found");
                    authorAdapter.setAuthorList(new ArrayList<>());
                }
            });
        } else if (followType.equals("following")) {
            usersViewModel.getFollowing(userId).observe(this, users -> {
                if (users != null) {
                    Log.d("FollowActivity", "Received " + users.size() + " followed users");
                    authorAdapter.setAuthorList(users);
                } else {
                    Log.w("FollowActivity", "No followed users found");
                    authorAdapter.setAuthorList(new ArrayList<>());
                }
            });
        } else {
            Log.e("FollowActivity", "Invalid follow type: " + followType);
        }

    }
}