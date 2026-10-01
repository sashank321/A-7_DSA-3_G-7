import type { CorpusFingerprint } from "../api/types";

export function normalizeChar(c: string): string {
  const code = c.charCodeAt(0);
  if (code >= 65 && code <= 90) return String.fromCharCode(code + 32);
  if (code < 32 || code > 126) return " ";
  return c;
}

export function normalizeText(raw: string): string {
  return Array.from(raw).map(normalizeChar).join("");
}

export function splitWords(text: string): string[] {
  const words: string[] = [];
  const re = /[a-z0-9]+/g;
  let m: RegExpExecArray | null;
  while ((m = re.exec(text.toLowerCase())) !== null) {
    words.push(m[0]);
  }
  return words;
}

export function profileCorpus(
  documents: string[],
  corpusId = "sess-client"
): CorpusFingerprint {
  const normalizedDocs = documents.map(normalizeText);
  const joined = normalizedDocs.join("\n");
  const charCount = joined.length;
  const docCount = documents.length;

  const words = splitWords(joined);
  const wordCount = words.length;

  const freq = new Map<string, number>();
  let longestWord = 0;
  for (const w of words) {
    if (w.length > longestWord) longestWord = w.length;
    freq.set(w, (freq.get(w) || 0) + 1);
  }

  const vocabSize = freq.size;
  let repeatedWords = 0;
  freq.forEach((cnt) => {
    if (cnt > 1) repeatedWords++;
  });

  // Calculate Shannon entropy over character frequencies
  const charFreq = new Map<string, number>();
  for (let i = 0; i < charCount; i++) {
    const ch = joined[i];
    charFreq.set(ch, (charFreq.get(ch) || 0) + 1);
  }
  let entropy = 0;
  if (charCount > 0) {
    charFreq.forEach((cnt) => {
      const p = cnt / charCount;
      entropy -= p * Math.log2(p);
    });
  }

  // Sentence count approximation
  let sentences = 0;
  for (let i = 0; i < charCount; i++) {
    const ch = joined[i];
    if (ch === "." || ch === "!" || ch === "?") sentences++;
  }
  if (sentences === 0) sentences = 1;

  let longestDoc = 0;
  let totalDocLen = 0;
  for (const d of documents) {
    if (d.length > longestDoc) longestDoc = d.length;
    totalDocLen += d.length;
  }

  const avgWordLen = wordCount > 0 ? (words.reduce((sum, w) => sum + w.length, 0) / wordCount) : 0;
  const avgDocLen = docCount > 0 ? totalDocLen / docCount : 0;
  const avgSentLen = wordCount / sentences;
  const vocabRichness = wordCount > 0 ? vocabSize / wordCount : 0;
  const duplicateScore = vocabSize > 0 ? (repeatedWords / vocabSize) : 0;
  const patternDensity = charCount > 0 ? (wordCount / (charCount / 100)) : 0;

  // Category tags
  const tags: string[] = [];
  if (charCount > 10000) tags.push("LARGE_SCALE");
  else tags.push("SPARSE");

  if (vocabRichness > 0.7) tags.push("HIGH_ENTROPY");
  if (duplicateScore > 0.4) tags.push("REPETITIVE");
  if (docCount > 1) tags.push("MULTI_DOC");

  return {
    corpusId,
    documentCount: docCount,
    characterCount: charCount,
    wordCount,
    vocabularySize: vocabSize,
    repeatedWordCount: repeatedWords,
    longestWordLength: longestWord,
    longestDocumentLength: longestDoc,
    averageWordLength: Number(avgWordLen.toFixed(3)),
    averageDocumentLength: Number(avgDocLen.toFixed(3)),
    averageSentenceLength: Number(avgSentLen.toFixed(3)),
    vocabularyRichness: Number(vocabRichness.toFixed(3)),
    entropyEstimate: Number(entropy.toFixed(3)),
    duplicateScore: Number(duplicateScore.toFixed(3)),
    patternDensity: Number(patternDensity.toFixed(3)),
    repeatedPhraseRatio: Number((duplicateScore * 0.75).toFixed(3)),
    uniqueCharacterCount: charFreq.size,
    estimatedAlphabetSize: Math.max(charFreq.size, 64),
    corpusCategory: tags[0] || "BALANCED",
    categoryTags: tags,
  };
}


/** Bigram richness: unique bigrams / total bigrams */
export function bigramRichness(text: string): number {
  if (text.length < 2) return 0;
  const bigrams = new Set<string>();
  for (let i = 0; i < text.length - 1; i++) bigrams.add(text[i] + text[i + 1]);
  return bigrams.size / (text.length - 1);
}
