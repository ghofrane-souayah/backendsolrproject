package com.example.usermangment.service;

import com.example.usermangment.dto.CompanyDto;
import com.example.usermangment.dto.CreateCompanyRequest;
import com.example.usermangment.model.Company;
import com.example.usermangment.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final NotificationService notificationService;

    public CompanyService(
            CompanyRepository companyRepository,
            NotificationService notificationService
    ) {
        this.companyRepository = companyRepository;
        this.notificationService = notificationService;
    }

    public CompanyDto create(CreateCompanyRequest req) {
        Company company = new Company();
        company.setName(req.getName());
        company.setCode(generateCode(req.getName()));

        Company saved = companyRepository.save(company);

        return new CompanyDto(saved.getId(), saved.getName(), saved.getCode());
    }

    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found"));

        // Supprime d'abord les notifications liées à cette compagnie
        notificationService.deleteByCompanyId(id);

        // Puis supprime la compagnie
        companyRepository.delete(company);
    }

    private String generateCode(String name) {
        String prefix = name == null || name.isBlank()
                ? "CMP"
                : name.trim()
                .replaceAll("\\s+", "")
                .substring(0, Math.min(3, name.trim().length()))
                .toUpperCase();

        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6)
                .toUpperCase();

        return prefix + "-" + suffix;
    }
}