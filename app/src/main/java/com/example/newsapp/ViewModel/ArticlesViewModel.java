package com.example.newsapp.ViewModel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Report;
import com.example.newsapp.Repository.ArticlesRepository;

import java.util.List;

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

    /**
     * Retrieves articles liked by a specific user.
     * @param userId The ID of the user.
     * @return LiveData containing the list of liked articles.
     */
    public LiveData<List<Articles>> getArticlesLikedByUser(String userId) {
        return articlesRepository.getArticlesLikedByUser(userId);
    }

    /**
     * Bookmarks or unbookmarks an article.
     * @param articleId The ID of the article.
     * @param context The context for showing dialogs.
     * @param listener Callback for success or error.
     */
    public void bookmarkArticle(String articleId, Context context, ArticlesRepository.OnBookmarkResultListener listener) {
        articlesRepository.updateBookmarkStatus(articleId, context, listener);
    }

    /**
     * Retrieves the bookmark status of an article for a specific user.
     * @param articleId The ID of the article.
     * @param userId The ID of the user.
     * @return LiveData containing the bookmark status.
     */
    public LiveData<Boolean> getBookmarkStatus(String articleId, String userId) {
        return articlesRepository.getBookmarkStatus(articleId, userId);
    }

    /**
     * Retrieves articles bookmarked by a specific user.
     * @param userId The ID of the user.
     * @return LiveData containing the list of bookmarked articles.
     */
    public LiveData<List<Articles>> getArticlesBookmarkedByUser(String userId) {
        return articlesRepository.getArticlesBookmarkedByUser(userId);
    }

    /**
     * Comprehensive debug method for liked articles
     */
    public void debugLikedArticlesComprehensive(String userId) {
        articlesRepository.debugLikedArticlesComprehensive(userId);
    }

    /**
     * Reports an article for admin review.
     * @param articleId The ID of the article being reported.
     * @param articleTitle The title of the article being reported.
     * @param reason The reason for reporting.
     * @param description Additional description (optional).
     * @param context The context for showing dialogs.
     * @param listener Callback for success or error.
     */
    public void reportArticle(String articleId, String articleTitle, String reason, String description, 
                             Context context, ArticlesRepository.OnReportResultListener listener) {
        articlesRepository.reportArticle(articleId, articleTitle, reason, description, context, listener);
    }

    /**
     * Retrieves all reports for admin review.
     * @return LiveData containing the list of reports.
     */
    public LiveData<List<Report>> getAllReports() {
        return articlesRepository.getAllReports();
    }

    /**
     * Updates the status of a report (for admin use).
     * @param reportId The ID of the report.
     * @param newStatus The new status (pending, reviewed, resolved, dismissed).
     * @param adminNotes Admin notes about the report.
     * @param listener Callback for success or error.
     */
    public void updateReportStatus(String reportId, String newStatus, String adminNotes, 
                                  ArticlesRepository.OnReportResultListener listener) {
        articlesRepository.updateReportStatus(reportId, newStatus, adminNotes, listener);
    }

    /**
     * Retrieves the comment count for a specific article.
     * @param articleId The ID of the article.
     * @return LiveData containing the comment count.
     */
    public LiveData<Integer> getCommentCount(String articleId) {
        return articlesRepository.getCommentCount(articleId);
    }

    /**
     * Searches articles by title or content.
     * @param query The search query.
     * @return LiveData containing the list of matching articles.
     */
    public LiveData<List<Articles>> searchArticles(String query) {
        return articlesRepository.searchArticles(query);
    }

    /**
     * Retrieves trending articles (articles with highest like count).
     * @param limit Maximum number of articles to return.
     * @return LiveData containing the list of trending articles.
     */
    public LiveData<List<Articles>> getTrendingArticles(int limit) {
        return articlesRepository.getTrendingArticles(limit);
    }
    public void updateArticle(Articles article) {
        articlesRepository.updateArticle(article);

    }
    public LiveData<Boolean> deleteImage(String publicId) {
        return articlesRepository.deleteImage(publicId);
    }
    public void uploadImage(Uri publicId, ArticlesRepository.OnImageUploadCallback callback) {
        articlesRepository.uploadImage(publicId, callback);
    }
}