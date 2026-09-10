package com.stratasearch.backend.dto;

import java.util.List;

// Common request body for planner/search/benchmark endpoints.
public class PlannerRequest {
    public String sessionId;
    public List<String> patterns;
    public Boolean repeated; // optional; default false
    public Double optimizationBias; // optional; 0.0 to 1.0 (Speed vs Memory)
}
