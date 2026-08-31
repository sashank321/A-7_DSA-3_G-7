package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.SearchRequest;
import com.stratasearch.backend.dto.SearchResponse;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.mapper.SearchMapper;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.validation.RequestValidator;
import stratasearch.models.PlannerResult;
import stratasearch.models.TelemetryRecord;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final EngineGateway Gateway;
    private final CorpusService Corpus;
    private final PlannerService PlannerService;

    public SearchService(EngineGateway Gateway, CorpusService Corpus, PlannerService PlannerService) {
        this.Gateway = Gateway;
        this.Corpus = Corpus;
        this.PlannerService = PlannerService;
    }

    public SearchResponse Execute(SearchRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        boolean WasForced = Req.forceAlgorithm != null && !Req.forceAlgorithm.trim().isEmpty();
        String AlgorithmName;
        if (WasForced) {
            if (!Gateway.IsRegisteredAlgorithm(Req.forceAlgorithm)) {
                throw new ApiException(400, "Unknown algorithm: " + Req.forceAlgorithm);
            }
            AlgorithmName = Req.forceAlgorithm;
        } else {
            boolean Repeated = Req.repeated != null && Req.repeated;
            Double Bias = Req.optimizationBias != null ? Req.optimizationBias : 0.0;
            PlannerResult Plan = PlannerService.PlanRaw(S, Patterns, Repeated, Bias);
            AlgorithmName = Plan.GetChosenAlgorithm();
        }
        TelemetryRecord T = Gateway.MeasureTelemetry(AlgorithmName, S.GetJoinedText(), Patterns);
        if (T == null) {
            throw new ApiException(400, "Algorithm unavailable: " + AlgorithmName);
        }
        return SearchMapper.ToDto(T, WasForced);
    }
}
