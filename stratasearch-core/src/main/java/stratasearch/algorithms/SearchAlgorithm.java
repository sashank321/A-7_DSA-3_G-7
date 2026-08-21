package stratasearch.algorithms;

// Common contract implemented by every classical string algorithm.
public interface SearchAlgorithm {
    String GetName();

    // Original zero-arg-sink behaviour preserved for backward compatibility.
    default SearchResult Search(String CorpusText, String[] Patterns) {
        return Search(CorpusText, Patterns, null);
    }

    // Sink may be null; when non-null the algorithm reports up to SearchStepSink.MaxSteps.
    SearchResult Search(String CorpusText, String[] Patterns, SearchStepSink Sink);

    String GetTimeComplexity();

    String GetMemoryUsage();

    boolean SupportsMultiplePatterns();

    boolean SupportsRepeatedQueries();

    boolean SupportsIndexing();
}
