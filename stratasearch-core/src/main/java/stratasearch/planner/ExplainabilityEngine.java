package stratasearch.planner;

import stratasearch.models.PlannerReason;
import stratasearch.models.PlannerResult;

// Converts a PlannerResult into a human-readable explanation report.
public class ExplainabilityEngine {

    public String DescribeAlgorithm(String Name) {
        if ("KMP".equals(Name)) {
            return "Knuth-Morris-Pratt: builds a longest-prefix-suffix table so the text pointer never moves backward; exact and deterministic.";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "Rabin-Karp: rolling hash turns window comparison into O(1) average hash updates, verified by direct compare on hash hit.";
        }
        if ("Z Algorithm".equals(Name)) {
            return "Z Algorithm: computes Z-values on Pattern+sentinel+Text, keeping a rightmost Z-box so each text char is compared a bounded number of times.";
        }
        if ("Aho-Corasick".equals(Name)) {
            return "Aho-Corasick: trie of all patterns with failure links; scans the text once and reports every match of every pattern.";
        }
        return "Suffix Array + LCP: sorted suffix indices plus longest-common-prefix array; supports binary-search pattern lookup on the built index.";
    }

    public String DescribeTradeOffs(String Name) {
        if ("KMP".equals(Name)) {
            return "Trade-off: needs O(M) preprocessing per pattern and handles only one pattern per run; no index is reused across queries.";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "Trade-off: worst case degrades to O(N*M) under hash collisions; memory is tiny but constant factors are hash-bound.";
        }
        if ("Z Algorithm".equals(Name)) {
            return "Trade-off: allocates an O(N+M) Z-array each query and only handles a single pattern; simple but allocation-heavy.";
        }
        if ("Aho-Corasick".equals(Name)) {
            return "Trade-off: trie memory grows with total pattern length; overkill for a single pattern, ideal for many.";
        }
        return "Trade-off: build cost is O(N log N) and O(N) memory up front; pays off only if the corpus is searched repeatedly.";
    }

    public String SuitableScenario(String Name) {
        if ("KMP".equals(Name)) {
            return "Best when: one stable exact pattern, strict worst-case linear time required.";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "Best when: single pattern, modest corpus, hash-friendly probing acceptable.";
        }
        if ("Z Algorithm".equals(Name)) {
            return "Best when: short pattern, small-to-moderate corpus, prefix-based matching desirable.";
        }
        if ("Aho-Corasick".equals(Name)) {
            return "Best when: many patterns must be matched in a single pass through the text.";
        }
        return "Best when: repeated queries hit the same corpus, or phrase/LCP analysis is needed.";
    }

    public String Explain(PlannerResult Result) {
        StringBuilder Sb = new StringBuilder();
        String Chosen = Result.GetChosenAlgorithm();
        PlannerReason Structured = Result.GetPlannerReason();
        Sb.append("=== Planner Explanation ===\n");
        Sb.append("Recommended Algorithm : ").append(Chosen).append('\n');
        Sb.append("Confidence            : ").append(Result.GetConfidencePercent()).append("%\n");
        Sb.append("Reason                : ").append(Structured.GetReason()).append('\n');
        Sb.append("Estimated Runtime     : ").append(Result.GetEstimatedRuntime()).append('\n');
        Sb.append("Estimated Memory      : ").append(Result.GetEstimatedMemory()).append('\n');
        Sb.append("How it works          : ").append(DescribeAlgorithm(Chosen)).append('\n');
        Sb.append("Scenario fit          : ").append(SuitableScenario(Chosen)).append('\n');

        Sb.append("\n--- Decision Tree ---\n");
        for (int I = 0; I < Result.GetDecisionStepCount(); I++) {
            Sb.append("  ").append(I + 1).append(". ").append(Result.GetDecisionStep(I)).append('\n');
        }

        Sb.append("\n--- Advantages ---\n");
        for (int I = 0; I < Structured.GetAdvantageCount(); I++) {
            Sb.append("  + ").append(Structured.GetAdvantage(I)).append('\n');
        }

        Sb.append("\n--- Trade-offs ---\n");
        for (int I = 0; I < Structured.GetTradeOffCount(); I++) {
            Sb.append("  - ").append(Structured.GetTradeOff(I)).append('\n');
        }

        Sb.append("\n--- Recommended because ---\n");
        for (int I = 0; I < Structured.GetRecommendedBecauseCount(); I++) {
            Sb.append("  * ").append(Structured.GetRecommendedBecause(I)).append('\n');
        }

        Sb.append("\n--- Avoided because ---\n");
        for (int I = 0; I < Structured.GetAvoidedBecauseCount(); I++) {
            Sb.append("  x ").append(Structured.GetAvoidedBecause(I)).append('\n');
        }

        Sb.append("\n--- Ranked candidate scores (lower = better) ---\n");
        int K = Result.GetCandidateCount();
        for (int I = 0; I < K; I++) {
            Sb.append("  ").append(I + 1).append(". ")
                    .append(Result.GetCandidateName(I))
                    .append("  score=")
                    .append(F4(Result.GetCandidateScore(I)));
            if (I == 0) {
                Sb.append("  <-- chosen");
            }
            Sb.append('\n');
        }

        Sb.append("\nAlternatives in order:\n");
        for (int I = 0; I < Result.GetAlternativeCount(); I++) {
            Sb.append("  - ").append(Result.GetAlternative(I)).append('\n');
        }
        return Sb.toString();
    }

    private String F4(double V) {
        long Scaled = (long) (V * 10000.0 + 0.5);
        long Whole = Scaled / 10000;
        int Frac = (int) (Scaled % 10000);
        String FracStr;
        if (Frac < 10) FracStr = "000" + Frac;
        else if (Frac < 100) FracStr = "00" + Frac;
        else if (Frac < 1000) FracStr = "0" + Frac;
        else FracStr = "" + Frac;
        return Whole + "." + FracStr;
    }
}
