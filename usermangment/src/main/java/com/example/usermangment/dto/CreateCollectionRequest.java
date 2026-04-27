package com.example.usermangment.dto;

public class CreateCollectionRequest {

    private Long serverId;
    private String name;
    private Integer numShards;
    private Integer replicationFactor;

    public CreateCollectionRequest() {
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getName() {
        return name;
    }

    public Integer getNumShards() {
        return numShards;
    }

    public Integer getReplicationFactor() {
        return replicationFactor;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumShards(Integer numShards) {
        this.numShards = numShards;
    }

    public void setReplicationFactor(Integer replicationFactor) {
        this.replicationFactor = replicationFactor;
    }
}