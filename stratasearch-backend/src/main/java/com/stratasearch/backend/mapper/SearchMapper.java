package com.stratasearch.backend.mapper;

import com.stratasearch.backend.dto.SearchResponse;
import stratasearch.models.TelemetryRecord;

import java.util.ArrayList;
import java.util.List;

// TelemetryRecord (core) -> SearchResponse (DTO).
public final class SearchMapper {
    private SearchMapper() {}

    public static SearchResponse ToDto(TelemetryRecord T, boolean WasForced) {
        SearchResponse D = new SearchResponse();
        D.algorithmUsed = T.GetAlgorithmName();
        D.wasForced = WasForced;
        D.matchCount = T.GetMatchCount();
        D.comparisonCount = T.GetComparisonCount();
        D.executionTimeNanos = T.GetExecutionTimeNanos();
        D.memoryUsedBytes = T.GetMemoryUsedBytes();
        List<Integer> Pos = new ArrayList<>();
        int[] Arr = T.GetMatchPositions();
        for (int I = 0; I < T.GetMatchPositionCount(); I++) {
            Pos.add(Arr[I]);
        }
        D.matchPositions = Pos;
        return D;
    }
}
