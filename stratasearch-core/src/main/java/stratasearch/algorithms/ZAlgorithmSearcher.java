package stratasearch.algorithms;

// Z Algorithm over concatenated (Pattern + '\u0001' + Text). Iterates every supplied pattern.
public class ZAlgorithmSearcher implements SearchAlgorithm {

    public String GetName() {
        return "Z Algorithm";
    }

    public String GetTimeComplexity() {
        return "O(P * (N + M))";
    }

    public String GetMemoryUsage() {
        return "O(N + M + matches)";
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
            String Combined = Pattern + "" + CorpusText;
            int L = Combined.length();
            int[] Z = new int[L];
            int Left = 0;
            int Right = 0;
            for (int I = 1; I < L; I++) {
                if (I > Right) {
                    Left = I;
                    Right = I;
                    while (Right < L) {
                        Counter[0]++;
                        if (Combined.charAt(Right - Left) == Combined.charAt(Right)) {
                            if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                                Sink.OnStep(StepIdx++, "z-expand", Right, Right - Left,
                                        "expand Z box at " + I);
                            }
                            Right++;
                        } else {
                            break;
                        }
                    }
                    Z[I] = Right - Left;
                    Right--;
                } else {
                    int K = I - Left;
                    if (Z[K] < Right - I + 1) {
                        Z[I] = Z[K];
                    } else {
                        Left = I;
                        while (Right < L) {
                            Counter[0]++;
                            if (Combined.charAt(Right - Left) == Combined.charAt(Right)) {
                                if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                                    Sink.OnStep(StepIdx++, "z-expand", Right, Right - Left,
                                            "expand inside Z box at " + I);
                                }
                                Right++;
                            } else {
                                break;
                            }
                        }
                        Z[I] = Right - Left;
                        Right--;
                    }
                }
            }
            for (int I = M + 1; I < L; I++) {
                if (Z[I] == M) {
                    Temp[Count] = I - (M + 1);
                    Count++;
                    PerPattern[P]++;
                    if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                        Sink.OnStep(StepIdx++, "match", I - (M + 1), M,
                                "match at " + (I - (M + 1)));
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
