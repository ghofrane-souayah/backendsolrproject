package com.example.usermangment.service;
// Ajoute ces imports en haut
import com.example.usermangment.dto.SolrFieldDto;
import com.example.usermangment.dto.SolrFieldTypeDto;
import com.example.usermangment.dto.SolrSchemaFieldsResponse;
import com.example.usermangment.dto.SolrSchemaTypesResponse;

import com.example.usermangment.config.SolrMonitoringProperties;
import com.example.usermangment.dto.SolrCoreDto;
import com.example.usermangment.dto.SolrHealthDto;
import com.example.usermangment.dto.SolrMonitoringResponse;
import com.example.usermangment.dto.SolrServerDetailsDto;
import com.example.usermangment.dto.SolrServerDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class SolrMonitoringService {

    private final SolrMonitoringProperties props;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final int CPU_WARN = 80;
    private static final int MEM_WARN = 80;

    public SolrMonitoringService(SolrMonitoringProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    // =========================================================
    // ✅ 1) MONITORING CLUSTER
    // =========================================================
    // ============================
// ✅ SCHEMA: FIELDS
// ============================
    public SolrSchemaFieldsResponse getSchemaFields(String serverName, String core) {
        var node = props.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(serverName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverName));

        String url = node.getBaseUrl() + "/solr/" + core + "/schema/fields?wt=json";
        String jsonStr = restTemplate.getForObject(url, String.class);

        List<SolrFieldDto> fields = new ArrayList<>();
        if (jsonStr == null || jsonStr.isBlank()) return new SolrSchemaFieldsResponse(fields);

        try {
            JsonNode root = mapper.readTree(jsonStr);
            JsonNode arr = root.path("fields");
            if (arr.isArray()) {
                for (JsonNode f : arr) {
                    fields.add(new SolrFieldDto(
                            f.path("name").asText(""),
                            f.path("type").asText(""),
                            f.path("stored").asBoolean(false),
                            f.path("indexed").asBoolean(false),
                            f.path("multiValued").asBoolean(false),
                            f.path("required").asBoolean(false)
                    ));
                }
            }
        } catch (Exception e) {
            // si parsing échoue => on retourne liste vide
        }

        return new SolrSchemaFieldsResponse(fields);
    }

    // ============================
// ✅ SCHEMA: TYPES (fieldTypes)
// ============================
    public SolrSchemaTypesResponse getSchemaTypes(String serverName, String core) {
        var node = props.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(serverName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + serverName));

        String url = node.getBaseUrl() + "/solr/" + core + "/schema/fieldtypes?wt=json";
        String jsonStr = restTemplate.getForObject(url, String.class);

        List<SolrFieldTypeDto> types = new ArrayList<>();
        if (jsonStr == null || jsonStr.isBlank()) return new SolrSchemaTypesResponse(types);

        try {
            JsonNode root = mapper.readTree(jsonStr);
            JsonNode arr = root.path("fieldTypes");
            if (arr.isArray()) {
                for (JsonNode t : arr) {
                    String name = t.path("name").asText("");
                    String clazz = t.path("class").asText(""); // Solr renvoie "class"
                    types.add(new SolrFieldTypeDto(name, clazz));
                }
            }
        } catch (Exception e) {
            // parsing fail => liste vide
        }

        return new SolrSchemaTypesResponse(types);
    }

    public SolrMonitoringResponse monitor() {
        List<SolrServerDto> nodes = new ArrayList<>();

        for (SolrMonitoringProperties.Node node : props.getNodes()) {
            // on réutilise la même logique que "details"
            SolrServerDetailsDto details = buildServerDetails(node);

            SolrServerDto dto = new SolrServerDto();
            dto.setName(details.getName());
            dto.setHost(details.getHost());
            dto.setPort(details.getPort());
            dto.setStatus(details.getStatus());
            dto.setCpu(details.getCpu());
            dto.setMemory(details.getMemory());
            dto.setCores(details.getCores());
            dto.setTotalDocs(details.getTotalDocs());
            dto.setTotalSizeInBytes(details.getTotalSizeInBytes());
            dto.setAlerts(details.getAlerts());
            dto.setError(details.getError());

            nodes.add(dto);
        }

        return new SolrMonitoringResponse(Instant.now(), nodes);
    }

    // =========================================================
    // ✅ 2) ETAPE 2 : DETAILS SERVEUR + HEALTH
    // =========================================================
    public SolrServerDetailsDto getServerByName(String name) {
        SolrMonitoringProperties.Node node = props.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + name));

        return buildServerDetails(node);
    }

    public SolrHealthDto health(String name) {
        SolrMonitoringProperties.Node node = props.getNodes().stream()
                .filter(n -> n.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Server not found: " + name));

        long start = System.currentTimeMillis();
        try {
            String url = node.getBaseUrl() + "/solr/admin/info/system?wt=json";
            restTemplate.getForObject(url, String.class);
            long ms = System.currentTimeMillis() - start;
            return new SolrHealthDto(node.getName(), "UP", ms);
        } catch (Exception e) {
            long ms = System.currentTimeMillis() - start;
            return new SolrHealthDto(node.getName(), "DOWN", ms);
        }
    }

    // =========================================================
    // ✅ Build details (1 node)
    // =========================================================
    private SolrServerDetailsDto buildServerDetails(SolrMonitoringProperties.Node node) {
        URI uri = URI.create(node.getBaseUrl());

        String status;
        int cpu = 0;
        int mem = 0;
        List<SolrCoreDto> cores = List.of();
        long totalDocs = 0;
        long totalSize = 0;
        List<String> alerts = new ArrayList<>();
        String error = null;

        try {
            // 1) Ping
            String pingUrl = node.getBaseUrl() + "/solr/admin/info/system?wt=json";
            restTemplate.getForObject(pingUrl, String.class);
            status = "UP";

            // 2) CPU/Mem
            String metricsUrl = node.getBaseUrl() + "/solr/admin/metrics?group=jvm&wt=json";
            String metricsJsonStr = restTemplate.getForObject(metricsUrl, String.class);

            if (metricsJsonStr != null && !metricsJsonStr.isBlank()) {
                JsonNode root = mapper.readTree(metricsJsonStr);
                cpu = extractCpuPercent(root);
                mem = extractHeapPercent(root);
            }

            // 3) Cores + totals
            cores = fetchCores(node.getBaseUrl());
            for (SolrCoreDto c : cores) {
                totalDocs += c.getNumDocs();
                totalSize += c.getSizeInBytes();
            }

            // 4) Alerts
            if (cpu >= CPU_WARN) alerts.add("HIGH_CPU");
            if (mem >= MEM_WARN) alerts.add("HIGH_MEMORY");
            if (cores.isEmpty()) alerts.add("NO_CORES");

        } catch (Exception e) {
            status = "DOWN";
            alerts = List.of("NODE_DOWN");
            String msg = e.getMessage();
            if (msg != null && msg.length() > 250) msg = msg.substring(0, 250);
            error = msg;
        }

        return new SolrServerDetailsDto(
                node.getName(),
                node.getBaseUrl(),
                uri.getHost(),
                uri.getPort(),
                status,
                cpu,
                mem,
                cores,
                totalDocs,
                totalSize,
                alerts,
                error
        );
    }

    // =========================================================
    // ✅ CPU / Memory
    // =========================================================
    private int extractCpuPercent(JsonNode root) {
        JsonNode solrJvm = root.path("metrics").path("solr.jvm");
        if (!solrJvm.isObject()) return 0;

        JsonNode sys = solrJvm.path("os.systemCpuLoad");
        JsonNode proc = solrJvm.path("os.processCpuLoad");

        double v = -1;
        if (sys.isNumber()) v = sys.asDouble();
        if (v <= 0 && proc.isNumber()) v = proc.asDouble();

        if (v < 0) return 0;

        double percent = v * 100.0;
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        return (int) Math.round(percent);
    }

    private int extractHeapPercent(JsonNode root) {
        JsonNode solrJvm = root.path("metrics").path("solr.jvm");
        if (!solrJvm.isObject()) return 0;

        JsonNode usedNode = solrJvm.path("memory.heap.used");
        JsonNode maxNode = solrJvm.path("memory.heap.max");
        if (!usedNode.isNumber() || !maxNode.isNumber()) return 0;

        long used = usedNode.asLong();
        long max = maxNode.asLong();
        if (max <= 0) return 0;

        double percent = (used * 100.0) / max;
        if (percent < 0) percent = 0;
        if (percent > 100) percent = 100;

        return (int) Math.round(percent);
    }

    // =========================================================
    // ✅ Cores
    // =========================================================
    private List<SolrCoreDto> fetchCores(String baseUrl) {
        try {
            String coresUrl = baseUrl + "/solr/admin/cores?action=STATUS&wt=json";
            String jsonStr = restTemplate.getForObject(coresUrl, String.class);
            if (jsonStr == null || jsonStr.isBlank()) return List.of();

            JsonNode root = mapper.readTree(jsonStr);
            JsonNode status = root.path("status");
            if (!status.isObject()) return List.of();

            List<SolrCoreDto> cores = new ArrayList<>();
            Iterator<Map.Entry<String, JsonNode>> fields = status.fields();

            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String coreName = entry.getKey();
                JsonNode coreObj = entry.getValue();
                JsonNode index = coreObj.path("index");

                long numDocs = index.path("numDocs").asLong(0);
                long deletedDocs = index.path("deletedDocs").asLong(0);
                long sizeInBytes = index.path("sizeInBytes").asLong(0);

                cores.add(new SolrCoreDto(coreName, numDocs, deletedDocs, sizeInBytes));
            }

            return cores;
        } catch (Exception e) {
            return List.of();
        }
    }
}
