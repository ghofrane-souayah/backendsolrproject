package com.example.usermangment.dto;

import java.time.LocalDateTime;
import java.util.List;

public class SolrServerDto {

    private Long id;
    private String name;
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

    private LocalDateTime lastHealthCheckTime;

    public SolrServerDto() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getHost() { return host; }
    public void setHost(String host) { this.host = host; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getCpu() { return cpu; }
    public void setCpu(int cpu) { this.cpu = cpu; }

    public int getMemory() { return memory; }
    public void setMemory(int memory) { this.memory = memory; }

    public List<SolrCoreDto> getCores() { return cores; }
    public void setCores(List<SolrCoreDto> cores) { this.cores = cores; }

    public long getTotalDocs() { return totalDocs; }
    public void setTotalDocs(long totalDocs) { this.totalDocs = totalDocs; }

    public long getTotalSizeInBytes() { return totalSizeInBytes; }
    public void setTotalSizeInBytes(long totalSizeInBytes) { this.totalSizeInBytes = totalSizeInBytes; }

    public List<String> getAlerts() { return alerts; }
    public void setAlerts(List<String> alerts) { this.alerts = alerts; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public LocalDateTime getLastHealthCheckTime() { return lastHealthCheckTime; }
    public void setLastHealthCheckTime(LocalDateTime lastHealthCheckTime) { this.lastHealthCheckTime = lastHealthCheckTime; }
}