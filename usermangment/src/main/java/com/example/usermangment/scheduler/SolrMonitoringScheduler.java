package com.example.usermangment.scheduler;

import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.repository.SolrInstanceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
@Slf4j
public class SolrMonitoringScheduler {

    private final SolrInstanceRepository repo;
    private final RestTemplate restTemplate;

    public SolrMonitoringScheduler(SolrInstanceRepository repo, RestTemplate restTemplate) {
        this.repo = repo;
        this.restTemplate = restTemplate;
    }

    @Scheduled(initialDelay = 2000, fixedDelay = 10000)
    @Transactional
    public void monitorInstances() {
        List<SolrInstance> instances = repo.findByStatusNotIgnoreCase("DISABLED");

        log.info("Monitoring Solr: {} instance(s) à vérifier", instances.size());

        for (SolrInstance s : instances) {
            String newStatus = ping(s) ? "UP" : "DOWN";

            if (s.getStatus() == null || !s.getStatus().equalsIgnoreCase(newStatus)) {
                s.setStatus(newStatus);
                log.info("Instance {} ({}) -> {}", s.getId(), s.getName(), newStatus);

                // facultatif mais plus explicite
                repo.save(s);
            } else {
                log.info("Instance {} ({}) reste {}", s.getId(), s.getName(), newStatus);
            }
        }
    }

    private boolean ping(SolrInstance s) {
        try {
            String url = buildSolrUrl(s);

            log.info("Ping Solr instance {} -> {}", s.getName(), url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();

        } catch (Exception e) {
            log.error("Ping échoué pour l'instance {} : {}", s.getName(), e.getMessage());
            return false;
        }
    }

    private String buildSolrUrl(SolrInstance s) {
        String host = s.getHost() == null ? "" : s.getHost().trim();
        Integer port = s.getPort();

        if (host.isBlank()) {
            throw new IllegalArgumentException("Host vide pour l'instance " + s.getName());
        }

        if (port == null) {
            throw new IllegalArgumentException("Port vide pour l'instance " + s.getName());
        }

        if (host.startsWith("http://")) {
            host = host.substring(7);
        } else if (host.startsWith("https://")) {
            host = host.substring(8);
        }

        if (host.endsWith("/")) {
            host = host.substring(0, host.length() - 1);
        }

        if (host.endsWith("/solr")) {
            host = host.substring(0, host.length() - 5);
        }

        int slashIndex = host.indexOf('/');
        if (slashIndex != -1) {
            host = host.substring(0, slashIndex);
        }

        int colonIndex = host.indexOf(':');
        if (colonIndex != -1) {
            host = host.substring(0, colonIndex);
        }

        return "http://" + host + ":" + port + "/solr/admin/info/system?wt=json";
    }

}