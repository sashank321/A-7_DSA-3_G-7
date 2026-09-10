package com.stratasearch.backend.dto;

import java.util.List;

public class VisualizationRequest {
    public String sessionId;
    public List<String> patterns;
    public String algorithm; // optional; planner picks if omitted
    public Boolean repeated; // optional hint for planner
}
