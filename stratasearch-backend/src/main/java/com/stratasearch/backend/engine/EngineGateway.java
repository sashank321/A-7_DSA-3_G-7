package com.stratasearch.backend.engine;

import stratasearch.algorithms.AhoCorasickSearcher;
import stratasearch.algorithms.KmpSearcher;
import stratasearch.algorithms.RabinKarpSearcher;
import stratasearch.algorithms.SearchAlgorithm;
import stratasearch.algorithms.SuffixArraySearcher;
import stratasearch.algorithms.ZAlgorithmSearcher;
import stratasearch.engine.AlgorithmRegistry;
import stratasearch.engine.BenchmarkEngine;
import stratasearch.engine.SearchEngine;
import stratasearch.models.BenchmarkResult;
import stratasearch.models.CorpusFingerprint;
import stratasearch.models.PlannerResult;
import stratasearch.models.TelemetryRecord;
import stratasearch.planner.CostModel;
import stratasearch.planner.ExplainabilityEngine;
import stratasearch.planner.QueryPlanner;
import stratasearch.profiler.CorpusProfiler;
import stratasearch.util.TextNormalizer;

// Single Spring-side gateway that owns every interaction with the stratasearch core.
// All other backend layers must go through this class. Nothing else constructs core objects.
public class EngineGateway {

    private final AlgorithmRegistry Registry;
    private final SearchEngine Search;
    private final BenchmarkEngine Benchmark;
    private final QueryPlanner Planner;
    private final CorpusProfiler Profiler;
    private final ExplainabilityEngine Explain;

    public EngineGateway() {
        Registry = new AlgorithmRegistry();
        Registry.Register(new KmpSearcher());
        Registry.Register(new RabinKarpSearcher());
        Registry.Register(new ZAlgorithmSearcher());
        Registry.Register(new AhoCorasickSearcher());
        Registry.Register(new SuffixArraySearcher());
        Search = new SearchEngine(Registry);
        Benchmark = new BenchmarkEngine();
        Planner = new QueryPlanner();
        Profiler = new CorpusProfiler();
        Explain = new ExplainabilityEngine();
    }

    public String JoinDocuments(String[] Documents) {
        return TextNormalizer.JoinDocuments(Documents);
    }

    public CorpusFingerprint Profile(String[] Documents, String CorpusId,
                                     boolean MultiPatternIntent, boolean RepeatQueryIntent,
                                     double AverageQueryLength) {
        return Profiler.Profile(Documents, CorpusId, MultiPatternIntent, RepeatQueryIntent, AverageQueryLength);
    }

    public PlannerResult Plan(CorpusFingerprint F, String[] Patterns, boolean Repeated, double optimizationBias) {
        return Planner.Plan(F, Patterns, Repeated, optimizationBias);
    }

    public String ExplainPlan(PlannerResult Result) {
        return Explain.Explain(Result);
    }

    public SearchAlgorithm Resolve(String Name) {
        return Search.Resolve(Name);
    }

    public SearchAlgorithm[] GetAll() {
        String[] Names = Search.GetAllNames();
        SearchAlgorithm[] Out = new SearchAlgorithm[Names.length];
        for (int I = 0; I < Names.length; I++) {
            Out[I] = Search.Resolve(Names[I]);
        }
        return Out;
    }

    public TelemetryRecord MeasureTelemetry(String AlgorithmName, String CorpusText, String[] Patterns) {
        SearchAlgorithm Algo = Search.Resolve(AlgorithmName);
        if (Algo == null) {
            return null;
        }
        return Benchmark.Measure(Algo, CorpusText, Patterns);
    }

    public BenchmarkResult MeasureBenchmark(String AlgorithmName, String CorpusText, String[] Patterns) {
        SearchAlgorithm Algo = Search.Resolve(AlgorithmName);
        if (Algo == null) {
            return null;
        }
        return Benchmark.MeasureBenchmark(Algo, CorpusText, Patterns);
    }

    public BenchmarkResult[] MeasureAllBenchmarks(String CorpusText, String[] Patterns) {
        SearchAlgorithm[] All = GetAll();
        BenchmarkResult[] Out = new BenchmarkResult[All.length];
        for (int I = 0; I < All.length; I++) {
            Out[I] = Benchmark.MeasureBenchmark(All[I], CorpusText, Patterns);
        }
        return Out;
    }

    public boolean IsRegisteredAlgorithm(String Name) {
        return Search.Resolve(Name) != null;
    }
}
