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

import com.example.newsapp.Activity.DetailActivity;
import com.example.newsapp.Adapter.CategoriesAdapter;
import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.example.newsapp.ViewModel.CategoriesViewModel;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CategoriesSearchFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CategoriesSearchFragment extends Fragment  {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private LinearLayout emptyView;
    private RecyclerView recyclerView;

    private CategoriesAdapter categoriesAdapter;
    private List<Categories> categoriesList = new ArrayList<>();
    private CategoriesViewModel categoriesViewModel;


    public CategoriesSearchFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CategoriesSearchFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CategoriesSearchFragment newInstance(String param1, String param2) {
        CategoriesSearchFragment fragment = new CategoriesSearchFragment();
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
        View view = inflater.inflate(R.layout.fragment_categories_search, container, false);

        categoriesViewModel = new ViewModelProvider(this,
                new ViewModelProvider.AndroidViewModelFactory(requireActivity().getApplication()))
                .get(CategoriesViewModel.class);

        recyclerView = view.findViewById(R.id.recyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        categoriesAdapter = new CategoriesAdapter(categoriesList, new IClickItemArticlesListener() {
            @Override
            public void onItemClicked(Articles articles) {
                    //null
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
        });
        recyclerView.setAdapter(categoriesAdapter);
        categoriesViewModel.getAllCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null) {
                Log.d("CategoriesSearchFragment", "Categories loaded: " + categories.size());
                categoriesList = categories;
                categoriesAdapter.setCategoriesList(categoriesList);
            } else {
                Log.e("CategoriesSearchFragment", "Categories is null");
            }
        });
        return view;
    }
    public void performSearch(String query) {
        if (categoriesList == null || categoriesList.isEmpty()) {
            Log.e("CategoriesSearchFragment", "categoriesList is empty or null");
            return;
        }

        List<Categories> filteredList = new ArrayList<>();
        for (Categories categories : categoriesList) {
            if (categories.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(categories);
            }
        }

        if (filteredList.isEmpty()) {
            Log.d("CategoriesSearchFragment", "No categories found for query: " + query);
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.VISIBLE);
        } else {
            Log.d("CategoriesSearchFragment", "Filtered categories: " + filteredList.size());
            recyclerView.setVisibility(View.VISIBLE);
            emptyView.setVisibility(View.GONE);
            categoriesAdapter.setCategoriesList(filteredList);
        }
    }



}