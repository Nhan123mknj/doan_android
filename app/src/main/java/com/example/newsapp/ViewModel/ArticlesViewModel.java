package com.example.newsapp.ViewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.Repository.ArticlesRepository;

import java.util.List;
import java.util.Map;

/**
 * ViewModel for managing articles data in the UI layer.
 */
public class ArticlesViewModel extends ViewModel {
    private final ArticlesRepository articlesRepository;

    public ArticlesViewModel() {
        articlesRepository = new ArticlesRepository();
    }

    /**
     * Retrieves all published articles.
     * @return LiveData containing the list of articles.
     */
    public LiveData<List<Articles>> getAllArticles() {
        return articlesRepository.getAllArticles();
    }

    /**
     * Retrieves an article by its ID.
     * @param articleId The ID of the article.
     * @return LiveData containing the article.
     */
    public LiveData<Articles> getArticleById(String articleId) {
        return articlesRepository.getArticleById(articleId);
    }


    /**
     * Retrieves featured articles.
     * @return LiveData containing the list of featured articles.
     */
    public LiveData<List<Articles>> getArticlesFeatured() {
        return articlesRepository.getArticlesFeatured();
    }

    /**
     * Likes or unlikes an article.
     * @param articleId The ID of the article.
     * @param context The context for showing dialogs.
     * @param listener Callback for success or error.
     */
    public void likeArticle(String articleId, Context context, ArticlesRepository.OnLikeResultListener listener) {
        articlesRepository.updateLikeCount(articleId, context, listener);
    }

    /**
     * Retrieves the like status of an article for a specific user.
     * @param articleId The ID of the article.
     * @param userId The ID of the user.
     * @return LiveData containing the like status.
     */
    public LiveData<Boolean> getLikeStatus(String articleId, String userId) {
        return articlesRepository.getLikeStatus(articleId, userId);
    }
    public void addArticle(Articles article) {
        articlesRepository.addArticle(article);
    }
    public LiveData<List<Articles>> getArticlesByCategory(String categoryId) {
        return articlesRepository.getArticlesByCategory(categoryId);
    }
    public LiveData<List<Articles>> getArticlesByAuthor(String authorId) {
        return articlesRepository.getArticlesByAuthor(authorId);
    }
    public void deleteArticle(String articleId) {
        articlesRepository.deleteArticle(articleId);
    }

}