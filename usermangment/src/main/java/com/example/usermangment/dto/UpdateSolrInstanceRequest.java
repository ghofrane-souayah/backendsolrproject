package com.example.usermangment.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class UpdateSolrInstanceRequest {

    private String name;
    private String host;

    @Min(1)
    @Max(65535)
    private Integer port;

    private String status;

    private String instancePath;
    private String corePath;
    private String imagePath;

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public String getInstancePath() {
        return instancePath;
    }

    public String getCorePath() {
        return corePath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setInstancePath(String instancePath) {
        this.instancePath = instancePath;
    }

    public void setCorePath(String corePath) {
        this.corePath = corePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }
}