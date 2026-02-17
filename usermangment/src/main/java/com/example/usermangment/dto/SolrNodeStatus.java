package com.example.usermangment.dto;

public record SolrNodeStatus(
        String name,
        String baseUrl,
        boolean alive,
        long latencyMs,
        String error
) {}

