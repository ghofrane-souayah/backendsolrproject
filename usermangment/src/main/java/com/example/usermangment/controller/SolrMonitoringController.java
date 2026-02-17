package com.example.usermangment.controller;

import com.example.usermangment.dto.SolrMonitoringResponse;
import com.example.usermangment.service.SolrMonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solr")
public class SolrMonitoringController {

    private final SolrMonitoringService service;

    public SolrMonitoringController(SolrMonitoringService service) {
        this.service = service;
    }

    @GetMapping("/monitoring")
    public SolrMonitoringResponse monitoring() {
        return service.monitor();
    }
}
