package stratasearch.engine;

import stratasearch.algorithms.SearchAlgorithm;

// Open-closed registry of algorithm instances. New algorithms only call Register once.
public class AlgorithmRegistry {
    private SearchAlgorithm[] Algorithms;
    private int Count;
    private static final int InitialCapacity = 8;

    public AlgorithmRegistry() {
        Algorithms = new SearchAlgorithm[InitialCapacity];
        Count = 0;
    }

    public void Register(SearchAlgorithm Algorithm) {
        if (Count == Algorithms.length) {
            SearchAlgorithm[] Grown = new SearchAlgorithm[Algorithms.length * 2];
            for (int I = 0; I < Count; I++) {
                Grown[I] = Algorithms[I];
            }
            Algorithms = Grown;
        }
        Algorithms[Count] = Algorithm;
        Count++;
    }

    public SearchAlgorithm Get(String Name) {
        for (int I = 0; I < Count; I++) {
            if (Algorithms[I].GetName().equals(Name)) {
                return Algorithms[I];
            }
        }
        return null;
    }

    public int GetCount() {
        return Count;
    }

    public SearchAlgorithm GetAt(int Index) {
        if (Index < 0 || Index >= Count) {
            return null;
        }
        return Algorithms[Index];
    }

    public String[] GetAllNames() {
        String[] Names = new String[Count];
        for (int I = 0; I < Count; I++) {
            Names[I] = Algorithms[I].GetName();
        }
        return Names;
    }
}
