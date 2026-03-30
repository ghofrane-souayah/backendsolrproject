package com.example.usermangment.repository;

import com.example.usermangment.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    // ✅ pour vérifier si un nom existe (case-insensitive)
    boolean existsByNameIgnoreCase(String name);

    // optionnel (utile pour update / fetch)
    Optional<Company> findByNameIgnoreCase(String name);

    // optionnel si tu veux code unique
    boolean existsByCodeIgnoreCase(String code);
    Optional<Company> findByCodeIgnoreCase(String code);
}