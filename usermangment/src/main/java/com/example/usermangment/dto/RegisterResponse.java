package com.example.usermangment.dto;

import java.util.List;

public class RegisterResponse {
    private Long id;
    private String email;
    private String username;
    private List<String> roles;
    private String message;

    public RegisterResponse(Long id, String email, String username, List<String> roles, String message) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.roles = roles;
        this.message = message;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public List<String> getRoles() { return roles; }
    public String getMessage() { return message; }
}