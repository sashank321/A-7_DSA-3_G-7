package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.BenchmarkResponse;
import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.mapper.BenchmarkMapper;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.validation.RequestValidator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BenchmarkService {

    private final EngineGateway Gateway;
    private final CorpusService Corpus;

    public BenchmarkService(EngineGateway Gateway, CorpusService Corpus) {
        this.Gateway = Gateway;
        this.Corpus = Corpus;
    }

    public List<BenchmarkResponse> RunAll(PlannerRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        return BenchmarkMapper.ToDtoList(Gateway.MeasureAllBenchmarks(S.GetJoinedText(), Patterns));
    }

    public List<BenchmarkResponse> RunAllRaw(CorpusSession S, String[] Patterns) {
        return BenchmarkMapper.ToDtoList(Gateway.MeasureAllBenchmarks(S.GetJoinedText(), Patterns));
    }
}
