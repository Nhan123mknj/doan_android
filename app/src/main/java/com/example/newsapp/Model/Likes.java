package com.example.newsapp.Model;




public class Likes {
    private String userId;
    private String articleId;
    private long likedAt;

    public Likes() {
    }

    public Likes(String userId, String articleId, long likedAt) {
        this.userId = userId;
        this.articleId = articleId;
        this.likedAt = likedAt;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public long getLikedAt() {
        return likedAt;
    }

    public void setLikedAt(long likedAt) {
        this.likedAt = likedAt;
    }
}
