package com.example.usermangment.controller;

import com.example.usermangment.service.DockerSolrService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/docker-solr")
@RequiredArgsConstructor
public class DockerSolrTestController {

    private final DockerSolrService dockerSolrService;

    @PostMapping("/start")
    public ResponseEntity<?> start(
            @RequestParam String name,
            @RequestParam int port
    ) {
        dockerSolrService.startContainer(name, port);

        return ResponseEntity.ok(Map.of(
                "message", "Conteneur Solr démarré",
                "name", name,
                "port", port
        ));
    }

    @DeleteMapping("/stop")
    public ResponseEntity<?> stop(@RequestParam String name) {
        dockerSolrService.stopAndRemoveContainer(name);

        return ResponseEntity.ok(Map.of(
                "message", "Conteneur supprimé",
                "name", name
        ));
    }
}