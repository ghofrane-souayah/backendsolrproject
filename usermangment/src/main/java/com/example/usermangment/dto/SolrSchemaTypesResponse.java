package com.example.usermangment.dto;

import java.util.List;

public class SolrSchemaTypesResponse {
    private List<SolrFieldTypeDto> fieldTypes;

    public SolrSchemaTypesResponse() {}
    public SolrSchemaTypesResponse(List<SolrFieldTypeDto> fieldTypes) { this.fieldTypes = fieldTypes; }

    public List<SolrFieldTypeDto> getFieldTypes() { return fieldTypes; }
    public void setFieldTypes(List<SolrFieldTypeDto> fieldTypes) { this.fieldTypes = fieldTypes; }
}
