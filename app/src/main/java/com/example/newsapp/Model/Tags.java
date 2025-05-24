package com.example.newsapp.Model;

import androidx.annotation.NonNull;


import java.io.Serializable;

public class Tags implements Serializable {

    private String tagId;
    private String name;
    private String description;

    public Tags() {
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
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
