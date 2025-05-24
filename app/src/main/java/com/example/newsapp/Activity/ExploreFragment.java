package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Adapter.CategoriesAdapter;
import com.example.newsapp.Adapter.TrendingAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.ViewModel.CategoriesViewModel;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment implements IClickItemArticlesListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private EditText searchEditText;
    private RecyclerView rvCategories, rvTrendingArticles, rvRecommendedArticles;
    private TextView tvSeeAllTrending, tvSeeAllRecommended;
    
    private CategoriesAdapter categoriesAdapter;
    private TrendingAdapter trendingAdapter;
    private ArticlesAdapter recommendedAdapter;
    
    private CategoriesViewModel categoriesViewModel;
    private ArticlesViewModel articlesViewModel;

    public ExploreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ExploreFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize ViewModels
        categoriesViewModel = new ViewModelProvider(this).get(CategoriesViewModel.class);
        articlesViewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);
        
        initViews(view);
        setupRecyclerViews();
        setupSearch();
        setupClickListeners();
        loadData();
        
        return view;
    }

    private void initViews(View view) {
        searchEditText = view.findViewById(R.id.searchEditText);
        rvCategories = view.findViewById(R.id.rvCategories);
        rvTrendingArticles = view.findViewById(R.id.rvTrendingArticles);
        rvRecommendedArticles = view.findViewById(R.id.rvRecommendedArticles);
        tvSeeAllTrending = view.findViewById(R.id.tvSeeAllTrending);
        tvSeeAllRecommended = view.findViewById(R.id.tvSeeAllRecommended);
    }

    private void setupRecyclerViews() {
        // Categories RecyclerView
        categoriesAdapter = new CategoriesAdapter(new ArrayList<>(), this);
        rvCategories.setLayoutManager(new LinearLayoutManager(getContext()));
        rvCategories.setAdapter(categoriesAdapter);

        // Trending Articles RecyclerView (Horizontal)
        trendingAdapter = new TrendingAdapter(new ArrayList<>(), this);
        LinearLayoutManager trendingLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        rvTrendingArticles.setLayoutManager(trendingLayoutManager);
        rvTrendingArticles.setAdapter(trendingAdapter);

        // Recommended Articles RecyclerView - Use correct constructor
        recommendedAdapter = new ArticlesAdapter(new ArrayList<>(), this, ArticlesAdapter.TYPE_TRENDING);
        rvRecommendedArticles.setLayoutManager(new LinearLayoutManager(getContext()));
        rvRecommendedArticles.setAdapter(recommendedAdapter);
    }

    private void setupSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (!query.isEmpty()) {
                    searchArticles(query);
                } else {
                    // Show recommended articles when search is empty
                    loadRecommendedArticles();
                }
            }
        });

        // Handle Enter key press to open full search results
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                Intent intent = new Intent(getContext(), SearchResultActivity.class);
                intent.putExtra("query", query);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }

    private void setupClickListeners() {
        tvSeeAllTrending.setOnClickListener(v -> {
            // Navigate to trending articles activity
            Intent intent = new Intent(getContext(), SearchResultActivity.class);
            intent.putExtra("search_type", "trending");
            startActivity(intent);
        });

        tvSeeAllRecommended.setOnClickListener(v -> {
            // Navigate to all articles activity
            Intent intent = new Intent(getContext(), SearchResultActivity.class);
            intent.putExtra("search_type", "all");
            startActivity(intent);
        });
    }

    private void loadData() {
        loadCategories();
        loadTrendingArticles();
        loadRecommendedArticles();
    }

    private void loadCategories() {
        categoriesViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Log.d("ExploreFragment", "Loaded " + categories.size() + " categories");
                categoriesAdapter.setCategoriesList(categories);
            }
        });
    }

    private void loadTrendingArticles() {
        articlesViewModel.getTrendingArticles(10).observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                Log.d("ExploreFragment", "=== TRENDING ARTICLES DEBUG ===");
                Log.d("ExploreFragment", "Loaded " + articles.size() + " trending articles");
                for (Articles article : articles) {
                    Log.d("ExploreFragment", "Trending: " + article.getTitle() + " - Likes: " + article.getLikesCount());
                }
                trendingAdapter.setTrendingArticles(articles);
            } else {
                Log.w("ExploreFragment", "No trending articles received");
            }
        });
    }

    private void loadRecommendedArticles() {
        articlesViewModel.getArticlesFeatured().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                Log.d("ExploreFragment", "Loaded " + articles.size() + " recommended articles");
                recommendedAdapter.setArticlesList(articles);
            }
        });
    }

    private void searchArticles(String query) {
        Log.d("ExploreFragment", "Searching for: " + query);
        articlesViewModel.searchArticles(query).observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                Log.d("ExploreFragment", "Found " + articles.size() + " search results");
                recommendedAdapter.setArticlesList(articles);
            }
        });
    }

    @Override
    public void onItemClicked(Articles articles) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra("articles", articles);
        startActivity(intent);
    }

    @Override
    public void onItemClickedCategories(Categories categories) {
        Intent intent = new Intent(getContext(), ArticlesByCategoriesActivity.class);
        intent.putExtra("categories", categories);
        startActivity(intent);
    }

    @Override
    public void onItemClickedDelete(Articles articles) {
        // Not used in explore fragment
    }
}