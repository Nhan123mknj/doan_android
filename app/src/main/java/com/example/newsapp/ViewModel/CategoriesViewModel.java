package com.example.newsapp.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.Model.Categories;
import com.example.newsapp.Repository.CategoriesRespository;

import java.util.List;

public class CategoriesViewModel extends ViewModel {
    private CategoriesRespository categoriesRespository;
    public CategoriesViewModel() {
        categoriesRespository = new CategoriesRespository();
    }
    public LiveData<List<Categories>> getAllCategories() {
        return categoriesRespository.getAllCategories();
    }
}
