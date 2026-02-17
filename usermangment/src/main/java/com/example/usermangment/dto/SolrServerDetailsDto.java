package com.example.usermangment.dto;

import java.util.List;

public class SolrServerDetailsDto {

    private String name;
    private String baseUrl;
    private String host;
    private int port;
    private String status;
    private int cpu;
    private int memory;
    private List<SolrCoreDto> cores;
    private long totalDocs;
    private long totalSizeInBytes;
    private List<String> alerts;
    private String error;

    public SolrServerDetailsDto(
            String name,
            String baseUrl,
            String host,
            int port,
            String status,
            int cpu,
            int memory,
            List<SolrCoreDto> cores,
            long totalDocs,
            long totalSizeInBytes,
            List<String> alerts,
            String error
    ) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.host = host;
        this.port = port;
        this.status = status;
        this.cpu = cpu;
        this.memory = memory;
        this.cores = cores;
        this.totalDocs = totalDocs;
        this.totalSizeInBytes = totalSizeInBytes;
        this.alerts = alerts;
        this.error = error;
    }

    // ✅ GETTERS (OBLIGATOIRES)

    public String getName() {
        return name;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getStatus() {
        return status;
    }

    public int getCpu() {
        return cpu;
    }

    public int getMemory() {
        return memory;
    }

    public List<SolrCoreDto> getCores() {
        return cores;
    }

    public long getTotalDocs() {
        return totalDocs;
    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public List<String> getAlerts() {
        return alerts;
    }

    public String getError() {
        return error;
    }
}
