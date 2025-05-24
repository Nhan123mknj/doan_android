package com.example.newsapp.Repository;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Categories;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class CategoriesRespository {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    public LiveData<List<Categories>> getAllCategories() {
        MutableLiveData<List<Categories>> categoriesLiveData = new MutableLiveData<>();
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("CategoriesViewModel", "Error loading categories: " + error.getMessage());
                categoriesLiveData.setValue(new ArrayList<>());
                return;
            }
            List<Categories> categories = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Categories category = doc.toObject(Categories.class);
                    if (category != null) {
                        category.setCategoryId(doc.getId());
                        categories.add(category);
                    }
                }
            }
            Log.d("CategoriesViewModel", "Loaded categories: " + categories.size());
            categoriesLiveData.setValue(categories);
        });
        return categoriesLiveData;
    }

}
