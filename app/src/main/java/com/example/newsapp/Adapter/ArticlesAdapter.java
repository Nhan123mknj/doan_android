package com.example.newsapp.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class ArticlesAdapter extends RecyclerView.Adapter<ArticlesAdapter.ViewHolder> {
    private List<Articles> articlesList;
    private IClickItemArticlesListener listener;

    private String layoutType;

    public static final String TYPE_LASTEST = "lastest";
    public static final String TYPE_TRENDING = "trending";


    public ArticlesAdapter(List<Articles> articlesList, IClickItemArticlesListener listener, String layoutType) {
        this.articlesList = articlesList;
        this.listener = listener;
        this.layoutType = layoutType;
    }

    public void setArticlesList(List<Articles> articlesList) {
        this.articlesList = articlesList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ArticlesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (layoutType.equals(TYPE_LASTEST)) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_lastest, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_trending, parent, false);
        }
        return new ViewHolder(view, layoutType);
    }

    @Override
    public void onBindViewHolder(@NonNull ArticlesAdapter.ViewHolder holder, int position) {
        Articles articles = articlesList.get(position);

        if (articles == null) return;

        if (holder.title != null) {
            holder.title.setText(articles.getTitle());
        }
        if (holder.publishedAt != null) {
            holder.publishedAt.setText(articles.getPublishedAt());
        }
        if (holder.thumbnail != null) {
            Picasso.get().load(articles.getUrlToImage()).into(holder.thumbnail);
        }
        if (holder.categoryTextView != null) {
            holder.categoryTextView.setText(articles.getCategoryName());
        }
        if (holder.authorTextView != null) {
            holder.authorTextView.setText(articles.getUsername());
        }
        if (holder.more_icon != null && layoutType.equals(TYPE_LASTEST)) {
            holder.more_icon.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(holder.itemView.getContext(),holder.more_icon);
                popupMenu.inflate(R.menu.menu_options);
                popupMenu.setOnMenuItemClickListener(item -> {
                    switch (item.getItemId()){
                        case R.id.update:
                            listener.onItemClickedUpdate(articles);
                            return true;
                        case R.id.delete:
                            listener.onItemClickedDelete(articles);
                            return true;
                        default:
                            return false;
                    }
                });
                popupMenu.show();
            });
        }
        holder.itemView.setOnClickListener(v -> listener.onItemClicked(articles));
    }

    @Override
    public int getItemCount() {
        return articlesList.size();
    }
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbnail,more_icon;
        private TextView title, publishedAt, categoryTextView, authorTextView;

        public ViewHolder(@NonNull View itemView, String layoutType) {
            super(itemView);

            if (layoutType.equals(TYPE_LASTEST)) {
                title = itemView.findViewById(R.id.title);
                publishedAt = itemView.findViewById(R.id.date);
                thumbnail = itemView.findViewById(R.id.thumbnail);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                authorTextView = itemView.findViewById(R.id.authorTextView);
                more_icon = itemView.findViewById(R.id.more_icon);
            } else if (layoutType.equals(TYPE_TRENDING)) {
                title = itemView.findViewById(R.id.title_trending);
                publishedAt = itemView.findViewById(R.id.pubDate);
                thumbnail = itemView.findViewById(R.id.news_trending_img);
                categoryTextView = itemView.findViewById(R.id.categoryTextView);
                authorTextView = itemView.findViewById(R.id.authorTextView);
            }
        }
    }
}
