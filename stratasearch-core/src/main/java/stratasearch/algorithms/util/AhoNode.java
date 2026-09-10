package stratasearch.algorithms.util;

// Trie / automaton node for Aho-Corasick. Fixed 128-slot children array.
public class AhoNode {
    private final AhoNode[] Children;
    private AhoNode Fail;
    private int[] OutputPatternIndices;
    private int OutputCount;
    private boolean Terminal;

    public AhoNode() {
        Children = new AhoNode[128];
        Fail = null;
        OutputPatternIndices = new int[4];
        OutputCount = 0;
        Terminal = false;
    }

    public AhoNode GetChild(char C) {
        return Children[C];
    }

    public void SetChild(char C, AhoNode Node) {
        Children[C] = Node;
    }

    public AhoNode GetFail() {
        return Fail;
    }

    public void SetFail(AhoNode Node) {
        Fail = Node;
    }

    public void AddOutput(int PatternIndex) {
        if (OutputCount == OutputPatternIndices.length) {
            int[] Grown = new int[OutputPatternIndices.length * 2];
            for (int I = 0; I < OutputCount; I++) {
                Grown[I] = OutputPatternIndices[I];
            }
            OutputPatternIndices = Grown;
        }
        OutputPatternIndices[OutputCount] = PatternIndex;
        OutputCount++;
    }

    public int GetOutputCount() {
        return OutputCount;
    }

    public int GetOutputPatternIndex(int I) {
        return OutputPatternIndices[I];
    }

    public boolean IsTerminal() {
        return Terminal;
    }

    public void SetTerminal(boolean Value) {
        Terminal = Value;
    }
}
