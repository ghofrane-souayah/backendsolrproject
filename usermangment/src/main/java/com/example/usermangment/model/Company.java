package com.example.usermangment.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_company_code", columnNames = "code"),
                @UniqueConstraint(name = "uk_company_name", columnNames = "name")
        })
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ✅ PostgreSQL auto-increment
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String code;

    public Company() {}

    public Long getId() { return id; }

    public String getName() { return name; }
    public String getCode() { return code; }

    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCode(String code) { this.code = code; }
}
