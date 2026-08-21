package stratasearch.algorithms;

// Result of a single Search() call. ComparisonCount is measured inside the algorithm.
public class SearchResult {
    private final String AlgorithmName;
    private final int[] MatchPositions;
    private final int MatchCount;
    private final long ComparisonCount;
    private final int[] PerPatternMatchCounts;

    public SearchResult(String AlgorithmName,
                        int[] MatchPositions,
                        int MatchCount,
                        long ComparisonCount,
                        int[] PerPatternMatchCounts) {
        this.AlgorithmName = AlgorithmName;
        this.MatchPositions = MatchPositions;
        this.MatchCount = MatchCount;
        this.ComparisonCount = ComparisonCount;
        this.PerPatternMatchCounts = PerPatternMatchCounts;
    }

    public String GetAlgorithmName() {
        return AlgorithmName;
    }

    public int[] GetMatchPositions() {
        return MatchPositions;
    }

    public int GetMatchCount() {
        return MatchCount;
    }

    public long GetComparisonCount() {
        return ComparisonCount;
    }

    public int GetPerPatternMatchCount(int PatternIndex) {
        if (PerPatternMatchCounts == null || PatternIndex >= PerPatternMatchCounts.length) {
            return -1;
        }
        return PerPatternMatchCounts[PatternIndex];
    }

    public String PositionsToString() {
        if (MatchCount == 0) {
            return "(none)";
        }
        StringBuilder Sb = new StringBuilder();
        for (int I = 0; I < MatchCount; I++) {
            if (I > 0) {
                Sb.append(", ");
            }
            Sb.append(MatchPositions[I]);
        }
        return Sb.toString();
    }
}
