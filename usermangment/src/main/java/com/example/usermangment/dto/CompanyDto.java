package com.example.usermangment.dto;

public class CompanyDto {
    private Long id;
    private String name;
    private String code;

    public CompanyDto() {}

    public CompanyDto(Long id, String name, String code) {
        this.id = id;
        this.name = name;
        this.code = code;
    }


    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }
}
