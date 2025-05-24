package com.example.newsapp.Interface;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;

public interface IClickItemArticlesListener {
    void onItemClicked(Articles articles);
    void onItemClickedCategories(Categories categories);

    void onItemClickedDelete(Articles articles);

}
