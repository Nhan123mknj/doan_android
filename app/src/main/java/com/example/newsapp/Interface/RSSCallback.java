package com.example.newsapp.Interface;

import com.example.newsapp.Model.Articles;

import java.util.List;

public interface RSSCallback {
    void onSuccess(List<Articles> articles);
    void onError(String errorMessage);

}
