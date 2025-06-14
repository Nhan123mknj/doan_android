package com.example.newsapp.Model;



import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;



public class Users implements Serializable {


    private String userId;
    private String name;
    private String email;
    private String avatarUrl;
    private Date createdAt;
    private String description;
    private String phone;
    private int countViews;
    private int followerCount;
    private int followingCount;
    public Users() {

    }


    public int getFollowerCount() { return followerCount; }
    public void setFollowerCount(int count) { this.followerCount = count; }
    public int getFollowingCount() { return followingCount; }
    public void setFollowingCount(int count) { this.followingCount = count; }
    public int getCountViews() {
        return countViews;
    }

    public void setCountViews(int countViews) {
        this.countViews = countViews;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
