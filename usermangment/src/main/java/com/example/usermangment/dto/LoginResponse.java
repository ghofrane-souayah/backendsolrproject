package com.example.usermangment.dto;

import java.util.List;

public class LoginResponse {
    private Long id;
    private String email;
    private String username;
    private List<String> roles;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long id, String email, String username, List<String> roles, String token) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.token = token;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public String getToken() { return token; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public void setToken(String token) { this.token = token; }
}
