package com.example.usermangment.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solr_instances")
public class SolrInstance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 255)
    private String host;

    @Column(nullable = false)
    private Integer port;

    @Column(length = 20)
    private String status; // UP / DOWN

    @Column(name = "instance_path", length = 500)
    private String instancePath;

    @Column(name = "core_path", length = 500)
    private String corePath;

    @Column(name = "image_path", length = 500)
    private String imagePath;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    public SolrInstance() {}

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getHost() { return host; }
    public Integer getPort() { return port; }
    public String getStatus() { return status; }
    public String getInstancePath() { return instancePath; }
    public String getCorePath() { return corePath; }
    public String getImagePath() { return imagePath; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Company getCompany() { return company; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHost(String host) { this.host = host; }
    public void setPort(Integer port) { this.port = port; }
    public void setStatus(String status) { this.status = status; }
    public void setInstancePath(String instancePath) { this.instancePath = instancePath; }
    public void setCorePath(String corePath) { this.corePath = corePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }
    public void setCompany(Company company) { this.company = company; }
}