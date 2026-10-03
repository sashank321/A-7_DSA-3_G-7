import type {
  AlgorithmScore,
  CorpusFingerprint,
  PlannerExplainResponse,
} from "../api/types";

const W_SIZE = 0.35;
const W_PATTERN = 0.25;
const W_REPEAT = 0.2;
const W_ENTROPY = 0.2;
const W_QUERY = 0.1;

function normalizeSize(chars: number): number {
  if (chars <= 0) return 0;
  const v = chars / 100000;
  return v > 1 ? 1 : v;
}

function normalizeLen(len: number): number {
  const v = len / 100;
  return v > 1 ? 1 : v;
}

export function evaluatePlanner(
  fp: CorpusFingerprint,
  patterns: string[],
  repeated = false,
  bias = 0
): PlannerExplainResponse {
  const patternCount = patterns.length;
  const avgPatternLen =
    patternCount > 0
      ? patterns.reduce((acc, p) => acc + p.length, 0) / patternCount
      : 0;

  // 1. KMP Score
  let scoreKmp = 0.5;
  scoreKmp += W_PATTERN * (patternCount > 1 ? 0.6 : 0.0);
  scoreKmp += W_REPEAT * (repeated ? 0.5 : 0.0);
  scoreKmp -= W_SIZE * normalizeSize(fp.characterCount) * 0.3;
  scoreKmp -= W_ENTROPY * (fp.entropyEstimate / 4.0) * 0.1;
  scoreKmp += W_QUERY * normalizeLen(avgPatternLen) * 0.05;
  scoreKmp += bias * 0.1;

  // 2. Rabin-Karp Score
  let scoreRk = 0.55;
  scoreRk += W_PATTERN * (patternCount > 1 ? 0.7 : 0.0);
  scoreRk += W_REPEAT * (repeated ? 0.5 : 0.0);
  scoreRk += W_SIZE * normalizeSize(fp.characterCount) * 0.2;
  scoreRk -= W_PATTERN * normalizeLen(avgPatternLen) * 0.1;
  scoreRk += W_QUERY * normalizeLen(avgPatternLen) * 0.05;
  // O(1) memory footprint - zero bias penalty

  // 3. Z Algorithm Score
  let scoreZ = 0.55;
  scoreZ += W_PATTERN * (patternCount > 1 ? 0.65 : 0.0);
  scoreZ += W_REPEAT * (repeated ? 0.5 : 0.0);
  scoreZ += W_SIZE * normalizeSize(fp.characterCount) * 0.25;
  scoreZ -= W_PATTERN * (avgPatternLen <= 10 ? 0.15 : 0.0);
  scoreZ += W_QUERY * normalizeLen(avgPatternLen) * 0.05;
  scoreZ += bias * 0.9;

  // 4. Aho-Corasick Score
  let scoreAho = 0.6;
  scoreAho -= W_PATTERN * (patternCount >= 2 ? 0.75 : -0.2);
  scoreAho += W_REPEAT * (repeated ? 0.3 : 0.0);
  scoreAho += W_SIZE * normalizeSize(fp.characterCount) * 0.15;
  scoreAho -= W_ENTROPY * (fp.entropyEstimate / 4.0) * 0.1;
  scoreAho += bias * 0.2;

  // 5. Suffix Array Score
  let scoreSa = 0.8;
  scoreSa -= W_REPEAT * (repeated ? 0.8 : -0.3);
  scoreSa += W_SIZE * (fp.characterCount > 100000 ? 0.4 : -0.1);
  scoreSa -= W_PATTERN * (patternCount > 5 ? 0.3 : 0.0);
  scoreSa += bias * 0.4;

  const scores: AlgorithmScore[] = [
    { algorithm: "Aho-Corasick", score: Number(scoreAho.toFixed(4)) },
    { algorithm: "KMP", score: Number(scoreKmp.toFixed(4)) },
    { algorithm: "Z Algorithm", score: Number(scoreZ.toFixed(4)) },
    { algorithm: "Rabin-Karp", score: Number(scoreRk.toFixed(4)) },
    { algorithm: "Suffix Array + LCP", score: Number(scoreSa.toFixed(4)) },
  ].sort((a, b) => a.score - b.score);

  const best = scores[0];
  const second = scores[1];
  const margin = Math.max(0.01, second.score - best.score);
  const confidence = Math.min(99, Math.round(50 + margin * 120));

  const advantages: string[] = [];
  const tradeOffs: string[] = [];
  const recommendedBecause: string[] = [];
  const avoidedBecause: string[] = [];
  const decisionSteps: string[] = [
    `Input parameters evaluated: Patterns=${patternCount}, CorpusChars=${fp.characterCount}, VocabularyRichness=${fp.vocabularyRichness}, Repeated=${repeated}`,
  ];

  if (best.algorithm === "Aho-Corasick") {
    decisionSteps.push(
      "Multiple patterns detected -> Trie automaton compiles multi-pattern search into single pass O(N)."
    );
    advantages.push("Single pass scans all patterns simultaneously.");
    advantages.push("Failure links guarantee worst-case linear traversal.");
    tradeOffs.push("Higher upfront memory required for Trie node structure.");
    recommendedBecause.push(`Multi-pattern query workload (${patternCount} patterns).`);
  } else if (best.algorithm === "Suffix Array + LCP") {
    decisionSteps.push(
      "Repeated queries on persistent corpus -> prebuilt suffix index amortizes subsequent search to O(M log N)."
    );
    advantages.push("Sub-linear O(M log N) query speed via binary search.");
    tradeOffs.push("O(N log N) index construction cost.");
    recommendedBecause.push("Corpus reuse / repeated search workload.");
  } else if (best.algorithm === "KMP") {
    decisionSteps.push("Single pattern workload -> optimal linear worst-case LPS preprocessing.");
    advantages.push("Zero memory allocation per match; strictly deterministic.");
    tradeOffs.push("Linear scan must rerun for each distinct pattern.");
    recommendedBecause.push("Single pattern, deterministic search guarantee.");
  } else if (best.algorithm === "Rabin-Karp") {
    decisionSteps.push("Moderate corpus size -> polynomial rolling hash avoids state table memory.");
    advantages.push("O(1) memory overhead; rolling hash window.");
    tradeOffs.push("Hash collisions require character-by-character verification.");
    recommendedBecause.push("Minimal memory footprint requirement.");
  } else {
    decisionSteps.push("Short pattern query -> Z-box prefix expansions fit CPU cache nicely.");
    advantages.push("No complex state machine; fast consecutive cache-friendly scans.");
    tradeOffs.push("Requires concatenated string allocation (Pattern + Sentinel + Text).");
    recommendedBecause.push("Short pattern matched on local corpus block.");
  }

  for (let i = 1; i < scores.length; i++) {
    const diff = (scores[i].score - best.score).toFixed(4);
    avoidedBecause.push(
      `${scores[i].algorithm} scored +${diff} higher (less favorable for current workload characteristics).`
    );
  }

  decisionSteps.push(`Lowest cost model score = ${best.score} for ${best.algorithm}.`);
  decisionSteps.push(`Recommendation locked -> ${best.algorithm} (${confidence}% confidence).`);

  const runtimeMap: Record<string, string> = {
    KMP: "O(P * (N + M))",
    "Rabin-Karp": "O(P * (N + M)) average",
    "Z Algorithm": "O(P * (N + M))",
    "Aho-Corasick": "O(N + total pattern length + matches)",
    "Suffix Array + LCP": "Build O(N log N), Query O(M log N)",
  };

  const memoryMap: Record<string, string> = {
    KMP: "O(M + matches)",
    "Rabin-Karp": "O(matches)",
    "Z Algorithm": "O(N + M + matches)",
    "Aho-Corasick": "O(total pattern length)",
    "Suffix Array + LCP": "O(N)",
  };

  return {
    recommendedAlgorithm: best.algorithm,
    confidencePercent: confidence,
    estimatedRuntime: runtimeMap[best.algorithm] || "O(N)",
    estimatedMemory: memoryMap[best.algorithm] || "O(M)",
    reason: `Selected ${best.algorithm} via dynamic cost model based on corpus size and pattern characteristics.`,
    advantages,
    tradeOffs,
    recommendedBecause,
    avoidedBecause,
    alternatives: scores.slice(1).map((s) => s.algorithm),
    decisionSteps,
    scores,
  };
}
