package stratasearch.planner;

import stratasearch.models.CorpusFingerprint;

// Rule-based cost model. Lower score = better fit for this workload.
// All weights are constants declared here so the logic is transparent and explainable.
public class CostModel {

    private static final double W_SIZE = 0.35;
    private static final double W_PATTERN = 0.25;
    private static final double W_REPEAT = 0.20;
    private static final double W_ENTROPY = 0.20;
    private static final double W_QUERY = 0.10;

    private double NormalizeSize(int CharCount) {
        if (CharCount <= 0) {
            return 0.0;
        }
        double V = CharCount / 100000.0;
        return V > 1.0 ? 1.0 : V;
    }

    private double NormalizeLen(double AvgPatternLen) {
        double V = AvgPatternLen / 100.0;
        return V > 1.0 ? 1.0 : V;
    }

    public double ScoreKmp(CorpusFingerprint F, int PatternCount, double AvgPatternLen, boolean Repeated, double AvgQueryLen, double Bias) {
        double Score = 0.5;
        Score += W_PATTERN * (PatternCount > 1 ? 0.6 : 0.0);
        Score += W_REPEAT * (Repeated ? 0.5 : 0.0);
        Score -= W_SIZE * NormalizeSize(F.GetCharacterCount()) * 0.3;
        Score -= W_ENTROPY * (F.GetEntropyEstimate() / 4.0) * 0.1;
        Score += W_QUERY * NormalizeLen(AvgQueryLen) * 0.05;
        Score += Bias * 0.1; // O(M) memory penalty
        return Score;
    }

    public double ScoreRabinKarp(CorpusFingerprint F, int PatternCount, double AvgPatternLen, boolean Repeated, double AvgQueryLen, double Bias) {
        double Score = 0.55;
        Score += W_PATTERN * (PatternCount > 1 ? 0.7 : 0.0);
        Score += W_REPEAT * (Repeated ? 0.5 : 0.0);
        Score += W_SIZE * NormalizeSize(F.GetCharacterCount()) * 0.2;
        Score -= W_PATTERN * NormalizeLen(AvgPatternLen) * 0.1;
        Score += W_QUERY * NormalizeLen(AvgQueryLen) * 0.05;
        Score += Bias * 0.0; // O(1) memory footprint - zero penalty
        return Score;
    }

    public double ScoreZ(CorpusFingerprint F, int PatternCount, double AvgPatternLen, boolean Repeated, double AvgQueryLen, double Bias) {
        double Score = 0.55;
        Score += W_PATTERN * (PatternCount > 1 ? 0.65 : 0.0);
        Score += W_REPEAT * (Repeated ? 0.5 : 0.0);
        Score += W_SIZE * NormalizeSize(F.GetCharacterCount()) * 0.25;
        Score -= W_PATTERN * (AvgPatternLen <= 10 ? 0.15 : 0.0);
        Score += W_QUERY * NormalizeLen(AvgQueryLen) * 0.05;
        Score += Bias * 0.9; // O(N+M) memory allocation per query
        return Score;
    }

    public double ScoreAhoCorasick(CorpusFingerprint F, int PatternCount, double AvgPatternLen, boolean Repeated, double AvgQueryLen, double Bias) {
        double Score = 0.70;
        if (PatternCount <= 1) {
            Score += 0.40;
        } else {
            Score -= W_PATTERN * 0.55;
        }
        Score += W_REPEAT * (Repeated ? 0.4 : 0.0);
        Score += W_SIZE * NormalizeSize(F.GetCharacterCount()) * 0.1;
        Score -= W_QUERY * NormalizeLen(AvgQueryLen) * 0.05;
        Score += Bias * 1.5; // Heavy penalty for building a large trie state machine
        return Score;
    }

    public double ScoreSuffixArray(CorpusFingerprint F, int PatternCount, double AvgPatternLen, boolean Repeated, double AvgQueryLen, double Bias) {
        double Score = 0.75;
        Score -= W_REPEAT * (Repeated ? 0.55 : -0.25);
        Score += W_SIZE * (1.0 - NormalizeSize(F.GetCharacterCount())) * 0.15;
        boolean IndexFriendly = "INDEX_FRIENDLY".equals(F.GetCorpusCategory()) || F.HasCategoryTag("INDEX_FRIENDLY");
        if (IndexFriendly) {
            Score -= 0.10;
        }
        if (PatternCount > 1 && !Repeated) {
            Score += 0.15;
        }
        Score -= W_QUERY * NormalizeLen(AvgQueryLen) * 0.05;
        Score += Bias * 1.2; // High penalty for O(N) indexing arrays
        return Score;
    }

    public double Score(String AlgorithmName,
                        CorpusFingerprint F,
                        int PatternCount,
                        double AvgPatternLen,
                        boolean Repeated,
                        double AvgQueryLen,
                        double Bias) {
        if ("KMP".equals(AlgorithmName)) {
            return ScoreKmp(F, PatternCount, AvgPatternLen, Repeated, AvgQueryLen, Bias);
        }
        if ("Rabin-Karp".equals(AlgorithmName)) {
            return ScoreRabinKarp(F, PatternCount, AvgPatternLen, Repeated, AvgQueryLen, Bias);
        }
        if ("Z Algorithm".equals(AlgorithmName)) {
            return ScoreZ(F, PatternCount, AvgPatternLen, Repeated, AvgQueryLen, Bias);
        }
        if ("Aho-Corasick".equals(AlgorithmName)) {
            return ScoreAhoCorasick(F, PatternCount, AvgPatternLen, Repeated, AvgQueryLen, Bias);
        }
        return ScoreSuffixArray(F, PatternCount, AvgPatternLen, Repeated, AvgQueryLen, Bias);
    }
}
