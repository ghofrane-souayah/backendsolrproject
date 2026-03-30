package com.example.usermangment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CopySolrInstanceRequest {

    @NotBlank
    private String newName;

    @NotNull
    private Integer newPort;

    public String getNewName() { return newName; }
    public Integer getNewPort() { return newPort; }

    public void setNewName(String newName) { this.newName = newName; }
    public void setNewPort(Integer newPort) { this.newPort = newPort; }
}