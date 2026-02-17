package com.example.usermangment.dto;

import com.example.usermangment.model.Role;

public class CreateUserDto {
    private String username;
    private String email;
    private String password;
    private Role role;

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
