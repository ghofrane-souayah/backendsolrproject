package com.example.usermangment.controller;

import com.example.usermangment.dto.CompanyDto;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;
import com.example.usermangment.service.CompanyService;
import com.example.usermangment.dto.CreateCompanyRequest;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/companies")
@CrossOrigin(origins = {"http://localhost:3000"})
public class CompanyController {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final CompanyService companyService; // ✅ IMPORTANT

    // ✅ Constructor injection
    public CompanyController(CompanyRepository companyRepository,
                             UserRepository userRepository,
                             CompanyService companyService) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
        this.companyService = companyService;
    }

    // =========================
    // GET ALL (SUPER_ADMIN)
    // =========================
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(c -> new CompanyDto(c.getId(), c.getName(), c.getCode()))
                .toList();
    }

    // =========================
    // GET /me
    // =========================
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public CompanyDto myCompany(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (user.getCompany() == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No company assigned");
        }

        return new CompanyDto(
                user.getCompany().getId(),
                user.getCompany().getName(),
                user.getCompany().getCode()
        );
    }

    // =========================
    // POST -> use Service
    // =========================
    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public CompanyDto create(@RequestBody CreateCompanyRequest req) {
        return companyService.create(req);
    }
}