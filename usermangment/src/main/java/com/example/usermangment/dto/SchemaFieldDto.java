package com.example.usermangment.dto;

import java.util.List;

public class SchemaFieldDto {
    private String name;
    private String type;
    private boolean stored;
    private boolean indexed;
    private boolean multiValued;
    private List<String> copyDests; // optionnel

    public SchemaFieldDto() {}

    public SchemaFieldDto(String name, String type, boolean stored, boolean indexed, boolean multiValued, List<String> copyDests) {
        this.name = name;
        this.type = type;
        this.stored = stored;
        this.indexed = indexed;
        this.multiValued = multiValued;
        this.copyDests = copyDests;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isStored() { return stored; }
    public boolean isIndexed() { return indexed; }
    public boolean isMultiValued() { return multiValued; }
    public List<String> getCopyDests() { return copyDests; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setStored(boolean stored) { this.stored = stored; }
    public void setIndexed(boolean indexed) { this.indexed = indexed; }
    public void setMultiValued(boolean multiValued) { this.multiValued = multiValued; }
    public void setCopyDests(List<String> copyDests) { this.copyDests = copyDests; }
}
