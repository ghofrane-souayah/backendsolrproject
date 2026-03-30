package com.example.usermangment.dto;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private String companyCode;

    public RegisterRequest() {}

    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getCompanyCode() { return companyCode; }

    public void setUsername(String username) { this.username = username; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setCompanyCode(String companyCode) { this.companyCode = companyCode; }
}