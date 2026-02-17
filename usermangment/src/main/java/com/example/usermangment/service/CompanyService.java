package com.example.usermangment.service;

import com.example.usermangment.dto.CompanyDto;
import com.example.usermangment.dto.CreateCompanyRequest;
import com.example.usermangment.dto.UpdateCompanyRequest;
import com.example.usermangment.model.Company;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepo;
    private final UserRepository userRepo;

    public CompanyService(CompanyRepository companyRepo, UserRepository userRepo) {
        this.companyRepo = companyRepo;
        this.userRepo = userRepo;
    }

    private CompanyDto toDto(Company c) {
        return new CompanyDto(c.getId(), c.getName(), c.getCode());
    }

    public List<CompanyDto> list() {
        return companyRepo.findAll().stream().map(this::toDto).toList();
    }

    public CompanyDto get(Long id) {
        Company c = companyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not found"));
        return toDto(c);
    }

    public CompanyDto create(CreateCompanyRequest req) {
        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");

        if (companyRepo.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "company name already exists");
        }

        Company c = new Company();
        c.setName(name);

        // ✅ code unique généré automatiquement
        c.setCode(generateUniqueCode());

        Company saved = companyRepo.save(c);
        return toDto(saved);
    }

    public CompanyDto update(Long id, UpdateCompanyRequest req) {
        Company c = companyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not found"));

        String name = req.getName() == null ? "" : req.getName().trim();
        if (name.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name is required");

        // évite conflit nom (autre company)
        companyRepo.findAll().forEach(other -> {
            if (!other.getId().equals(id) && other.getName() != null
                    && other.getName().equalsIgnoreCase(name)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "company name already exists");
            }
        });

        c.setName(name);
        return toDto(companyRepo.save(c));
    }

    public void delete(Long id) {
        Company c = companyRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "company not found"));

        // ✅ empêcher suppression si des users existent (sinon FK error)
        long countUsers = userRepo.countByCompany_Id(id);
        if (countUsers > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "company has users, cannot delete");
        }

        companyRepo.delete(c);
    }

    // --------------------------
    // Code unique type: CMP-8F3K2A
    // --------------------------
    private String generateUniqueCode() {
        SecureRandom rnd = new SecureRandom();
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        while (true) {
            StringBuilder sb = new StringBuilder("CMP-");
            for (int i = 0; i < 6; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
            String code = sb.toString();
            if (!companyRepo.existsByCode(code)) return code;
        }
    }
}
