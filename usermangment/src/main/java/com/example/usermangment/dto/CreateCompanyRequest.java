package com.example.usermangment.dto;

public class CreateCompanyRequest {
    private String name;

    public CreateCompanyRequest() {}

    public CreateCompanyRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}