package com.example.newsapp.Model;



import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;


public class Reply implements Serializable {
    @PropertyName("replyId")
    private String replyId;

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

    public Reply() {
    }

    public Reply(String replyId, String userId, String username, String profilePicUrl, String content, long timestamp) {
        this.replyId = replyId;
        this.userId = userId;
        this.username = username;
        this.profilePicUrl = profilePicUrl;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getReplyId() {
        return replyId;
    }

    public void setReplyId(String replyId) {
        this.replyId = replyId;
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

    public String getProfilePicUrl() {
        return profilePicUrl;
    }

    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
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
}
