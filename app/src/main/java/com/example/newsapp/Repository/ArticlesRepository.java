package com.example.newsapp.Repository;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cloudinary.android.MediaManager;
import com.cloudinary.utils.ObjectUtils;
import com.example.newsapp.Activity.LoginActivity;
import com.example.newsapp.Helper.DialogLogin;
import com.example.newsapp.Model.Articles;
import com.example.newsapp.Model.Report;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Repository for managing articles data, handling Firestore queries and
 * updates.
 */
public class ArticlesRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Map<String, String> categoryMap = new HashMap<>();
    private final Map<String, String> userNameMap = new HashMap<>();
    private final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
    public ArticlesRepository() {
        loadCategories();
        loadUsers();
    }

    /**
     * Retrieves all published articles, sorted by publishedAt.
     * @return LiveData containing the list of articles.
     */
    public LiveData<List<Articles>> getAllArticles() {
        MutableLiveData<List<Articles>> allArticlesLiveData = new MutableLiveData<>();
        db.collection("articles")
                .orderBy("publishedAt")
                .whereEqualTo("status", "published")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching articles: " + error.getMessage());
                        allArticlesLiveData.setValue(new ArrayList<>());
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
                    allArticlesLiveData.setValue(articles);
                });
        return allArticlesLiveData;
    }

    /**
     * Retrieves featured articles (featured = true, status = published), sorted by publishedAt.
     * @return LiveData containing the list of featured articles.
     */
    public LiveData<List<Articles>> getArticlesFeatured() {
        MutableLiveData<List<Articles>> featuredArticlesLiveData = new MutableLiveData<>();
        db.collection("articles")
                .whereEqualTo("featured", true)
                .whereEqualTo("status", "published")
                .orderBy("publishedAt")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching featured articles: " + error.getMessage());
                        featuredArticlesLiveData.setValue(new ArrayList<>());
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
                    Log.d("ArticlesRepository", "Total featured articles loaded: " + articles.size());
                    featuredArticlesLiveData.setValue(articles);
                });
        return featuredArticlesLiveData;
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
        if (categoryId == null || categoryId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid categoryId");
            articlesLiveData.setValue(new ArrayList<>());
            return articlesLiveData;
        }
        
        Log.d("ArticlesRepository", "Loading articles for category: " + categoryId);
        db.collection("articles")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("status", "published")
                .addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching articles by category: " + error.getMessage());
                articlesLiveData.setValue(new ArrayList<>());
                return;
            }
            List<Articles> articles = new ArrayList<>();
            if (value != null) {
                Log.d("ArticlesRepository", "Found " + value.size() + " documents for category " + categoryId);
                for (DocumentSnapshot doc : value.getDocuments()) {
                    Articles article = doc.toObject(Articles.class);
                    if (article != null) {
                        article.setArticleId(doc.getId());
                        article.setCategoryName(categoryMap.get(article.getCategoryId()));
                        article.setUsername(userNameMap.get(article.getAuthor()));
                        articles.add(article);
                        Log.d("ArticlesRepository", "Added article: " + article.getTitle());
                    }
                }
            }
            Log.d("ArticlesRepository", "Total articles for category " + categoryId + ": " + articles.size());
            articlesLiveData.setValue(articles);
        });
        return articlesLiveData;
    }

    public void addArticle(Articles article,Context context) {
        if (currentUser == null) {
            mainHandler.post(() -> DialogLogin.openDialogLogin(context, Gravity.CENTER, () -> {
                Intent loginIntent = new Intent(context, LoginActivity.class);
                ((Activity) context).startActivityForResult(loginIntent, 100);
            }));
            Log.e("ArticlesRepository", "Login required");
            return;
        }
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
    public interface OnImageUploadCallback {
        void onSuccess(String imageUrl, String publicId);
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

    public void updateArticle(Articles article) {
        db.collection("articles")
                .document(article.getArticleId())
                .set(article)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        article.setStatus("pending");
                        Log.d("ArticlesRepository", "Article updated successfully");
                    }
                });
    }
    public void uploadImage(Uri imageUri, OnImageUploadCallback callback) {
        MediaManager.get().upload(imageUri)
                .option("folder", "articles_thumb")
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {

                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) {
                        // Có thể thêm logic nếu cần
                    }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String imageUrl = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");
                        callback.onSuccess(imageUrl, publicId);
                    }

                    @Override
                    public void onError(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, com.cloudinary.android.callback.ErrorInfo error) {
                        // Có thể thêm logic nếu cần
                    }
                }).dispatch();
    }
    public LiveData<Boolean> deleteImage(String publicId) {
        MutableLiveData<Boolean> resultLiveData = new MutableLiveData<>();

        new Thread(() -> {
            try {
                Map result = MediaManager.get().getCloudinary().uploader().destroy(publicId, ObjectUtils.emptyMap());
                if ("ok".equals(result.get("result"))) {
                    resultLiveData.postValue(true);
                } else {
                    resultLiveData.postValue(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
                resultLiveData.postValue(false);
            }
        }).start();

        return resultLiveData;
    }

    public LiveData<List<Articles>> getArticlesLikedByUser(String userId) {
        MutableLiveData<List<Articles>> likedArticlesLiveData = new MutableLiveData<>();
        if (userId == null || userId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid userId");
            likedArticlesLiveData.setValue(new ArrayList<>());
            return likedArticlesLiveData;
        }

        Log.d("ArticlesRepository", "=== STARTING LIKED ARTICLES QUERY ===");
        Log.d("ArticlesRepository", "User ID: " + userId);

        db.collection("articles")
                .whereArrayContains("liked_by", userId)
                .whereEqualTo("status", "published")

                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching liked articles: " + error.getMessage());
                        Log.e("ArticlesRepository", "Error code: " + error.getCode());
                        likedArticlesLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    Log.d("ArticlesRepository", "=== LIKED ARTICLES QUERY SUCCESS ===");
                    Log.d("ArticlesRepository", "User: " + userId);
                    List<Articles> articles = new ArrayList<>();
                    if (value != null) {
                        Log.d("ArticlesRepository", "Got " + value.size() + " documents from liked query");
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Log.d("ArticlesRepository", "Processing liked document: " + doc.getId());
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                articles.add(article);
                                Log.d("ArticlesRepository", "Added liked article: " + article.getTitle());
                            } else {
                                Log.w("ArticlesRepository",
                                        "Liked article object is null for document: " + doc.getId());
                            }
                        }
                    } else {
                        Log.w("ArticlesRepository", "Liked QuerySnapshot is null");
                    }
                    Log.d("ArticlesRepository", "=== FINAL LIKED ARTICLES COUNT: " + articles.size() + " ===");
                    likedArticlesLiveData.setValue(articles);
                });
        return likedArticlesLiveData;
    }

    /**
     * Updates the bookmark status for an article and toggles the user's bookmark
     * status.
     * 
     * @param articleId The ID of the article.
     * @param context   The context for showing dialogs.
     * @param listener  Callback for success or error.
     */
    public void updateBookmarkStatus(String articleId, Context context, OnBookmarkResultListener listener) {
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
            List<String> savedBy = snapshot.get("saved_by") != null
                    ? new ArrayList<>((List<String>) snapshot.get("saved_by"))
                    : new ArrayList<>();
            if (savedBy == null) {
                savedBy = new ArrayList<>();
            }
            boolean isBookmarked = savedBy.contains(userId);
            if (isBookmarked) {
                savedBy.remove(userId);
            } else {
                if (!savedBy.contains(userId)) {
                    savedBy.add(userId);
                }
            }
            transaction.update(articleRef, "saved_by", savedBy);
            return isBookmarked;
        }).addOnSuccessListener(isBookmarkedBefore -> {
            listener.onSuccess(!isBookmarkedBefore);
        }).addOnFailureListener(e -> {
            Log.e("ArticlesRepository", "Error updating bookmark: " + e.getMessage());
            listener.onError("Error updating bookmark: " + e.getMessage());
        });
    }

    /**
     * Retrieves the bookmark status of an article for a specific user.
     * 
     * @param articleId The ID of the article.
     * @param userId    The ID of the user.
     * @return LiveData containing the bookmark status (true if bookmarked, false
     *         otherwise).
     */
    public LiveData<Boolean> getBookmarkStatus(String articleId, String userId) {
        MutableLiveData<Boolean> bookmarkStatusLiveData = new MutableLiveData<>();
        if (articleId == null || userId == null || articleId.isEmpty() || userId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid articleId or userId: articleId=" + articleId + ", userId=" + userId);
            bookmarkStatusLiveData.setValue(false);
            return bookmarkStatusLiveData;
        }
        DocumentReference articleRef = db.collection("articles").document(articleId);
        articleRef.addSnapshotListener((snapshot, error) -> {
            if (error != null) {
                Log.e("ArticlesRepository", "Error fetching bookmark status: " + error.getMessage());
                bookmarkStatusLiveData.setValue(false);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                List<String> savedBy = (List<String>) snapshot.get("saved_by");
                bookmarkStatusLiveData.setValue(savedBy != null && savedBy.contains(userId));
            } else {
                bookmarkStatusLiveData.setValue(false);
            }
        });
        return bookmarkStatusLiveData;
    }

    /**
     * Retrieves articles bookmarked by a specific user.
     * 
     * @param userId The ID of the user.
     * @return LiveData containing the list of bookmarked articles.
     */
    public LiveData<List<Articles>> getArticlesBookmarkedByUser(String userId) {
        MutableLiveData<List<Articles>> bookmarkedArticlesLiveData = new MutableLiveData<>();
        if (userId == null || userId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid userId");
            bookmarkedArticlesLiveData.setValue(new ArrayList<>());
            return bookmarkedArticlesLiveData;
        }

        Log.d("ArticlesRepository", "=== STARTING BOOKMARKED ARTICLES QUERY ===");
        Log.d("ArticlesRepository", "User ID: " + userId);

        db.collection("articles")
                .whereArrayContains("saved_by", userId)
                .whereEqualTo("status", "published")
                // Temporarily remove orderBy due to Firestore composite index requirement
                // .orderBy("publishedAt")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching bookmarked articles: " + error.getMessage());
                        bookmarkedArticlesLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    Log.d("ArticlesRepository", "=== BOOKMARKED ARTICLES QUERY SUCCESS ===");
                    List<Articles> articles = new ArrayList<>();
                    if (value != null) {
                        Log.d("ArticlesRepository", "Got " + value.size() + " bookmarked documents");
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                articles.add(article);
                                Log.d("ArticlesRepository", "Added bookmarked article: " + article.getTitle());
                            }
                        }
                    }
                    Log.d("ArticlesRepository", "=== FINAL BOOKMARKED ARTICLES COUNT: " + articles.size() + " ===");
                    bookmarkedArticlesLiveData.setValue(articles);
                });
        return bookmarkedArticlesLiveData;
    }


    /**
     * Submits a report for an article.
     * 
     * @param articleId    The ID of the article being reported.
     * @param articleTitle The title of the article being reported.
     * @param reason       The reason for reporting.
     * @param description  Additional description (optional).
     * @param context      The context for showing dialogs.
     * @param listener     Callback for success or error.
     */
    public void reportArticle(String articleId, String articleTitle, String reason, String description,
            Context context, OnReportResultListener listener) {
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
        String userName = userNameMap.get(userId);

        // Check if user has already reported this article
        db.collection("reports")
                .whereEqualTo("article_id", articleId)
                .whereEqualTo("reporter_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        listener.onError("Bạn đã báo cáo bài viết này rồi");
                        return;
                    }

                    // Create new report
                    Report report = new Report(
                            articleId,
                            articleTitle,
                            userId,
                            userName != null ? userName : "Người dùng",
                            reason,
                            description,
                            System.currentTimeMillis());

                    db.collection("reports")
                            .add(report)
                            .addOnSuccessListener(documentReference -> {
                                Log.d("ArticlesRepository", "Report submitted with ID: " + documentReference.getId());
                                listener.onSuccess("Báo cáo đã được gửi thành công");
                            })
                            .addOnFailureListener(e -> {
                                Log.e("ArticlesRepository", "Error submitting report: " + e.getMessage());
                                listener.onError("Lỗi khi gửi báo cáo: " + e.getMessage());
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("ArticlesRepository", "Error checking existing reports: " + e.getMessage());
                    listener.onError("Lỗi khi kiểm tra báo cáo: " + e.getMessage());
                });
    }

    /**
     * Retrieves all reports for admin review.
     * 
     * @return LiveData containing the list of reports.
     */
    public LiveData<List<Report>> getAllReports() {
        MutableLiveData<List<Report>> reportsLiveData = new MutableLiveData<>();
        db.collection("reports")
                .orderBy("reported_at", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching reports: " + error.getMessage());
                        reportsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<Report> reports = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Report report = doc.toObject(Report.class);
                            if (report != null) {
                                report.setReportId(doc.getId());
                                reports.add(report);
                            }
                        }
                    }
                    Log.d("ArticlesRepository", "Total reports loaded: " + reports.size());
                    reportsLiveData.setValue(reports);
                });
        return reportsLiveData;
    }

    /**
     * Updates the status of a report (for admin use).
     * 
     * @param reportId   The ID of the report.
     * @param newStatus  The new status (pending, reviewed, resolved, dismissed).
     * @param adminNotes Admin notes about the report.
     * @param listener   Callback for success or error.
     */
    public void updateReportStatus(String reportId, String newStatus, String adminNotes,
            OnReportResultListener listener) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        if (adminNotes != null && !adminNotes.isEmpty()) {
            updates.put("admin_notes", adminNotes);
        }

        db.collection("reports")
                .document(reportId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d("ArticlesRepository", "Report status updated successfully");
                    listener.onSuccess("Trạng thái báo cáo đã được cập nhật");
                })
                .addOnFailureListener(e -> {
                    Log.e("ArticlesRepository", "Error updating report status: " + e.getMessage());
                    listener.onError("Lỗi khi cập nhật trạng thái: " + e.getMessage());
                });
    }

    /**
     * Retrieves the comment count for a specific article (including replies).
     * @param articleId The ID of the article.
     * @return LiveData containing the total comment count (parent comments + replies).
     */
    public LiveData<Integer> getCommentCount(String articleId) {
        MutableLiveData<Integer> commentCountLiveData = new MutableLiveData<>();
        if (articleId == null || articleId.isEmpty()) {
            Log.e("ArticlesRepository", "Invalid articleId");
            commentCountLiveData.setValue(0);
            return commentCountLiveData;
        }
        
        db.collection("comments")
                .document(articleId)
                .collection("commentList")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching comment count: " + error.getMessage());
                        commentCountLiveData.setValue(0);
                        return;
                    }
                    
                    int totalCommentCount = 0;
                    if (value != null) {
                        // Đếm số bình luận cha
                        int parentCommentCount = value.size();
                        totalCommentCount = parentCommentCount;
                        
                        // Đếm số replies trong mỗi bình luận cha
                        int replyCount = 0;
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                List<Map<String, Object>> replies = (List<Map<String, Object>>) doc.get("replies");
                                if (replies != null) {
                                    replyCount += replies.size();
                                }
                            } catch (Exception e) {
                                Log.w("ArticlesRepository", "Error counting replies for comment " + doc.getId() + ": " + e.getMessage());
                            }
                        }
                        
                        totalCommentCount += replyCount;
                        Log.d("ArticlesRepository", "Comment count for article " + articleId + 
                              ": " + parentCommentCount + " parent comments + " + replyCount + 
                              " replies = " + totalCommentCount + " total");
                    }
                    commentCountLiveData.setValue(totalCommentCount);
                });
        return commentCountLiveData;
    }

    /**
     * Searches articles by title or content.
     * @param query The search query.
     * @return LiveData containing the list of matching articles.
     */
    public LiveData<List<Articles>> searchArticles(String query) {
        MutableLiveData<List<Articles>> searchResultsLiveData = new MutableLiveData<>();
        if (query == null || query.trim().isEmpty()) {
            searchResultsLiveData.setValue(new ArrayList<>());
            return searchResultsLiveData;
        }
        
        String searchQuery = query.toLowerCase().trim();
        Log.d("ArticlesRepository", "Searching for: " + searchQuery);
        
        db.collection("articles")
                .whereEqualTo("status", "published")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error searching articles: " + error.getMessage());
                        searchResultsLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    
                    List<Articles> filteredArticles = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                
                                // Check if title or content contains search query
                                String title = article.getTitle() != null ? article.getTitle().toLowerCase() : "";
                                String content = article.getContent() != null ? article.getContent().toLowerCase() : "";
                                
                                if (title.contains(searchQuery) || content.contains(searchQuery)) {
                                    filteredArticles.add(article);
                                }
                            }
                        }
                    }
                    Log.d("ArticlesRepository", "Found " + filteredArticles.size() + " articles for query: " + searchQuery);
                    searchResultsLiveData.setValue(filteredArticles);
                });
        return searchResultsLiveData;
    }

    /**
     * Retrieves trending articles (articles with highest like count).
     * @param limit Maximum number of articles to return.
     * @return LiveData containing the list of trending articles.
     */
    public LiveData<List<Articles>> getTrendingArticles(int limit) {
        MutableLiveData<List<Articles>> trendingArticlesLiveData = new MutableLiveData<>();
        db.collection("articles")
                .whereEqualTo("status", "published")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e("ArticlesRepository", "Error fetching trending articles: " + error.getMessage());
                        trendingArticlesLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    List<Articles> allArticles = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Articles article = doc.toObject(Articles.class);
                            if (article != null) {
                                article.setArticleId(doc.getId());
                                article.setCategoryName(categoryMap.get(article.getCategoryId()));
                                article.setUsername(userNameMap.get(article.getAuthor()));
                                allArticles.add(article);
                            }
                        }
                    }
                    
                    // Sort by likes count in descending order (client-side)
                    allArticles.sort((a1, a2) -> Integer.compare(a2.getLikesCount(), a1.getLikesCount()));
                    
                    // Take only the specified limit
                    List<Articles> trendingArticles = new ArrayList<>();
                    for (int i = 0; i < Math.min(limit, allArticles.size()); i++) {
                        trendingArticles.add(allArticles.get(i));
                    }
                    
                    Log.d("ArticlesRepository", "Loaded " + trendingArticles.size() + " trending articles (sorted by likes)");
                    trendingArticlesLiveData.setValue(trendingArticles);
                });
        return trendingArticlesLiveData;
    }

    public interface OnReportResultListener {
        void onSuccess(String message);
        void onError(String errorMessage);
    }

    public interface OnBookmarkResultListener {
        void onSuccess(boolean isBookmarked);

        void onError(String errorMessage);
    }
}