/** Formatting helpers shared across the UI. */

export function formatNanos(ns: number): string {
  if (ns < 1_000) return `${ns} ns`;
  if (ns < 1_000_000) return `${(ns / 1_000).toFixed(2)} µs`;
  if (ns < 1_000_000_000) return `${(ns / 1_000_000).toFixed(2)} ms`;
  return `${(ns / 1_000_000_000).toFixed(3)} s`;
}

export function formatBytes(bytes: number): string {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

export function formatInt(n: number): string {
  return n.toLocaleString("en-US");
}

export function formatPercent(v: number, digits = 1): string {
  return `${v.toFixed(digits)}%`;
}

export function formatEpoch(epochMs: number): string {
  return new Date(epochMs).toLocaleString();
}

/** Truncate a long string sensibly for previews. */
export function truncate(s: string, max = 90): string {
  return s.length <= max ? s : `${s.slice(0, max - 1)}…`;
}
