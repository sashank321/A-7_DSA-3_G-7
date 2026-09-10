package com.stratasearch.backend.service;

import com.stratasearch.backend.dto.VisualizationRequest;
import com.stratasearch.backend.dto.VisualizationResponse;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.model.CorpusSession;
import com.stratasearch.backend.validation.RequestValidator;
import org.springframework.stereotype.Service;
import stratasearch.algorithms.SearchAlgorithm;
import stratasearch.algorithms.SearchStepSink;
import stratasearch.algorithms.VisualizationStep;

import java.util.ArrayList;
import java.util.List;

@Service
public class VisualizationService {

    private final EngineGateway Gateway;
    private final CorpusService Corpus;
    private final PlannerService Planner;

    public VisualizationService(EngineGateway Gateway, CorpusService Corpus, PlannerService Planner) {
        this.Gateway = Gateway;
        this.Corpus = Corpus;
        this.Planner = Planner;
    }

    public VisualizationResponse Build(VisualizationRequest Req) {
        CorpusSession S = Corpus.Resolve(Req.sessionId);
        String[] Patterns = RequestValidator.NormalizePatterns(Req.patterns);
        boolean WasForced = Req.algorithm != null && !Req.algorithm.trim().isEmpty();
        String Name;
        if (WasForced) {
            if (!Gateway.IsRegisteredAlgorithm(Req.algorithm)) {
                throw new ApiException(400, "Unknown algorithm: " + Req.algorithm);
            }
            Name = Req.algorithm;
        } else {
            boolean Repeated = Req.repeated != null && Req.repeated;
            Double Bias = 0.0; // Visualization doesn't take bias from frontend yet, assume 0.0
            Name = Planner.PlanRaw(S, Patterns, Repeated, Bias).GetChosenAlgorithm();
        }
        SearchAlgorithm Algo = Gateway.Resolve(Name);

        List<VisualizationStep> Steps = new ArrayList<>();
        SearchStepSink Sink = new SearchStepSink() {
            public void OnStep(int StepIndex, String Kind, long TextPointer, long PatternPointer, String Detail) {
                if (Steps.size() < SearchStepSink.MaxSteps) {
                    Steps.add(new VisualizationStep(StepIndex, Kind, TextPointer, PatternPointer, Detail));
                }
            }
        };
        System.out.println("[Visualize] running " + Name + " over corpus " + S.GetSessionId());
        Algo.Search(S.GetJoinedText(), Patterns, Sink);

        VisualizationResponse R = new VisualizationResponse();
        R.algorithm = Name;
        R.wasForced = WasForced;
        R.steps = Steps;
        R.stepCount = Steps.size();
        return R;
    }
}
