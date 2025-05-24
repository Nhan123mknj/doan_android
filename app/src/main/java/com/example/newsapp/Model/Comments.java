package com.example.newsapp.Model;



import com.google.firebase.firestore.PropertyName;

import java.util.Date;
import java.util.List;


public class Comments {
    @PropertyName("commentId")
    private String commentId;
    @PropertyName("articles_id")
    private String articles_id;

    @PropertyName("userId")
    private String userId;

    @PropertyName("username")
    private String username;
    @PropertyName("profilePicUrl")
    private String profilePicUrl;
    @PropertyName("content")
    private String content;

    @PropertyName("timestamp")
    private long timestamp;

    @PropertyName("replies")
    private List<Reply> replies;

    public Comments() {
    }

    public Comments(String articles_id, String userId, String username, String profilePicUrl, String content, long timestamp, List<Reply> replies) {
        this.articles_id = articles_id;
        this.userId = userId;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.content = content;
        this.timestamp = timestamp;
        this.replies = replies;
    }

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }

    public String getArticles_id() {
        return articles_id;
    }

    public void setArticles_id(String articles_id) {
        this.articles_id = articles_id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<Reply> getReplies() {
        return replies;
    }

    public void setReplies(List<Reply> replies) {
        this.replies = replies;
    }
}
