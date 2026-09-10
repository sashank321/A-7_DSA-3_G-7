package com.stratasearch.backend.dto;

import java.util.List;

// Compact planner recommendation (score table + confidence, without full explanation).
public class PlannerResponse {
    public String recommendedAlgorithm;
    public double confidencePercent;
    public String estimatedRuntime;
    public String estimatedMemory;
    public List<AlgorithmScore> scores;

    public static class AlgorithmScore {
        public String algorithm;
        public double score;

        public AlgorithmScore(String algorithm, double score) {
            this.algorithm = algorithm;
            this.score = score;
        }
    }
}
