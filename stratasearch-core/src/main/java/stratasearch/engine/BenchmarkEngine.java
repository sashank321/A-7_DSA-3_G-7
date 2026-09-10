package stratasearch.engine;

import stratasearch.algorithms.SearchAlgorithm;
import stratasearch.algorithms.SearchResult;
import stratasearch.models.BenchmarkResult;
import stratasearch.models.TelemetryRecord;

// Wraps any SearchAlgorithm with measured time and memory telemetry.
public class BenchmarkEngine {

    private static class Measurement {
        long TimeNanos;
        long MemoryBytes;
        SearchResult Result;
    }

    private Measurement RunOnce(SearchAlgorithm Algo, String CorpusText, String[] Patterns) {
        Runtime Rt = Runtime.getRuntime();
        Rt.gc();
        long BeforeMem = Rt.totalMemory() - Rt.freeMemory();
        long Start = System.nanoTime();
        SearchResult Result = Algo.Search(CorpusText, Patterns);
        long End = System.nanoTime();
        long AfterMem = Rt.totalMemory() - Rt.freeMemory();
        long MemoryDelta = AfterMem - BeforeMem;
        if (MemoryDelta < 0) {
            MemoryDelta = 0;
        }
        Measurement M = new Measurement();
        M.TimeNanos = End - Start;
        M.MemoryBytes = MemoryDelta;
        M.Result = Result;
        return M;
    }

    public TelemetryRecord Measure(SearchAlgorithm Algo, String CorpusText, String[] Patterns) {
        Measurement M = RunOnce(Algo, CorpusText, Patterns);
        return new TelemetryRecord(
                M.Result.GetAlgorithmName(),
                M.TimeNanos,
                M.MemoryBytes,
                M.Result.GetComparisonCount(),
                M.Result.GetMatchCount(),
                M.Result.GetMatchPositions(),
                M.Result.GetMatchCount());
    }

    public BenchmarkResult MeasureBenchmark(SearchAlgorithm Algo, String CorpusText, String[] Patterns) {
        Measurement M = RunOnce(Algo, CorpusText, Patterns);
        return new BenchmarkResult(
                M.Result.GetAlgorithmName(),
                M.TimeNanos,
                M.MemoryBytes,
                M.Result.GetComparisonCount(),
                M.Result.GetMatchCount(),
                M.Result.GetMatchPositions(),
                Algo.GetTimeComplexity(),
                Algo.GetMemoryUsage());
    }
}
