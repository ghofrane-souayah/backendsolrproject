package com.example.usermangment.model;

import java.sql.Timestamp;

public class Report {

    private Long id;
    private String name;
    private String type;
    private Long companyId;
    private Long solrInstanceId;
    private String content;
    private Timestamp createdAt;

    public Report() {
    }

    public Report(
            Long id,
            String name,
            String type,
            Long companyId,
            Long solrInstanceId,
            String content,
            Timestamp createdAt
    ) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.companyId = companyId;
        this.solrInstanceId = solrInstanceId;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public Long getSolrInstanceId() {
        return solrInstanceId;
    }

    public void setSolrInstanceId(Long solrInstanceId) {
        this.solrInstanceId = solrInstanceId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}