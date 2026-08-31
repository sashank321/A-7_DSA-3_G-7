package com.stratasearch.backend.dto;

import java.util.List;

// Search request; forceAlgorithm optional (defaults to planner recommendation).
public class SearchRequest {
    public String sessionId;
    public List<String> patterns;
    public String forceAlgorithm; // optional, exact registered name or null
    public Boolean repeated;      // optional hint used when not forcing algorithm
    public Double optimizationBias; // optional; 0.0 to 1.0 (Speed vs Memory)
}
