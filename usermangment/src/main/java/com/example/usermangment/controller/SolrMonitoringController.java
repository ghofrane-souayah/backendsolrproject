package com.example.usermangment.controller;

import com.example.usermangment.dto.SolrMonitoringResponse;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.SolrInstance;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.SolrInstanceRepository;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.SolrMonitoringService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/solr")
public class SolrMonitoringController {

    private final SolrMonitoringService service;
    private final UserRepository userRepo;
    private final SolrInstanceRepository solrRepo;

    public SolrMonitoringController(SolrMonitoringService service,
                                    UserRepository userRepo,
                                    SolrInstanceRepository solrRepo) {
        this.service = service;
        this.userRepo = userRepo;
        this.solrRepo = solrRepo;
    }

    private User me(Authentication auth) {
        if (auth == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No auth");
        String principal = auth.getName(); // <-- email
        return userRepo.findByEmail(principal)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found: " + principal));
    }

    @GetMapping("/monitoring")
    public SolrMonitoringResponse monitoring(Authentication auth) {

        User u = me(auth);

        List<SolrInstance> instances;
        if (u.getRole() == Role.SUPER_ADMIN) {
            instances = solrRepo.findAll();
        } else {
            if (u.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User has no company");
            }
            instances = solrRepo.findByCompanyId(u.getCompany().getId());
        }

        return service.monitor(instances);
    }
}