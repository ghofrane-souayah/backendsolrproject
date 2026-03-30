package com.example.usermangment.dto;

import java.time.LocalDateTime;

public class SolrInstanceDto {
    private Long id;
    private String name;
    private String host;
    private Integer port;
    private String status;
    private LocalDateTime createdAt;
    private Long companyId;

    public SolrInstanceDto() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getHost() { return host; }
    public Integer getPort() { return port; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getCompanyId() { return companyId; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setHost(String host) { this.host = host; }
    public void setPort(Integer port) { this.port = port; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}
