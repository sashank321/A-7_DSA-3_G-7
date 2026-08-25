package stratasearch.algorithms;

// Rabin-Karp rolling hash. Iterates every supplied pattern over the corpus with manual verification.
public class RabinKarpSearcher implements SearchAlgorithm {
    private static final long Base = 257L;
    private static final long Mod = 1000000007L;

    public String GetName() {
        return "Rabin-Karp";
    }

    public String GetTimeComplexity() {
        return "O(P * (N + M)) average";
    }

    public String GetMemoryUsage() {
        return "O(matches)";
    }

    public boolean SupportsMultiplePatterns() {
        return false;
    }

    public boolean SupportsRepeatedQueries() {
        return false;
    }

    public boolean SupportsIndexing() {
        return false;
    }

    public SearchResult Search(String CorpusText, String[] Patterns) {
        return Search(CorpusText, Patterns, null);
    }

    public SearchResult Search(String CorpusText, String[] Patterns, SearchStepSink Sink) {
        long[] Counter = new long[1];
        int N = CorpusText.length();
        int PCount = Patterns.length;
        int[] PerPattern = new int[PCount];
        int[] Temp = new int[N == 0 ? 1 : N * PCount + 1];
        int Count = 0;
        int StepIdx = 0;
        for (int P = 0; P < PCount; P++) {
            String Pattern = Patterns[P];
            int M = Pattern.length();
            if (M == 0 || N < M) {
                continue;
            }
            long HighPower = 1L;
            for (int I = 0; I < M - 1; I++) {
                HighPower = (HighPower * Base) % Mod;
            }
            long PatternHash = 0L;
            long WindowHash = 0L;
            for (int I = 0; I < M; I++) {
                PatternHash = (PatternHash * Base + (long) Pattern.charAt(I)) % Mod;
                WindowHash = (WindowHash * Base + (long) CorpusText.charAt(I)) % Mod;
            }
            for (int I = 0; I <= N - M; I++) {
                Counter[0]++;
                if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                    Sink.OnStep(StepIdx++, "hash-compare", I, 0,
                            "window@" + I + " hash=" + WindowHash + " vs " + PatternHash);
                }
                if (PatternHash == WindowHash) {
                    boolean Verified = true;
                    for (int J = 0; J < M; J++) {
                        Counter[0]++;
                        if (CorpusText.charAt(I + J) != Pattern.charAt(J)) {
                            Verified = false;
                            break;
                        }
                    }
                    if (Verified) {
                        Temp[Count] = I;
                        Count++;
                        PerPattern[P]++;
                        if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                            Sink.OnStep(StepIdx++, "match", I, M, "match at " + I);
                        }
                    }
                }
                if (I < N - M) {
                    WindowHash = (WindowHash - (long) CorpusText.charAt(I) * HighPower % Mod + Mod) % Mod;
                    WindowHash = (WindowHash * Base + (long) CorpusText.charAt(I + M)) % Mod;
                }
            }
        }
        int[] Positions = new int[Count];
        for (int K = 0; K < Count; K++) {
            Positions[K] = Temp[K];
        }
        IntSortUtil.SortAscending(Positions, Count);
        return new SearchResult(GetName(), Positions, Count, Counter[0], PerPattern);
    }
}
