package com.example.newsapp.Adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class SettingAdapter extends RecyclerView.Adapter<SettingAdapter.SettingViewHolder> {

    @NonNull
    @Override
    public SettingAdapter.SettingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull SettingAdapter.SettingViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
    public class SettingViewHolder extends RecyclerView.ViewHolder {
        public SettingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}
