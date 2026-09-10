package stratasearch.algorithms;

import stratasearch.algorithms.util.AhoNode;
import stratasearch.algorithms.util.ManualQueue;

// Aho-Corasick multi-pattern automaton. Scans corpus once for all patterns.
public class AhoCorasickSearcher implements SearchAlgorithm {

    public String GetName() {
        return "Aho-Corasick";
    }

    public String GetTimeComplexity() {
        return "O(N + total pattern length + matches)";
    }

    public String GetMemoryUsage() {
        return "O(total pattern length)";
    }

    public boolean SupportsMultiplePatterns() {
        return true;
    }

    public boolean SupportsRepeatedQueries() {
        return false;
    }

    public boolean SupportsIndexing() {
        return false;
    }

    private int CountTrieInsertions(String[] Patterns) {
        int Total = 0;
        for (int P = 0; P < Patterns.length; P++) {
            Total += Patterns[P].length();
        }
        return Total + 1;
    }

    private AhoNode BuildTrie(String[] Patterns) {
        AhoNode Root = new AhoNode();
        for (int P = 0; P < Patterns.length; P++) {
            AhoNode Current = Root;
            for (int I = 0; I < Patterns[P].length(); I++) {
                char C = Patterns[P].charAt(I);
                AhoNode Next = Current.GetChild(C);
                if (Next == null) {
                    Next = new AhoNode();
                    Current.SetChild(C, Next);
                }
                Current = Next;
            }
            Current.SetTerminal(true);
            Current.AddOutput(P);
        }
        return Root;
    }

    private void BuildFailureLinks(AhoNode Root, int NodeCapacity) {
        ManualQueue Queue = new ManualQueue(NodeCapacity);
        Root.SetFail(Root);
        for (char C = 0; C < 128; C++) {
            AhoNode Child = Root.GetChild(C);
            if (Child != null) {
                Child.SetFail(Root);
                Queue.Enqueue(Child);
            }
        }
        while (!Queue.IsEmpty()) {
            AhoNode Current = (AhoNode) Queue.Dequeue();
            for (char C = 0; C < 128; C++) {
                AhoNode Child = Current.GetChild(C);
                if (Child != null) {
                    AhoNode Fallback = Current.GetFail();
                    while (Fallback != Root && Fallback.GetChild(C) == null) {
                        Fallback = Fallback.GetFail();
                    }
                    AhoNode Target = Fallback.GetChild(C);
                    if (Target == null || Target == Child) {
                        Target = Root;
                    }
                    Child.SetFail(Target);
                    for (int O = 0; O < Target.GetOutputCount(); O++) {
                        Child.AddOutput(Target.GetOutputPatternIndex(O));
                    }
                    Queue.Enqueue(Child);
                }
            }
        }
    }

    public SearchResult Search(String CorpusText, String[] Patterns) {
        return Search(CorpusText, Patterns, null);
    }

    public SearchResult Search(String CorpusText, String[] Patterns, SearchStepSink Sink) {
        long[] Counter = new long[1];
        int PCount = Patterns.length;
        int[] PerPattern = new int[PCount];
        int N = CorpusText.length();
        int[] Temp = new int[N == 0 ? 1 : N * PCount + 1];
        int Count = 0;
        int StepIdx = 0;
        if (PCount > 0) {
            int NodeCapacity = CountTrieInsertions(Patterns);
            AhoNode Root = BuildTrie(Patterns);
            BuildFailureLinks(Root, NodeCapacity);
            AhoNode Current = Root;
            for (int I = 0; I < N; I++) {
                char C = CorpusText.charAt(I);
                while (Current != Root && Current.GetChild(C) == null) {
                    if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                        Sink.OnStep(StepIdx++, "fail", I, -1,
                                "fall back via failure link on '" + C + "'");
                    }
                    Current = Current.GetFail();
                }
                AhoNode Next = Current.GetChild(C);
                if (Next != null) {
                    Current = Next;
                } else {
                    Current = Root;
                }
                if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                    Sink.OnStep(StepIdx++, "trie-step", I, -1,
                            "consume '" + C + "' -> " + (Current == Root ? "root" : "trie-state"));
                }
                for (int O = 0; O < Current.GetOutputCount(); O++) {
                    Counter[0]++;
                    int PatIdx = Current.GetOutputPatternIndex(O);
                    PerPattern[PatIdx]++;
                    if (Count < Temp.length) {
                        Temp[Count] = I - Patterns[PatIdx].length() + 1;
                        if (Sink != null && StepIdx < SearchStepSink.MaxSteps) {
                            Sink.OnStep(StepIdx++, "match", Temp[Count], Patterns[PatIdx].length(),
                                    "pattern[" + PatIdx + "] \"" + Patterns[PatIdx] + "\" @ " + Temp[Count]);
                        }
                        Count++;
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
