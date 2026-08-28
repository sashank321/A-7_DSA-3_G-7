import { http } from "./http";
import type {
  AlgorithmCapabilitiesBulk,
  AlgorithmCapability,
  BenchmarkHistory,
  BenchmarkLatest,
  BenchmarkRow,
  CorpusFingerprint,
  HealthResponse,
  JobEvent,
  JobStatusResponse,
  JobSubmissionResponse,
  MetaResponse,
  PlannerExplainResponse,
  PlannerRequest,
  PlannerResponse,
  SearchRequest,
  SearchResponse,
  UploadCorpusResponse,
  VisualizationRequest,
  VisualizationResponse,
  WorkflowAnalyzeResponse,
} from "./types";

// ---------- System ----------
export const getHealth = () => http.get("health").json<HealthResponse>();
export const getMeta = () => http.get("meta").json<MetaResponse>();

// ---------- Corpus ----------
export function uploadFiles(files: File[]): Promise<UploadCorpusResponse> {
  const form = new FormData();
  files.forEach((f) => form.append("files", f));
  return http.post("corpus/upload", { body: form }).json<UploadCorpusResponse>();
}

export function uploadJson(documents: string[]): Promise<UploadCorpusResponse> {
  return http.post("corpus/upload-json", { json: { documents } }).json<UploadCorpusResponse>();
}

export const getFingerprint = (sessionId: string) =>
  http.get(`corpus/${sessionId}`).json<CorpusFingerprint>();

// ---------- Algorithms ----------
export const getAlgorithmNames = () => http.get("algorithms").json<string[]>();
export const getCapabilities = () =>
  http.get("algorithms/capabilities").json<AlgorithmCapabilitiesBulk>();
export const getCapability = (name: string) =>
  http.get(`algorithms/${encodeURIComponent(name)}`).json<AlgorithmCapability>();

// ---------- Planner ----------
export const planRecommend = (req: PlannerRequest) =>
  http.post("planner/recommend", { json: req }).json<PlannerResponse>();
export const planExplain = (req: PlannerRequest) =>
  http.post("planner/explain", { json: req }).json<PlannerExplainResponse>();

// ---------- Search ----------
export const runSearch = (req: SearchRequest) =>
  http.post("search", { json: req }).json<SearchResponse>();

// ---------- Benchmark ----------
export const runBenchmark = (req: PlannerRequest) =>
  http.post("benchmark", { json: req }).json<BenchmarkRow[]>();
export const getBenchmarkLatest = (sessionId: string) =>
  http.get("benchmark/latest", { searchParams: { sessionId } }).json<BenchmarkLatest>();
export const getBenchmarkHistory = (sessionId: string) =>
  http.get("benchmark/history", { searchParams: { sessionId } }).json<BenchmarkHistory>();

// ---------- Visualization ----------
export const runVisualize = (req: VisualizationRequest) =>
  http.post("visualize", { json: req }).json<VisualizationResponse>();

// ---------- Workflow ----------
export const analyzeSync = (req: PlannerRequest) =>
  http.post("workflow/analyze", { json: req }).json<WorkflowAnalyzeResponse>();
export const analyzeAsync = (req: PlannerRequest) =>
  http.post("workflow/analyze-async", { json: req }).json<JobSubmissionResponse>();

// ---------- Jobs ----------
export const getJobStatus = (jobId: string) => http.get(`jobs/${jobId}`).json<JobStatusResponse>();
export const getJobEvents = (jobId: string) =>
  http.get(`jobs/${jobId}/events`).json<JobEvent[]>();
