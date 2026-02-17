package com.example.usermangment.dto;

public class SolrFieldDto {
    private String name;
    private String type;
    private boolean stored;
    private boolean indexed;
    private boolean multiValued;
    private boolean required;

    public SolrFieldDto() {}

    public SolrFieldDto(String name, String type, boolean stored, boolean indexed, boolean multiValued, boolean required) {
        this.name = name;
        this.type = type;
        this.stored = stored;
        this.indexed = indexed;
        this.multiValued = multiValued;
        this.required = required;
    }

    public String getName() { return name; }
    public String getType() { return type; }
    public boolean isStored() { return stored; }
    public boolean isIndexed() { return indexed; }
    public boolean isMultiValued() { return multiValued; }
    public boolean isRequired() { return required; }

    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
    public void setStored(boolean stored) { this.stored = stored; }
    public void setIndexed(boolean indexed) { this.indexed = indexed; }
    public void setMultiValued(boolean multiValued) { this.multiValued = multiValued; }
    public void setRequired(boolean required) { this.required = required; }
}
