package com.example.usermangment.dto;

import com.example.usermangment.model.Role;

public class UpdateUserRequest {
    private String username;
    private String email;
    private String password; // optionnel
    private Role role;
    private boolean enabled = true;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public boolean isEnabled() { return enabled; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
