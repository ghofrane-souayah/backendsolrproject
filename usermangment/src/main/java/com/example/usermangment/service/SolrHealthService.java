package com.example.usermangment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.repository.SolrInstanceRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SolrHealthService {

    private static final Logger log = LoggerFactory.getLogger(SolrHealthService.class);

    private final SolrInstanceRepository solrInstanceRepository;
    private final RestTemplate restTemplate;

    @Scheduled(fixedDelayString = "${solr.scheduler.delay-ms:30000}")
    public void checkAllInstancesHealth() {
        List<SolrInstance> instances = solrInstanceRepository.findByStatusNotIgnoreCase("DISABLED");

        for (SolrInstance instance : instances) {
            if (instance == null) {
                continue;
            }

            boolean healthy = isSolrHealthy(instance);
            String newStatus = healthy ? "UP" : "DOWN";

            instance.setLastHealthCheckTime(LocalDateTime.now());

            if (instance.getStatus() == null || !newStatus.equalsIgnoreCase(instance.getStatus())) {
                log.debug("Solr instance '{}' status changed from {} to {}",
                        instance.getName(), instance.getStatus(), newStatus);
                instance.setStatus(newStatus);
            }

            solrInstanceRepository.save(instance);
        }
    }

    public boolean isSolrHealthy(SolrInstance instance) {
        try {
            String url = "http://" + instance.getHost() + ":" + instance.getPort()
                    + "/solr/admin/info/system?wt=json";

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            log.debug("Health check success for {}:{} with status {}",
                    instance.getHost(), instance.getPort(), response.getStatusCode().value());

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            log.debug("Health check failed for {}:{} - {}",
                    instance.getHost(), instance.getPort(), e.getMessage());
            return false;
        }
    }
}