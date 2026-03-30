package com.example.usermangment.controller;

import com.example.usermangment.dto.SolrHealthDto;
import com.example.usermangment.dto.SolrSchemaFieldsResponse;
import com.example.usermangment.dto.SolrSchemaTypesResponse;
import com.example.usermangment.dto.SolrServerDetailsDto;
import com.example.usermangment.service.SolrMonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solr/servers")
@CrossOrigin(origins = {"http://localhost:3000"})
public class SolrServerController {

    private final SolrMonitoringService service;

    public SolrServerController(SolrMonitoringService service) {
        this.service = service;
    }

    // ✅ DETAILS SERVEUR (PAR ID)
    @GetMapping("/{id}")
    public SolrServerDetailsDto details(@PathVariable Long id) {
        return service.getServerById(id);
    }

    // ✅ HEALTH SERVEUR (PAR ID)
    @GetMapping("/{id}/health")
    public SolrHealthDto health(@PathVariable Long id) {
        SolrServerDetailsDto d = service.getServerById(id);
        String status = (d.getStatus() != null ? d.getStatus() : "DOWN");
        return new SolrHealthDto(d.getName(), status, 0);
    }

    // ✅ SCHEMA FIELDS (PAR ID)
    @GetMapping("/{id}/cores/{core}/schema/fields")
    public SolrSchemaFieldsResponse schemaFields(@PathVariable Long id,
                                                 @PathVariable String core) {
        return service.getSchemaFieldsById(id, core);
    }

    // ✅ SCHEMA TYPES (PAR ID)
    @GetMapping("/{id}/cores/{core}/schema/types")
    public SolrSchemaTypesResponse schemaTypes(@PathVariable Long id,
                                               @PathVariable String core) {
        return service.getSchemaTypesById(id, core);
    }
}