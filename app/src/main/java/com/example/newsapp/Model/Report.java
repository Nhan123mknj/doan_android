package com.example.newsapp.Model;

import com.google.firebase.firestore.PropertyName;
import java.io.Serializable;

public class Report implements Serializable {
    
    private String reportId;
    
    @PropertyName("article_id")
    private String articleId;
    
    @PropertyName("article_title")
    private String articleTitle;
    
    @PropertyName("reporter_id")
    private String reporterId;
    
    @PropertyName("reporter_name")
    private String reporterName;
    
    @PropertyName("reason")
    private String reason;
    
    @PropertyName("description")
    private String description;
    
    @PropertyName("reported_at")
    private long reportedAt;
    
    @PropertyName("status")
    private String status; // pending, reviewed, resolved, dismissed
    
    @PropertyName("admin_notes")
    private String adminNotes;

    public Report() {
        // Required empty constructor for Firestore
    }

    public Report(String articleId, String articleTitle, String reporterId, String reporterName, 
                  String reason, String description, long reportedAt) {
        this.articleId = articleId;
        this.articleTitle = articleTitle;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reason = reason;
        this.description = description;
        this.reportedAt = reportedAt;
        this.status = "pending";
    }

    // Getters and Setters
    public String getReportId() {
        return reportId;
    }

    public void setReportId(String reportId) {
        this.reportId = reportId;
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getArticleTitle() {
        return articleTitle;
    }

    public void setArticleTitle(String articleTitle) {
        this.articleTitle = articleTitle;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getReportedAt() {
        return reportedAt;
    }

    public void setReportedAt(long reportedAt) {
        this.reportedAt = reportedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAdminNotes() {
        return adminNotes;
    }

    public void setAdminNotes(String adminNotes) {
        this.adminNotes = adminNotes;
    }
} 