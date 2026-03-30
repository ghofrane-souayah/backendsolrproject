package com.example.usermangment.service;

import com.example.usermangment.config.SolrMonitoringProperties;
import com.example.usermangment.dto.*;
import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.repository.SolrInstanceRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.Instant;
import java.util.*;

@Service
public class SolrMonitoringService {

    private final SolrMonitoringProperties props;
    private final RestTemplate restTemplate;
    private final SolrInstanceRepository solrInstanceRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final int CPU_WARN = 80;
    private static final int MEM_WARN = 80;

    public SolrMonitoringService(SolrMonitoringProperties props,
                                 RestTemplate restTemplate,
                                 SolrInstanceRepository solrInstanceRepository) {
        this.props = props;
        this.restTemplate = restTemplate;
        this.solrInstanceRepository = solrInstanceRepository;
    }

    public SolrServerDetailsDto getServerById(Long id) {
        SolrInstance inst = solrInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Server not found: " + id));

        SolrMonitoringProperties.Node node = new SolrMonitoringProperties.Node();
        node.setName(inst.getName());
        node.setBaseUrl("http://" + inst.getHost() + ":" + inst.getPort());

        return buildServerDetails(node);
    }

    public SolrSchemaFieldsResponse getSchemaFieldsById(Long id, String core) {
        SolrInstance inst = solrInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Server not found: " + id));

        String baseUrl = "http://" + inst.getHost() + ":" + inst.getPort();
        String url = baseUrl + "/solr/" + core + "/schema/fields?wt=json";

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
        } catch (Exception ignored) {}

        return new SolrSchemaFieldsResponse(fields);
    }

    public SolrSchemaTypesResponse getSchemaTypesById(Long id, String core) {
        SolrInstance inst = solrInstanceRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Server not found: " + id));

        String baseUrl = "http://" + inst.getHost() + ":" + inst.getPort();
        String url = baseUrl + "/solr/" + core + "/schema/fieldtypes?wt=json";

        String jsonStr = restTemplate.getForObject(url, String.class);
        List<SolrFieldTypeDto> types = new ArrayList<>();
        if (jsonStr == null || jsonStr.isBlank()) return new SolrSchemaTypesResponse(types);

        try {
            JsonNode root = mapper.readTree(jsonStr);
            JsonNode arr = root.path("fieldTypes");
            if (arr.isArray()) {
                for (JsonNode t : arr) {
                    types.add(new SolrFieldTypeDto(
                            t.path("name").asText(""),
                            t.path("class").asText("")
                    ));
                }
            }
        } catch (Exception ignored) {}

        return new SolrSchemaTypesResponse(types);
    }

    public SolrMonitoringResponse monitor(List<SolrInstance> instances) {
        List<SolrServerDto> nodes = new ArrayList<>();
        if (instances == null) return new SolrMonitoringResponse(Instant.now(), nodes);

        for (SolrInstance inst : instances) {
            if (inst == null) continue;

            String baseUrl = "http://" + inst.getHost() + ":" + inst.getPort();

            SolrMonitoringProperties.Node node = new SolrMonitoringProperties.Node();
            node.setName(inst.getName());
            node.setBaseUrl(baseUrl);

            SolrServerDetailsDto details = buildServerDetails(node);

            SolrServerDto dto = new SolrServerDto();
            dto.setId(inst.getId());
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

    private SolrServerDetailsDto buildServerDetails(SolrMonitoringProperties.Node node) {
        URI uri = URI.create(node.getBaseUrl());

        String status = "DOWN";
        int cpu = 0;
        int mem = 0;
        List<SolrCoreDto> cores = List.of();
        long totalDocs = 0;
        long totalSize = 0;
        List<String> alerts = new ArrayList<>();
        String error = null;

        String pingError = null;
        boolean up = false;

        // retry ping système
        for (int i = 0; i < 3; i++) {
            try {
                String pingUrl = node.getBaseUrl() + "/solr/admin/info/system?wt=json";
                String response = restTemplate.getForObject(pingUrl, String.class);

                if (response != null && !response.isBlank()) {
                    up = true;
                    break;
                }
            } catch (Exception e) {
                pingError = e.getMessage();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    pingError = "Interrupted while checking Solr status";
                    break;
                }
            }
        }

        if (!up) {
            status = "DOWN";
            alerts = new ArrayList<>();
            alerts.add("NODE_DOWN");

            String msg = pingError;
            if (msg != null && msg.length() > 250) {
                msg = msg.substring(0, 250);
            }
            error = msg;

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

        status = "UP";
        error = null;

        try {
            String metricsUrl = node.getBaseUrl() + "/solr/admin/metrics?group=jvm&wt=json";
            String metricsJsonStr = restTemplate.getForObject(metricsUrl, String.class);

            if (metricsJsonStr != null && !metricsJsonStr.isBlank()) {
                JsonNode root = mapper.readTree(metricsJsonStr);
                cpu = extractCpuPercent(root);
                mem = extractHeapPercent(root);
            }
        } catch (Exception ignored) {
            cpu = 0;
            mem = 0;
        }

        try {
            cores = fetchCores(node.getBaseUrl());
            for (SolrCoreDto c : cores) {
                totalDocs += c.getNumDocs();
                totalSize += c.getSizeInBytes();
            }
        } catch (Exception ignored) {
            cores = List.of();
            totalDocs = 0;
            totalSize = 0;
        }

        if (cpu >= CPU_WARN) alerts.add("HIGH_CPU");
        if (mem >= MEM_WARN) alerts.add("HIGH_MEMORY");
        if (cores.isEmpty()) alerts.add("NO_CORES");

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