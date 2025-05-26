package com.example.newsapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link LikeNewFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class LikeNewFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticlesAdapter adapter;
    private ArticlesViewModel articlesViewModel;
    private TextView emptyTextView;

    public LikeNewFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment LikeNewFragment.
     */
    public static LikeNewFragment newInstance() {
        return new LikeNewFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_like_new, container, false);
        initView(view);
        setupRecyclerView();
        setupViewModel();
        loadLikedArticles();
        return view;
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewLikedNews);
        emptyTextView = view.findViewById(R.id.emptyTextView);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ArticlesAdapter(new ArrayList<>(), new IClickItemArticlesListener() {
            @Override
            public void onItemClicked(Articles articles) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("articles", articles);
                startActivity(intent);
            }

            @Override
            public void onItemClickedCategories(Categories categories) {
                // Not used in this context
            }

            @Override
            public void onItemClickedDelete(Articles articles) {
                // Not used in this context - users shouldn't delete liked articles
            }

            @Override
            public void onItemClickedUpdate(Articles articles) {

            }
        }, ArticlesAdapter.TYPE_LASTEST);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        articlesViewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
    }

    private void loadLikedArticles() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("LikeNewFragment", "User not logged in");
            showEmptyState();
            return;
        }

        String userId = user.getUid();
        Log.d("LikeNewFragment", "Loading saved articles for user: " + userId);
        
        // Run comprehensive debug
        articlesViewModel.debugLikedArticlesComprehensive(userId);
        
        articlesViewModel.getArticlesBookmarkedByUser(userId).observe(getViewLifecycleOwner(), articles -> {
            Log.d("LikeNewFragment", "Observer called with articles: " + (articles == null ? "null" : articles.size() + " items"));
            
            if (articles != null && !articles.isEmpty()) {
                Log.d("LikeNewFragment", "Found " + articles.size() + " saved articles");
                for (int i = 0; i < articles.size(); i++) {
                    Log.d("LikeNewFragment", "Article " + i + ": " + articles.get(i).getTitle());
                }
                adapter.setArticlesList(articles);
                showRecyclerView();
                Log.d("LikeNewFragment", "RecyclerView should be visible now");
            } else {
                Log.d("LikeNewFragment", "No saved articles found - showing empty state");
                showEmptyState();
            }
        });
    }

    private void showRecyclerView() {
        Log.d("LikeNewFragment", "showRecyclerView() called");
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        Log.d("LikeNewFragment", "RecyclerView visibility: " + recyclerView.getVisibility() + ", EmptyTextView visibility: " + emptyTextView.getVisibility());
    }

    private void showEmptyState() {
        Log.d("LikeNewFragment", "showEmptyState() called");
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("Chưa có tin tức đã lưu");
        Log.d("LikeNewFragment", "RecyclerView visibility: " + recyclerView.getVisibility() + ", EmptyTextView visibility: " + emptyTextView.getVisibility());
    }
}