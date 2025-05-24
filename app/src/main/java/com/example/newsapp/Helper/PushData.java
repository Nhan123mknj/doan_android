package com.example.newsapp.Helper;

import com.example.newsapp.Model.Articles;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class PushData {
    private DatabaseReference mDatabase;

    public PushData(){
        mDatabase = FirebaseDatabase.getInstance().getReference("articles");
    }
    public void saveData(List<Articles> articles){
        for (Articles article : articles) {
            String articleId = mDatabase.push().getKey();
            if (articleId != null) {
                mDatabase.child(articleId).setValue(article);
            }
        }
    }
}
