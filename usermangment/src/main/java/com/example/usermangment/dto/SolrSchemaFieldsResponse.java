package com.example.usermangment.dto;

import java.util.List;

public class SolrSchemaFieldsResponse {
    private List<SolrFieldDto> fields;

    public SolrSchemaFieldsResponse() {}
    public SolrSchemaFieldsResponse(List<SolrFieldDto> fields) { this.fields = fields; }

    public List<SolrFieldDto> getFields() { return fields; }
    public void setFields(List<SolrFieldDto> fields) { this.fields = fields; }
}
