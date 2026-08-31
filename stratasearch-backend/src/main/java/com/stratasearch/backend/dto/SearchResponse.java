package com.stratasearch.backend.dto;

import java.util.List;

// Outcome of POST /api/search.
public class SearchResponse {
    public String algorithmUsed;
    public boolean wasForced;
    public int matchCount;
    public long comparisonCount;
    public List<Integer> matchPositions;
    public long executionTimeNanos;
    public long memoryUsedBytes;
}
