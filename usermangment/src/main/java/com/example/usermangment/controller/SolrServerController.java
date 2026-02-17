package com.example.usermangment.controller;

import com.example.usermangment.dto.SolrHealthDto;
import com.example.usermangment.dto.SolrSchemaFieldsResponse;
import com.example.usermangment.dto.SolrSchemaTypesResponse;
import com.example.usermangment.dto.SolrServerDetailsDto;
import com.example.usermangment.service.SolrMonitoringService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solr/servers")
public class SolrServerController {

    private final SolrMonitoringService service;

    public SolrServerController(SolrMonitoringService service) {
        this.service = service;
    }

    // ✅ details serveur
    @GetMapping("/{name}")
    public SolrServerDetailsDto details(@PathVariable String name) {
        return service.getServerByName(name);
    }

    // ✅ health serveur
    @GetMapping("/{name}/health")
    public SolrHealthDto health(@PathVariable String name) {
        return service.health(name);
    }

    // ✅ schema fields
    @GetMapping("/{name}/cores/{core}/schema/fields")
    public SolrSchemaFieldsResponse schemaFields(@PathVariable String name, @PathVariable String core) {
        return service.getSchemaFields(name, core);
    }

    // ✅ schema types
    @GetMapping("/{name}/cores/{core}/schema/types")
    public SolrSchemaTypesResponse schemaTypes(@PathVariable String name, @PathVariable String core) {
        return service.getSchemaTypes(name, core);
    }
}
