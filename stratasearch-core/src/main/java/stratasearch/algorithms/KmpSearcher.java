package stratasearch.algorithms;

// Knuth-Morris-Pratt exact search. Iterates every supplied pattern over the corpus.
public class KmpSearcher implements SearchAlgorithm {

    public String GetName() {
        return "KMP";
    }

    public String GetTimeComplexity() {
        return "O(P * (N + M))";
    }

    public String GetMemoryUsage() {
        return "O(M + matches)";
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

    private int[] BuildLps(String Pattern, long[] Counter) {
        int M = Pattern.length();
        int[] Lps = new int[M];
        int Len = 0;
        int I = 1;
        Lps[0] = 0;
        while (I < M) {
            Counter[0]++;
            if (Pattern.charAt(I) == Pattern.charAt(Len)) {
                Len++;
                Lps[I] = Len;
                I++;
            } else {
                if (Len != 0) {
                    Len = Lps[Len - 1];
                } else {
                    Lps[I] = 0;
                    I++;
                }
            }
        }
        return Lps;
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
            int[] Lps = BuildLps(Pattern, Counter);
            int I = 0;
            int J = 0;
            while (I < N) {
                Counter[0]++;
                if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                    Sink.OnStep(StepIdx++, "compare", I, J,
                            "text[" + I + "] vs pattern[" + J + "]");
                }
                if (CorpusText.charAt(I) == Pattern.charAt(J)) {
                    I++;
                    J++;
                    if (J == M) {
                        Temp[Count] = I - M;
                        Count++;
                        PerPattern[P]++;
                        if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                            Sink.OnStep(StepIdx++, "match", I - M, J,
                                    "match at " + (I - M));
                        }
                        J = Lps[J - 1];
                    }
                } else {
                    if (J != 0) {
                        if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                            Sink.OnStep(StepIdx++, "fallback", I, J,
                                    "lps fallback " + J + " -> " + Lps[J - 1]);
                        }
                        J = Lps[J - 1];
                    } else {
                        I++;
                    }
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
