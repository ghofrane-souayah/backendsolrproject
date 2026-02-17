package com.example.usermangment.dto;

public class SolrFieldTypeDto {
    private String name;
    private String clazz; // "class" est mot réservé en Java

    public SolrFieldTypeDto() {}

    public SolrFieldTypeDto(String name, String clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public String getName() { return name; }
    public String getClazz() { return clazz; }

    public void setName(String name) { this.name = name; }
    public void setClazz(String clazz) { this.clazz = clazz; }
}
