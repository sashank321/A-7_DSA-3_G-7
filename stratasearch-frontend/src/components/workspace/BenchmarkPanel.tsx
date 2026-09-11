import { motion } from "framer-motion";
import type { BenchmarkRow } from "../../api/types";
import { formatBytes, formatNanos } from "../../lib/format";

export function BenchmarkPanel({ rows }: { rows: BenchmarkRow[] }) {
  if (!rows || rows.length === 0) return null;
  const sorted = [...rows].sort((a, b) => a.executionTimeNanos - b.executionTimeNanos);
  const fastest = sorted[0]?.algorithm;
  const minNanos = Math.min(...rows.map((r) => r.executionTimeNanos), 1);
  const maxNanos = Math.max(...rows.map((r) => r.executionTimeNanos), 1);

  return (
    <motion.section
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]"
    >
      <div className="mb-4">
        <p className="mb-1 text-xs font-semibold tracking-widest text-amber uppercase">Stage 04 · Benchmark</p>
        <h2 className="font-display text-2xl tracking-tight text-white font-medium">Head-to-Head Benchmark</h2>
        <p className="mt-1 text-xs text-white/50">
          Every registered algorithm runs the identical query, ranked by wall-clock time.
        </p>
      </div>

      <div className="mt-5 overflow-hidden rounded-2xl border border-white/[0.08] bg-white/[0.02]">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-xs">
            <thead className="bg-white/5 text-[9px] uppercase tracking-[0.16em] text-white/40 border-b border-white/10">
              <tr>
                <th className="px-4 py-3 font-medium">Rank</th>
                <th className="px-4 py-3 font-medium">Algorithm</th>
                <th className="px-4 py-3 font-medium">Execution Time</th>
                <th className="px-4 py-3 font-medium">Memory</th>
                <th className="px-4 py-3 font-medium">Comparisons</th>
                <th className="hidden px-4 py-3 font-medium sm:table-cell">Matches</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-white/5">
              {sorted.map((r, i) => {
                const norm =
                  (r.executionTimeNanos - minNanos) / Math.max(maxNanos - minNanos, 1);
                return (
                  <motion.tr
                    key={r.algorithm}
                    initial={{ opacity: 0, x: -8 }}
                    animate={{ opacity: 1, x: 0 }}
                    transition={{ delay: 0.05 * i, duration: 0.4 }}
                    className={`transition-colors ${
                      r.algorithm === fastest ? "bg-amber/[0.08]" : "hover:bg-white/5"
                    }`}
                  >
                    <td className="px-4 py-3.5 font-mono text-white/40">{i + 1}</td>
                    <td className="px-4 py-3.5 font-semibold text-white">
                      {r.algorithm}
                      {r.algorithm === fastest && (
                        <span className="ml-2 rounded bg-amber px-2 py-0.5 text-[9px] font-bold text-white">
                          FASTEST
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3.5">
                      <div className="flex items-center gap-3">
                        <div className="flex flex-col">
                          <span className="font-mono text-white/90">{formatNanos(r.executionTimeNanos)}</span>
                          <span className="font-mono text-[9px] text-white/40">
                            {r.algorithm === fastest ? "1.0x (fastest)" : `${(r.executionTimeNanos / minNanos).toFixed(1)}x slower`}
                          </span>
                        </div>
                        <div className="h-1.5 w-16 overflow-hidden rounded-full bg-white/[0.03] border border-white/[0.06]">
                          <div
                            className="h-full grad-amber"
                            style={{ width: `${100 - norm * 80}%` }}
                          />
                        </div>
                      </div>
                    </td>
                    <td className="px-4 py-3.5 font-mono text-white/60">{formatBytes(r.memoryUsedBytes)}</td>
                    <td className="px-4 py-3.5 font-mono text-white/60">{r.comparisonCount.toLocaleString()}</td>
                    <td className="hidden px-4 py-3.5 font-mono text-white/60 sm:table-cell">{r.matchCount}</td>
                  </motion.tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>

      <p className="mt-4 font-mono text-[9px] uppercase tracking-[0.16em] text-white/30">
        Complexity / Memory Reference (Worst-case)
      </p>
      <div className="mt-3 grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {rows.map((r) => (
          <div
            key={r.algorithm + "-cx"}
            className="rounded-2xl border border-white/[0.06] bg-white/[0.02] px-4.5 py-3.5 text-xs hover:bg-white/[0.04] transition-all"
          >
            <span className="font-semibold text-white/95">{r.algorithm}</span>
            <div className="mt-1 font-mono text-[10px] text-white/40">
              {r.complexity} · {r.memoryComplexity}
            </div>
          </div>
        ))}
      </div>
    </motion.section>
  );
}
