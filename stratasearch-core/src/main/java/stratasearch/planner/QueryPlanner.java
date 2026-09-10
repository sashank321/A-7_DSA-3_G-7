package stratasearch.planner;

import stratasearch.models.CorpusFingerprint;
import stratasearch.models.PlannerReason;
import stratasearch.models.PlannerResult;
import stratasearch.util.ManualStringList;

// Scores every candidate via CostModel, picks lowest, builds structured reason + decision trail.
public class QueryPlanner {

    private static final String[] CandidateNames = {
            "KMP", "Rabin-Karp", "Z Algorithm", "Aho-Corasick", "Suffix Array + LCP"
    };

    private final CostModel Model;

    public QueryPlanner() {
        Model = new CostModel();
    }

    private double AveragePatternLength(String[] Patterns) {
        if (Patterns.length == 0) {
            return 0.0;
        }
        int Sum = 0;
        for (int P = 0; P < Patterns.length; P++) {
            Sum += Patterns[P].length();
        }
        return (double) Sum / (double) Patterns.length;
    }

    private String RuntimeEstimateFor(String Name) {
        if ("KMP".equals(Name)) {
            return "O(P * (N + M))";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "O(P * (N + M)) average";
        }
        if ("Z Algorithm".equals(Name)) {
            return "O(P * (N + M))";
        }
        if ("Aho-Corasick".equals(Name)) {
            return "O(N + total pattern length + matches)";
        }
        return "Build O(N log N), Query O(P * M log N)";
    }

    private String MemoryEstimateFor(String Name) {
        if ("KMP".equals(Name)) {
            return "O(M + matches)";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "O(matches)";
        }
        if ("Z Algorithm".equals(Name)) {
            return "O(N + M + matches)";
        }
        if ("Aho-Corasick".equals(Name)) {
            return "O(total pattern length)";
        }
        return "O(N + matches)";
    }

    private PlannerReason BuildStructuredReason(String Name,
                                                CorpusFingerprint F,
                                                int PatternCount,
                                                boolean Repeated,
                                                double[] Scores,
                                                int[] Order,
                                                double Bias) {
        ManualStringList Advantages = new ManualStringList();
        ManualStringList TradeOffs = new ManualStringList();
        ManualStringList Recommended = new ManualStringList();
        ManualStringList Avoided = new ManualStringList();

        if ("Aho-Corasick".equals(Name)) {
            Advantages.Add("Single pass over the corpus finds all " + PatternCount + " patterns simultaneously.");
            Advantages.Add("Failure links preserve linear total work regardless of pattern count.");
            TradeOffs.Add("Trie memory grows with total pattern length.");
            TradeOffs.Add("Overkill overhead when only one pattern is present.");
            Recommended.Add("Multi-pattern workload with " + PatternCount + " patterns.");
            Recommended.Add("Corpus category: " + F.GetCorpusCategory() + ".");
        } else if ("Suffix Array + LCP".equals(Name)) {
            Advantages.Add("Index is built once and reused across every query.");
            Advantages.Add("Per-query lookup drops to O(M log N) after build.");
            if (Repeated) {
                Advantages.Add("Amortization makes repeated querying nearly free after the first build.");
            }
            TradeOffs.Add("Build cost O(N log N) is upfront.");
            TradeOffs.Add("O(N) memory for suffix and LCP tables.");
            if (Repeated) {
                Recommended.Add("Planner was told this is a repeated-query workload.");
            } else {
                Recommended.Add("Corpus category signals index-friendly reuse.");
            }
        } else if ("KMP".equals(Name)) {
            Advantages.Add("Deterministic linear scan with tiny O(M) auxiliary table.");
            Advantages.Add("Text pointer never backtracks; robust on adversarial inputs.");
            TradeOffs.Add("Handles one pattern per run; no index is reused.");
            Recommended.Add("Single exact pattern with guaranteed linear worst case.");
        } else if ("Rabin-Karp".equals(Name)) {
            Advantages.Add("Rolling hash supports constant-time window slide.");
            Advantages.Add("Natural stepping stone toward multiple-pattern hashing.");
            TradeOffs.Add("Worst case degrades if many hash collisions occur.");
            TradeOffs.Add("Still has to re-verify every hash hit character-by-character.");
            Recommended.Add("Hash-friendly probing on a modest corpus.");
        } else {
            Advantages.Add("Simple linear scan with no extra failure logic.");
            Advantages.Add("Z-box pruning keeps comparisons low when pattern is short.");
            TradeOffs.Add("Allocates an O(N+M) Z-array for every query.");
            TradeOffs.Add("Single pattern only.");
            Recommended.Add("Short pattern on a small-to-moderate corpus.");
        }

        // Explain why each runner-up was avoided using its score relative to chosen.
        double ChosenScore = Scores[Order[0]];
        for (int I = 1; I < Order.length; I++) {
            String Other = CandidateNames[Order[I]];
            double Gap = Scores[Order[I]] - ChosenScore;
            if ("Aho-Corasick".equals(Other)) {
                Avoided.Add("Aho-Corasick loses by " + F4(Gap) + " because workload is not multi-pattern heavy.");
            } else if ("Suffix Array + LCP".equals(Other)) {
                Avoided.Add("Suffix Array + LCP loses by " + F4(Gap) + " because index build cost is not amortized here.");
            } else if ("KMP".equals(Other)) {
                Avoided.Add("KMP loses by " + F4(Gap) + " because workload rewards an index/multi-pattern structure.");
            } else if ("Rabin-Karp".equals(Other)) {
                Avoided.Add("Rabin-Karp loses by " + F4(Gap) + " because hash overhead is not justified here.");
            } else {
                Avoided.Add("Z Algorithm loses by " + F4(Gap) + " because per-query allocation cost is not offset.");
            }
        }

        double Confidence = ComputeConfidence(Scores, Order);
        return new PlannerReason(
                Name,
                BuildPlainReason(Name, F, PatternCount, Repeated),
                Advantages.ToArray(),
                TradeOffs.ToArray(),
                Confidence,
                Recommended.ToArray(),
                Avoided.ToArray());
    }

    private String BuildPlainReason(String Name, CorpusFingerprint F, int PatternCount, boolean Repeated) {
        if ("Aho-Corasick".equals(Name)) {
            return "Multiple patterns (" + PatternCount + ") supplied; one-pass trie automaton wins for multi-pattern scan.";
        }
        if ("Suffix Array + LCP".equals(Name)) {
            if (Repeated) {
                return "Repeated queries over the same corpus; index build cost amortizes and each query runs in O(M log N).";
            }
            return "Corpus category " + F.GetCorpusCategory() + " favors indexed retrieval for repeated analysis.";
        }
        if ("KMP".equals(Name)) {
            return "Single exact pattern with stable linear guarantees on a corpus of " + F.GetCharacterCount() + " chars.";
        }
        if ("Rabin-Karp".equals(Name)) {
            return "Hash-based single-pattern probe; acceptable when corpus is modest and rolling-hash flexibility is useful.";
        }
        return "Short exact pattern on a moderate corpus; Z-box scan stays linear and simple.";
    }

    private double ComputeConfidence(double[] Scores, int[] Order) {
        if (Order.length < 2) {
            return 100.0;
        }
        double Best = Scores[Order[0]];
        double Runner = Scores[Order[1]];
        double Gap = Runner - Best;
        if (Gap <= 0.0) {
            return 50.0;
        }
        double Confidence = 50.0 + (Gap / (Runner <= 0.0 ? 1.0 : Runner)) * 50.0;
        if (Confidence > 99.0) Confidence = 99.0;
        if (Confidence < 50.0) Confidence = 50.0;
        long Rounded = (long) (Confidence * 100.0 + 0.5);
        return Rounded / 100.0;
    }

    private String[] BuildDecisionSteps(CorpusFingerprint F, String[] Patterns, boolean Repeated, double[] Scores, int[] Order, double Bias) {
        ManualStringList Steps = new ManualStringList();
        Steps.Add("Input analyzed: Patterns=" + Patterns.length + ", CorpusChars=" + F.GetCharacterCount()
                + ", VocabularyRichness=" + F4(F.GetVocabularyRichness())
                + ", PatternDensity=" + F4(F.GetPatternDensity())
                + ", Repeated=" + Repeated);
        if (Patterns.length >= 2) {
            Steps.Add("Patterns >= 2 ? YES -> multi-pattern workload favors Aho-Corasick");
        } else {
            Steps.Add("Patterns >= 2 ? NO -> single-pattern path");
        }
        if (Repeated) {
            Steps.Add("Repeated queries on same corpus ? YES -> suffix-array build cost can amortize");
        } else {
            Steps.Add("Repeated queries on same corpus ? NO -> index build cost is not amortized");
        }
        if ("INDEX_FRIENDLY".equals(F.GetCorpusCategory())) {
            Steps.Add("Corpus category INDEX_FRIENDLY -> suffix-array gets an extra score bonus");
        } else {
            Steps.Add("Corpus category " + F.GetCorpusCategory() + " -> no special index bonus applied");
        }
        if (Bias > 0.0) {
            Steps.Add("Optimization Bias (Memory) = " + F4(Bias) + " -> penalizing memory-heavy algorithms");
        }
        Steps.Add("Scored all 5 candidates via CostModel; lowest score wins");
        Steps.Add("Lowest score = " + F4(Scores[Order[0]]) + " for " + CandidateNames[Order[0]]);
        Steps.Add("Recommendation locked -> " + CandidateNames[Order[0]]);
        return Steps.ToArray();
    }

    public PlannerResult Plan(CorpusFingerprint F, String[] Patterns, boolean Repeated, double Bias) {
        int K = CandidateNames.length;
        double[] Scores = new double[K];
        double AvgLen = AveragePatternLength(Patterns);
        double AvgQueryLen = AvgLen; // patterns double as queries in Phase 1
        for (int I = 0; I < K; I++) {
            Scores[I] = Model.Score(CandidateNames[I], F, Patterns.length, AvgLen, Repeated, AvgQueryLen, Bias);
        }
        int[] Order = new int[K];
        for (int I = 0; I < K; I++) {
            Order[I] = I;
        }
        for (int I = 0; I < K - 1; I++) {
            int Min = I;
            for (int J = I + 1; J < K; J++) {
                if (Scores[Order[J]] < Scores[Order[Min]]) {
                    Min = J;
                }
            }
            int Tmp = Order[I];
            Order[I] = Order[Min];
            Order[Min] = Tmp;
        }
        String Chosen = CandidateNames[Order[0]];
        String[] Alternatives = new String[K - 1];
        for (int I = 1; I < K; I++) {
            Alternatives[I - 1] = CandidateNames[Order[I]];
        }
        String[] SortedNames = new String[K];
        double[] SortedScores = new double[K];
        for (int I = 0; I < K; I++) {
            SortedNames[I] = CandidateNames[Order[I]];
            SortedScores[I] = Scores[Order[I]];
        }
        PlannerReason Structured = BuildStructuredReason(Chosen, F, Patterns.length, Repeated, Scores, Order, Bias);
        String[] Steps = BuildDecisionSteps(F, Patterns, Repeated, Scores, Order, Bias);
        return new PlannerResult(
                Chosen,
                Structured,
                Structured.GetConfidencePercent(),
                RuntimeEstimateFor(Chosen),
                MemoryEstimateFor(Chosen),
                Alternatives,
                SortedNames,
                SortedScores,
                K,
                Steps);
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
