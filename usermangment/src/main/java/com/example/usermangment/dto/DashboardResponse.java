package com.example.usermangment.dto;

import java.util.List;

public class DashboardResponse {

    // Pour afficher côté front
    private String scope;        // "GLOBAL" ou "COMPANY" ou "SELF"
    private String companyName;  // null si global
    private Long companyId;      // null si global

    // Stats générales
    private long totalUsers;
    private long totalEnabled;
    private long totalDisabled;

    private long totalAdmins;
    private long totalSuperAdmins;
    private long totalNormalUsers;

    // Seulement pour SUPER_ADMIN (global)
    private Long totalCompanies; // null si pas SUPER_ADMIN
    private List<CompanyUsersCount> usersByCompany; // optionnel

    public DashboardResponse() {}

    public DashboardResponse(
            String scope,
            String companyName,
            Long companyId,
            long totalUsers,
            long totalEnabled,
            long totalDisabled,
            long totalAdmins,
            long totalSuperAdmins,
            long totalNormalUsers,
            Long totalCompanies,
            List<CompanyUsersCount> usersByCompany
    ) {
        this.scope = scope;
        this.companyName = companyName;
        this.companyId = companyId;
        this.totalUsers = totalUsers;
        this.totalEnabled = totalEnabled;
        this.totalDisabled = totalDisabled;
        this.totalAdmins = totalAdmins;
        this.totalSuperAdmins = totalSuperAdmins;
        this.totalNormalUsers = totalNormalUsers;
        this.totalCompanies = totalCompanies;
        this.usersByCompany = usersByCompany;
    }

    public String getScope() { return scope; }
    public String getCompanyName() { return companyName; }
    public Long getCompanyId() { return companyId; }

    public long getTotalUsers() { return totalUsers; }
    public long getTotalEnabled() { return totalEnabled; }
    public long getTotalDisabled() { return totalDisabled; }

    public long getTotalAdmins() { return totalAdmins; }
    public long getTotalSuperAdmins() { return totalSuperAdmins; }
    public long getTotalNormalUsers() { return totalNormalUsers; }

    public Long getTotalCompanies() { return totalCompanies; }
    public List<CompanyUsersCount> getUsersByCompany() { return usersByCompany; }

    // petit DTO interne pour stats par company
    public static class CompanyUsersCount {
        private Long companyId;
        private String companyName;
        private long usersCount;

        public CompanyUsersCount() {}

        public CompanyUsersCount(Long companyId, String companyName, long usersCount) {
            this.companyId = companyId;
            this.companyName = companyName;
            this.usersCount = usersCount;
        }

        public Long getCompanyId() { return companyId; }
        public String getCompanyName() { return companyName; }
        public long getUsersCount() { return usersCount; }
    }
}
