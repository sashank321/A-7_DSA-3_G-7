package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.dto.WorkflowAnalyzeResponse;
import com.stratasearch.backend.mapper.FingerprintMapper;
import com.stratasearch.backend.mapper.PlannerMapper;
import com.stratasearch.backend.mapper.SearchMapper;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.model.JobEvent;
import com.stratasearch.backend.validation.RequestValidator;
import stratasearch.models.PlannerResult;
import stratasearch.models.TelemetryRecord;
import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    private final CorpusService Corpus;
    private final PlannerService Planner;
    private final BenchmarkService Benchmark;
    private final BenchmarkHistoryService History;
    private final com.stratasearch.backend.engine.EngineGateway Gateway;

    public WorkflowService(CorpusService Corpus,
                           PlannerService Planner,
                           BenchmarkService Benchmark,
                           BenchmarkHistoryService History,
                           com.stratasearch.backend.engine.EngineGateway Gateway) {
        this.Corpus = Corpus;
        this.Planner = Planner;
        this.Benchmark = Benchmark;
        this.History = History;
        this.Gateway = Gateway;
    }

    public WorkflowAnalyzeResponse Analyze(PlannerRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        boolean Repeated = Req.repeated != null && Req.repeated;
        Double Bias = Req.optimizationBias != null ? Req.optimizationBias : 0.0;
        return AnalyzeInternal(S, Patterns, Repeated, Bias, null);
    }

    // Variant used by JobExecutor so progress events are emitted as the pipeline runs.
    public WorkflowAnalyzeResponse AnalyzeWithEvents(String SessionId, String[] Patterns, boolean Repeated, Double Bias, Job Job) {
        CorpusSession S = Corpus.Resolve(SessionId);
        return AnalyzeInternal(S, Patterns, Repeated, Bias, Job);
    }

    private WorkflowAnalyzeResponse AnalyzeInternal(CorpusSession S, String[] Patterns, boolean Repeated, Double Bias, Job J) {
        long Started = System.currentTimeMillis();
        java.util.List<String> Timeline = new java.util.ArrayList<>();

        double AvgQueryLen = 0.0;
        if (Patterns.length > 0) {
            int Sum = 0;
            for (String P : Patterns) Sum += P.length();
            AvgQueryLen = (double) Sum / (double) Patterns.length;
        }
        boolean Multi = Patterns.length >= 2;

        Stage(J, "PROFILE_PROGRESS", 10, "Profiling corpus...");
        stratasearch.models.CorpusFingerprint F =
                Gateway.Profile(S.GetDocuments(), S.GetSessionId(), Multi, Repeated, AvgQueryLen);
        Timeline.add("profile");

        Stage(J, "PLANNER_PROGRESS", 35, "Planning algorithm...");
        PlannerResult Plan = Gateway.Plan(F, Patterns, Repeated, Bias);
        Timeline.add("planner");

        String Chosen = Plan.GetChosenAlgorithm();
        Stage(J, "SEARCH_PROGRESS", 55, "Running search with " + Chosen + "...");
        TelemetryRecord Telemetry = Gateway.MeasureTelemetry(Chosen, S.GetJoinedText(), Patterns);
        Timeline.add("search");

        Stage(J, "BENCHMARK_PROGRESS", 80, "Benchmarking all algorithms...");
        java.util.List<com.stratasearch.backend.dto.BenchmarkResponse> Rows =
                Benchmark.RunAllRaw(S, Patterns);
        Timeline.add("benchmark");

        History.Record(S.GetSessionId(), Rows);

        WorkflowAnalyzeResponse Out = new WorkflowAnalyzeResponse();
        Out.profile = FingerprintMapper.ToDto(F);
        Out.planner = PlannerMapper.ToExplain(Plan);
        Out.benchmark = Rows;
        Out.search = SearchMapper.ToDto(Telemetry, false);

        WorkflowAnalyzeResponse.TelemetryDto Td = new WorkflowAnalyzeResponse.TelemetryDto();
        Td.algorithmName = Telemetry.GetAlgorithmName();
        Td.executionTimeNanos = Telemetry.GetExecutionTimeNanos();
        Td.memoryUsedBytes = Telemetry.GetMemoryUsedBytes();
        Td.comparisonCount = Telemetry.GetComparisonCount();
        Td.matchCount = Telemetry.GetMatchCount();
        java.util.List<Integer> Pos = new java.util.ArrayList<>();
        int[] PosArr = Telemetry.GetMatchPositions();
        for (int I = 0; I < Telemetry.GetMatchPositionCount(); I++) Pos.add(PosArr[I]);
        Td.matchPositions = Pos;
        Out.telemetry = Td;

        Out.timeline = Timeline;
        Out.durationMs = System.currentTimeMillis() - Started;
        Out.generatedAt = java.time.Instant.now().toString();
        return Out;
    }

    private void Stage(Job J, String Type, Integer Progress, String Msg) {
        if (J != null) {
            J.AddEvent(new JobEvent(Type, Progress, Msg, null));
        }
    }
}
