package com.example.usermangment.service;

import com.example.usermangment.config.SolrMonitoringProperties;
import com.example.usermangment.dto.AddFieldRequest;
import com.example.usermangment.dto.SchemaFieldDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SolrSchemaService {

    private final SolrMonitoringProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    public SolrSchemaService(SolrMonitoringProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    private SolrMonitoringProperties.Node findNode(String serverName) {
        return props.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(serverName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverName));
    }

    // ✅ LIST FIELDS
    public List<SchemaFieldDto> listFields(String serverName, String core) {
        var node = findNode(serverName);
        String url = node.getBaseUrl() + "/solr/" + core + "/schema/fields?wt=json";
        String json = restTemplate.getForObject(url, String.class);

        if (json == null || json.isBlank()) return List.of();

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode fields = root.path("fields");
            if (!fields.isArray()) return List.of();

            List<SchemaFieldDto> out = new ArrayList<>();
            for (JsonNode f : fields) {
                String name = f.path("name").asText("");
                String type = f.path("type").asText("");
                boolean stored = f.path("stored").asBoolean(false);
                boolean indexed = f.path("indexed").asBoolean(false);
                boolean multiValued = f.path("multiValued").asBoolean(false);

                List<String> copyDests = new ArrayList<>();
                JsonNode copy = f.path("copyDests");
                if (copy.isArray()) {
                    for (JsonNode d : copy) copyDests.add(d.asText());
                }

                out.add(new SchemaFieldDto(name, type, stored, indexed, multiValued, copyDests));
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ✅ LIST FIELD TYPES
    public List<String> listFieldTypes(String serverName, String core) {
        var node = findNode(serverName);
        String url = node.getBaseUrl() + "/solr/" + core + "/schema/fieldtypes?wt=json";
        String json = restTemplate.getForObject(url, String.class);

        if (json == null || json.isBlank()) return List.of();

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode types = root.path("fieldTypes");
            if (!types.isArray()) return List.of();

            List<String> out = new ArrayList<>();
            for (JsonNode t : types) {
                String name = t.path("name").asText("");
                if (!name.isBlank()) out.add(name);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ✅ LIST DYNAMIC FIELDS
    public List<String> listDynamicFields(String serverName, String core) {
        var node = findNode(serverName);
        String url = node.getBaseUrl() + "/solr/" + core + "/schema/dynamicfields?wt=json";
        String json = restTemplate.getForObject(url, String.class);

        if (json == null || json.isBlank()) return List.of();

        try {
            JsonNode root = mapper.readTree(json);
            JsonNode dfs = root.path("dynamicFields");
            if (!dfs.isArray()) return List.of();

            List<String> out = new ArrayList<>();
            for (JsonNode d : dfs) {
                String name = d.path("name").asText("");
                if (!name.isBlank()) out.add(name);
            }
            return out;
        } catch (Exception e) {
            return List.of();
        }
    }

    // ✅ ADD FIELD
    public Map<String, Object> addField(String serverName, String core, AddFieldRequest req) {
        Map<String, Object> payload = Map.of(
                "add-field", Map.of(
                        "name", req.getName(),
                        "type", req.getType(),
                        "stored", req.isStored(),
                        "indexed", req.isIndexed(),
                        "multiValued", req.isMultiValued()
                )
        );
        return postToSolrSchema(serverName, core, payload);
    }

    // ✅ DELETE FIELD (Solr: delete-field = POST)
    public Map<String, Object> deleteField(String serverName, String core, String fieldName) {
        Map<String, Object> payload = Map.of(
                "delete-field", Map.of("name", fieldName)
        );
        return postToSolrSchema(serverName, core, payload);
    }

    // ✅ UPDATE FIELD (Solr: replace-field = POST)
    public Map<String, Object> updateField(String serverName, String core, AddFieldRequest req) {
        Map<String, Object> fieldDef = new HashMap<>();
        fieldDef.put("name", req.getName());
        fieldDef.put("type", req.getType());
        fieldDef.put("stored", req.isStored());
        fieldDef.put("indexed", req.isIndexed());
        fieldDef.put("multiValued", req.isMultiValued());

        Map<String, Object> payload = Map.of("replace-field", fieldDef);
        return postToSolrSchema(serverName, core, payload);
    }

    // ✅ helper commun
    private Map<String, Object> postToSolrSchema(String serverName, String core, Map<String, Object> payload) {
        var node = findNode(serverName);
        String url = node.getBaseUrl() + "/solr/" + core + "/schema?wt=json";

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            @SuppressWarnings("unchecked")
            Map<String, Object> resp = restTemplate.postForObject(url, entity, Map.class);

            return resp == null ? Map.of("ok", false) : resp;

        } catch (HttpStatusCodeException ex) {
            return Map.of(
                    "ok", false,
                    "httpStatus", ex.getStatusCode().value(),
                    "responseBody", ex.getResponseBodyAsString()
            );
        }
    }
}
