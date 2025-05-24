package com.example.newsapp.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;
import com.example.newsapp.Repository.ArticlesRepository;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;





public class DetailActivity extends AppCompatActivity {
    private Articles article;
    private ArticlesViewModel viewModel;

    TextView contentTextView,title,date,likeCount;
    ImageView thumbnail;
    ImageButton btnBack, btnLike, btnSave, btnComment;
    TextView categoryTextView, authorName;
    Context context;
    private static final int LOGIN_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail);

        initView();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        viewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);

        Intent intent = getIntent();
        intent.getExtras();
        article = (Articles) intent.getSerializableExtra("articles");

        if(article != null){
            viewModel.getArticleById(article.getArticleId()).observe(this, articles -> {
                contentTextView.setText(articles.getContent());
                title.setText(articles.getTitle());
                date.setText(articles.getPublishedAt());
                likeCount.setText(String.valueOf(article.getLikesCount()));
                Picasso.get().load(articles.getUrlToImage()).into(thumbnail);
                categoryTextView.setText(articles.getCategoryName());
                authorName.setText(articles.getUsername());
                Log.d("cateid", article.getArticleId());
            });
        }



        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String userId =user.getUid();
            viewModel.getLikeStatus(article.getArticleId(), userId).observe(this, isLiked -> {
                if (isLiked != null) {
                    btnLike.setImageResource(isLiked ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline);
                } else {
                    btnLike.setImageResource(R.drawable.ic_heart_outline);
                }
            });
        } else {
            btnLike.setImageResource(R.drawable.ic_heart_outline);
        }
        btnComment.setOnClickListener(v -> {
            Intent intent1 = new Intent(DetailActivity.this, CommentActivity.class);
            intent1.putExtra("articleId", article.getArticleId());
            startActivity(intent1);
        });
        btnLike.setOnClickListener(v -> {
            btnLike.setEnabled(false);
            viewModel.likeArticle(article.getArticleId(), DetailActivity.this, new ArticlesRepository.OnLikeResultListener() {
                @Override
                public void onSuccess(boolean isLiked) {
                    btnLike.setEnabled(true);
                    Toast.makeText(DetailActivity.this, isLiked ? "Đã thích bài viết" : "Đã bỏ thích", Toast.LENGTH_SHORT).show();
                    viewModel.getArticleById(article.getArticleId()).observe(DetailActivity.this, articles -> {
                        if (articles != null) {
                            likeCount.setText(String.valueOf(articles.getLikesCount()));
                        }
                    });
                }

                @Override
                public void onError(String errorMessage) {
                    btnLike.setEnabled(true);
                    if (errorMessage.equals("Yêu cầu đăng nhập")) {
                        Intent loginIntent = new Intent(DetailActivity.this, LoginActivity.class);
                        startActivity(loginIntent);
                    } else {
                        Toast.makeText(DetailActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        }

    private void initView() {
        title = findViewById(R.id.title);
        likeCount = findViewById(R.id.likeCount);
        date = findViewById(R.id.pubDate);
        thumbnail = findViewById(R.id.thumbnail);
        btnBack = findViewById(R.id.btnBack);
        btnLike = findViewById(R.id.btnLike);
        btnSave = findViewById(R.id.btn_save);
        contentTextView = findViewById(R.id.content);
        btnComment = findViewById(R.id.btnComment);
        categoryTextView = findViewById(R.id.categoryTextView);
        authorName = findViewById(R.id.authorName);
    }

}

