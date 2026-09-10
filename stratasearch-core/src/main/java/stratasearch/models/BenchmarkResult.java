package stratasearch.models;

// Immutable benchmark outcome for one algorithm on one workload.
public class BenchmarkResult {
    private final String AlgorithmName;
    private final long ExecutionTimeNanos;
    private final long MemoryUsedBytes;
    private final long ComparisonCount;
    private final int MatchCount;
    private final int[] MatchPositions;
    private final String Complexity;
    private final String MemoryComplexity;

    public BenchmarkResult(String AlgorithmName,
                           long ExecutionTimeNanos,
                           long MemoryUsedBytes,
                           long ComparisonCount,
                           int MatchCount,
                           int[] MatchPositions,
                           String Complexity,
                           String MemoryComplexity) {
        this.AlgorithmName = AlgorithmName;
        this.ExecutionTimeNanos = ExecutionTimeNanos;
        this.MemoryUsedBytes = MemoryUsedBytes;
        this.ComparisonCount = ComparisonCount;
        this.MatchCount = MatchCount;
        this.MatchPositions = MatchPositions;
        this.Complexity = Complexity;
        this.MemoryComplexity = MemoryComplexity;
    }

    public String GetAlgorithmName() { return AlgorithmName; }
    public long GetExecutionTimeNanos() { return ExecutionTimeNanos; }
    public long GetMemoryUsedBytes() { return MemoryUsedBytes; }
    public long GetComparisonCount() { return ComparisonCount; }
    public int GetMatchCount() { return MatchCount; }
    public int[] GetMatchPositions() { return MatchPositions; }
    public String GetComplexity() { return Complexity; }
    public String GetMemoryComplexity() { return MemoryComplexity; }
}
