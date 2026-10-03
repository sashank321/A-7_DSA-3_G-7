import { create } from "zustand";
import * as api from "../api/endpoints";
import { subscribeToJob } from "../api/jobSocket";
import type {
  AlgorithmCapability,
  BenchmarkRow,
  CorpusFingerprint,
  JobEvent,
  SearchResponse,
  VisualizationResponse,
  WorkflowAnalyzeResponse,
} from "../api/types";
import {
  DEFAULT_CAPABILITIES,
  executeAlgorithm,
  runVisualizationSimulation,
  runWorkflowSimulation,
} from "../engine/simulation";
import { SAMPLE_DOCUMENTS, SAMPLE_PATTERNS } from "../engine/sampleData";

export interface LabFile {
  name: string;
  content: string;
}

type RunPhase = "idle" | "uploading" | "analyzing" | "done" | "error";

interface LabState {
  // corpus
  documents: LabFile[];
  sessionId: string | null;
  characterCount: number;
  fingerprint: CorpusFingerprint | null;

  // query
  patterns: string[];
  repeated: boolean;
  optimizationBias: number;

  // full-analysis job
  jobId: string | null;
  runPhase: RunPhase;
  jobEvents: JobEvent[];
  workflow: WorkflowAnalyzeResponse | null;
  errorMessage: string | null;

  // ad-hoc tooling
  capabilities: AlgorithmCapability[];
  searchResult: SearchResponse | null;
  forcedAlgorithm: string | null;
  benchmarkRows: BenchmarkRow[] | null;
  visualization: VisualizationResponse | null;
  vizAlgorithm: string | null;

  // citation graph
  citationGraph: { nodes: any[]; links: any[] } | null;

  // actions
  addDocuments: (files: LabFile[]) => void;
  removeDocument: (name: string) => void;
  clearCorpus: () => void;
  loadSampleCorpus: () => void;
  setPatterns: (raw: string) => void;
  setRepeated: (repeated: boolean) => void;
  setOptimizationBias: (bias: number) => void;
  setForcedAlgorithm: (name: string | null) => void;
  setVizAlgorithm: (name: string | null) => void;
  loadCapabilities: () => Promise<void>;
  analyze: () => Promise<void>;
  runSearchOnly: () => Promise<void>;
  runBenchmarkOnly: () => Promise<void>;
  runVisualize: () => Promise<void>;
  computeCitationGraph: () => void;
  reset: () => void;
}

const MAX_DOCS = 32;
const MAX_DOC_CHARS = 400_000;

function computeGraph(docs: LabFile[]) {
  const nodesMap = new Map();
  docs.forEach((d) => {
    nodesMap.set(d.name, {
      id: d.name,
      name: d.name,
      size: d.content.length,
      inDegree: 0,
      outDegree: 0,
    });
  });

  const links: any[] = [];
  const stripExt = (name: string) => name.replace(/\.[^/.]+$/, "");

  for (const source of docs) {
    for (const target of docs) {
      if (source.name === target.name) continue;
      const targetName = stripExt(target.name).toLowerCase();
      if (targetName.length < 3) continue;

      if (source.content.toLowerCase().includes(targetName)) {
        links.push({ source: source.name, target: target.name });
        nodesMap.get(source.name).outDegree++;
        nodesMap.get(target.name).inDegree++;
      }
    }
  }

  return { nodes: Array.from(nodesMap.values()), links };
}

const initialPatterns = SAMPLE_PATTERNS.split("\n").map((p) => p.trim());

export const useLabStore = create<LabState>((set, get) => ({
  documents: SAMPLE_DOCUMENTS,
  sessionId: null,
  characterCount: SAMPLE_DOCUMENTS.reduce((acc, d) => acc + d.content.length, 0),
  fingerprint: null,

  patterns: initialPatterns,
  repeated: false,
  optimizationBias: 0,

  jobId: null,
  runPhase: "idle",
  jobEvents: [],
  workflow: null,
  errorMessage: null,

  capabilities: DEFAULT_CAPABILITIES,
  searchResult: null,
  forcedAlgorithm: null,
  benchmarkRows: null,
  visualization: null,
  vizAlgorithm: null,
  citationGraph: computeGraph(SAMPLE_DOCUMENTS),

  addDocuments: (files) => {
    const nextDocs = [...get().documents, ...files].slice(0, MAX_DOCS);
    set({
      documents: nextDocs,
      sessionId: null,
      fingerprint: null,
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobEvents: [],
      runPhase: "idle",
      errorMessage: null,
      citationGraph: computeGraph(nextDocs),
    });
  },

  removeDocument: (name) => {
    const nextDocs = get().documents.filter((d) => d.name !== name);
    set({
      documents: nextDocs,
      sessionId: null,
      fingerprint: null,
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobEvents: [],
      runPhase: "idle",
      citationGraph: computeGraph(nextDocs),
    });
  },

  clearCorpus: () =>
    set({
      documents: [],
      sessionId: null,
      characterCount: 0,
      fingerprint: null,
      jobId: null,
      runPhase: "idle",
      jobEvents: [],
      workflow: null,
      errorMessage: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      citationGraph: { nodes: [], links: [] },
    }),

  loadSampleCorpus: () => {
    set({
      documents: SAMPLE_DOCUMENTS,
      characterCount: SAMPLE_DOCUMENTS.reduce((acc, d) => acc + d.content.length, 0),
      patterns: initialPatterns,
      sessionId: null,
      fingerprint: null,
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobEvents: [],
      runPhase: "idle",
      errorMessage: null,
      citationGraph: computeGraph(SAMPLE_DOCUMENTS),
    });
  },

  setPatterns: (raw) =>
    set({
      patterns: raw
        .split(/\r?\n|,/)
        .map((p) => p.trim())
        .filter((p) => p.length > 0),
    }),

  setRepeated: (repeated) => set({ repeated }),
  setOptimizationBias: (bias) => set({ optimizationBias: bias }),
  setForcedAlgorithm: (name) => set({ forcedAlgorithm: name }),
  setVizAlgorithm: (name) => set({ vizAlgorithm: name }),

  loadCapabilities: async () => {
    try {
      const bulk = await api.getCapabilities();
      if (bulk?.items && bulk.items.length > 0) {
        set({ capabilities: bulk.items });
        return;
      }
    } catch {
      // offline / fallback
    }
    set({ capabilities: DEFAULT_CAPABILITIES });
  },

  /**
   * Dual-mode analysis execution:
   * 1. Attempts remote backend invocation via upload + WebSocket streaming.
   * 2. If the backend is offline or on a cloud proxy returning HTML (Vercel static),
   *    seamlessly runs the real, non-mocked in-browser DSA engine with live progressive events!
   */
  analyze: async () => {
    const { documents, patterns, repeated, optimizationBias } = get();
    if (documents.length === 0) {
      set({ errorMessage: "Add at least one document before analyzing.", runPhase: "error" });
      return;
    }
    if (patterns.length === 0) {
      set({ errorMessage: "Add at least one pattern before analyzing.", runPhase: "error" });
      return;
    }

    set({
      runPhase: "uploading",
      errorMessage: null,
      jobEvents: [],
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobId: null,
    });

    let useClientEngine = false;
    let serverSessionId: string | null = null;

    try {
      const clipped = documents.map((d) => d.content.slice(0, MAX_DOC_CHARS));
      const upload = await api.uploadJson(clipped);
      if (!upload || !upload.sessionId) {
        throw new Error("Invalid session response from backend");
      }
      serverSessionId = upload.sessionId;
      set({ sessionId: upload.sessionId, characterCount: upload.characterCount });
    } catch (uploadErr) {
      console.info("Backend API unreachable or static hosting; engaging in-browser DSA engine:", uploadErr);
      useClientEngine = true;
    }

    if (useClientEngine || !serverSessionId) {
      set({ runPhase: "analyzing", jobId: "job-local" });
      try {
        const result = await runWorkflowSimulation(
          documents,
          patterns,
          repeated,
          optimizationBias,
          (event) => {
            set((s) => ({ jobEvents: [...s.jobEvents, event] }));
          }
        );
        set({
          workflow: result,
          runPhase: "done",
          fingerprint: result.profile,
          benchmarkRows: result.benchmark,
        });
      } catch (err) {
        set({ runPhase: "error", errorMessage: (err as Error).message });
      }
      return;
    }

    // Remote Server Mode
    set({ runPhase: "analyzing" });
    try {
      const job = await api.analyzeAsync({
        sessionId: serverSessionId,
        patterns,
        repeated,
        optimizationBias,
      });
      set({ jobId: job.jobId });

      let streamCompleted = false;
      const unsubscribe = subscribeToJob(job.jobId, {
        onSnapshot: (events) => set({ jobEvents: events }),
        onClosed: (status, result, error) => {
          streamCompleted = true;
          unsubscribe();
          if (status === "SUCCEEDED" && result) {
            const workflowRes = result as WorkflowAnalyzeResponse;
            set({
              workflow: workflowRes,
              runPhase: "done",
              fingerprint: workflowRes.profile,
              benchmarkRows: workflowRes.benchmark,
            });
          } else {
            set({
              runPhase: "error",
              errorMessage: error ?? "Workflow failed without an error message.",
            });
          }
        },
        onError: async () => {
          if (!streamCompleted) {
            console.warn("WebSocket disconnected; falling back to in-browser DSA engine...");
            const fallback = await runWorkflowSimulation(
              documents,
              patterns,
              repeated,
              optimizationBias,
              (event) => set((s) => ({ jobEvents: [...s.jobEvents, event] }))
            );
            set({
              workflow: fallback,
              runPhase: "done",
              fingerprint: fallback.profile,
              benchmarkRows: fallback.benchmark,
            });
          }
        },
      });
    } catch (err) {
      console.warn("Async submission failed, running in-browser fallback:", err);
      const fallback = await runWorkflowSimulation(
        documents,
        patterns,
        repeated,
        optimizationBias,
        (event) => set((s) => ({ jobEvents: [...s.jobEvents, event] }))
      );
      set({
        workflow: fallback,
        runPhase: "done",
        fingerprint: fallback.profile,
        benchmarkRows: fallback.benchmark,
      });
    }
  },

  runSearchOnly: async () => {
    const s = get();
    if (s.documents.length === 0 || s.patterns.length === 0) return;

    if (s.sessionId) {
      try {
        const res = await api.runSearch({
          sessionId: s.sessionId,
          patterns: s.patterns,
          repeated: s.repeated,
          forceAlgorithm: s.forcedAlgorithm,
        });
        set({ searchResult: res });
        return;
      } catch {
        // fallback
      }
    }

    const algo = s.forcedAlgorithm || s.workflow?.planner.recommendedAlgorithm || "KMP";
    const joined = s.documents.map((d) => d.content).join("\n");
    const executed = executeAlgorithm(algo, joined, s.patterns);
    set({
      searchResult: {
        algorithmUsed: algo,
        wasForced: !!s.forcedAlgorithm,
        matchCount: executed.result.matchCount,
        comparisonCount: executed.result.comparisonCount,
        matchPositions: executed.result.positions,
        executionTimeNanos: executed.nanos,
        memoryUsedBytes: executed.memoryBytes,
      },
    });
  },

  runBenchmarkOnly: async () => {
    const s = get();
    if (s.documents.length === 0 || s.patterns.length === 0) return;

    if (s.sessionId) {
      try {
        const rows = await api.runBenchmark({
          sessionId: s.sessionId,
          patterns: s.patterns,
          repeated: s.repeated,
        });
        set({ benchmarkRows: rows });
        return;
      } catch {
        // fallback
      }
    }

    const joined = s.documents.map((d) => d.content).join("\n");
    const allAlgos = [
      { name: "KMP", complexity: "O(P * (N + M))", memComplexity: "O(M + matches)" },
      { name: "Rabin-Karp", complexity: "O(P * (N + M)) average", memComplexity: "O(matches)" },
      { name: "Z Algorithm", complexity: "O(P * (N + M))", memComplexity: "O(N + M + matches)" },
      { name: "Aho-Corasick", complexity: "O(N + total pattern length + matches)", memComplexity: "O(total pattern length)" },
      { name: "Suffix Array + LCP", complexity: "Build O(N log N), Query O(M log N)", memComplexity: "O(N)" },
    ];
    const rows: BenchmarkRow[] = allAlgos.map((a) => {
      const executed = executeAlgorithm(a.name, joined, s.patterns);
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
    set({ benchmarkRows: rows });
  },

  runVisualize: async () => {
    const s = get();
    if (s.documents.length === 0 || s.patterns.length === 0) return;

    if (s.sessionId) {
      try {
        const res = await api.runVisualize({
          sessionId: s.sessionId,
          patterns: s.patterns,
          repeated: s.repeated,
          algorithm: s.vizAlgorithm,
        });
        set({ visualization: res });
        return;
      } catch {
        // fallback
      }
    }

    const algo = s.vizAlgorithm || s.workflow?.planner.recommendedAlgorithm || "KMP";
    const res = runVisualizationSimulation(s.documents, s.patterns, algo);
    set({ visualization: res });
  },

  computeCitationGraph: () => {
    set({ citationGraph: computeGraph(get().documents) });
  },

  reset: () => {
    get().loadSampleCorpus();
  },
}));
