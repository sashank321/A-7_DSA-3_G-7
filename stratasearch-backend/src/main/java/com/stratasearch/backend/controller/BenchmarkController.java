package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.BenchmarkResponse;
import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.service.BenchmarkHistoryService;
import com.stratasearch.backend.service.BenchmarkService;
import com.stratasearch.backend.service.CorpusService;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/benchmark")
public class BenchmarkController {

    private final BenchmarkService Service;
    private final BenchmarkHistoryService History;
    private final CorpusService Corpus;

    public BenchmarkController(BenchmarkService Service, BenchmarkHistoryService History, CorpusService Corpus) {
        this.Service = Service;
        this.History = History;
        this.Corpus = Corpus;
    }

    @PostMapping
    public List<BenchmarkResponse> Run(@RequestBody PlannerRequest Req) {
        List<BenchmarkResponse> Rows = Service.RunAll(Req);
        History.Record(Req.sessionId, Rows);
        return Rows;
    }

    @GetMapping("/latest")
    public Map<String, Object> Latest(@RequestParam("sessionId") String SessionId) {
        Corpus.Resolve(SessionId);
        BenchmarkHistoryService.BenchmarkRun Run = History.Latest(SessionId);
        Map<String, Object> Out = new HashMap<>();
        if (Run == null) {
            Out.put("sessionId", SessionId);
            Out.put("found", false);
        } else {
            Out.put("sessionId", SessionId);
            Out.put("found", true);
            Out.put("ranAtEpochMs", Run.RanAtEpochMs);
            Out.put("rows", Run.Rows);
        }
        return Out;
    }

    @GetMapping("/history")
    public Map<String, Object> History(@RequestParam("sessionId") String SessionId) {
        Corpus.Resolve(SessionId);
        java.util.List<BenchmarkHistoryService.BenchmarkRun> Runs = History.ListRuns(SessionId);
        Map<String, Object> Out = new HashMap<>();
        Out.put("sessionId", SessionId);
        Out.put("runCount", Runs.size());
        Out.put("runs", Runs);
        return Out;
    }
}
