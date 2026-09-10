package com.stratasearch.backend.dto;

import java.util.List;

// Full planner explanation (PlannerResponse + reason struct + decision tree + alternatives).
public class PlannerExplainResponse {
    public String recommendedAlgorithm;
    public double confidencePercent;
    public String estimatedRuntime;
    public String estimatedMemory;
    public String reason;
    public List<String> advantages;
    public List<String> tradeOffs;
    public List<String> recommendedBecause;
    public List<String> avoidedBecause;
    public List<String> alternatives;
    public List<String> decisionSteps;
    public List<PlannerResponse.AlgorithmScore> scores;
}
