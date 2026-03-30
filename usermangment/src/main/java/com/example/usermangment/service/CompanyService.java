package com.example.usermangment.service;

import com.example.usermangment.dto.CompanyDto;
import com.example.usermangment.model.Company;
import com.example.usermangment.repository.CompanyRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.example.usermangment.dto.CreateCompanyRequest;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepo;

    public CompanyService(CompanyRepository companyRepo) {
        this.companyRepo = companyRepo;
    }

    public CompanyDto create(CreateCompanyRequest req) {
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");
        }

        if (companyRepo.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "company name already exists");
        }

        Company c = new Company();
        c.setName(name);

        // ✅ code unique généré
        String code = generateUniqueCode();
        c.setCode(code);

        Company saved = companyRepo.save(c);
        return toDto(saved);
    }

    private CompanyDto toDto(Company c) {
        return new CompanyDto(c.getId(), c.getName(), c.getCode());
    }

    // ✅ génère un code unique en vérifiant la DB
    private String generateUniqueCode() {
        String code;
        do {
            code = "CMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        } while (companyRepo.existsByCodeIgnoreCase(code));
        return code;
    }
}