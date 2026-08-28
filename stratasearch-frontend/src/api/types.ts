/**
 * StrataSearch backend API types — mirrors every controller DTO.
 *
 * CRITICAL serialization gotchas (no Jackson naming strategy on the server):
 *  - VisualizationStep keys are PascalCase:  StepIndex / Kind / TextPointer / PatternPointer / Detail
 *  - BenchmarkRun keys are PascalCase:       RanAtEpochMs / Rows
 *  - Everything else is camelCase as declared in the DTOs.
 */

// ---------- System ----------
export interface HealthResponse {
  status: string;
  core: string;
  planner: string;
  algorithms: number;
  activeSessions: number;
}

export interface MetaResponse {
  version: string;
  plannerVersion: string;
  build: string;
  algorithmCount: number;
  supportedFormats: string[];
  apiBase: string;
}

// ---------- Corpus ----------
export interface UploadCorpusResponse {
  sessionId: string;
  documentCount: number;
  characterCount: number;
}

export interface CorpusFingerprint {
  corpusId: string;
  documentCount: number;
  characterCount: number;
  wordCount: number;
  vocabularySize: number;
  repeatedWordCount: number;
  longestWordLength: number;
  longestDocumentLength: number;
  averageWordLength: number;
  averageDocumentLength: number;
  averageSentenceLength: number;
  vocabularyRichness: number;
  entropyEstimate: number;
  duplicateScore: number;
  patternDensity: number;
  repeatedPhraseRatio: number;
  uniqueCharacterCount: number;
  estimatedAlphabetSize: number;
  corpusCategory: string;
  categoryTags: string[];
}

// ---------- Algorithms ----------
export interface AlgorithmCapability {
  name: string;
  timeComplexity: string;
  memoryUsage: string;
  supportsMultiplePatterns: boolean;
  supportsRepeatedQueries: boolean;
  supportsIndexing: boolean;
  description: string;
  bestUseCase: string;
}

export interface AlgorithmCapabilitiesBulk {
  items: AlgorithmCapability[];
}

// ---------- Planner ----------
export interface PlannerRequest {
  sessionId: string;
  patterns: string[];
  repeated?: boolean | null;
  optimizationBias?: number;
}

export interface AlgorithmScore {
  algorithm: string;
  score: number;
}

export interface PlannerResponse {
  recommendedAlgorithm: string;
  confidencePercent: number;
  estimatedRuntime: string;
  estimatedMemory: string;
  scores: AlgorithmScore[];
}

export interface PlannerExplainResponse {
  recommendedAlgorithm: string;
  confidencePercent: number;
  estimatedRuntime: string;
  estimatedMemory: string;
  reason: string;
  advantages: string[];
  tradeOffs: string[];
  recommendedBecause: string[];
  avoidedBecause: string[];
  alternatives: string[];
  decisionSteps: string[];
  scores: AlgorithmScore[];
}

// ---------- Search ----------
export interface SearchRequest {
  sessionId: string;
  patterns: string[];
  forceAlgorithm?: string | null;
  repeated?: boolean | null;
  optimizationBias?: number;
}

export interface SearchResponse {
  algorithmUsed: string;
  wasForced: boolean;
  matchCount: number;
  comparisonCount: number;
  matchPositions: number[];
  executionTimeNanos: number;
  memoryUsedBytes: number;
}

// ---------- Benchmark ----------
export interface BenchmarkRow {
  algorithm: string;
  executionTimeNanos: number;
  memoryUsedBytes: number;
  comparisonCount: number;
  matchCount: number;
  complexity: string;
  memoryComplexity: string;
}

export interface BenchmarkLatestNotFound {
  sessionId: string;
  found: false;
}
export interface BenchmarkLatestFound {
  sessionId: string;
  found: true;
  ranAtEpochMs: number;
  rows: BenchmarkRow[];
}
export type BenchmarkLatest = BenchmarkLatestFound | BenchmarkLatestNotFound;

/** PASCALCASE — matches BenchmarkHistoryService.BenchmarkRun verbatim. */
export interface BenchmarkRun {
  RanAtEpochMs: number;
  Rows: BenchmarkRow[];
}

export interface BenchmarkHistory {
  sessionId: string;
  runCount: number;
  runs: BenchmarkRun[];
}

// ---------- Visualization ----------
export interface VisualizationRequest {
  sessionId: string;
  patterns: string[];
  algorithm?: string | null;
  repeated?: boolean | null;
}

/** PASCALCASE — matches stratasearch core VisualizationStep verbatim. */
export interface VisualizationStep {
  StepIndex: number;
  Kind: string;
  TextPointer: number;
  PatternPointer: number;
  Detail: string;
}

export interface VisualizationResponse {
  algorithm: string;
  wasForced: boolean;
  stepCount: number;
  steps: VisualizationStep[];
}

// ---------- Workflow / Jobs ----------
export interface TelemetryDto {
  algorithmName: string;
  executionTimeNanos: number;
  memoryUsedBytes: number;
  comparisonCount: number;
  matchCount: number;
  matchPositions: number[];
}

export interface WorkflowAnalyzeResponse {
  profile: CorpusFingerprint;
  planner: PlannerExplainResponse;
  search: SearchResponse;
  benchmark: BenchmarkRow[];
  telemetry: TelemetryDto;
  timeline: string[];
  durationMs: number;
  generatedAt: string;
}

export type JobStatus = "QUEUED" | "RUNNING" | "SUCCEEDED" | "FAILED";

export interface JobSubmissionResponse {
  jobId: string;
  status: "QUEUED";
  eventsUrl: string;
  resultUrl: string;
}

export interface JobEvent {
  type: string;
  timestamp: number;
  progress: number | null;
  message: string;
  payload: Record<string, unknown> | null;
}

export interface JobStatusResponse {
  jobId: string;
  status: JobStatus;
  errorMessage: string | null;
  result: WorkflowAnalyzeResponse | null;
  events: JobEvent[];
}

// ---------- Errors ----------
export interface ApiErrorBody {
  timestamp: number;
  status: number;
  error: string;
  message: string;
  path: string;
}

// ---------- WebSocket frame shapes (ws://.../ws/jobs/{jobId}) ----------
export interface WsSubscribedFrame {
  type: "SUBSCRIBED";
  jobId: string;
}
export interface WsClosedFrame {
  type: "CLOSED";
  status: "SUCCEEDED" | "FAILED";
  result: WorkflowAnalyzeResponse | null;
  error: string | null;
}
/** Any other frame is a raw JSON array: JobEvent[] (full snapshot every 200ms). */
export type WsFrame = WsSubscribedFrame | WsClosedFrame | JobEvent[];

// ---------- Job event payload conveniences ----------
export interface PlannerCompletePayload {
  recommendedAlgorithm: string;
  confidencePercent: number;
}
export interface ProfileCompletePayload {
  categoryTags: string[];
}
export interface SearchCompletePayload {
  algorithmUsed: string;
  matchCount: number;
}
