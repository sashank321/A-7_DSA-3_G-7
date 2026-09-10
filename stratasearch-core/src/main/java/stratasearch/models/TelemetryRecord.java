package stratasearch.models;

// Immutable measured telemetry produced by BenchmarkEngine.
public class TelemetryRecord {
    private final String AlgorithmName;
    private final long ExecutionTimeNanos;
    private final long MemoryUsedBytes;
    private final long ComparisonCount;
    private final int MatchCount;
    private final int[] MatchPositions;
    private final int MatchPositionCount;

    public TelemetryRecord(String AlgorithmName,
                           long ExecutionTimeNanos,
                           long MemoryUsedBytes,
                           long ComparisonCount,
                           int MatchCount,
                           int[] MatchPositions,
                           int MatchPositionCount) {
        this.AlgorithmName = AlgorithmName;
        this.ExecutionTimeNanos = ExecutionTimeNanos;
        this.MemoryUsedBytes = MemoryUsedBytes;
        this.ComparisonCount = ComparisonCount;
        this.MatchCount = MatchCount;
        this.MatchPositions = MatchPositions;
        this.MatchPositionCount = MatchPositionCount;
    }

    public String GetAlgorithmName() {
        return AlgorithmName;
    }

    public long GetExecutionTimeNanos() {
        return ExecutionTimeNanos;
    }

    public long GetMemoryUsedBytes() {
        return MemoryUsedBytes;
    }

    public long GetComparisonCount() {
        return ComparisonCount;
    }

    public int GetMatchCount() {
        return MatchCount;
    }

    public int[] GetMatchPositions() {
        return MatchPositions;
    }

    public int GetMatchPositionCount() {
        return MatchPositionCount;
    }

    public String ToString() {
        StringBuilder Sb = new StringBuilder();
        Sb.append("=== Telemetry ===\n");
        Sb.append("Algorithm        : ").append(AlgorithmName).append('\n');
        Sb.append("ExecutionTime    : ").append(ExecutionTimeNanos).append(" ns\n");
        Sb.append("MemoryUsed       : ").append(MemoryUsedBytes).append(" bytes\n");
        Sb.append("Comparisons      : ").append(ComparisonCount).append('\n');
        Sb.append("MatchCount       : ").append(MatchCount).append('\n');
        Sb.append("MatchPositions   : ");
        if (MatchPositionCount == 0) {
            Sb.append("(none)");
        } else {
            for (int I = 0; I < MatchPositionCount; I++) {
                if (I > 0) {
                    Sb.append(", ");
                }
                Sb.append(MatchPositions[I]);
            }
        }
        return Sb.toString();
    }
}
