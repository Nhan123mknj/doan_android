package com.example.newsapp.Model;

public class Follow {
    private String followerId;
    private String authorId;

    public Follow() {

    }
    public Follow(String followerId, String authorId) {
        this.followerId = followerId;
        this.authorId = authorId;
    }
    public String getFollowerId() {
        return followerId;
    }
    public void setFollowerId(String followerId) {
        this.followerId = followerId;
    }
    public String getAuthorId() {
        return authorId;
    }
    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }
}
