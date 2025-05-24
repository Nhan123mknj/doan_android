package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.CommentAdapter;
import com.example.newsapp.Model.Comments;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.CommentViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class CommentActivity extends AppCompatActivity {
    ImageButton btnBack, btnSend;
    EditText etComment;
    RecyclerView rvComments;

    CommentAdapter commentAdapter;
    CommentViewModel commentViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        rvComments = findViewById(R.id.rvComments);
        btnBack = findViewById(R.id.btnBack);
        btnSend = findViewById(R.id.btnSend);
        etComment = findViewById(R.id.etComment);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = getIntent();
        String articleId = intent.getStringExtra("articleId");
        btnBack.setOnClickListener(v -> finish());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rvComments.setLayoutManager(linearLayoutManager);

        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);

        commentAdapter = new CommentAdapter( new ArrayList<>());
        rvComments.setLayoutManager(new LinearLayoutManager(this));
        rvComments.setAdapter(commentAdapter);
        commentViewModel.getCommentsByArticleId(articleId).observe(this, comments -> {
            if (comments != null) {
                commentAdapter.setComments(comments);
                Log.d("CommentActivity", "Đã tải bình luận: " + comments.size());
            } else {
                commentAdapter.setComments(new ArrayList<>());
                Log.w("CommentActivity", "Không nhận được bình luận");
            }
        });

        btnSend.setOnClickListener(v -> {


            if (user == null) {
                Toast.makeText(this, "Vui lòng đăng nhập để bình luận", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, LoginActivity.class));
                return;
            }
            String content = etComment.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Nội dung không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }
            Comments comment = new Comments();
            comment.setArticles_id(articleId);
            String username = user.getDisplayName();
            try {
                comment.setUserId(user.getUid() != null ? user.getUid() : "anonymous");
                comment.setUsername(username);
                comment.setProfilePicUrl(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : "");
            } catch (Exception e) {
                Log.e("CommentActivity", "Lỗi khi lấy thông tin người dùng: " + e.getMessage(), e);
                Toast.makeText(this, "Lỗi thông tin người dùng", Toast.LENGTH_SHORT).show();
                return;
            }
            comment.setContent(content);
            comment.setTimestamp(System.currentTimeMillis());
            Log.d("CommentActivity", "Gửi bình luận: articleId=" + articleId + ", content=" + content);
            commentViewModel.addComment(articleId, comment);
            etComment.setText("");
            Toast.makeText(this, "Đang gửi bình luận", Toast.LENGTH_SHORT).show();
        });
    }
}