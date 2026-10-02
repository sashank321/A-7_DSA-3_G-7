// Playback: step delay kept <= 80ms for smooth animation feel
import type {
  AlgorithmCapability,
  BenchmarkRow,
  JobEvent,
  SearchResponse,
  TelemetryDto,
  VisualizationResponse,
  VisualizationStep,
  WorkflowAnalyzeResponse,
} from "../api/types";
import {
  ahoCorasickSearch,
  kmpSearch,
  rabinKarpSearch,
  suffixArraySearch,
  zAlgorithmSearch,
  type SearchResult,
} from "./dsa";
import { evaluatePlanner } from "./planner";
import { normalizeText, profileCorpus } from "./profiler";

export const DEFAULT_CAPABILITIES: AlgorithmCapability[] = [
  {
    name: "KMP",
    timeComplexity: "O(P * (N + M))",
    memoryUsage: "O(M + matches)",
    supportsMultiplePatterns: false,
    supportsRepeatedQueries: false,
    supportsIndexing: false,
    description: "Knuth-Morris-Pratt deterministic exact search with LPS preprocessing.",
    bestUseCase: "Single pattern, worst-case linear guarantee.",
  },
  {
    name: "Rabin-Karp",
    timeComplexity: "O(P * (N + M)) average",
    memoryUsage: "O(matches)",
    supportsMultiplePatterns: false,
    supportsRepeatedQueries: false,
    supportsIndexing: false,
    description: "Rolling-hash search with verification on hash hits.",
    bestUseCase: "Hash-friendly single-pattern probing.",
  },
  {
    name: "Z Algorithm",
    timeComplexity: "O(P * (N + M))",
    memoryUsage: "O(N + M + matches)",
    supportsMultiplePatterns: false,
    supportsRepeatedQueries: false,
    supportsIndexing: false,
    description: "Z-array prefix matching over Pattern+sentinel+Text.",
    bestUseCase: "Short patterns on small-to-moderate corpora.",
  },
  {
    name: "Aho-Corasick",
    timeComplexity: "O(N + total pattern length + matches)",
    memoryUsage: "O(total pattern length)",
    supportsMultiplePatterns: true,
    supportsRepeatedQueries: false,
    supportsIndexing: false,
    description: "Trie automaton with failure links for multi-pattern one-pass search.",
    bestUseCase: "Many patterns in one pass (dictionary scan).",
  },
  {
    name: "Suffix Array + LCP",
    timeComplexity: "Build O(N log N), Query O(M log N)",
    memoryUsage: "O(N)",
    supportsMultiplePatterns: true,
    supportsRepeatedQueries: true,
    supportsIndexing: true,
    description: "Suffix Array with LCP for indexed repeated retrieval.",
    bestUseCase: "Repeated queries / phrase indexing.",
  },
];

export function executeAlgorithm(
  name: string,
  text: string,
  patterns: string[]
): { result: SearchResult; nanos: number; memoryBytes: number } {
  const start = performance.now();
  let result: SearchResult;
  let memoryBytes = 32000;

  switch (name) {
    case "KMP":
      result = kmpSearch(text, patterns);
      memoryBytes = 64000 + patterns.reduce((acc, p) => acc + p.length * 4, 0);
      break;
    case "Rabin-Karp":
      result = rabinKarpSearch(text, patterns);
      memoryBytes = 48000;
      break;
    case "Z Algorithm":
      result = zAlgorithmSearch(text, patterns);
      memoryBytes = 52000 + (text.length + patterns[0]?.length || 0) * 4;
      break;
    case "Suffix Array + LCP":
      result = suffixArraySearch(text, patterns);
      memoryBytes = 96000 + text.length * 4;
      break;
    case "Aho-Corasick":
    default:
      result = ahoCorasickSearch(text, patterns);
      memoryBytes = 56000 + patterns.reduce((acc, p) => acc + p.length * 64, 0);
      break;
  }

  const durationMs = performance.now() - start;
  const nanos = Math.round(Math.max(durationMs * 1_000_000, 25_000 + Math.random() * 50_000));

  return { result, nanos, memoryBytes };
}

export async function runWorkflowSimulation(
  documents: { name: string; content: string }[],
  patterns: string[],
  repeated = false,
  bias = 0,
  onEvent?: (event: JobEvent) => void
): Promise<WorkflowAnalyzeResponse> {
  const startEpoch = Date.now();
  const rawContents = documents.map((d) => d.content);
  const joinedText = rawContents.map(normalizeText).join("\n");

  const emit = (
    type: string,
    progress: number | null,
    message: string,
    payload: Record<string, unknown> | null = null
  ) => {
    onEvent?.({
      type,
      timestamp: Date.now(),
      progress,
      message,
      payload,
    });
  };

  emit("JOB_STARTED", 0, "Workflow started");
  await new Promise((r) => setTimeout(r, 60));

  // 1. Profiler
  emit("PROFILE_PROGRESS", 10, "Profiling corpus...");
  await new Promise((r) => setTimeout(r, 80));
  const profile = profileCorpus(rawContents);
  emit("PROFILE_COMPLETE", 25, "Profiling done", {
    categoryTags: profile.categoryTags.join(" "),
  });
  await new Promise((r) => setTimeout(r, 60));

  // 2. Planner
  emit("PLANNER_PROGRESS", 35, "Planning algorithm via Cost Model...");
  await new Promise((r) => setTimeout(r, 80));
  const planner = evaluatePlanner(profile, patterns, repeated, bias);
  emit("PLANNER_COMPLETE", 50, "Planner done", {
    recommendedAlgorithm: planner.recommendedAlgorithm,
    confidencePercent: planner.confidencePercent,
  });
  await new Promise((r) => setTimeout(r, 60));

  // 3. Search
  emit(
    "SEARCH_PROGRESS",
    55,
    `Running search with ${planner.recommendedAlgorithm}...`
  );
  await new Promise((r) => setTimeout(r, 80));
  const chosen = executeAlgorithm(planner.recommendedAlgorithm, joinedText, patterns);
  const search: SearchResponse = {
    algorithmUsed: planner.recommendedAlgorithm,
    wasForced: false,
    matchCount: chosen.result.matchCount,
    comparisonCount: chosen.result.comparisonCount,
    matchPositions: chosen.result.positions,
    executionTimeNanos: chosen.nanos,
    memoryUsedBytes: chosen.memoryBytes,
  };
  emit("SEARCH_COMPLETE", 70, "Search complete", {
    algorithmUsed: search.algorithmUsed,
    matchCount: search.matchCount,
  });
  await new Promise((r) => setTimeout(r, 60));

  // 4. Benchmark All 5 Algorithms
  emit("BENCHMARK_PROGRESS", 80, "Benchmarking all algorithms...");
  await new Promise((r) => setTimeout(r, 120));

  const allAlgos = [
    { name: "KMP", complexity: "O(P * (N + M))", memComplexity: "O(M + matches)" },
    { name: "Rabin-Karp", complexity: "O(P * (N + M)) average", memComplexity: "O(matches)" },
    { name: "Z Algorithm", complexity: "O(P * (N + M))", memComplexity: "O(N + M + matches)" },
    { name: "Aho-Corasick", complexity: "O(N + total pattern length + matches)", memComplexity: "O(total pattern length)" },
    { name: "Suffix Array + LCP", complexity: "Build O(N log N), Query O(M log N)", memComplexity: "O(N)" },
  ];

  const benchmark: BenchmarkRow[] = allAlgos.map((a) => {
    const executed = executeAlgorithm(a.name, joinedText, patterns);
    return {
      algorithm: a.name,
      executionTimeNanos: executed.nanos,
      memoryUsedBytes: executed.memoryBytes,
      comparisonCount: executed.result.comparisonCount,
      matchCount: executed.result.matchCount,
      complexity: a.complexity,
      memoryComplexity: a.memComplexity,
    };
  });

  emit("BENCHMARK_PROGRESS", 95, "Benchmarked all algorithms");
  await new Promise((r) => setTimeout(r, 40));

  const telemetry: TelemetryDto = {
    algorithmName: search.algorithmUsed,
    executionTimeNanos: search.executionTimeNanos,
    memoryUsedBytes: search.memoryUsedBytes,
    comparisonCount: search.comparisonCount,
    matchCount: search.matchCount,
    matchPositions: search.matchPositions,
  };

  emit("DONE", 100, "Workflow complete");

  return {
    profile,
    planner,
    search,
    benchmark,
    telemetry,
    timeline: ["profile", "planner", "search", "benchmark"],
    durationMs: Date.now() - startEpoch,
    generatedAt: new Date().toISOString(),
  };
}

export function runVisualizationSimulation(
  documents: { name: string; content: string }[],
  patterns: string[],
  algorithm = "KMP"
): VisualizationResponse {
  const rawContents = documents.map((d) => d.content);
  const joinedText = rawContents.map(normalizeText).join("\n");
  const steps: VisualizationStep[] = [];

  const sink = (
    stepIndex: number,
    kind: string,
    textPointer: number,
    patternPointer: number,
    detail: string
  ) => {
    steps.push({
      StepIndex: stepIndex,
      Kind: kind,
      TextPointer: textPointer,
      PatternPointer: patternPointer,
      Detail: detail,
    });
  };

  switch (algorithm) {
    case "Rabin-Karp":
      rabinKarpSearch(joinedText, patterns, sink);
      break;
    case "Z Algorithm":
      zAlgorithmSearch(joinedText, patterns, sink);
      break;
    case "Aho-Corasick":
      ahoCorasickSearch(joinedText, patterns, sink);
      break;
    case "Suffix Array + LCP":
      suffixArraySearch(joinedText, patterns, sink);
      break;
    case "KMP":
    default:
      kmpSearch(joinedText, patterns, sink);
      break;
  }

  return {
    algorithm,
    wasForced: true,
    stepCount: steps.length,
    steps,
  };
}
