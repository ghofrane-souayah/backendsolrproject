package com.example.usermangment.controller;

import com.example.usermangment.dto.CollectionsResponseDto;
import com.example.usermangment.dto.CreateCollectionRequest;
import com.example.usermangment.service.CoresService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solr/cores")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:3001"})
public class CoresController {

    private final CoresService coresService;

    public CoresController(CoresService coresService) {
        this.coresService = coresService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public CollectionsResponseDto list(@RequestParam Long serverId) {
        return coresService.listCores(serverId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void create(@RequestBody CreateCollectionRequest req) {
        coresService.createCore(req);
    }

    @DeleteMapping("/{name}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public void delete(
            @PathVariable String name,
            @RequestParam Long serverId
    ) {
        coresService.deleteCore(serverId, name);
    }
}