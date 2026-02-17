package com.example.usermangment.service;

import com.example.usermangment.dto.DashboardResponse;
import com.example.usermangment.model.Company;
import com.example.usermangment.model.Role;
import com.example.usermangment.model.User;
import com.example.usermangment.repository.CompanyRepository;
import com.example.usermangment.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DashboardService {

    private final UserRepository userRepo;
    private final CompanyRepository companyRepo;

    public DashboardService(UserRepository userRepo, CompanyRepository companyRepo) {
        this.userRepo = userRepo;
        this.companyRepo = companyRepo;
    }

    public DashboardResponse getFor(User current) {

        if (current.getRole() == Role.SUPER_ADMIN) {
            long totalUsers = userRepo.count();
            long enabled = userRepo.countByEnabledTrue();
            long disabled = userRepo.countByEnabledFalse();

            long admins = userRepo.countByRole(Role.ADMIN);
            long superAdmins = userRepo.countByRole(Role.SUPER_ADMIN);
            long normalUsers = userRepo.countByRole(Role.USER);

            long totalCompanies = companyRepo.count();

            // optionnel: users par company
            List<DashboardResponse.CompanyUsersCount> byCompany =
                    companyRepo.findAll().stream()
                            .map(c -> new DashboardResponse.CompanyUsersCount(
                                    c.getId(),
                                    c.getName(),
                                    userRepo.countByCompany_Id(c.getId())
                            ))
                            .toList();

            return new DashboardResponse(
                    "GLOBAL",
                    null,
                    null,
                    totalUsers,
                    enabled,
                    disabled,
                    admins,
                    superAdmins,
                    normalUsers,
                    totalCompanies,
                    byCompany
            );
        }

        if (current.getRole() == Role.ADMIN) {
            if (current.getCompany() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "admin has no company");
            }
            Company c = current.getCompany();
            Long companyId = c.getId();

            long totalUsers = userRepo.countByCompany_Id(companyId);
            long enabled = userRepo.countByCompany_IdAndEnabledTrue(companyId);
            long disabled = userRepo.countByCompany_IdAndEnabledFalse(companyId);

            long admins = userRepo.countByCompany_IdAndRole(companyId, Role.ADMIN);
            long superAdmins = userRepo.countByCompany_IdAndRole(companyId, Role.SUPER_ADMIN); // normalement 0
            long normalUsers = userRepo.countByCompany_IdAndRole(companyId, Role.USER);

            return new DashboardResponse(
                    "COMPANY",
                    c.getName(),
                    companyId,
                    totalUsers,
                    enabled,
                    disabled,
                    admins,
                    superAdmins,
                    normalUsers,
                    null,
                    null
            );
        }

        // USER -> juste lui (self)
        return new DashboardResponse(
                "SELF",
                current.getCompany() != null ? current.getCompany().getName() : null,
                current.getCompany() != null ? current.getCompany().getId() : null,
                1,
                current.isEnabled() ? 1 : 0,
                current.isEnabled() ? 0 : 1,
                0,
                0,
                1,
                null,
                null
        );
    }
}
