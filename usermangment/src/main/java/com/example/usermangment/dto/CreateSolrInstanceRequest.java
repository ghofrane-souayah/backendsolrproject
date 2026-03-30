package com.example.usermangment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateSolrInstanceRequest {

    @NotBlank(message = "name is required")
    private String name;

    @NotBlank(message = "host is required")
    private String host;

    @NotNull(message = "port is required")
    @Min(value = 1, message = "port must be >= 1")
    @Max(value = 65535, message = "port must be <= 65535")
    private Integer port;

    // SUPER_ADMIN seulement : sera vérifié dans le service
    private Long companyId;

    // plus obligatoires : le backend les gère tout seul
    private String instancePath;
    private String corePath;
    private String imagePath;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public String getInstancePath() {
        return instancePath;
    }

    public void setInstancePath(String instancePath) {
        this.instancePath = instancePath;
    }

    public String getCorePath() {
        return corePath;
    }

    public void setCorePath(String corePath) {
        this.corePath = corePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}