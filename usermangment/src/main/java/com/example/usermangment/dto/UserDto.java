package com.example.usermangment.dto;

import com.example.usermangment.model.Role;

public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String role;
    private boolean enabled;
    public Long companyId;
    public String companyName;


    public UserDto() {
    }

    public UserDto(Long id, String username, String email, String role, boolean enabled, Long companyId, String companyName) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.role = role;
        this.enabled = enabled;
        this.companyId = companyId;
        this.companyName = companyName;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getRole() {
        return role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public Long getCompanyId() {
        return companyId;
    }
    public String getCompanyName(){
        return companyName;
    }
}