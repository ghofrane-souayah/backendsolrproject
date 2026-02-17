package com.example.usermangment.dto;

import com.example.usermangment.model.Role;

public class CreateUserRequest {
    private String username;
    private String email;
    private String password;
    private Role role;
    private boolean enabled = true;

    // (optionnel) si tu veux que SUPER_ADMIN choisisse une company
    private Long companyId;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }
    public Long getCompanyId() { return companyId; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
}
