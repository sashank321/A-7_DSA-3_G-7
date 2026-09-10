package com.stratasearch.backend.mapper;

import com.stratasearch.backend.adapter.CoreAdapter;
import com.stratasearch.backend.dto.PlannerExplainResponse;
import com.stratasearch.backend.dto.PlannerResponse;
import stratasearch.models.PlannerResult;

import java.util.ArrayList;
import java.util.List;

// PlannerResult (core) -> PlannerResponse / PlannerExplainResponse (DTO).
public final class PlannerMapper {
    private PlannerMapper() {}

    public static PlannerResponse ToCompact(PlannerResult R) {
        PlannerResponse D = new PlannerResponse();
        D.recommendedAlgorithm = R.GetChosenAlgorithm();
        D.confidencePercent = R.GetConfidencePercent();
        D.estimatedRuntime = R.GetEstimatedRuntime();
        D.estimatedMemory = R.GetEstimatedMemory();
        D.scores = ScoreList(R);
        return D;
    }

    public static PlannerExplainResponse ToExplain(PlannerResult R) {
        PlannerExplainResponse D = new PlannerExplainResponse();
        D.recommendedAlgorithm = R.GetChosenAlgorithm();
        D.confidencePercent = R.GetConfidencePercent();
        D.estimatedRuntime = R.GetEstimatedRuntime();
        D.estimatedMemory = R.GetEstimatedMemory();
        CoreAdapter.PlannerReasonView V = CoreAdapter.ReasonView(R);
        D.reason = V.Reason;
        D.advantages = V.Advantages;
        D.tradeOffs = V.TradeOffs;
        D.recommendedBecause = V.RecommendedBecause;
        D.avoidedBecause = V.AvoidedBecause;
        D.alternatives = CoreAdapter.AsList(R.GetAlternatives());
        D.decisionSteps = CoreAdapter.AsList(R.GetDecisionSteps());
        D.scores = ScoreList(R);
        return D;
    }

    private static List<PlannerResponse.AlgorithmScore> ScoreList(PlannerResult R) {
        List<PlannerResponse.AlgorithmScore> Out = new ArrayList<>();
        int K = R.GetCandidateCount();
        for (int I = 0; I < K; I++) {
            Out.add(new PlannerResponse.AlgorithmScore(R.GetCandidateName(I), R.GetCandidateScore(I)));
        }
        return Out;
    }
}
