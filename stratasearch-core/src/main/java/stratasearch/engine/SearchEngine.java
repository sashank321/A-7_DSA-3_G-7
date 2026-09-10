package stratasearch.engine;

import stratasearch.algorithms.SearchAlgorithm;
import stratasearch.algorithms.SearchResult;

// Resolves algorithm name -> implementation via AlgorithmRegistry and executes the search.
public class SearchEngine {

    private final AlgorithmRegistry Registry;

    public SearchEngine(AlgorithmRegistry Registry) {
        this.Registry = Registry;
    }

    public SearchAlgorithm Resolve(String Name) {
        return Registry.Get(Name);
    }

    public String[] GetAllNames() {
        return Registry.GetAllNames();
    }

    public SearchResult Execute(String Name, String CorpusText, String[] Patterns) {
        SearchAlgorithm Algo = Resolve(Name);
        if (Algo == null) {
            return null;
        }
        return Algo.Search(CorpusText, Patterns);
    }
}
