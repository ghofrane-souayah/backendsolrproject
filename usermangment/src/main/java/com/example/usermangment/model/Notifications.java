package com.example.usermangment.model;

public class Notifications {
    private Long id;
    private String type;
    private String level;
    private String title;
    private String message;
    private String source;
    private String status;
    private java.sql.Timestamp createdAt;
    private Long companyId;

    public Notifications() {
    }

    public Notifications(
            Long id,
            String type,
            String level,
            String title,
            String message,
            String source,
            String status,
            java.sql.Timestamp createdAt,
            Long companyId
    ) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.title = title;
        this.message = message;
        this.source = source;
        this.status = status;
        this.createdAt = createdAt;
        this.companyId = companyId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public java.sql.Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(java.sql.Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}