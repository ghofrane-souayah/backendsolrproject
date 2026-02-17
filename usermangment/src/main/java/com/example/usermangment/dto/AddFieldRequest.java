package com.example.usermangment.dto;

public class AddFieldRequest {
    private String name;
    private String type;
    private boolean stored;
    private boolean indexed;
    private boolean multiValued;

    public AddFieldRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public boolean isStored() { return stored; }
    public void setStored(boolean stored) { this.stored = stored; }

    public boolean isIndexed() { return indexed; }
    public void setIndexed(boolean indexed) { this.indexed = indexed; }

    public boolean isMultiValued() { return multiValued; }
    public void setMultiValued(boolean multiValued) { this.multiValued = multiValued; }
}
