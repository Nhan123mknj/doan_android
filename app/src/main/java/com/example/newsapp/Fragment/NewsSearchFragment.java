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
import android.widget.LinearLayout;
import android.widget.Toast;

import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NewsSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NewsSearchFragment extends Fragment {
    private RecyclerView recyclerView;
    private ArticlesAdapter adapter;
    private List<Articles> articlesList = new ArrayList<>();
    private ArticlesViewModel articlesViewModel;
    private LinearLayout emptyView;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public NewsSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NewsSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NewsSearchFragment newInstance(String param1, String param2) {
        NewsSearchFragment fragment = new NewsSearchFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_news_search, container, false);
        articlesViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(ArticlesViewModel.class);
        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new ArticlesAdapter(articlesList, new IClickItemArticlesListener() {
            @Override
            public void onItemClicked(Articles articles) {
                Intent intent = new Intent(getContext(), DetailActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("articles", articles);
                intent.putExtras(bundle);
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
        articlesViewModel.getAllArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                articlesList = new ArrayList<>(articles);
                adapter.setArticlesList(articlesList);
            }
        });
        return view;
    }
    public void performSearch(String query) {
        if (articlesList == null || articlesList.isEmpty()) {
            Log.e("NewsSearchFragment", "articlesList is empty or null");
            return;
        }

        List<Articles> filteredList = new ArrayList<>();
        for (Articles article : articlesList) {
            if (article.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(article);
            }
        }

        if (filteredList.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            adapter.setArticlesList(filteredList);
        }
    }

}