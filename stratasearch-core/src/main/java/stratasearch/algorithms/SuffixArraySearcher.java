package stratasearch.algorithms;

import stratasearch.algorithms.util.SuffixSortUtil;

// Suffix Array + LCP index search. Best when many queries hit the same corpus.
public class SuffixArraySearcher implements SearchAlgorithm {

    public String GetName() {
        return "Suffix Array + LCP";
    }

    public String GetTimeComplexity() {
        return "Build O(N log N), Query O(M log N)";
    }

    public String GetMemoryUsage() {
        return "O(N)";
    }

    public boolean SupportsMultiplePatterns() {
        return true;
    }

    public boolean SupportsRepeatedQueries() {
        return true;
    }

    public boolean SupportsIndexing() {
        return true;
    }

    // Compare suffix starting at FromIndex to bounded probe [Probe, ProbeLen). Counts comparisons.
    private static int CompareSuffixToProbe(String Corpus, int FromIndex, char[] Probe, int ProbeLen, long[] Counter) {
        int N = Corpus.length();
        int K = 0;
        while (K < ProbeLen && FromIndex + K < N) {
            Counter[0]++;
            char A = Corpus.charAt(FromIndex + K);
            char B = Probe[K];
            if (A != B) {
                return A - B;
            }
            K++;
        }
        if (K == ProbeLen) {
            return 0;
        }
        return -1;
    }

    private static int[] BinarySearchRange(String Corpus, int[] Sa, String Pattern, long[] Counter) {
        int M = Pattern.length();
        char[] Probe = new char[M + 1];
        for (int I = 0; I < M; I++) {
            Probe[I] = Pattern.charAt(I);
        }
        Probe[M] = '\uFFFF';
        int N = Sa.length;
        int LoB = 0;
        int HiB = N;
        while (LoB < HiB) {
            int Mid = (LoB + HiB) / 2;
            if (CompareSuffixToProbe(Corpus, Sa[Mid], Probe, M, Counter) < 0) {
                LoB = Mid + 1;
            } else {
                HiB = Mid;
            }
        }
        int Lo = LoB;
        LoB = 0;
        HiB = N;
        while (LoB < HiB) {
            int Mid = (LoB + HiB) / 2;
            if (CompareSuffixToProbe(Corpus, Sa[Mid], Probe, M + 1, Counter) < 0) {
                LoB = Mid + 1;
            } else {
                HiB = Mid;
            }
        }
        return new int[]{Lo, LoB};
    }

    public SearchResult Search(String CorpusText, String[] Patterns) {
        return Search(CorpusText, Patterns, null);
    }

    public SearchResult Search(String CorpusText, String[] Patterns, SearchStepSink Sink) {
        long[] Counter = new long[1];
        int N = CorpusText.length();
        int PCount = Patterns.length;
        int[] PerPattern = new int[PCount];
        if (N == 0 || PCount == 0) {
            return new SearchResult(GetName(), new int[0], 0, 0, PerPattern);
        }
        int[] Sa = SuffixSortUtil.BuildSuffixArray(CorpusText);
        int[] Lcp = SuffixSortUtil.BuildLcpArray(CorpusText, Sa);
        int[] Temp = new int[N * PCount + 1];
        int Count = 0;
        int StepIdx = 0;
        for (int P = 0; P < PCount; P++) {
            String Pattern = Patterns[P];
            int M = Pattern.length();
            if (M == 0 || M > N) {
                continue;
            }
            if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                Sink.OnStep(StepIdx++, "sa-binary-search", -1, M,
                        "binary search suffix array for \"" + Pattern + "\"");
            }
            int[] Range = BinarySearchRange(CorpusText, Sa, Pattern, Counter);
            int Lo = Range[0];
            int Hi = Range[1];
            if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                Sink.OnStep(StepIdx++, "sa-range", Lo, Hi, "matched suffix range [" + Lo + "," + Hi + ")");
            }
            for (int Slot = Lo; Slot < Hi; Slot++) {
                if (Sa[Slot] + M <= N) {
                    boolean Verified = true;
                    for (int J = 0; J < M; J++) {
                        Counter[0]++;
                        if (CorpusText.charAt(Sa[Slot] + J) != Pattern.charAt(J)) {
                            Verified = false;
                            break;
                        }
                    }
                    if (Verified && Count < Temp.length) {
                        Temp[Count] = Sa[Slot];
                        Count++;
                        PerPattern[P]++;
                        if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                            Sink.OnStep(StepIdx++, "match", Sa[Slot], M,
                                    "suffix[" + Slot + "] starts match at " + Sa[Slot]);
                        }
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
