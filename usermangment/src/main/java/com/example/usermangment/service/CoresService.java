package com.example.usermangment.service;

import com.example.usermangment.dto.CollectionInfoDto;
import com.example.usermangment.dto.CollectionsResponseDto;
import com.example.usermangment.dto.CreateCollectionRequest;
import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.repository.SolrInstanceRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CoresService {

    private final SolrInstanceRepository solrInstanceRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    public CoresService(SolrInstanceRepository solrInstanceRepository) {
        this.solrInstanceRepository = solrInstanceRepository;
    }

    public CollectionsResponseDto listCores(Long serverId) {
        SolrInstance server = findServer(serverId);

        String url = baseUrl(server) + "/solr/admin/cores?action=STATUS&wt=json";
        Map<String, Object> resp = getMap(url);

        Map<String, Object> status = mapValue(resp.get("status"));
        List<CollectionInfoDto> result = new ArrayList<>();

        for (Map.Entry<String, Object> entry : status.entrySet()) {
            String coreName = entry.getKey();
            Map<String, Object> coreMap = mapValue(entry.getValue());
            Map<String, Object> index = mapValue(coreMap.get("index"));

            Long numDocs = longValue(index.get("numDocs"));
            Long sizeInBytes = longValue(index.get("sizeInBytes"));

            result.add(new CollectionInfoDto(
                    coreName,
                    "-",
                    1,
                    1,
                    numDocs,
                    sizeInBytes
            ));
        }

        CollectionsResponseDto.ServerMiniDto serverDto =
                new CollectionsResponseDto.ServerMiniDto(
                        server.getId(),
                        server.getName(),
                        server.getHost(),
                        server.getPort()
                );

        return new CollectionsResponseDto(serverDto, result);
    }

    public void createCore(CreateCollectionRequest req) {
        if (req.getServerId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serverId is required");
        }
        if (req.getName() == null || req.getName().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }

        SolrInstance server = findServer(req.getServerId());
        String coreName = req.getName().trim();

        String url = baseUrl(server)
                + "/solr/admin/cores?action=CREATE"
                + "&name=" + encode(coreName)
                + "&instanceDir=" + encode(coreName)
                + "&configSet=_default"
                + "&wt=json";

        try {
            Map<String, Object> resp = getMap(url);
            ensureSolrSuccess(resp, "create core failed");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
    public void deleteCore(Long serverId, String name) {
        if (serverId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "serverId is required");
        }
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "core name is required");
        }

        SolrInstance server = findServer(serverId);

        String url = baseUrl(server)
                + "/solr/admin/cores?action=UNLOAD"
                + "&core=" + encode(name)
                + "&deleteIndex=true"
                + "&wt=json";

        Map<String, Object> resp = getMap(url);
        ensureSolrSuccess(resp, "delete core failed");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getMap(String url) {
        Map<String, Object> body = restTemplate.getForObject(url, Map.class);
        if (body == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "empty response from Solr");
        }
        return body;
    }

    private SolrInstance findServer(Long serverId) {
        return solrInstanceRepository.findById(serverId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "server not found"));
    }

    private String baseUrl(SolrInstance server) {
        return "http://" + server.getHost() + ":" + server.getPort();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private void ensureSolrSuccess(Map<String, Object> resp, String defaultMessage) {
        if (resp.get("error") != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    defaultMessage + ": " + resp.get("error")
            );
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Collections.emptyMap();
    }

    private long longValue(Object value) {
        if (value == null) return 0L;
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (Exception e) {
            return 0L;
        }
    }
}