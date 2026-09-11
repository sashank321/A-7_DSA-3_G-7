import { motion } from "framer-motion";
import type { SearchResponse, TelemetryDto } from "../../api/types";
import { formatBytes, formatNanos } from "../../lib/format";

interface Props {
  search: SearchResponse | null;
  telemetry: TelemetryDto | null;
}

function Stat({ label, value, accent }: { label: string; value: string; accent?: boolean }) {
  return (
    <div className={`rounded-2xl border p-4.5 transition-all duration-300 ${accent ? "bg-white/[0.08] border-white/[0.15] shadow-md" : "border-white/[0.06] bg-white/[0.02]"}`}>
      <div className={`font-mono text-[9px] uppercase tracking-[0.16em] ${accent ? "text-amber" : "text-white/40"}`}>
        {label}
      </div>
      <div className={`mt-1 font-display text-2xl font-semibold tracking-tight ${accent ? "text-white" : "text-white/90"}`}>{value}</div>
    </div>
  );
}

export function TelemetryPanel({ search, telemetry }: Props) {
  const source = telemetry ?? search;
  if (!source) return null;
  const algoName = telemetry?.algorithmName ?? search?.algorithmUsed ?? "";
  const positions = search?.matchPositions ?? telemetry?.matchPositions ?? [];

  return (
    <motion.section
      initial={{ opacity: 0, y: 16 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.5 }}
      className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]"
    >
      <div className="mb-4 flex flex-wrap items-end justify-between gap-3">
        <div>
          <p className="mb-1 text-xs font-semibold tracking-widest text-amber uppercase">Stage 03 · Run</p>
          <h2 className="font-display text-2xl tracking-tight text-white">Live Search Telemetry</h2>
          <p className="mt-1 text-xs text-white/50">
            Reported by {algoName}
            {search?.wasForced && (
              <span className="ml-2 rounded bg-white/10 border border-white/10 px-1.5 py-0.5 text-[9px] font-bold uppercase tracking-wider text-white/60">
                forced
              </span>
            )}
          </p>
        </div>
      </div>

      <div className="grid grid-cols-2 gap-3 sm:grid-cols-4">
        <Stat label="Matches" value={String(positions.length)} accent />
        <Stat label="Comparisons" value={source.comparisonCount.toLocaleString()} />
        <Stat label="Time" value={formatNanos(source.executionTimeNanos)} />
        <Stat label="Memory" value={formatBytes(source.memoryUsedBytes)} />
      </div>

      {positions.length > 0 && (
        <div className="mt-5">
          <p className="mb-2 font-mono text-[9px] uppercase tracking-[0.16em] text-white/40">
            Match positions (first 200)
          </p>
          <div className="flex flex-wrap gap-1.5 rounded-xl border border-white/10 bg-black/20 p-3 max-h-36 overflow-y-auto pr-1">
            {positions.slice(0, 200).map((p, i) => (
              <motion.span
                key={`${p}-${i}`}
                initial={{ opacity: 0, scale: 0.8 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ delay: Math.min(0.001 * i, 0.4) }}
                className="rounded-md bg-white/5 border border-white/10 px-2 py-0.5 font-mono text-xs text-white/80"
              >
                {p}
              </motion.span>
            ))}
            {positions.length > 200 && (
              <span className="self-center font-mono text-xs text-white/40 ml-1">
                +{positions.length - 200} more
              </span>
            )}
          </div>
        </div>
      )}
    </motion.section>
  );
}
