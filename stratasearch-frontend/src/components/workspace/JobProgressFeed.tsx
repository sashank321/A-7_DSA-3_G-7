import { motion, AnimatePresence } from "framer-motion";
import { useLabStore } from "../../stores/labStore";
import type { JobEvent } from "../../api/types";

const STAGE_LABEL: Record<string, string> = {
  JOB_STARTED: "Job dispatched",
  PROFILE_PROGRESS: "Profiling corpus",
  PROFILE_COMPLETE: "Fingerprint ready",
  PLANNER_PROGRESS: "Scoring algorithms",
  PLANNER_COMPLETE: "Planner picked",
  SEARCH_PROGRESS: "Running search",
  BENCHMARK_PROGRESS: "Benchmarking all 5",
  SEARCH_COMPLETE: "Search complete",
  DONE: "Workflow complete",
  FAILED: "Workflow failed",
};

function typeDotClass(type: string): string {
  if (type.endsWith("COMPLETE") || type === "DONE") return "bg-white/80";
  if (type === "FAILED") return "bg-white/30 border border-white/20";
  if (type.endsWith("PROGRESS")) return "bg-amber";
  return "bg-white/10 border border-white/5";
}

export default function JobProgressFeed() {
  const jobEvents = useLabStore((s) => s.jobEvents);
  const runPhase = useLabStore((s) => s.runPhase);
  const errorMessage = useLabStore((s) => s.errorMessage);

  if (jobEvents.length === 0 && runPhase !== "error") return null;

  const last = jobEvents[jobEvents.length - 1];
  const overallProgress =
    runPhase === "done" ? 100 : last && typeof last.progress === "number" ? last.progress : 0;

  return (
    <section className="rounded-[24px] border border-white/[0.08] bg-white/[0.03] p-7 shadow-[0_8px_32px_rgba(0,0,0,0.37)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.12] hover:bg-white/[0.04]">
      <div className="mb-4 flex items-center justify-between">
        <h3 className="font-display text-xl tracking-tight font-medium">Live Pipeline Progress</h3>
        <span className="font-mono text-[9px] uppercase tracking-[0.18em] text-white/40">
          {jobEvents.length} event{jobEvents.length === 1 ? "" : "s"}
        </span>
      </div>

      <div className="mb-5 h-1 overflow-hidden rounded-full bg-white/10">
        <motion.div
          className="h-full grad-amber"
          animate={{ width: `${overallProgress}%` }}
          transition={{ type: "spring", stiffness: 120, damping: 24 }}
        />
      </div>

      <ol className="space-y-3">
        <AnimatePresence initial={false}>
          {jobEvents.map((e: JobEvent, i) => (
            <motion.li
              key={`${e.type}-${e.timestamp}-${i}`}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              className="flex items-start gap-3"
            >
              <span className={`mt-2.5 h-1.5 w-1.5 shrink-0 rounded-full ${typeDotClass(e.type)}`} />
              <div className="min-w-0 flex-1">
                <div className="flex items-center justify-between gap-3">
                  <span className="text-xs font-semibold text-white/90">
                    {STAGE_LABEL[e.type] ?? e.type}
                    {typeof e.progress === "number" && (
                      <span className="ml-2 font-mono text-[9px] text-white/40">{e.progress}%</span>
                    )}
                  </span>
                  <span className="font-mono text-[9px] text-white/35">
                    {new Date(e.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                {e.message && (
                  <p className="truncate text-xs text-white/50 mt-0.5">{e.message}</p>
                )}
              </div>
            </motion.li>
          ))}
        </AnimatePresence>
      </ol>

      {runPhase === "error" && errorMessage && (
        <p className="mt-4 rounded-xl border border-red-500/20 bg-red-500/10 px-3.5 py-2.5 text-xs text-red-300 leading-relaxed">
          {errorMessage}
        </p>
      )}
    </section>
  );
}
