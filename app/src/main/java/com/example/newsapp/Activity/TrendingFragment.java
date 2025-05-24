package com.example.newsapp.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.example.newsapp.databinding.FragmentTrendingBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrendingFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrendingFragment extends Fragment {

    private ArticlesAdapter articlesTrendingAdapter;
    private RecyclerView recyclerView;
    private ArticlesViewModel articlesTrendingViewModel;
    private List<Articles> articlesListTrending;
    private ImageButton btnBack;
    private  static final String RSS_URL = "https://vnexpress.net/rss/tin-noi-bat.rss";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TrendingFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TrendingFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TrendingFragment newInstance(String param1, String param2) {
        TrendingFragment fragment = new TrendingFragment();
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
        View view = inflater.inflate(R.layout.fragment_trending, container, false);

        initView(view);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        articlesTrendingViewModel = new ViewModelProvider(this,new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication())).get(ArticlesViewModel.class);

        articlesListTrending = new ArrayList<>();

        articlesTrendingAdapter = new ArticlesAdapter(articlesListTrending, new IClickItemArticlesListener() {
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
        }, ArticlesAdapter.TYPE_TRENDING);

        articlesTrendingViewModel.getArticlesFeatured().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                articlesTrendingAdapter.setArticlesList(articles);
            }
        });

        btnBack.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });

        recyclerView.setAdapter(articlesTrendingAdapter);
       return view;

    }

    private void initView(View view) {
        recyclerView = view.findViewById(R.id.list_trending);
        btnBack = view.findViewById(R.id.btnBack);
    }


}