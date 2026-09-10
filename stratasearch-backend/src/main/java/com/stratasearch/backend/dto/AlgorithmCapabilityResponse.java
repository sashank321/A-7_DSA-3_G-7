package com.stratasearch.backend.dto;

// Capability card for GET /api/algorithms/{name}.
public class AlgorithmCapabilityResponse {
    public String name;
    public String timeComplexity;
    public String memoryUsage;
    public boolean supportsMultiplePatterns;
    public boolean supportsRepeatedQueries;
    public boolean supportsIndexing;
    public String description;
    public String bestUseCase;
}
