
export interface SearchResult {
  algorithm: string;
  positions: number[];
  matchCount: number;
  comparisonCount: number;
  perPattern: number[];
}

export type StepSink = (
  stepIndex: number,
  kind: string,
  textPointer: number,
  patternPointer: number,
  detail: string
) => void;

const MAX_STEPS = 500;

// ---------- KMP (Knuth-Morris-Pratt) ----------
function buildLps(pattern: string, counter: { count: number }): number[] {
  const m = pattern.length;
  const lps = new Array<number>(m).fill(0);
  let len = 0;
  let i = 1;
  while (i < m) {
    counter.count++;
    if (pattern[i] === pattern[len]) {
      len++;
      lps[i] = len;
      i++;
    } else {
      if (len !== 0) {
        len = lps[len - 1];
      } else {
        lps[i] = 0;
        i++;
      }
    }
  }
  return lps;
}

export function kmpSearch(
  corpusText: string,
  patterns: string[],
  sink?: StepSink
): SearchResult {
  const counter = { count: 0 };
  const n = corpusText.length;
  const perPattern = new Array<number>(patterns.length).fill(0);
  const matchedPositions: number[] = [];
  let stepIdx = 0;

  for (let p = 0; p < patterns.length; p++) {
    const pattern = patterns[p];
    const m = pattern.length;
    if (m === 0 || n < m) continue;

    const lps = buildLps(pattern, counter);
    let i = 0;
    let j = 0;

    while (i < n) {
      counter.count++;
      if (sink && stepIdx < MAX_STEPS) {
        sink(stepIdx++, "compare", i, j, `text[${i}] vs pattern[${j}]`);
      }

      if (corpusText[i] === pattern[j]) {
        i++;
        j++;
        if (j === m) {
          const matchPos = i - m;
          matchedPositions.push(matchPos);
          perPattern[p]++;
          if (sink && stepIdx < MAX_STEPS) {
            sink(stepIdx++, "match", matchPos, j, `match at ${matchPos}`);
          }
          j = lps[j - 1];
        }
      } else {
        if (j !== 0) {
          if (sink && stepIdx < MAX_STEPS) {
            sink(stepIdx++, "fallback", i, j, `lps fallback ${j} -> ${lps[j - 1]}`);
          }
          j = lps[j - 1];
        } else {
          i++;
        }
      }
    }
  }

  matchedPositions.sort((a, b) => a - b);
  return {
    algorithm: "KMP",
    positions: matchedPositions,
    matchCount: matchedPositions.length,
    comparisonCount: counter.count,
    perPattern,
  };
}

// ---------- Rabin-Karp Rolling Hash ----------
const RK_BASE = 257;
const RK_MOD = 1000000007;

export function rabinKarpSearch(
  corpusText: string,
  patterns: string[],
  sink?: StepSink
): SearchResult {
  const counter = { count: 0 };
  const n = corpusText.length;
  const perPattern = new Array<number>(patterns.length).fill(0);
  const matchedPositions: number[] = [];
  let stepIdx = 0;

  for (let p = 0; p < patterns.length; p++) {
    const pattern = patterns[p];
    const m = pattern.length;
    if (m === 0 || n < m) continue;

    let highPower = 1;
    for (let i = 0; i < m - 1; i++) {
      highPower = (highPower * RK_BASE) % RK_MOD;
    }

    let patternHash = 0;
    let windowHash = 0;
    for (let i = 0; i < m; i++) {
      patternHash = (patternHash * RK_BASE + pattern.charCodeAt(i)) % RK_MOD;
      windowHash = (windowHash * RK_BASE + corpusText.charCodeAt(i)) % RK_MOD;
    }

    for (let i = 0; i <= n - m; i++) {
      counter.count++;
      if (sink && stepIdx < MAX_STEPS) {
        sink(
          stepIdx++,
          "hash-compare",
          i,
          0,
          `window@${i} hash=${windowHash} vs ${patternHash}`
        );
      }

      if (patternHash === windowHash) {
        let verified = true;
        for (let j = 0; j < m; j++) {
          counter.count++;
          if (corpusText[i + j] !== pattern[j]) {
            verified = false;
            break;
          }
        }
        if (verified) {
          matchedPositions.push(i);
          perPattern[p]++;
          if (sink && stepIdx < MAX_STEPS) {
            sink(stepIdx++, "match", i, m, `match at ${i}`);
          }
        }
      }

      if (i < n - m) {
        windowHash =
          (windowHash - ((corpusText.charCodeAt(i) * highPower) % RK_MOD) + RK_MOD) %
          RK_MOD;
        windowHash =
          (windowHash * RK_BASE + corpusText.charCodeAt(i + m)) % RK_MOD;
      }
    }
  }

  matchedPositions.sort((a, b) => a - b);
  return {
    algorithm: "Rabin-Karp",
    positions: matchedPositions,
    matchCount: matchedPositions.length,
    comparisonCount: counter.count,
    perPattern,
  };
}

// ---------- Z-Algorithm ----------
export function zAlgorithmSearch(
  corpusText: string,
  patterns: string[],
  sink?: StepSink
): SearchResult {
  const counter = { count: 0 };
  const n = corpusText.length;
  const perPattern = new Array<number>(patterns.length).fill(0);
  const matchedPositions: number[] = [];
  let stepIdx = 0;

  for (let p = 0; p < patterns.length; p++) {
    const pattern = patterns[p];
    const m = pattern.length;
    if (m === 0 || n < m) continue;

    const combined = pattern + "\u0001" + corpusText;
    const l = combined.length;
    const z = new Array<number>(l).fill(0);
    let left = 0;
    let right = 0;

    for (let i = 1; i < l; i++) {
      if (i > right) {
        left = i;
        right = i;
        while (right < l) {
          counter.count++;
          if (combined[right - left] === combined[right]) {
            if (sink && stepIdx < MAX_STEPS) {
              sink(stepIdx++, "z-expand", right, right - left, `expand Z box at ${i}`);
            }
            right++;
          } else {
            break;
          }
        }
        z[i] = right - left;
        right--;
      } else {
        const k = i - left;
        if (z[k] < right - i + 1) {
          z[i] = z[k];
        } else {
          left = i;
          while (right < l) {
            counter.count++;
            if (combined[right - left] === combined[right]) {
              if (sink && stepIdx < MAX_STEPS) {
                sink(
                  stepIdx++,
                  "z-expand",
                  right,
                  right - left,
                  `expand inside Z box at ${i}`
                );
              }
              right++;
            } else {
              break;
            }
          }
          z[i] = right - left;
          right--;
        }
      }
    }

    for (let i = m + 1; i < l; i++) {
      if (z[i] === m) {
        const pos = i - (m + 1);
        matchedPositions.push(pos);
        perPattern[p]++;
        if (sink && stepIdx < MAX_STEPS) {
          sink(stepIdx++, "match", pos, m, `match at ${pos}`);
        }
      }
    }
  }

  matchedPositions.sort((a, b) => a - b);
  return {
    algorithm: "Z Algorithm",
    positions: matchedPositions,
    matchCount: matchedPositions.length,
    comparisonCount: counter.count,
    perPattern,
  };
}

// ---------- Aho-Corasick ----------
interface AhoNode {
  children: Map<string, AhoNode>;
  fail: AhoNode | null;
  outputs: number[];
  isTerminal: boolean;
}

function createAhoNode(): AhoNode {
  return {
    children: new Map(),
    fail: null,
    outputs: [],
    isTerminal: false,
  };
}

export function ahoCorasickSearch(
  corpusText: string,
  patterns: string[],
  sink?: StepSink
): SearchResult {
  const counter = { count: 0 };
  const root = createAhoNode();
  root.fail = root;

  // 1. Build Trie
  for (let p = 0; p < patterns.length; p++) {
    const pat = patterns[p];
    let curr = root;
    for (let i = 0; i < pat.length; i++) {
      const ch = pat[i];
      if (!curr.children.has(ch)) {
        curr.children.set(ch, createAhoNode());
      }
      curr = curr.children.get(ch)!;
    }
    curr.isTerminal = true;
    curr.outputs.push(p);
  }

  // 2. Build Failure Links (BFS)
  const queue: AhoNode[] = [];
  root.children.forEach((child) => {
    child.fail = root;
    queue.push(child);
  });

  while (queue.length > 0) {
    const current = queue.shift()!;
    current.children.forEach((child, ch) => {
      let failNode = current.fail;
      while (failNode !== root && !failNode?.children.has(ch)) {
        failNode = failNode?.fail || root;
      }
      if (failNode?.children.has(ch) && failNode.children.get(ch) !== child) {
        child.fail = failNode.children.get(ch)!;
      } else {
        child.fail = root;
      }
      if (child.fail.outputs.length > 0) {
        child.outputs = [...child.outputs, ...child.fail.outputs];
      }
      queue.push(child);
    });
  }

  // 3. Scan Corpus in Single Pass
  const n = corpusText.length;
  const perPattern = new Array<number>(patterns.length).fill(0);
  const matchedPositions: number[] = [];
  let currNode = root;
  let stepIdx = 0;

  for (let i = 0; i < n; i++) {
    const ch = corpusText[i];
    counter.count++;

    while (currNode !== root && !currNode.children.has(ch)) {
      if (sink && stepIdx < MAX_STEPS) {
        sink(stepIdx++, "fail-transition", i, 0, `fail fallback on '${ch}'`);
      }
      currNode = currNode.fail || root;
    }

    if (currNode.children.has(ch)) {
      currNode = currNode.children.get(ch)!;
      if (sink && stepIdx < MAX_STEPS) {
        sink(stepIdx++, "trie-advance", i, 0, `trie step on '${ch}'`);
      }
    } else {
      currNode = root;
    }

    if (currNode.outputs.length > 0) {
      for (const p of currNode.outputs) {
        const matchPos = i - patterns[p].length + 1;
        matchedPositions.push(matchPos);
        perPattern[p]++;
        if (sink && stepIdx < MAX_STEPS) {
          sink(
            stepIdx++,
            "match",
            matchPos,
            patterns[p].length,
            `match pattern[${p}] at ${matchPos}`
          );
        }
      }
    }
  }

  matchedPositions.sort((a, b) => a - b);
  return {
    algorithm: "Aho-Corasick",
    positions: matchedPositions,
    matchCount: matchedPositions.length,
    comparisonCount: counter.count,
    perPattern,
  };
}

// ---------- Suffix Array + LCP ----------
export function suffixArraySearch(
  corpusText: string,
  patterns: string[],
  sink?: StepSink
): SearchResult {
  const counter = { count: 0 };
  const n = corpusText.length;
  const perPattern = new Array<number>(patterns.length).fill(0);
  const matchedPositions: number[] = [];
  let stepIdx = 0;

  // Build Suffix Array by sorting indices
  const sa = new Array<number>(n);
  for (let i = 0; i < n; i++) sa[i] = i;
  sa.sort((a, b) => {
    counter.count++;
    const sA = corpusText.slice(a);
    const sB = corpusText.slice(b);
    return sA < sB ? -1 : sA > sB ? 1 : 0;
  });

  for (let p = 0; p < patterns.length; p++) {
    const pattern = patterns[p];
    const m = pattern.length;
    if (m === 0 || n < m) continue;

    // Binary search for lower bound
    let low = 0;
    let high = n - 1;
    let first = -1;

    while (low <= high) {
      const mid = (low + high) >> 1;
      counter.count++;
      const suffix = corpusText.slice(sa[mid], sa[mid] + m);

      if (sink && stepIdx < MAX_STEPS) {
        sink(
          stepIdx++,
          "sa-binary-search",
          sa[mid],
          mid,
          `binary search mid=${mid} suffix='${suffix}'`
        );
      }

      if (suffix >= pattern) {
        if (suffix === pattern) first = mid;
        high = mid - 1;
      } else {
        low = mid + 1;
      }
    }

    if (first !== -1) {
      // Binary search for upper bound
      low = first;
      high = n - 1;
      let last = first;

      while (low <= high) {
        const mid = (low + high) >> 1;
        counter.count++;
        const suffix = corpusText.slice(sa[mid], sa[mid] + m);
        if (suffix <= pattern) {
          if (suffix === pattern) last = mid;
          low = mid + 1;
        } else {
          high = mid - 1;
        }
      }

      for (let idx = first; idx <= last; idx++) {
        matchedPositions.push(sa[idx]);
        perPattern[p]++;
        if (sink && stepIdx < MAX_STEPS) {
          sink(stepIdx++, "match", sa[idx], m, `match at ${sa[idx]}`);
        }
      }
    }
  }

  matchedPositions.sort((a, b) => a - b);
  return {
    algorithm: "Suffix Array + LCP",
    positions: matchedPositions,
    matchCount: matchedPositions.length,
    comparisonCount: counter.count,
    perPattern,
  };
}
