package com.example.newsapp.Model;


import com.google.firebase.firestore.PropertyName;

import java.io.Serializable;


public class Categories implements Serializable {

    private String categoryId;

    @PropertyName("name")
    private String name;

    @PropertyName("description")
    private String description;

    @PropertyName("image")
    private String image;

    public Categories() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
