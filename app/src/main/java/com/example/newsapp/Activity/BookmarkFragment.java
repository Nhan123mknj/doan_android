package com.example.newsapp.Activity;

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
 * Use the {@link BookmarkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BookmarkFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArticlesAdapter adapter;
    private ArticlesViewModel articlesViewModel;
    private TextView emptyTextView;

    public BookmarkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment BookmarkFragment.
     */
    public static BookmarkFragment newInstance() {
        return new BookmarkFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bookmark, container, false);
        initView(view);
        setupRecyclerView();
        setupViewModel();
        loadBookmarkedArticles();
        return view;
    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.recyclerViewBookmarkedNews);
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
                // Not used in this context - users shouldn't delete bookmarked articles
            }
        }, ArticlesAdapter.TYPE_LASTEST);
        recyclerView.setAdapter(adapter);
    }

    private void setupViewModel() {
        articlesViewModel = new ViewModelProvider(this).get(ArticlesViewModel.class);
    }

    private void loadBookmarkedArticles() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Log.d("BookmarkFragment", "User not logged in");
            showEmptyState();
            return;
        }

        String userId = user.getUid();
        articlesViewModel.getArticlesBookmarkedByUser(userId).observe(getViewLifecycleOwner(), articles -> {
            if (articles != null && !articles.isEmpty()) {
                Log.d("BookmarkFragment", "Bookmarked articles loaded: " + articles.size());
                adapter.setArticlesList(articles);
                showRecyclerView();
            } else {
                Log.d("BookmarkFragment", "No bookmarked articles found");
                showEmptyState();
            }
        });
    }

    private void showRecyclerView() {
        recyclerView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyTextView.setText("Chưa có bài viết đã lưu");
    }
}