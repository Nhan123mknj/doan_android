package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
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

public class SearchResultActivity extends AppCompatActivity implements IClickItemArticlesListener {
    
    private TextView titleText, resultCountText;
    private ImageButton btnBack;
    private RecyclerView recyclerView;
    private ArticlesAdapter adapter;
    private ArticlesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        initViews();
        setupRecyclerView();
        handleIntent();
    }

    private void initViews() {
        titleText = findViewById(R.id.titleText);
        resultCountText = findViewById(R.id.resultCountText);
        btnBack = findViewById(R.id.btnBack);
        recyclerView = findViewById(R.id.recyclerView);

        btnBack.setOnClickListener(v -> finish());
        viewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
    }

    private void setupRecyclerView() {
        adapter = new ArticlesAdapter(new ArrayList<>(), this, ArticlesAdapter.TYPE_TRENDING);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private void handleIntent() {
        Intent intent = getIntent();
        String searchType = intent.getStringExtra("search_type");
        String query = intent.getStringExtra("query");

        if ("trending".equals(searchType)) {
            titleText.setText("Xu hướng");
            loadTrendingArticles();
        } else if ("all".equals(searchType)) {
            titleText.setText("Tất cả bài viết");
            loadAllArticles();
        } else if (query != null) {
            titleText.setText("Kết quả tìm kiếm");
            searchArticles(query);
        }
    }

    private void loadTrendingArticles() {
        viewModel.getTrendingArticles(50).observe(this, articles -> {
            if (articles != null) {
                adapter.setArticlesList(articles);
                resultCountText.setText(articles.size() + " bài viết xu hướng");
            }
        });
    }

    private void loadAllArticles() {
        viewModel.getAllArticles().observe(this, articles -> {
            if (articles != null) {
                adapter.setArticlesList(articles);
                resultCountText.setText(articles.size() + " bài viết");
            }
        });
    }

    private void searchArticles(String query) {
        viewModel.searchArticles(query).observe(this, articles -> {
            if (articles != null) {
                adapter.setArticlesList(articles);
                resultCountText.setText("Tìm thấy " + articles.size() + " kết quả cho \"" + query + "\"");
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