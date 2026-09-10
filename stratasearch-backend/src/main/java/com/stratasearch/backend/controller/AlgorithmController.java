package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.AlgorithmCapabilityResponse;
import com.stratasearch.backend.dto.AlgorithmCapabilitiesBulk;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.exception.ApiException;
import org.springframework.web.bind.annotation.*;
import stratasearch.algorithms.SearchAlgorithm;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/algorithms")
public class AlgorithmController {

    private final EngineGateway Gateway;

    public AlgorithmController(EngineGateway Gateway) {
        this.Gateway = Gateway;
    }

    @GetMapping
    public List<String> List() {
        List<String> Names = new ArrayList<>();
        for (SearchAlgorithm A : Gateway.GetAll()) {
            Names.add(A.GetName());
        }
        return Names;
    }

    @GetMapping("/capabilities")
    public AlgorithmCapabilitiesBulk Capabilities() {
        AlgorithmCapabilitiesBulk Bulk = new AlgorithmCapabilitiesBulk();
        Bulk.items = new ArrayList<>();
        for (SearchAlgorithm A : Gateway.GetAll()) {
            AlgorithmCapabilityResponse D = BuildCard(A.GetName());
            Bulk.items.add(D);
        }
        return Bulk;
    }

    @GetMapping("/{name}")
    public AlgorithmCapabilityResponse Capability(@PathVariable("name") String Name) {
        return BuildCard(Name);
    }

    private AlgorithmCapabilityResponse BuildCard(String Name) {
        SearchAlgorithm A = Gateway.Resolve(Name);
        if (A == null) {
            throw new ApiException(404, "Unknown algorithm: " + Name);
        }
        AlgorithmCapabilityResponse D = new AlgorithmCapabilityResponse();
        D.name = A.GetName();
        D.timeComplexity = A.GetTimeComplexity();
        D.memoryUsage = A.GetMemoryUsage();
        D.supportsMultiplePatterns = A.SupportsMultiplePatterns();
        D.supportsRepeatedQueries = A.SupportsRepeatedQueries();
        D.supportsIndexing = A.SupportsIndexing();
        D.description = Describe(A.GetName());
        D.bestUseCase = BestUseCase(A.GetName());
        return D;
    }

    private String Describe(String Name) {
        switch (Name) {
            case "KMP": return "Knuth-Morris-Pratt deterministic exact search with LPS preprocessing.";
            case "Rabin-Karp": return "Rolling-hash search with verification on hash hits.";
            case "Z Algorithm": return "Z-array prefix matching over Pattern+sentinel+Text.";
            case "Aho-Corasick": return "Trie automaton with failure links for multi-pattern one-pass search.";
            default: return "Suffix Array with LCP for indexed repeated retrieval.";
        }
    }

    private String BestUseCase(String Name) {
        switch (Name) {
            case "KMP": return "Single pattern, worst-case linear guarantee.";
            case "Rabin-Karp": return "Hash-friendly single-pattern probing.";
            case "Z Algorithm": return "Short patterns on small-to-moderate corpora.";
            case "Aho-Corasick": return "Many patterns in one pass (dictionary scan).";
            default: return "Repeated queries / phrase indexing.";
        }
    }
}
