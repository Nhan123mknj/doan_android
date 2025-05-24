package com.example.newsapp.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.newsapp.Fragment.AuthorSearchFragment;
import com.example.newsapp.Fragment.CategoriesSearchFragment;
import com.example.newsapp.Fragment.NewsSearchFragment;

import java.util.ArrayList;
import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new NewsSearchFragment();
            case 1:
                return new CategoriesSearchFragment();
            case 2:
                return new AuthorSearchFragment();
            default:
                return new NewsSearchFragment();
        }

    }

    @Override
    public int getItemCount() {
        return 3;
    }
}