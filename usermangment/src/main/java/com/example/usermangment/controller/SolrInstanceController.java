package com.example.usermangment.controller;

import com.example.usermangment.dto.CopySolrInstanceRequest;
import com.example.usermangment.dto.CreateSolrInstanceRequest;
import com.example.usermangment.dto.SolrInstanceDto;
import com.example.usermangment.dto.UpdateSolrInstanceRequest;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.NotificationService;
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
    private final NotificationService notificationService;

    public SolrInstanceController(
            SolrInstanceService service,
            UserRepository userRepo,
            NotificationService notificationService
    ) {
        this.service = service;
        this.userRepo = userRepo;
        this.notificationService = notificationService;
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
        User current = me(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            SolrInstanceDto result = service.create(req, current);

            notificationService.createSuccess(
                    "Instance créée",
                    "L'instance " + result.getName() + " a été créée avec succès.",
                    "Solr Admin",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec création instance",
                    e.getMessage() != null ? e.getMessage() : "La création de l'instance a échoué.",
                    "Solr Admin",
                    companyId
            );
            throw e;
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SolrInstanceDto update(@PathVariable Long id,
                                  @Valid @RequestBody UpdateSolrInstanceRequest req,
                                  Authentication auth) {
        User current = me(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            SolrInstanceDto result = service.update(id, req, current);

            notificationService.createSuccess(
                    "Instance modifiée",
                    "L'instance " + result.getName() + " a été modifiée avec succès.",
                    "Solr Admin",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec modification instance",
                    e.getMessage() != null ? e.getMessage() : "La modification de l'instance a échoué.",
                    "Solr Admin",
                    companyId
            );
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id, Authentication auth) {
        User current = me(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            service.delete(id, current);

            notificationService.createSuccess(
                    "Instance supprimée",
                    "L'instance avec l'id " + id + " a été supprimée avec succès.",
                    "Solr Admin",
                    companyId
            );
        } catch (Exception e) {
            notificationService.createError(
                    "Échec suppression instance",
                    e.getMessage() != null ? e.getMessage() : "La suppression de l'instance a échoué.",
                    "Solr Admin",
                    companyId
            );
            throw e;
        }
    }

    @PostMapping("/{id}/copy")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public SolrInstanceDto copy(@PathVariable Long id,
                                @Valid @RequestBody CopySolrInstanceRequest req,
                                Authentication auth) {
        User current = me(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            SolrInstanceDto result = service.copy(id, req, current);

            notificationService.createSuccess(
                    "Instance copiée",
                    "L'instance " + result.getName() + " a été copiée avec succès.",
                    "Solr Admin",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec copie instance",
                    e.getMessage() != null ? e.getMessage() : "La copie de l'instance a échoué.",
                    "Solr Admin",
                    companyId
            );
            throw e;
        }
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
    public SolrInstanceDto start(@PathVariable Long id, Authentication auth) {
        User current = me(auth);
        Long companyId = current.getCompany() != null ? current.getCompany().getId() : null;

        try {
            SolrInstanceDto result = service.start(id, current);

            notificationService.createSuccess(
                    "Instance démarrée",
                    "L'instance " + result.getName() + " a été démarrée avec succès.",
                    "Solr Admin",
                    companyId
            );

            return result;
        } catch (Exception e) {
            notificationService.createError(
                    "Échec démarrage instance",
                    e.getMessage() != null ? e.getMessage() : "Le démarrage de l'instance a échoué.",
                    "Solr Admin",
                    companyId
            );
            throw e;
        }
    }
}