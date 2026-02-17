package com.example.usermangment.dto;

public class SolrCoreDto {
    private String name;
    private long numDocs;

    // optionnels (si tu veux afficher plus tard)
    private long deletedDocs;
    private long sizeInBytes;

    public SolrCoreDto() {}

    public SolrCoreDto(String name, long numDocs, long deletedDocs, long sizeInBytes) {
        this.name = name;
        this.numDocs = numDocs;
        this.deletedDocs = deletedDocs;
        this.sizeInBytes = sizeInBytes;
    }

    public String getName() { return name; }
    public long getNumDocs() { return numDocs; }
    public long getDeletedDocs() { return deletedDocs; }
    public long getSizeInBytes() { return sizeInBytes; }

    public void setName(String name) { this.name = name; }
    public void setNumDocs(long numDocs) { this.numDocs = numDocs; }
    public void setDeletedDocs(long deletedDocs) { this.deletedDocs = deletedDocs; }
    public void setSizeInBytes(long sizeInBytes) { this.sizeInBytes = sizeInBytes; }
}
