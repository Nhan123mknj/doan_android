package com.example.newsapp.Repository;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Activity.LoginActivity;
import com.example.newsapp.Helper.DialogLogin;
import com.example.newsapp.Model.Articles;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository for managing articles data, handling Firestore queries and updates.
 */
public class ArticlesRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> categoryMap = new HashMap<>();
    private final Map<String, String> userNameMap = new HashMap<>();

    private final MutableLiveData<List<Articles>> articlesLiveData = new MutableLiveData<>();

    public ArticlesRepository() {
        loadCategories();
        loadUsers();
    }

    /**
     * Retrieves all published articles, sorted by publishedAt.
     * @return LiveData containing the list of articles.
     */
    public LiveData<List<Articles>> getAllArticles() {
        db.collection("articles")
                .orderBy("publishedAt")
                .whereEqualTo("status", "published")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching articles: " + error.getMessage());
                        articlesLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<Articles> articles = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                    articles.add(article);
                            }
                        }
                    }
                    articlesLiveData.setValue(articles);
                });
        return articlesLiveData;
    }

    /**
     * Retrieves featured articles (featured = true, status = published), sorted by publishedAt.
     * @return LiveData containing the list of featured articles.
     */
    public LiveData<List<Articles>> getArticlesFeatured() {
        db.collection("articles")
                .whereEqualTo("featured", true)
                .whereEqualTo("status", "published")
                .orderBy("publishedAt")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching featured articles: " + error.getMessage());
                        articlesLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<Articles> articles = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                articles.add(article);
                            }
                        }
                    }
                    Log.d("ArticlesRepository", "Total articles loaded: " + articles.size());
                    articlesLiveData.setValue(articles);
                });
        return articlesLiveData;
    }

    /**
     * Retrieves an article by its ID.
     * @param articleId The ID of the article.
     * @return LiveData containing the article or null if not found.
     */
    public LiveData<Articles> getArticleById(String articleId) {
        MutableLiveData<Articles> articleLiveData = new MutableLiveData<>();
        if (articleId == null || articleId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid articleId");
            articleLiveData.setValue(null);
            return articleLiveData;
        }
        DocumentReference articleRef = db.collection("articles").document(articleId);
        articleRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching article: " + error.getMessage());
                articleLiveData.setValue(null);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                Articles article = snapshot.toObject(Articles.class);
                if (article != null) {
                    article.setArticleId(snapshot.getId());
                    article.setCategoryName(categoryMap.get(article.getCategoryId()));
                    article.setUsername(userNameMap.get(article.getAuthor()));
                    Log.d("ArticlesRepository", "Loaded article: " + article.getTitle());
                    articleLiveData.setValue(article);
                } else {
                    articleLiveData.setValue(null);
                }
            } else {
                Log.w("ArticlesRepository", "Article not found: " + articleId);
                articleLiveData.setValue(null);
            }
        });
        return articleLiveData;
    }

    /**
     * Updates the like count for an article and toggles the user's like status.
     * @param articleId The ID of the article.
     * @param context The context for showing dialogs.
     * @param listener Callback for success or error.
     */
    public void updateLikeCount(String articleId, Context context, OnLikeResultListener listener) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            mainHandler.post(() -> DialogLogin.openDialogLogin(context, Gravity.CENTER, () -> {
                Intent loginIntent = new Intent(context, LoginActivity.class);
                ((Activity) context).startActivityForResult(loginIntent, 100);
            }));
            listener.onError("Login required");
            return;
        }
        String userId = currentUser.getUid();
        DocumentReference articleRef = db.collection("articles").document(articleId);
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(articleRef);
            if (!snapshot.exists()) {
                throw new FirebaseFirestoreException("Article not found", FirebaseFirestoreException.Code.NOT_FOUND);
            }
            List<String> likedBy = snapshot.get("liked_by") != null ? new ArrayList<>((List<String>) snapshot.get("liked_by")) : new ArrayList<>();
            if (likedBy == null) {
                likedBy = new ArrayList<>();
            }
            Long likesCountLong = snapshot.getLong("likesCount");
            int likesCount = likesCountLong != null ? likesCountLong.intValue() : 0;
            boolean isLiked = likedBy.contains(userId);
            if (isLiked) {
                likedBy.remove(userId);
                likesCount = Math.max(0, likesCount - 1);
            } else {
                if (!likedBy.contains(userId)) {
                    likedBy.add(userId);
                }
                likesCount += 1;
            }
            transaction.update(articleRef, "liked_by", likedBy, "likesCount", likesCount);
            return isLiked;
        }).addOnSuccessListener(isLikedBefore -> {
            listener.onSuccess(!isLikedBefore);
        }).addOnFailureListener(e -> {
            Log.e("ArticlesRepository", "Error updating like: " + e.getMessage());
            listener.onError("Error updating like: " + e.getMessage());
        });
    }

    /**
     * Retrieves the like status of an article for a specific user.
     * @param articleId The ID of the article.
     * @param userId The ID of the user.
     * @return LiveData containing the like status (true if liked, false otherwise).
     */
    public LiveData<Boolean> getLikeStatus(String articleId, String userId) {
        MutableLiveData<Boolean> likeStatusLiveData = new MutableLiveData<>();
        if (articleId == null || userId == null || articleId.isEmpty() || userId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid articleId or userId: articleId=" + articleId + ", userId=" + userId);
            likeStatusLiveData.setValue(false);
            return likeStatusLiveData;
        }
        DocumentReference articleRef = db.collection("articles").document(articleId);
        articleRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching like status: " + error.getMessage());
                likeStatusLiveData.setValue(false);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                List<String> likedBy = (List<String>) snapshot.get("liked_by");
                likeStatusLiveData.setValue(likedBy != null && likedBy.contains(userId));
            } else {
                likeStatusLiveData.setValue(false);
            }
        });
        return likeStatusLiveData;
    }
    public LiveData<List<Articles>> getArticlesByCategory(String categoryId) {
        MutableLiveData<List<Articles>> articlesLiveData = new MutableLiveData<>();
        db.collection("articles").whereEqualTo("categoryId", categoryId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching articles by category: " + error.getMessage());
                articlesLiveData.setValue(new ArrayList<>());
                return;
            }
            List<Articles> articles = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Articles article = doc.toObject(Articles.class);
                    if (article != null) {
                        article.setArticleId(doc.getId());
                        article.setCategoryName(categoryMap.get(article.getCategoryId()));
                        article.setUsername(userNameMap.get(article.getAuthor()));
                        articles.add(article);
                    }
                }
            }
            articlesLiveData.setValue(articles);
        });
        return articlesLiveData;
    }
    public void addArticle(Articles article) {
        long currentTimeMillis = System.currentTimeMillis();
        SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        String currentDateString = inputFormat.format(new Date(currentTimeMillis));
        article.setStatus("pending");
        article.setPublishedAt(Articles.formatDate(currentDateString));
        db.collection("articles")
                .add(article)
                .addOnSuccessListener(documentReference -> {
                    article.setArticleId(documentReference.getId());
                    Log.d("ArticlesRepository", "Article added with ID: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("ArticlesRepository", "Error adding article: " + e.getMessage());
                });
    }
    public LiveData<List<Articles>> getArticlesByAuthor(String authorId) {
        MutableLiveData<List<Articles>> articlesLiveData = new MutableLiveData<>();
        db.collection("articles").whereEqualTo("author", authorId).addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching articles by author: " + error.getMessage());
                articlesLiveData.setValue(new ArrayList<>());
                return;
            }
            List<Articles> articles = new ArrayList<>();
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Articles article = doc.toObject(Articles.class);
                    if (article != null) {
                        article.setArticleId(doc.getId());
                        article.setCategoryName(categoryMap.get(article.getCategoryId()));
                        article.setUsername(userNameMap.get(article.getAuthor()));
                        articles.add(article);
                    }
                }
            }
            Log.d("ArticlesRepository", "Articles fetched for author: " + authorId + ", count: " + articles.size());
                articlesLiveData.setValue(articles);
        });
        return articlesLiveData;
    }
    public interface OnLikeResultListener {
        void onSuccess(boolean isLiked);
        void onError(String errorMessage);
    }
    private void loadCategories() {
        db.collection("categories").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error loading categories: " + error.getMessage());
                return;
            }
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    String categoryId = doc.getId();
                    String categoryName = doc.getString("name");
                    categoryMap.put(categoryId, categoryName);
                }
            }
        });
    }

    private void loadUsers() {
        db.collection("users").addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error loading users: " + error.getMessage());
                return;
            }
            if (value != null) {
                for (DocumentSnapshot doc : value.getDocuments()) {
                    String userId = doc.getId();
                    String username = doc.getString("name");
                    userNameMap.put(userId, username);
                }
            }
        });
    }
    public void deleteArticle(String articleId) {
        db.collection("articles")
                .document(articleId)
                .delete()
                .addOnCompleteListener(task -> {
                    Log.d("ArticlesRepository", "Article deleted successfully");
                })
                .addOnFailureListener(e -> {
                    Log.e("ArticlesRepository", "Error deleting article: " + e.getMessage());
                });
    }
}