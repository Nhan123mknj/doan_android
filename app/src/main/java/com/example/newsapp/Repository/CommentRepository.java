package com.example.newsapp.Repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.newsapp.Model.Comments;
import com.example.newsapp.Model.Reply;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Repository for managing comments and replies, handling Firestore queries and updates.
 */
public class CommentRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<List<Comments>> commentsLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();
    private static final String COMMENTS_COLLECTION = "comments";
    private static final String COMMENT_LIST_SUBCOLLECTION = "commentList";
    private final Map<String, String> userNameMap = new HashMap<>();
    private FirebaseAuth auth;

    public CommentRepository() {
        loadUsers();
    }

    /**
     * Retrieves comments for an article, optionally filtered by categoryId.
     * @param articleId The ID of the article.

     * @return LiveData containing the list of comments.
     */

    public LiveData<List<Comments>> getCommentsByArticleId(String articleId) {
        if (articleId == null || articleId.isEmpty()) {
            Log.e("CommentRepository", "Invalid articleId");
            commentsLiveData.setValue(new ArrayList<>());
            errorLiveData.setValue("Invalid article ID");
            return commentsLiveData;
        }

        Query query = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .orderBy("timestamp", Query.Direction.DESCENDING);

        query.addSnapshotListener((snapshots, error) -> {
            if (error != null) {
                Log.e("CommentRepository", "Error fetching comments: " + error.getMessage());
                commentsLiveData.setValue(new ArrayList<>());
                errorLiveData.setValue("Error fetching comments: " + error.getMessage());
                return;
            }
            List<Comments> comments = new ArrayList<>();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots) {
                    Comments comment = doc.toObject(Comments.class);
                    if (comment != null) {
                        comment.setCommentId(doc.getId());
                        comment.setUsername(userNameMap.get(comment.getUserId()));
                        
                        // Thiết lập username cho replies
                        List<Reply> replies = comment.getReplies();
                        if (replies != null) {
                            for (Reply reply : replies) {
                                if (reply != null && reply.getUserId() != null) {
                                    String replyUsername = userNameMap.get(reply.getUserId());
                                    if (replyUsername != null && !replyUsername.isEmpty()) {
                                        reply.setUsername(replyUsername);
                                    }
                                    Log.d("CommentRepository", "Reply username set: " + reply.getUsername() + " for userId: " + reply.getUserId());
                                }
                            }
                        }
                        
                        comments.add(comment);
                    }
                }
            }
            commentsLiveData.setValue(comments);
            errorLiveData.setValue(comments.isEmpty() ? "No comments found" : null);
        });
        return commentsLiveData;
    }


    public void addComment(String articleId, Comments comment) {

        db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection("commentList")
                .add(comment)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        documentReference.getId();
                        comment.setCommentId(documentReference.getId());

                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentDAO", "Lỗi khi thêm comment: " + e.getMessage());
                });
    }

    /**
     * Adds a reply to a comment.
     * @param articleId The ID of the article.
     * @param commentId The ID of the comment.
     * @param reply The reply to add.
     */
    public void addReply(String articleId, String commentId, Reply reply) {
        if (articleId == null || commentId == null || reply == null) {
            Log.e("CommentRepository", "Invalid articleId, commentId, or reply");
            return;
        }
        
        Log.d("CommentRepository", "Adding reply to comment: " + commentId);
        DocumentReference commentRef = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .document(commentId);
                
        // Use transaction to ensure atomic update
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(commentRef);
            if (!snapshot.exists()) {
                throw new RuntimeException("Comment not found: " + commentId);
            }
            
            Comments comment = snapshot.toObject(Comments.class);
            if (comment == null) {
                throw new RuntimeException("Invalid comment data");
            }
            
            List<Reply> replies = comment.getReplies();
            if (replies == null) {
                replies = new ArrayList<>();
            }
            
            // Generate unique reply ID
            reply.setReplyId(db.collection("temp").document().getId());
            replies.add(reply);
            comment.setReplies(replies);
            
            // Update the comment with new reply
            transaction.set(commentRef, comment);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("CommentRepository", "Reply added successfully to comment: " + commentId);
        }).addOnFailureListener(e -> {
            Log.e("CommentRepository", "Error adding reply: " + e.getMessage());
        });
    }

    /**
     * Updates a comment's content.
     * @param articleId The ID of the article.
     * @param commentId The ID of the comment.
     * @param newContent The new content for the comment.
     */
    public void updateComment(String articleId, String commentId, String newContent) {
        if (articleId == null || commentId == null || newContent == null) {
            Log.e("CommentRepository", "Invalid parameters for updateComment");
            return;
        }
        
        DocumentReference commentRef = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .document(commentId);
                
        commentRef.update("content", newContent, "timestamp", System.currentTimeMillis())
                .addOnSuccessListener(aVoid -> {
                    Log.d("CommentRepository", "Comment updated successfully: " + commentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentRepository", "Error updating comment: " + e.getMessage());
                });
    }

    /**
     * Deletes a comment.
     * @param articleId The ID of the article.
     * @param commentId The ID of the comment.
     */
    public void deleteComment(String articleId, String commentId) {
        if (articleId == null || commentId == null) {
            Log.e("CommentRepository", "Invalid parameters for deleteComment");
            return;
        }
        
        DocumentReference commentRef = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .document(commentId);
                
        commentRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Log.d("CommentRepository", "Comment deleted successfully: " + commentId);
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentRepository", "Error deleting comment: " + e.getMessage());
                });
    }

    /**
     * Updates a reply's content.
     * @param articleId The ID of the article.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     * @param newContent The new content for the reply.
     */
    public void updateReply(String articleId, String commentId, String replyId, String newContent) {
        if (articleId == null || commentId == null || replyId == null || newContent == null) {
            Log.e("CommentRepository", "Invalid parameters for updateReply");
            return;
        }
        
        DocumentReference commentRef = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .document(commentId);
                
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(commentRef);
            if (!snapshot.exists()) {
                throw new RuntimeException("Comment not found: " + commentId);
            }
            
            Comments comment = snapshot.toObject(Comments.class);
            if (comment == null) {
                throw new RuntimeException("Invalid comment data");
            }
            
            List<Reply> replies = comment.getReplies();
            if (replies != null) {
                for (Reply reply : replies) {
                    if (reply != null && replyId.equals(reply.getReplyId())) {
                        reply.setContent(newContent);
                        reply.setTimestamp(System.currentTimeMillis());
                        break;
                    }
                }
                comment.setReplies(replies);
                transaction.set(commentRef, comment);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("CommentRepository", "Reply updated successfully: " + replyId);
        }).addOnFailureListener(e -> {
            Log.e("CommentRepository", "Error updating reply: " + e.getMessage());
        });
    }

    /**
     * Deletes a reply.
     * @param articleId The ID of the article.
     * @param commentId The ID of the comment.
     * @param replyId The ID of the reply.
     */
    public void deleteReply(String articleId, String commentId, String replyId) {
        if (articleId == null || commentId == null || replyId == null) {
            Log.e("CommentRepository", "Invalid parameters for deleteReply");
            return;
        }
        
        DocumentReference commentRef = db.collection(COMMENTS_COLLECTION)
                .document(articleId)
                .collection(COMMENT_LIST_SUBCOLLECTION)
                .document(commentId);
                
        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(commentRef);
            if (!snapshot.exists()) {
                throw new RuntimeException("Comment not found: " + commentId);
            }
            
            Comments comment = snapshot.toObject(Comments.class);
            if (comment == null) {
                throw new RuntimeException("Invalid comment data");
            }
            
            List<Reply> replies = comment.getReplies();
            if (replies != null) {
                replies.removeIf(reply -> reply != null && replyId.equals(reply.getReplyId()));
                comment.setReplies(replies);
                transaction.set(commentRef, comment);
            }
            return null;
        }).addOnSuccessListener(aVoid -> {
            Log.d("CommentRepository", "Reply deleted successfully: " + replyId);
        }).addOnFailureListener(e -> {
            Log.e("CommentRepository", "Error deleting reply: " + e.getMessage());
        });
    }

    /**
     * Gets the LiveData for errors.
     * @return LiveData containing error messages.
     */
    public MutableLiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public MutableLiveData<List<Comments>> getCommentsLiveData() {
        return commentsLiveData;
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
}