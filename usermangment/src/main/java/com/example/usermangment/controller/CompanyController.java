package com.example.usermangment.controller;

import com.example.usermangment.dto.CompanyDto;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;

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

    public CompanyController(CompanyRepository companyRepository,
                             UserRepository userRepository) {
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    // 🔹 SUPER_ADMIN → voir toutes les companies
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public List<CompanyDto> getAllCompanies() {
        return companyRepository.findAll()
                .stream()
                .map(c -> new CompanyDto(
                        c.getId(),
                        c.getName(),
                        c.getCode()
                ))
                .toList();
    }

    // 🔹 ADMIN / SUPER_ADMIN → voir sa company
    @GetMapping("/me")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public CompanyDto myCompany(Authentication authentication) {

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Company company = user.getCompany();

        if (company == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No company assigned");
        }

        return new CompanyDto(
                company.getId(),
                company.getName(),
                company.getCode()
        );
    }
}
