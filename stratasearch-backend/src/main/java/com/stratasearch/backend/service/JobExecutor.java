package com.stratasearch.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.model.JobEvent;
import com.stratasearch.backend.model.JobRegistry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// Runs jobs on a small thread pool, emitting progress events that also feed WebSocket clients.
@Service
public class JobExecutor {

    private final JobRegistry Registry;
    private final BenchmarkHistoryService History;
    private final ObjectMapper Mapper = new ObjectMapper();
    private final ExecutorService Pool = Executors.newFixedThreadPool(4);

    public JobExecutor(JobRegistry Registry, BenchmarkHistoryService History) {
        this.Registry = Registry;
        this.History = History;
    }

    public Job Submit(com.stratasearch.backend.dto.PlannerRequest Req, WorkflowService Workflow) {
        if (Registry.ActiveCount() >= 5) {
            throw new com.stratasearch.backend.exception.ApiException(429, "Too many concurrent analysis jobs are running. Maximum is 5.");
        }
        Job J = Registry.Create(Req.sessionId,
                com.stratasearch.backend.validation.RequestValidator.NormalizePatterns(Req.patterns),
                Req.repeated != null && Req.repeated);
        Double Bias = Req.optimizationBias != null ? Req.optimizationBias : 0.0;
        Pool.submit(() -> Run(J, Workflow, Bias));
        return J;
    }

    private void Run(Job J, WorkflowService Workflow, Double Bias) {
        try {
            Emit(J, "JOB_STARTED", 0, "Workflow started", null);
            J.SetStatus(Job.Status.RUNNING);

            Emit(J, "PROFILE_PROGRESS", 10, "Profiling corpus", null);
            com.stratasearch.backend.dto.WorkflowAnalyzeResponse Out = Workflow.AnalyzeWithEvents(
                    J.GetSessionId(), J.GetPatterns(), J.IsRepeated(), Bias, J);
            Emit(J, "PROFILE_COMPLETE", 25, "Profiling done",
                    Map.of("categoryTags", Out.profile.categoryTags));
            Emit(J, "PLANNER_COMPLETE", 50, "Planner done",
                    Map.of("recommendedAlgorithm", Out.planner.recommendedAlgorithm,
                           "confidencePercent", Out.planner.confidencePercent));
            Emit(J, "BENCHMARK_PROGRESS", 80, "Benchmarked all algorithms", null);
            Emit(J, "SEARCH_COMPLETE", 95, "Search complete",
                    Map.of("algorithmUsed", Out.search.algorithmUsed,
                           "matchCount", Out.search.matchCount));
            J.SetResult(Out);
            J.SetStatus(Job.Status.SUCCEEDED);
            Emit(J, "DONE", 100, "Workflow complete", null);
        } catch (Exception Ex) {
            J.SetErrorMessage(Ex.getMessage());
            J.SetStatus(Job.Status.FAILED);
            Emit(J, "FAILED", null, Ex.getMessage(), null);
        }
    }

    private void Emit(Job J, String Type, Integer Progress, String Message, Object Payload) {
        JobEvent E = new JobEvent(Type, Progress, Message, Payload);
        J.AddEvent(E);
        System.out.println("[Job " + J.GetJobId() + "] " + Type + " p=" + Progress + " :: " + Message);
    }

    // Broadcast-ready JSON form of a job's event history.
    public String SnapshotJson(Job J) {
        try {
            Map<String, Object> Out = new java.util.HashMap<>();
            Out.put("jobId", J.GetJobId());
            Out.put("status", J.GetStatus().name());
            Out.put("events", J.GetEvents());
            if (J.GetErrorMessage() != null) Out.put("error", J.GetErrorMessage());
            if (J.GetResult() != null) Out.put("result", J.GetResult());
            return Mapper.writeValueAsString(Out);
        } catch (Exception Ex) {
            return "{\"status\":\"ERROR\",\"message\":\"serialization failed\"}";
        }
    }
}
