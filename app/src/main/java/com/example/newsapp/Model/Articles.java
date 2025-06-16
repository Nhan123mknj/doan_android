package com.example.newsapp.Model;



import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import java.util.List;
import java.util.Locale;


public class Articles  implements Serializable {


    String articleId;
    @PropertyName("link")
    private String  link;
    @PropertyName("summary")
    private String summary;
    @PropertyName("title")
    private String title;
    @PropertyName("username")
    private String username;

    private String authorAvatar;
    @PropertyName("category_name")
    private String categoryName;
    @PropertyName("content")
    private String content;

    @PropertyName("author")
    private String author;

    @PropertyName("published_at")
    private String publishedAt;

    @PropertyName("urlToImage")
    private String urlToImage;

    @PropertyName("category_id")
    private String categoryId;

    @PropertyName("tags")
    private List<String> tags;
    @PropertyName("views")
    private int views;
    private int likesCount;
    @PropertyName("liked_by")
    private List<String> likedBy;
    @PropertyName("saved_by")
    private List<String> savedBy;
    @PropertyName("is_featured")
    private boolean isFeatured;
    @PropertyName("status")
    private String status;
    private String imagePublicId;

    public Articles() {

    }

    public Articles( String link, String summary, String title, String content, String author, String publishedAt, String urlToImage, String categoryId, List<String> tags, int views, int likesCount, List<String> likedBy, boolean isFeatured, String status) {
        this.link = link;
        this.summary = summary;
        this.title = title;
        this.content = content;
        this.author = author;
        this.publishedAt = formatDate(publishedAt);
        this.urlToImage = urlToImage;
        this.categoryId = categoryId;
        this.tags = tags;
        this.views = views;
        this.likesCount = likesCount;
        this.likedBy = likedBy;
        this.isFeatured = isFeatured;
        this.status = status;
    }

    public String getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(String authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getImagePublicId() {
        return imagePublicId;
    }

    public void setImagePublicId(String imagePublicId) {
        this.imagePublicId = imagePublicId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(String publishedAt) {
        this.publishedAt = publishedAt;
    }

    public String getUrlToImage() {
        return urlToImage;
    }

    public void setUrlToImage(String urlToImage) {
        this.urlToImage = urlToImage;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int  getLikesCount() {
        return likesCount;
    }

    public void setLikesCount(int  likesCount) {
        this.likesCount = likesCount;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public List<String> getSavedBy() {
        return savedBy;
    }

    public void setSavedBy(List<String> savedBy) {
        this.savedBy = savedBy;
    }

    public boolean isFeatured() {
        return isFeatured;
    }

    public void setFeatured(boolean featured) {
        isFeatured = featured;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static String formatDate(String inputDate) {
        try {

            SimpleDateFormat inputFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
            Date date = inputFormat.parse(inputDate);


            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());


            return outputFormat.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }





}
