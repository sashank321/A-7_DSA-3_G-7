package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.PlannerExplainResponse;
import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.dto.PlannerResponse;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.mapper.PlannerMapper;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.validation.RequestValidator;
import stratasearch.models.PlannerResult;
import org.springframework.stereotype.Service;

@Service
public class PlannerService {

    private final EngineGateway Gateway;
    private final CorpusService Corpus;

    public PlannerService(EngineGateway Gateway, CorpusService Corpus) {
        this.Gateway = Gateway;
        this.Corpus = Corpus;
    }

    private stratasearch.models.CorpusFingerprint Refingerprint(CorpusSession S, String[] Patterns, boolean Repeated) {
        double AvgQueryLen = 0.0;
        if (Patterns.length > 0) {
            int Sum = 0;
            for (String P : Patterns) Sum += P.length();
            AvgQueryLen = (double) Sum / (double) Patterns.length;
        }
        boolean Multi = Patterns.length >= 2;
        return Gateway.Profile(S.GetDocuments(), S.GetSessionId(), Multi, Repeated, AvgQueryLen);
    }

    public PlannerResponse Recommend(PlannerRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        boolean Repeated = Req.repeated != null && Req.repeated;
        double Bias = Req.optimizationBias != null ? Req.optimizationBias : 0.0;
        stratasearch.models.CorpusFingerprint F = Refingerprint(S, Patterns, Repeated);
        PlannerResult R = Gateway.Plan(F, Patterns, Repeated, Bias);
        return PlannerMapper.ToCompact(R);
    }

    public PlannerExplainResponse Explain(PlannerRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        boolean Repeated = Req.repeated != null && Req.repeated;
        double Bias = Req.optimizationBias != null ? Req.optimizationBias : 0.0;
        stratasearch.models.CorpusFingerprint F = Refingerprint(S, Patterns, Repeated);
        PlannerResult R = Gateway.Plan(F, Patterns, Repeated, Bias);
        return PlannerMapper.ToExplain(R);
    }

    // Reusable by WorkflowService to avoid double planning.
    public PlannerResult PlanRaw(CorpusSession S, String[] Patterns, boolean Repeated, double optimizationBias) {
        stratasearch.models.CorpusFingerprint F = Refingerprint(S, Patterns, Repeated);
        return Gateway.Plan(F, Patterns, Repeated, optimizationBias);
    }
}
