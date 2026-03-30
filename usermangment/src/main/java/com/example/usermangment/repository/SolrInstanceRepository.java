package com.example.usermangment.repository;

import com.example.usermangment.model.SolrInstance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolrInstanceRepository extends JpaRepository<SolrInstance, Long> {

    List<SolrInstance> findByCompanyId(Long companyId);

    Optional<SolrInstance> findByIdAndCompanyId(Long id, Long companyId);

    boolean existsByCompanyIdAndNameIgnoreCase(Long companyId, String name);

    boolean existsByCompanyIdAndHostAndPort(Long companyId, String host, Integer port);

    // ✅ AJOUT
    List<SolrInstance> findByStatusNotIgnoreCase(String status);
}