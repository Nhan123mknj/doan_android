package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;

import java.util.ArrayList;

public class ArticlesByCategoriesActivity extends AppCompatActivity implements IClickItemArticlesListener {

    private TextView categoryTitleText, categoryDescText, articlesCountText;
    private ImageButton btnBack;
    private RecyclerView rvArticles;
    private ArticlesAdapter adapter;
    private ArticlesViewModel viewModel;
    
    private Categories category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_by_categories);

        initViews();
        setupRecyclerView();
        handleIntent();
    }

    private void initViews() {
        categoryTitleText = findViewById(R.id.categoryTitleText);
        categoryDescText = findViewById(R.id.categoryDescText);
        articlesCountText = findViewById(R.id.articlesCountText);
        btnBack = findViewById(R.id.btnBack);
        rvArticles = findViewById(R.id.rvArticles);

        btnBack.setOnClickListener(v -> finish());
        viewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ArticlesAdapter(new ArrayList<>(), this, ArticlesAdapter.TYPE_TRENDING);
        rvArticles.setLayoutManager(new LinearLayoutManager(this));
        rvArticles.setAdapter(adapter);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        category = (Categories) intent.getSerializableExtra("categories");

        if (category != null) {
            categoryTitleText.setText(category.getName());
            categoryDescText.setText(category.getDescription() != null ? category.getDescription() : "Khám phá tin tức trong danh mục này");
            
            loadArticlesByCategory();
        } else {
            Log.e("ArticlesByCategoriesActivity", "No category received");
            finish();
        }
    }

    private void loadArticlesByCategory() {
        Log.d("ArticlesByCategoriesActivity", "Loading articles for category: " + category.getCategoryId());
        
        viewModel.getArticlesByCategory(category.getCategoryId()).observe(this, articles -> {
            if (articles != null) {
                Log.d("ArticlesByCategoriesActivity", "Loaded " + articles.size() + " articles for category " + category.getName());
                adapter.setArticlesList(articles);
                
                // Update articles count text
                if (articles.size() == 0) {
                    articlesCountText.setText("Chưa có bài viết nào trong danh mục này");
                } else if (articles.size() == 1) {
                    articlesCountText.setText("1 bài viết");
                } else {
                    articlesCountText.setText(articles.size() + " bài viết");
                }
            } else {
                Log.w("ArticlesByCategoriesActivity", "No articles received");
                articlesCountText.setText("Không thể tải bài viết");
            }
        });
    }

    @Override
    public void onItemClicked(Articles articles) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra("articles", articles);
        startActivity(intent);
    }

    @Override
    public void onItemClickedCategories(Categories categories) {
        // Not used here
    }

    @Override
    public void onItemClickedDelete(Articles articles) {
        // Not used here
    }

    @Override
    public void onItemClickedUpdate(Articles articles) {

    }
}