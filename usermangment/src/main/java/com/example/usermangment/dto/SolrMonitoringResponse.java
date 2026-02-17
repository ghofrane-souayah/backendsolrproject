package com.example.usermangment.dto;

import java.time.Instant;
import java.util.List;

public record SolrMonitoringResponse(
        Instant generatedAt,
        List<SolrServerDto> nodes
) {}
