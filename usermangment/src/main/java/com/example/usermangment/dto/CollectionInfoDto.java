package com.example.usermangment.dto;

public class CollectionInfoDto {

    private String name;
    private String configName;
    private Integer numShards;
    private Integer replicationFactor;
    private Long numDocs;
    private Long sizeInBytes;

    public CollectionInfoDto() {
    }

    public CollectionInfoDto(
            String name,
            String configName,
            Integer numShards,
            Integer replicationFactor,
            Long numDocs,
            Long sizeInBytes
    ) {
        this.name = name;
        this.configName = configName;
        this.numShards = numShards;
        this.replicationFactor = replicationFactor;
        this.numDocs = numDocs;
        this.sizeInBytes = sizeInBytes;
    }

    public String getName() {
        return name;
    }

    public String getConfigName() {
        return configName;
    }

    public Integer getNumShards() {
        return numShards;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public Long getNumDocs() {
        return numDocs;
    }

    public Long getSizeInBytes() {
        return sizeInBytes;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }

    public void setNumShards(Integer numShards) {
        this.numShards = numShards;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }

    public void setNumDocs(Long numDocs) {
        this.numDocs = numDocs;
    }

    public void setSizeInBytes(Long sizeInBytes) {
        this.sizeInBytes = sizeInBytes;
    }
}