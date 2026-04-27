package com.example.usermangment.repository;

import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByCompanyId(Long companyId);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);

    List<User> findAllByCompany_Id(Long companyId);

    // ✅ Compter tous les users d’une company
    long countByCompany_Id(Long companyId);

    // ✅ Compter tous les users par rôle (ex: SUPER_ADMIN)
    long countByRole(Role role);

    // ✅ Compter admins par company
    long countByCompany_IdAndRole(Long companyId, Role role);
    // ✅ GLOBAL enabled
    long countByEnabledTrue();
    long countByEnabledFalse();

    // ✅ PAR COMPANY enabled
    long countByCompany_IdAndEnabledTrue(Long companyId);
    long countByCompany_IdAndEnabledFalse(Long companyId);

}
