package com.example.newsapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder> {

    private List<Articles> trendingArticles;
    private IClickItemArticlesListener listener;

    public TrendingAdapter(List<Articles> trendingArticles, IClickItemArticlesListener listener) {
        this.trendingArticles = trendingArticles;
        this.listener = listener;
    }

    public void setTrendingArticles(List<Articles> trendingArticles) {
        this.trendingArticles = trendingArticles;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.trending_article_item, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        Articles article = trendingArticles.get(position);
        
        holder.title.setText(article.getTitle());
        holder.category.setText(article.getCategoryName() != null ? article.getCategoryName() : "General");
        holder.date.setText(article.getPublishedAt());
        
        // Format likes count
        int likesCount = article.getLikesCount();
        if (likesCount >= 1000) {
            double countK = likesCount / 1000.0;
            holder.likes.setText(String.format("%.1fK", countK));
        } else {
            holder.likes.setText(String.valueOf(likesCount));
        }
        
        // Load image
        if (article.getUrlToImage() != null && !article.getUrlToImage().isEmpty()) {
            Picasso.get()
                    .load(article.getUrlToImage())
                    .placeholder(R.drawable.img_trending)
                    .error(R.drawable.img_trending)
                    .into(holder.image);
        } else {
            holder.image.setImageResource(R.drawable.img_trending);
        }
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClicked(article);
            }
        });
    }

    @Override
    public int getItemCount() {
        return trendingArticles != null ? trendingArticles.size() : 0;
    }

    public static class TrendingViewHolder extends RecyclerView.ViewHolder {
        TextView title, category, date, likes;
        ImageView image;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.trendingTitle);
            category = itemView.findViewById(R.id.trendingCategory);
            date = itemView.findViewById(R.id.trendingDate);
            likes = itemView.findViewById(R.id.trendingLikes);
            image = itemView.findViewById(R.id.trendingImage);
        }
    }
} 