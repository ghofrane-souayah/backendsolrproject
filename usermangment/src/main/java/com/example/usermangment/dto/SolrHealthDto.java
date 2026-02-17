package com.example.usermangment.dto;

public class SolrHealthDto {
    private String name;
    private String status; // UP / DOWN
    private long responseTimeMs;

    public SolrHealthDto() {}

    public SolrHealthDto(String name, String status, long responseTimeMs) {
        this.name = name;
        this.status = status;
        this.responseTimeMs = responseTimeMs;
    }

    public String getName() { return name; }
    public String getStatus() { return status; }
    public long getResponseTimeMs() { return responseTimeMs; }

    public void setName(String name) { this.name = name; }
    public void setStatus(String status) { this.status = status; }
    public void setResponseTimeMs(long responseTimeMs) { this.responseTimeMs = responseTimeMs; }
}
