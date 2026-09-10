package com.stratasearch.backend.dto;

import java.util.List;

// Outcome of POST /api/benchmark (one row per registered algorithm).
public class BenchmarkResponse {
    public String algorithm;
    public long executionTimeNanos;
    public long memoryUsedBytes;
    public long comparisonCount;
    public int matchCount;
    public String complexity;
    public String memoryComplexity;
}
