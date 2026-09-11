import { useEffect, useMemo, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Play, Pause, SkipForward, SkipBack, RotateCcw, Eye } from "lucide-react";
import { useLabStore } from "../../stores/labStore";
import type { VisualizationStep } from "../../api/types";

const KIND_COLORS: Record<string, string> = {
  INIT: "#5a5a7a",
  COMPARE: "#3b82f6",
  MATCH: "#3f9d63",
  MISMATCH: "#c8513a",
  SHIFT: "#5a5a7a",
  SUFFIX: "#1d4ed8",
  BRANCH: "#2d2d4e",
  DONE: "#3f9d63",
};
const KIND_FALLBACK = "#5a5a7a";

const PLACEHOLDER_TEXT = "abaababaabaababaabaababaabaaba";

export function VisualizationPlayer() {
  const workflow = useLabStore((s) => s.workflow);
  const capabilities = useLabStore((s) => s.capabilities);
  const vizAlgorithm = useLabStore((s) => s.vizAlgorithm);
  const setVizAlgorithm = useLabStore((s) => s.setVizAlgorithm);
  const visualization = useLabStore((s) => s.visualization);
  const runVisualize = useLabStore((s) => s.runVisualize);
  const sessionId = useLabStore((s) => s.sessionId);
  const documents = useLabStore((s) => s.documents);
  const patterns = useLabStore((s) => s.patterns);
  const loadCapabilities = useLabStore((s) => s.loadCapabilities);

  const [algorithmChoice, setAlgorithmChoice] = useState<string | null>(null);
  const [playing, setPlaying] = useState(false);
  const [cursor, setCursor] = useState(0);
  const [loadingViz, setLoadingViz] = useState(false);

  const working = visualization;

  // reset cursor + autoplay whenever a fresh step list arrives
  useEffect(() => {
    if (working) {
      setCursor(0);
      setPlaying(true);
    }
  }, [working]);

  // step forward while playing
  useEffect(() => {
    if (!working || !playing) return;
    if (cursor >= working.steps.length - 1) {
      setPlaying(false);
      return;
    }
    const t = setTimeout(() => setCursor((c) => c + 1), 220);
    return () => clearTimeout(t);
  }, [playing, cursor, working]);

  async function loadViz() {
    if (!sessionId) return;
    setLoadingViz(true);
    setPlaying(false);
    try {
      const chosen = algorithmChoice ?? vizAlgorithm ?? workflow?.planner.recommendedAlgorithm ?? null;
      if (chosen) setVizAlgorithm(chosen);
      await runVisualize();
    } finally {
      setLoadingViz(false);
    }
  }

  const step: VisualizationStep | undefined = working?.steps[cursor];
  const tPtr = step?.TextPointer ?? -1;
  const pPtr = step?.PatternPointer ?? -1;
  const kindColor = working && step ? KIND_COLORS[step.Kind.toUpperCase()] ?? KIND_FALLBACK : KIND_FALLBACK;

  const corpusText = useMemo(() => {
    const joined = documents.map((d) => d.content).join("\n");
    return joined || PLACEHOLDER_TEXT;
  }, [documents]);

  const patternText = useMemo(() => {
    return patterns[0] || "pattern";
  }, [patterns]);

  const textChars = useMemo(() => Array.from(corpusText), [corpusText]);

  // Window viewport slice for the text tape
  const VIEW_RANGE = 12;
  const activePtr = tPtr >= 0 ? tPtr : 0;
  const startIdx = Math.max(0, activePtr - VIEW_RANGE);
  const endIdx = Math.min(textChars.length, activePtr + VIEW_RANGE + 1);
  const visibleChars = textChars.slice(startIdx, endIdx);



  if (!workflow) {
    return (
      <section className="rounded-[24px] border border-white/[0.08] bg-white/[0.03] p-10 text-center text-sm text-white/50 backdrop-blur-2xl shadow-[0_8px_32px_rgba(0,0,0,0.37)]">
        <Eye size={22} className="mx-auto mb-2 text-white/30" />
        Run an analysis to enable step-by-step visualization.
      </section>
    );
  }

  return (
    <section className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]">
      <div className="mb-5 flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="mb-1 text-xs font-semibold tracking-widest text-amber uppercase">Step Replay</p>
          <h2 className="font-display text-2xl tracking-tight text-white font-medium">Execution Visualization</h2>
        </div>
        <div className="flex items-center gap-2">
          <select
            value={algorithmChoice ?? ""}
            onFocus={() => {
              if (capabilities.length === 0) {
                void loadCapabilities().catch(() => {});
              }
            }}
            onChange={(e) => setAlgorithmChoice(e.target.value || null)}
            className="rounded-lg border border-white/[0.08] bg-zinc-900/40 px-3 py-1.5 text-xs text-white outline-none focus:border-blue-500/50 transition-colors"
          >
            <option value="" className="bg-zinc-900 text-white">Auto · {workflow.planner.recommendedAlgorithm}</option>
            {capabilities.map((c) => (
              <option key={c.name} value={c.name} className="bg-zinc-900 text-white">
                {c.name}
              </option>
            ))}
          </select>
          <button
            onClick={loadViz}
            disabled={loadingViz}
            className="rounded-lg bg-white/90 backdrop-blur-md px-4 py-1.5 text-xs font-semibold text-zinc-950 transition-all hover:bg-white hover:scale-105 active:scale-95 disabled:opacity-60 shadow-md"
          >
            {loadingViz ? "Loading…" : working ? "Reload" : "Generate Steps"}
          </button>
        </div>
      </div>

      {!working && (
        <div className="rounded-xl border border-white/10 bg-black/20 p-6 text-center text-xs text-white/50 leading-relaxed">
          Pick an algorithm above and click <span className="font-semibold text-white">Generate Steps</span> to walk through the matching process comparison matrices.
        </div>
      )}

      {working && (
        <>
          {/* text pointer strip */}
          <div className="overflow-x-auto rounded-xl border border-white/[0.08] bg-white/[0.02] p-4 custom-scrollbar">
            <div className="flex font-mono text-[8px] text-white/20 mb-1 leading-none select-none">
              {visibleChars.map((_, index) => {
                const i = startIdx + index;
                return (
                  <span key={i} className="mx-0.5 inline-flex w-6 items-center justify-center">
                    {i}
                  </span>
                );
              })}
            </div>
            <div className="flex font-mono text-xs leading-none">
              {visibleChars.map((c, index) => {
                const i = startIdx + index;
                const active = i === tPtr;
                const matched = tPtr >= 0 &&
                  pPtr >= 0 &&
                  working.steps.slice(0, cursor + 1).some((s) => s.Kind.toUpperCase() === "MATCH" && s.TextPointer === i);
                return (
                  <span
                    key={i}
                    className={`mx-0.5 inline-flex h-7 w-6 items-center justify-center rounded-md font-mono text-xs font-semibold transition-all duration-200 ${
                      active ? "bg-gradient-to-r from-blue-500 to-sky-400 text-white shadow-md shadow-blue-500/25 scale-105" : matched ? "bg-emerald-500/25 border border-emerald-500/35 text-emerald-300 animate-pulse" : "bg-white/5 border border-white/5 text-white/60"
                    }`}
                  >
                    {c === " " ? "·" : c}
                  </span>
                );
              })}
            </div>
            <div className="mt-2.5 font-mono text-[9px] uppercase tracking-[0.16em] text-white/30">
              text pointer @ index {tPtr}
            </div>
          </div>

          {/* pattern pointer */}
          <div className="mt-4 flex flex-wrap items-center gap-4 bg-white/[0.02] p-4 rounded-xl border border-white/[0.06]">
            <span className="font-mono text-[9px] uppercase tracking-[0.16em] text-white/30 self-center">
              pattern
            </span>
            <div className="flex flex-col">
              <div className="flex font-mono text-[8px] text-white/20 mb-1 leading-none select-none">
                {Array.from(patternText).map((_, i) => (
                  <span key={i} className="mx-0.5 inline-flex w-6 items-center justify-center">
                    {i}
                  </span>
                ))}
              </div>
              <div className="flex font-mono text-xs">
                {Array.from(patternText).map((c, i) => (
                  <span
                    key={i}
                    className={`mx-0.5 inline-flex h-7 w-6 items-center justify-center rounded-md font-mono text-xs font-semibold transition-all duration-200 ${
                      i === pPtr ? "bg-gradient-to-r from-blue-500 to-sky-400 text-white shadow-md shadow-blue-500/20" : "bg-white/5 border border-white/5 text-white/60"
                    }`}
                  >
                    {c}
                  </span>
                ))}
              </div>
            </div>
            <span className="ml-auto font-mono text-[9px] text-white/35 self-center">
              pointer @ index {pPtr}
            </span>
          </div>

          {/* step detail card */}
          <AnimatePresence mode="wait">
            <motion.div
              key={cursor}
              initial={{ opacity: 0, y: 12 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, y: -12 }}
              transition={{ duration: 0.25 }}
              className="mt-4 rounded-xl border border-white/10 bg-black/30 p-4"
            >
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-2">
                  <span
                    className="inline-block h-2 w-2 rounded-full"
                    style={{ background: kindColor }}
                  />
                  <span
                    className="font-mono text-[10px] uppercase tracking-[0.16em] font-semibold"
                    style={{ color: kindColor }}
                  >
                    {step?.Kind ?? "—"}
                  </span>
                </div>
                <span className="font-mono text-[10px] text-white/40">
                  step {cursor + 1} / {working.stepCount}
                </span>
              </div>
              <p className="mt-2 font-mono text-xs leading-relaxed text-white/90">{step?.Detail ?? ""}</p>
            </motion.div>
          </AnimatePresence>

          {/* transport controls */}
          <div className="mt-4 flex flex-wrap items-center gap-2">
            <button
              onClick={() => setCursor(0)}
              disabled={cursor === 0}
              className="rounded-lg bg-white/10 p-2 text-white/80 hover:bg-white/20 transition-all disabled:opacity-30"
              aria-label="Restart"
            >
              <RotateCcw size={15} />
            </button>
            <button
              onClick={() => setCursor((c) => Math.max(0, c - 1))}
              disabled={cursor === 0}
              className="rounded-lg bg-white/10 p-2 text-white/80 hover:bg-white/20 transition-all disabled:opacity-30"
              aria-label="Previous"
            >
              <SkipBack size={15} />
            </button>
            <motion.button
              onClick={() => setPlaying((p) => !p)}
              disabled={cursor >= working.steps.length - 1 && !playing}
              whileTap={{ scale: 0.95 }}
              className="rounded-lg bg-white p-2.5 text-black hover:scale-105 transition-transform disabled:opacity-30"
              aria-label={playing ? "Pause" : "Play"}
            >
              {playing ? <Pause size={15} fill="currentColor" /> : <Play size={15} fill="currentColor" />}
            </motion.button>
            <button
              onClick={() => setCursor((c) => Math.min(working.steps.length - 1, c + 1))}
              disabled={cursor >= working.steps.length - 1}
              className="rounded-lg bg-white/10 p-2 text-white/80 hover:bg-white/20 transition-all disabled:opacity-30"
              aria-label="Next"
            >
              <SkipForward size={15} />
            </button>
            <input
              type="range"
              min={0}
              max={Math.max(0, working.steps.length - 1)}
              value={cursor}
              onChange={(e) => setCursor(Number(e.target.value))}
              className="ml-2 flex-1 accent-white"
            />
          </div>
        </>
      )}
    </section>
  );
}
