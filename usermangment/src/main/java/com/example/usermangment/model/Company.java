package com.example.usermangment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 40)
    private String code; // companyCode (ex: CMP-A1B2C3)

    public Company() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCode() { return code; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
}
