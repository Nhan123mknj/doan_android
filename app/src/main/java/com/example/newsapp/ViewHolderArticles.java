package com.example.newsapp;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolderArticles extends RecyclerView.ViewHolder {
    public ImageView thumbnail;
    public TextView title, publishedAt;
    public ViewHolderArticles(@NonNull View itemView) {
        super(itemView);
        title = itemView.findViewById(R.id.title);
        publishedAt = itemView.findViewById(R.id.date);
        thumbnail = itemView.findViewById(R.id.thumbnail);
    }
}
