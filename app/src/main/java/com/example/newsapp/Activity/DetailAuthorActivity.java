package com.example.newsapp.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Helper.DialogLogin;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.Model.Users;
import com.example.newsapp.R;
import com.example.newsapp.Repository.UserRespository;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.UsersViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailAuthorActivity extends AppCompatActivity {
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TextView name, email, phone,tvDescription,newsCount;
    private CircleImageView avatar;
    private RecyclerView recyclerView;
    private Button btnBack,followButton;

    private UsersViewModel usersViewModel;
    private ArticlesViewModel articlesViewModel;
    private ArticlesAdapter adapter;
    FirebaseUser userAuth = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detail_author2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.author_detail_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Users user = (Users) getIntent().getSerializableExtra("users");
        initView();
        adapter = new ArticlesAdapter(new ArrayList<>(), new IClickItemArticlesListener() {
            @Override
            public void onItemClicked(Articles articles) {
                Intent intent = new Intent(DetailAuthorActivity.this, DetailActivity.class);
                intent.putExtra("articles", articles);
                startActivity(intent);
            }

            @Override
            public void onItemClickedCategories(Categories categories) {

            }

            @Override
            public void onItemClickedDelete(Articles articles) {

            }

            @Override
            public void onItemClickedUpdate(Articles articles) {

            }
        }, ArticlesAdapter.TYPE_LASTEST);
        recyclerView.setAdapter(adapter);
        usersViewModel = new ViewModelProvider(this).get(UsersViewModel.class);
        articlesViewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);

        showUserInformation(user);
        showArticlesByUsers(user);
        followButton.setOnClickListener(v -> {
            if (user == null) {
                mainHandler.post(() -> DialogLogin.openDialogLogin(
                        this,
                        Gravity.CENTER,
                        () -> {
                            Intent loginIntent = new Intent(this, LoginActivity.class);
                            ((Activity) this).startActivityForResult(loginIntent, 100);
                        }
                ));
                return;
            }
            usersViewModel.toggleFollowUser(userAuth.getUid(), user.getUserId(), isFollowing -> {
                if (isFollowing) {
                    followButton.setText("Đã theo dõi");
                } else {
                    followButton.setText("Theo dõi");
                }
            });
        });
    }

    private void showArticlesByUsers(Users user) {

        if(user == null){
            Log.d("ProfileFragment", "User is null");
        }
        articlesViewModel.getArticlesByAuthor(user.getUserId()).observe(this, articles -> {
            if (articles != null && !articles.isEmpty()) {
                Log.d("ProfileFragment", "Articles fetched: " + articles.size());
                adapter.setArticlesList(articles);
            } else {
                Log.d("ProfileFragment", "No articles found for user: " + user.getUserId());
            }
        });
    }

    private void showUserInformation(Users user) {

        if (user == null) return;

        usersViewModel.getUserById(user.getUserId()).observe(this, userObj -> {
            if(userObj == null) return;
            name.setText(userObj.getName());
            email.setText(userObj.getEmail());
            tvDescription.setText(userObj.getDescription());
            Picasso.get().load(userObj.getAvatarUrl()).into(avatar);
            newsCount.setText(String.valueOf(userObj.getCountViews()));
        });
        usersViewModel.checkFollowStatus(userAuth.getUid(), user.getUserId(), new UserRespository.OnFollowStatusChangedListener() {

            @Override
            public void onChanged(boolean isFollowing) {
                if (isFollowing) {
                    followButton.setText("Đã theo dõi");
                } else {
                    followButton.setText("Theo dõi");
                }
            }
        });
        btnBack.setOnClickListener(v -> finish());
    }

    private void initView() {
        name = findViewById(R.id.username);
        email = findViewById(R.id.tvEmail);
        newsCount = findViewById(R.id.newsCount);
        avatar = findViewById(R.id.profileImage);
        btnBack = findViewById(R.id.btnBack);
        tvDescription = findViewById(R.id.tvDescription);
        recyclerView = findViewById(R.id.rvNews);
        followButton = findViewById(R.id.followButton);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

    }
}