package com.stratasearch.backend.dto;

// One-call dashboard payload. Aggregates profile + planner + search + benchmark + telemetry.
public class WorkflowAnalyzeResponse {
    public CorpusFingerprintResponse profile;
    public PlannerExplainResponse planner;
    public SearchResponse search;
    public java.util.List<BenchmarkResponse> benchmark;
    public TelemetryDto telemetry;
    public java.util.List<String> timeline;
    public long durationMs;
    public String generatedAt;

    public static class TelemetryDto {
        public String algorithmName;
        public long executionTimeNanos;
        public long memoryUsedBytes;
        public long comparisonCount;
        public int matchCount;
        public java.util.List<Integer> matchPositions;
    }
}
