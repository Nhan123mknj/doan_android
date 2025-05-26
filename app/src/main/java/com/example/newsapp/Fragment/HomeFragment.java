package com.example.newsapp.Fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.SnapHelper;
import androidx.recyclerview.widget.LinearSnapHelper;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Adapter.ArticlesAdapter;
import com.example.newsapp.Adapter.ViewPagerAdapter;
import com.example.newsapp.Helper.RSSUtils;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.ArticlesViewModel;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private ArticlesAdapter lastestArticlesAdapter;
    private ArticlesAdapter trendingArticlesAdapter;
    private RecyclerView list_lastest,list_trending;
    private SearchView searchView;
    private TextView watchmoretrending;
    private TextView watchmorelastest;
    private ScrollView homeContent;
    LinearLayout searchContent;
    ImageView closeSearch;
    TabLayout tabLayout;
    ViewPager2 viewPager;
    private ArticlesViewModel articlesViewModel;
    private List<Articles> trendingArticlesList = new ArrayList<>();
    private List<Articles> lastestArticlesList = new ArrayList<>();
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {

    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        initView(view);

        list_lastest.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        list_trending.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        articlesViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(getActivity().getApplication()))
                .get(ArticlesViewModel.class);


        trendingArticlesAdapter = new ArticlesAdapter(trendingArticlesList,new IClickItemArticlesListener(){

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
        }, ArticlesAdapter.TYPE_TRENDING);
        list_trending.setAdapter(trendingArticlesAdapter);


        articlesViewModel.getArticlesFeatured().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                trendingArticlesAdapter.setArticlesList(articles);
            }
        });

       watchmoretrending.setOnClickListener(v -> {
           TrendingFragment trendingFragment = new TrendingFragment();


           getParentFragmentManager().beginTransaction()
                   .replace(R.id.frame_layout, trendingFragment)
                   .addToBackStack(null)
                   .commit();
       });
        lastestArticlesAdapter = new ArticlesAdapter(lastestArticlesList, new IClickItemArticlesListener() {
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
        list_lastest.setAdapter(lastestArticlesAdapter);


        articlesViewModel.getAllArticles().observe(getViewLifecycleOwner(), articles -> {
            if (articles != null) {
                lastestArticlesAdapter.setArticlesList(articles);
            }
        });

        SnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(list_trending);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {

                homeContent.setVisibility(View.GONE);
                searchContent.setVisibility(View.VISIBLE);

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                for (Fragment fragment : getChildFragmentManager().getFragments()) {
                    if (fragment instanceof NewsSearchFragment) {
                        ((NewsSearchFragment) fragment).performSearch(newText);
                    } else if (fragment instanceof CategoriesSearchFragment) {
                        ((CategoriesSearchFragment) fragment).performSearch(newText);
                    } else if (fragment instanceof AuthorSearchFragment) {
                        ((AuthorSearchFragment) fragment).performSearch(newText);
                    }
                }
                return true;
            }
        });

        return view;
    }

    private void filterList(String newText) {
        List<Articles> filteredList = new ArrayList<>();
        for (Articles articles : lastestArticlesList) {
            if (articles.getTitle().toLowerCase().contains(newText.toLowerCase())) {
                filteredList.add(articles);
            }
        }
        if(filteredList.isEmpty()){
            Toast.makeText(getContext(), "Khong tim thay", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView(View view) {
        watchmoretrending = view.findViewById(R.id.watchmoretrending);
        watchmorelastest = view.findViewById(R.id.watchmorelastest);
        searchView = view.findViewById(R.id.searchView);
        searchView.clearFocus();
        list_lastest = view.findViewById(R.id.list_lastest);
        list_trending = view.findViewById(R.id.list_trending);
        homeContent = view.findViewById(R.id.homeContent);
        searchContent = view.findViewById(R.id.searchContent);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        // Cài đặt ViewPager và TabLayout
        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("News");
                    break;
                case 1:
                    tab.setText("Topics");
                    break;
                case 2:
                    tab.setText("Author");
                    break;
            }
        }).attach();
    }


}
