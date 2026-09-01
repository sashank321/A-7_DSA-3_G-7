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

export const useLabStore = create<LabState>((set, get) => ({
  documents: [],
  sessionId: null,
  characterCount: 0,
  fingerprint: null,

  patterns: [],
  repeated: false,
  optimizationBias: 0,

  jobId: null,
  runPhase: "idle",
  jobEvents: [],
  workflow: null,
  errorMessage: null,

  capabilities: [],
  searchResult: null,
  forcedAlgorithm: null,
  benchmarkRows: null,
  visualization: null,
  vizAlgorithm: null,
  citationGraph: null,

  addDocuments: (files) =>
    set((s) => ({
      documents: [...s.documents, ...files].slice(0, MAX_DOCS),
      // new corpus → invalidate any prior session-derived state
      sessionId: null,
      fingerprint: null,
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobEvents: [],
      runPhase: "idle",
      errorMessage: null,
      citationGraph: null,
    })),

  removeDocument: (name) =>
    set((s) => ({
      documents: s.documents.filter((d) => d.name !== name),
      sessionId: null,
      fingerprint: null,
      workflow: null,
      searchResult: null,
      benchmarkRows: null,
      visualization: null,
      jobEvents: [],
      runPhase: "idle",
      citationGraph: null,
    })),

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
      citationGraph: null,
    }),

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
    const bulk = await api.getCapabilities();
    set({ capabilities: bulk.items });
  },

  /**
   * Upload corpus (upload-json) → submit async workflow → open job WebSocket
   * → stream snapshots → set final workflow result on CLOSED.
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

    try {
      // 1. Upload raw documents (same corpus used on the console engine)
      const clipped = documents.map((d) => d.content.slice(0, MAX_DOC_CHARS));
      const upload = await api.uploadJson(clipped);
      set({ sessionId: upload.sessionId, characterCount: upload.characterCount });

      // 2. Submit async workflow job
      set({ runPhase: "analyzing" });
      const job = await api.analyzeAsync({
        sessionId: upload.sessionId,
        patterns,
        repeated,
        optimizationBias,
      });
      set({ jobId: job.jobId });

      // 3. Stream progress over WebSocket until CLOSED
      const unsubscribe = subscribeToJob(job.jobId, {
        onSnapshot: (events) => set({ jobEvents: events }),
        onClosed: (status, result, error) => {
          unsubscribe();
          if (status === "SUCCEEDED" && result) {
            set({
              workflow: result as WorkflowAnalyzeResponse,
              runPhase: "done",
            });
          } else {
            set({
              runPhase: "error",
              errorMessage: error ?? "Workflow failed without an error message.",
            });
          }
        },
        onError: () => {
          set({
            runPhase: "error",
            errorMessage: "WebSocket connection to the job failed. Is the backend on :8080?",
          });
        },
      });
    } catch (err) {
      set({ runPhase: "error", errorMessage: (err as Error).message });
    }
  },

  runSearchOnly: async () => {
    const s = get();
    if (!s.sessionId || s.patterns.length === 0) return;
    const res = await api.runSearch({
      sessionId: s.sessionId,
      patterns: s.patterns,
      repeated: s.repeated,
      forceAlgorithm: s.forcedAlgorithm,
    });
    set({ searchResult: res });
  },

  runBenchmarkOnly: async () => {
    const s = get();
    if (!s.sessionId || s.patterns.length === 0) return;
    const rows = await api.runBenchmark({
      sessionId: s.sessionId,
      patterns: s.patterns,
      repeated: s.repeated,
    });
    set({ benchmarkRows: rows });
  },

  runVisualize: async () => {
    const s = get();
    if (!s.sessionId || s.patterns.length === 0) return;
    const res = await api.runVisualize({
      sessionId: s.sessionId,
      patterns: s.patterns,
      repeated: s.repeated,
      algorithm: s.vizAlgorithm,
    });
    set({ visualization: res });
  },

  computeCitationGraph: () => {
    const docs = get().documents;
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
    
    set({ citationGraph: { nodes: Array.from(nodesMap.values()), links } });
  },

  reset: () =>
    set({
      documents: [],
      sessionId: null,
      characterCount: 0,
      fingerprint: null,
      patterns: [],
      repeated: false,
      jobId: null,
      runPhase: "idle",
      jobEvents: [],
      workflow: null,
      errorMessage: null,
      searchResult: null,
      forcedAlgorithm: null,
      benchmarkRows: null,
      visualization: null,
      vizAlgorithm: null,
      citationGraph: null,
    }),
}));
