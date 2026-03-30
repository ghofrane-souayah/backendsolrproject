package com.example.usermangment.controller;

import com.example.usermangment.dto.*;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.SolrInstanceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/solr/instances")
@CrossOrigin(origins = {"http://localhost:3000"})
public class SolrInstanceController {

    private final SolrInstanceService service;
    private final UserRepository userRepo;

    public SolrInstanceController(SolrInstanceService service, UserRepository userRepo) {
        this.service = service;
        this.userRepo = userRepo;
    }

    private User me(Authentication auth) {
        return userRepo.findByEmail(auth.getName().trim().toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public List<SolrInstanceDto> list(Authentication auth) {
        return service.listFor(me(auth));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SolrInstanceDto create(@Valid @RequestBody CreateSolrInstanceRequest req, Authentication auth) {
        return service.create(req, me(auth));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SolrInstanceDto update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateSolrInstanceRequest req,
                                  Authentication auth) {
        return service.update(id, req, me(auth));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        service.delete(id, me(auth));
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SolrInstanceDto copy(@PathVariable Long id,
                                @Valid @RequestBody CopySolrInstanceRequest req,
                                Authentication auth) {
        return service.copy(id, req, me(auth));
    }
    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SolrInstanceDto start(@PathVariable Long id, Authentication auth) {
        return service.start(id, me(auth));
    }
}