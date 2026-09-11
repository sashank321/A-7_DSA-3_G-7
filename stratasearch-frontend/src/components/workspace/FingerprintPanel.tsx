import { motion } from "framer-motion";
import type { CorpusFingerprint } from "../../api/types";
import { formatInt } from "../../lib/format";

const METRIC_GROUPS: { title: string; rows: { key: keyof CorpusFingerprint; label: string; kind?: "num" | "int" | "tag" }[] }[] = [
  {
    title: "Scale",
    rows: [
      { key: "documentCount", label: "Documents", kind: "int" },
      { key: "characterCount", label: "Characters", kind: "int" },
      { key: "wordCount", label: "Words", kind: "int" },
      { key: "vocabularySize", label: "Vocabulary", kind: "int" },
    ],
  },
  {
    title: "Variety",
    rows: [
      { key: "vocabularyRichness", label: "Richness" },
      { key: "entropyEstimate", label: "Entropy" },
      { key: "averageWordLength", label: "Avg word len" },
      { key: "averageSentenceLength", label: "Avg sentence len" },
    ],
  },
  {
    title: "Structure",
    rows: [
      { key: "patternDensity", label: "Pattern density" },
      { key: "repeatedPhraseRatio", label: "Repeated-phrase ratio" },
      { key: "duplicateScore", label: "Duplicate score" },
      { key: "repeatedWordCount", label: "Repeated words", kind: "int" },
    ],
  },
  {
    title: "Alphabet",
    rows: [
      { key: "estimatedAlphabetSize", label: "Est. alphabet size", kind: "int" },
      { key: "uniqueCharacterCount", label: "Unique chars", kind: "int" },
      { key: "longestWordLength", label: "Longest word", kind: "int" },
      { key: "longestDocumentLength", label: "Longest doc", kind: "int" },
    ],
  },
];

function fmt(fp: CorpusFingerprint, row: { key: keyof CorpusFingerprint; kind?: "num" | "int" | "tag" }): string {
  const v = fp[row.key];
  if (row.kind === "tag") return String(v);
  if (row.kind === "int") return formatInt(Number(v));
  if (typeof v === "number") return v.toFixed(3);
  return String(v);
}

export function FingerprintPanel({ fingerprint }: { fingerprint: CorpusFingerprint }) {
  return (
    <motion.section
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]"
    >
      <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="mb-1 text-xs font-semibold tracking-widest text-amber uppercase">Stage 01 · Profile</p>
          <h2 className="font-display text-2xl tracking-tight text-white font-medium">Corpus Fingerprint</h2>
        </div>
        <div className="text-right">
          <span className="rounded-md bg-white/10 px-3 py-1 text-xs font-semibold text-white/95">
            {fingerprint.corpusCategory}
          </span>
        </div>
      </div>

      <div className="mt-3 flex flex-wrap gap-1.5">
        {fingerprint.categoryTags.map((t) => (
          <span
            key={t}
            className="rounded-md border border-amber/35 bg-amber/10 px-2 py-0.5 font-mono text-[9px] uppercase tracking-[0.14em] text-amber"
          >
            {t}
          </span>
        ))}
      </div>

      <div className="mt-6 grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
        {METRIC_GROUPS.map((g) => (
          <div key={g.title} className="rounded-2xl border border-white/[0.06] bg-white/[0.02] p-4.5 hover:bg-white/[0.04] transition-all">
            <h3 className="mb-3 font-mono text-[10px] uppercase tracking-[0.2em] text-white/40 border-b border-white/10 pb-1.5">{g.title}</h3>
            <dl className="space-y-2">
              {g.rows.map((row) => (
                <div key={row.key as string} className="flex items-baseline justify-between gap-2">
                  <dt className="text-xs text-white/50">{row.label}</dt>
                  <dd className="font-mono text-xs font-semibold text-white">{fmt(fingerprint, row)}</dd>
                </div>
              ))}
            </dl>
          </div>
        ))}
      </div>
    </motion.section>
  );
}
