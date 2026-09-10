package com.stratasearch.backend.mapper;

import com.stratasearch.backend.dto.BenchmarkResponse;
import stratasearch.models.BenchmarkResult;

// BenchmarkResult (core) -> BenchmarkResponse (DTO).
public final class BenchmarkMapper {
    private BenchmarkMapper() {}

    public static BenchmarkResponse ToDto(BenchmarkResult B) {
        BenchmarkResponse D = new BenchmarkResponse();
        D.algorithm = B.GetAlgorithmName();
        D.executionTimeNanos = B.GetExecutionTimeNanos();
        D.memoryUsedBytes = B.GetMemoryUsedBytes();
        D.comparisonCount = B.GetComparisonCount();
        D.matchCount = B.GetMatchCount();
        D.complexity = B.GetComplexity();
        D.memoryComplexity = B.GetMemoryComplexity();
        return D;
    }

    public static java.util.List<BenchmarkResponse> ToDtoList(BenchmarkResult[] Arr) {
        java.util.List<BenchmarkResponse> Out = new java.util.ArrayList<>();
        if (Arr != null) {
            for (BenchmarkResult B : Arr) Out.add(ToDto(B));
        }
        return Out;
    }
}
