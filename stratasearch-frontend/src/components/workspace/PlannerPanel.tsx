import { useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { ChevronDown, Award, GitBranch, CheckCircle2, XCircle } from "lucide-react";
import type { PlannerExplainResponse } from "../../api/types";

const BEST = "linear-gradient(135deg, var(--color-amber), var(--color-amber-2))";

function ScoreBar({ algo, score, recommended }: { algo: string; score: number; recommended: boolean }) {
  return (
    <div className="flex items-center gap-3">
      <span
        className={`min-w-[130px] text-xs ${recommended ? "font-semibold text-amber" : "text-white/60"}`}
      >
        {algo}
        {recommended && <Award size={12} className="ml-1 inline -translate-y-0.5 text-amber" />}
      </span>
      <div className="relative h-2 flex-1 rounded-full bg-white/[0.03] border border-white/[0.06] overflow-hidden">
        <motion.div
          initial={{ width: 0 }}
          animate={{ width: `${score}%` }}
          transition={{ duration: 0.7, ease: [0.22, 1, 0.36, 1] }}
          style={recommended ? { background: BEST } : undefined}
          className={`h-full ${recommended ? "" : "bg-white/20"}`}
        />
      </div>
      <span className="w-12 text-right font-mono text-xs text-white/90">{score.toFixed(1)}</span>
    </div>
  );
}

function ListBlock({
  icon,
  title,
  items,
  tone,
}: {
  icon: React.ReactNode;
  title: string;
  items: string[];
  tone: "good" | "bad" | "neutral";
}) {
  if (!items?.length) return null;
  const toneCls =
    tone === "good"
      ? "border-white/25 bg-white/10 text-white"
      : tone === "bad"
        ? "border-white/5 bg-white/5 text-white/50"
        : "border-white/10 bg-white/5 text-white/80";
  return (
    <div className={`rounded-xl border px-4 py-3 ${toneCls}`}>
      <p className="mb-2 flex items-center gap-2 font-mono text-[9px] uppercase tracking-[0.18em] opacity-80">
        {icon}
        {title}
      </p>
      <ul className="space-y-1">
        {items.map((t, i) => (
          <li key={i} className="text-xs leading-relaxed text-white/90">{t}</li>
        ))}
      </ul>
    </div>
  );
}

export function PlannerPanel({ planner }: { planner: PlannerExplainResponse }) {
  const [openSteps, setOpenSteps] = useState(false);
  const top = [...planner.scores].sort((a, b) => b.score - a.score);

  return (
    <motion.section
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]"
    >
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="mb-1 text-xs font-semibold tracking-widest text-amber uppercase">Stage 02 · Plan</p>
          <h2 className="font-display text-2xl tracking-tight text-white font-medium">Why this algorithm?</h2>
          <p className="mt-1 text-xs text-white/50">{planner.reason}</p>
        </div>
        <div className="relative flex items-center justify-center h-16 w-16 shrink-0">
          <svg className="w-full h-full transform -rotate-90">
            <circle
              cx="32"
              cy="32"
              r="26"
              className="stroke-white/[0.05]"
              strokeWidth="3.5"
              fill="transparent"
            />
            <motion.circle
              cx="32"
              cy="32"
              r="26"
              className="stroke-amber"
              strokeWidth="3.5"
              fill="transparent"
              strokeDasharray={2 * Math.PI * 26}
              initial={{ strokeDashoffset: 2 * Math.PI * 26 }}
              animate={{ strokeDashoffset: 2 * Math.PI * 26 - (planner.confidencePercent / 100) * (2 * Math.PI * 26) }}
              transition={{ duration: 0.8, ease: "easeOut" }}
              strokeLinecap="round"
            />
          </svg>
          <div className="absolute flex flex-col items-center justify-center">
            <span className="text-xs font-semibold font-mono text-white leading-none">
              {planner.confidencePercent.toFixed(0)}%
            </span>
            <span className="text-[6.5px] font-mono uppercase tracking-wider text-white/35 mt-0.5">Conf.</span>
          </div>
        </div>
      </div>

      <div className="mt-5 flex flex-wrap items-center gap-4 text-xs text-white/80">
        <span className="rounded-lg bg-white/90 backdrop-blur-md px-3.5 py-1.5 text-xs font-bold text-zinc-950 hover:bg-white shadow-md">
          → {planner.recommendedAlgorithm}
        </span>
        <span className="font-mono text-xs">
          <span className="text-white/40">time</span> {planner.estimatedRuntime}
        </span>
        <span className="font-mono text-xs">
          <span className="text-white/40">memory</span> {planner.estimatedMemory}
        </span>
      </div>

      {/* score table */}
      <div className="mt-5 space-y-2.5 rounded-xl bg-black/30 p-4 border border-white/5">
        {top.map((s) => (
          <ScoreBar
            key={s.algorithm}
            algo={s.algorithm}
            score={s.score}
            recommended={s.algorithm === planner.recommendedAlgorithm}
          />
        ))}
      </div>

      {/* advantages / trade-offs / because-clauses */}
      <div className="mt-5 grid gap-3 md:grid-cols-2">
        <ListBlock
          icon={<CheckCircle2 size={12} />}
          title="Advantages"
          items={planner.advantages}
          tone="good"
        />
        <ListBlock
          icon={<XCircle size={12} />}
          title="Trade-offs"
          items={planner.tradeOffs}
          tone="bad"
        />
        <ListBlock
          icon={<CheckCircle2 size={12} />}
          title="Recommended because"
          items={planner.recommendedBecause}
          tone="good"
        />
        <ListBlock
          icon={<XCircle size={12} />}
          title="Avoided because"
          items={planner.avoidedBecause}
          tone="bad"
        />
      </div>

      {planner.alternatives?.length > 0 && (
        <div className="mt-4 rounded-xl border border-white/10 bg-white/5 p-4">
          <p className="mb-2 font-mono text-[9px] uppercase tracking-[0.18em] text-amber">
            Next-best alternatives
          </p>
          <div className="flex flex-wrap gap-2">
            {planner.alternatives.map((a) => (
              <span
                key={a}
                className="rounded-lg border border-white/10 px-3 py-1 text-xs text-white/80"
              >
                {a}
              </span>
            ))}
          </div>
        </div>
      )}

      {/* decision tree steps */}
      {planner.decisionSteps?.length > 0 && (
        <div className="mt-3 rounded-xl border border-white/10 bg-white/5 overflow-hidden">
          <button
            onClick={() => setOpenSteps((v) => !v)}
            className="flex w-full items-center justify-between px-4 py-3 text-left transition-colors hover:bg-white/5"
          >
            <span className="flex items-center gap-2 font-mono text-[10px] uppercase tracking-[0.18em] text-amber">
              <GitBranch size={12} />
              Decision Tree · {planner.decisionSteps.length} steps
            </span>
            <motion.span animate={{ rotate: openSteps ? 180 : 0 }} transition={{ duration: 0.2 }}>
              <ChevronDown size={15} />
            </motion.span>
          </button>
          <AnimatePresence initial={false}>
            {openSteps && (
              <motion.ol
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
                className="border-t border-white/10 bg-black/10 px-4 pt-2 pb-3"
              >
                {planner.decisionSteps.map((s, i) => (
                  <li key={i} className="flex gap-3 py-1.5 text-xs text-white/85">
                    <span className="font-mono text-[10px] text-amber">{String(i + 1).padStart(2, "0")}</span>
                    <span className="leading-relaxed">{s}</span>
                  </li>
                ))}
              </motion.ol>
            )}
          </AnimatePresence>
        </div>
      )}
    </motion.section>
  );
}
