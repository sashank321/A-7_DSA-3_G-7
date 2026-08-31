package com.stratasearch.backend.adapter;

import stratasearch.models.CorpusFingerprint;
import stratasearch.models.PlannerReason;
import stratasearch.models.PlannerResult;

import java.util.ArrayList;
import java.util.List;

// Lives at the Spring<->core boundary. Converts core models into plain Java views
// so controllers / mappers never hold core types directly.
public final class CoreAdapter {

    private CoreAdapter() {}

    public static List<String> AsList(String[] Arr) {
        List<String> Out = new ArrayList<>();
        if (Arr != null) {
            for (String S : Arr) Out.add(S);
        }
        return Out;
    }

    public static String[] ToArray(List<String> L) {
        if (L == null) return new String[0];
        return L.toArray(new String[0]);
    }

    public static PlannerReasonView ReasonView(PlannerResult R) {
        PlannerReason Src = R.GetPlannerReason();
        return new PlannerReasonView(
                Src.GetAlgorithm(),
                Src.GetReason(),
                AsList(Src.GetAdvantages()),
                AsList(Src.GetTradeOffs()),
                Src.GetConfidencePercent(),
                AsList(Src.GetRecommendedBecause()),
                AsList(Src.GetAvoidedBecause()));
    }

    // Flat, framework-friendly view of PlannerReason.
    public static class PlannerReasonView {
        public final String Algorithm;
        public final String Reason;
        public final List<String> Advantages;
        public final List<String> TradeOffs;
        public final double ConfidencePercent;
        public final List<String> RecommendedBecause;
        public final List<String> AvoidedBecause;

        public PlannerReasonView(String Algorithm, String Reason,
                                 List<String> Advantages, List<String> TradeOffs,
                                 double ConfidencePercent,
                                 List<String> RecommendedBecause, List<String> AvoidedBecause) {
            this.Algorithm = Algorithm;
            this.Reason = Reason;
            this.Advantages = Advantages;
            this.TradeOffs = TradeOffs;
            this.ConfidencePercent = ConfidencePercent;
            this.RecommendedBecause = RecommendedBecause;
            this.AvoidedBecause = AvoidedBecause;
        }
    }
}
