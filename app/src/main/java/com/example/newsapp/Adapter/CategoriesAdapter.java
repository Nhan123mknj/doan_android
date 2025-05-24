package com.example.newsapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.newsapp.Interface.IClickItemArticlesListener;
import com.example.newsapp.Model.Categories;
import com.example.newsapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CategoriesAdapter extends RecyclerView.Adapter<CategoriesAdapter.CategoryViewHolder> {

    private List<Categories> categoriesList;
    private IClickItemArticlesListener iClickItemArticlesListener;
    public CategoriesAdapter(List<Categories> categoriesList, IClickItemArticlesListener iClickItemArticlesListener) {
        this.categoriesList = categoriesList;
        this.iClickItemArticlesListener = iClickItemArticlesListener;
    }
    public void setCategoriesList(List<Categories> categoriesList) {
        this.categoriesList = categoriesList;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public CategoriesAdapter.CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categories_item, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoriesAdapter.CategoryViewHolder holder, int position) {
        Categories categories = categoriesList.get(position);
        holder.categoryName.setText(categories.getName());
        holder.categoryDescription.setText(categories.getDescription());
        Picasso.get().load(categories.getImage()).into(holder.categoryImage);
        holder.itemView.setOnClickListener(v -> {
            iClickItemArticlesListener.onItemClickedCategories(categories);
        });
    }

    @Override
    public int getItemCount() {
        return categoriesList.size();
    }

    public class CategoryViewHolder extends RecyclerView.ViewHolder {

        TextView categoryName;
        TextView categoryDescription;
        ImageView categoryImage;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoriesName);
            categoryDescription = itemView.findViewById(R.id.description);
            categoryImage = itemView.findViewById(R.id.imgCate);

        }
    }
}
