package com.example.usermangment.controller;

import com.example.usermangment.dto.AddFieldRequest;
import com.example.usermangment.dto.SchemaFieldDto;
import com.example.usermangment.service.SolrSchemaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/solr")
@CrossOrigin(origins = "http://localhost:3000")
public class SolrSchemaController {

    private final SolrSchemaService schemaService;

    public SolrSchemaController(SolrSchemaService schemaService) {
        this.schemaService = schemaService;
    }

    @GetMapping("/servers/{serverName}/collections/{core}/schema/fields")
    public List<SchemaFieldDto> fields(@PathVariable String serverName, @PathVariable String core) {
        return schemaService.listFields(serverName, core);
    }

    @GetMapping("/servers/{serverName}/collections/{core}/schema/fieldtypes")
    public List<String> fieldTypes(@PathVariable String serverName, @PathVariable String core) {
        return schemaService.listFieldTypes(serverName, core);
    }

    @GetMapping("/servers/{serverName}/collections/{core}/schema/dynamicfields")
    public List<String> dynamicFields(@PathVariable String serverName, @PathVariable String core) {
        return schemaService.listDynamicFields(serverName, core);
    }

    // ✅ ADD
    @PostMapping("/servers/{serverName}/collections/{core}/schema/fields")
    public Map<String, Object> addField(
            @PathVariable String serverName,
            @PathVariable String core,
            @RequestBody AddFieldRequest req
    ) {
        return schemaService.addField(serverName, core, req);
    }

    // ✅ UPDATE (replace-field)
    @PutMapping("/servers/{serverName}/collections/{core}/schema/fields/{fieldName}")
    public Map<String, Object> updateField(
            @PathVariable String serverName,
            @PathVariable String core,
            @PathVariable String fieldName,
            @RequestBody AddFieldRequest req
    ) {
        // sécurité: le name du body doit correspondre au path
        req.setName(fieldName);
        return schemaService.updateField(serverName, core, req);
    }

    // ✅ DELETE
    @DeleteMapping("/servers/{serverName}/collections/{core}/schema/fields/{fieldName}")
    public Map<String, Object> deleteField(
            @PathVariable String serverName,
            @PathVariable String core,
            @PathVariable String fieldName
    ) {
        return schemaService.deleteField(serverName, core, fieldName);
    }
}
