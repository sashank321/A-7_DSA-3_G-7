import { useState, useMemo, useRef, useEffect } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { Play, Repeat, Sparkles } from "lucide-react";
import { useLabStore } from "../../stores/labStore";

export default function QueryPanel() {
  const patterns = useLabStore((s) => s.patterns);
  const setPatterns = useLabStore((s) => s.setPatterns);
  const repeated = useLabStore((s) => s.repeated);
  const setRepeated = useLabStore((s) => s.setRepeated);
  const analyze = useLabStore((s) => s.analyze);
  const runPhase = useLabStore((s) => s.runPhase);
  const errorMessage = useLabStore((s) => s.errorMessage);
  const jobId = useLabStore((s) => s.jobId);
  const documents = useLabStore((s) => s.documents);

  const [raw, setRaw] = useState(patterns.join("\n"));
  const [cursorPos, setCursorPos] = useState(0);
  const textareaRef = useRef<HTMLTextAreaElement>(null);

  const running = runPhase === "uploading" || runPhase === "analyzing";

  // 1. Build vocabulary from corpus
  const vocabulary = useMemo(() => {
    const words = new Map<string, number>();
    documents.forEach((d) => {
      const tokens = d.content.toLowerCase().split(/[^a-z0-9]+/);
      tokens.forEach((t) => {
        if (t.length > 2) {
          words.set(t, (words.get(t) || 0) + 1);
        }
      });
    });
    // Sort by frequency (descending)
    return Array.from(words.entries())
      .sort((a, b) => b[1] - a[1])
      .map((entry) => entry[0]);
  }, [documents]);

  // 2. Extract current word prefix being typed
  const currentWordMatch = raw.slice(0, cursorPos).match(/([a-z0-9]+)$/i);
  const currentWordPrefix = currentWordMatch ? currentWordMatch[1].toLowerCase() : "";

  // 3. Compute top suggestions
  const suggestions = useMemo(() => {
    if (currentWordPrefix.length < 1) return [];
    return vocabulary
      .filter((w) => w.startsWith(currentWordPrefix) && w !== currentWordPrefix)
      .slice(0, 5); // top 5 suggestions
  }, [currentWordPrefix, vocabulary]);

  const handleTextChange = (e: React.ChangeEvent<HTMLTextAreaElement>) => {
    setRaw(e.target.value);
    setCursorPos(e.target.selectionStart);
  };

  const handleSelectUpdate = (e: React.SyntheticEvent<HTMLTextAreaElement>) => {
    setCursorPos(e.currentTarget.selectionStart);
  };

  const applySuggestion = (word: string) => {
    if (!currentWordMatch) return;
    const prefixLen = currentWordMatch[1].length;
    const before = raw.slice(0, cursorPos - prefixLen);
    const after = raw.slice(cursorPos);
    
    const newRaw = before + word + after;
    setRaw(newRaw);
    
    const newCursor = before.length + word.length;
    setCursorPos(newCursor);
    
    // Restore focus and cursor position
    setTimeout(() => {
      if (textareaRef.current) {
        textareaRef.current.focus();
        textareaRef.current.setSelectionRange(newCursor, newCursor);
      }
    }, 0);
  };

  function commitAndRun() {
    setRaw(raw);
    setPatterns(raw);
    void analyze();
  }

  return (
    <section className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]">
      <h2 className="font-display text-2xl tracking-tight text-white font-medium">Patterns</h2>
      <p className="mt-1 text-xs text-white/50">
        One pattern per line (or comma-separated). The engine trims and lower-cases each.
      </p>

      <div className="flex flex-col gap-2 mt-4">
        <textarea
          ref={textareaRef}
          value={raw}
          onChange={handleTextChange}
          onKeyUp={handleSelectUpdate}
          onClick={handleSelectUpdate}
          rows={Math.min(8, Math.max(3, raw.split(/\r?\n/).length))}
          placeholder={"aba\nsearch pattern here\nkmp failure"}
          className="w-full resize-y rounded-xl border border-white/[0.08] bg-white/[0.02] px-3.5 py-2.5 font-mono text-[13px] leading-relaxed text-white outline-none focus:border-amber/50 transition-colors placeholder:text-white/30"
        />
        
        {/* Auto-complete Suggestions (Glassmorphism chips) */}
        <AnimatePresence>
          {suggestions.length > 0 && (
            <motion.div
              initial={{ opacity: 0, height: 0 }}
              animate={{ opacity: 1, height: "auto" }}
              exit={{ opacity: 0, height: 0 }}
              className="flex items-center gap-2 overflow-x-auto max-w-full scrollbar-hide"
            >
              <div className="flex items-center gap-1 text-[10px] uppercase font-bold tracking-wider text-amber/60 shrink-0">
                <Sparkles size={10} /> Predict
              </div>
              {suggestions.map((word) => (
                <button
                  key={word}
                  onClick={() => applySuggestion(word)}
                  className="shrink-0 whitespace-nowrap rounded-md border border-white/10 bg-white/5 px-2 py-1 text-xs font-mono text-white/80 transition-all hover:bg-amber/20 hover:border-amber/40 hover:text-amber active:scale-95"
                >
                  <span className="opacity-50">{currentWordPrefix}</span>
                  <span>{word.slice(currentWordPrefix.length)}</span>
                </button>
              ))}
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      <div className="mt-6 flex flex-wrap items-center justify-between gap-4">
        <label className="flex cursor-pointer items-center gap-3 text-sm text-white/80 select-none">
          <button
            type="button"
            onClick={() => setRepeated(!repeated)}
            className={`flex h-6 w-11 items-center rounded-full p-0.5 transition-colors ${
              repeated ? "bg-amber" : "bg-white/[0.04] border border-white/[0.08]"
            }`}
            aria-pressed={repeated}
          >
            <motion.span
              layout
              transition={{ type: "spring", stiffness: 500, damping: 30 }}
              className={`block h-4 w-4 rounded-full bg-white ${
                repeated ? "ml-auto" : "ml-0"
              }`}
            />
          </button>
          <span className="flex items-center gap-1.5">
            <Repeat size={14} className={repeated ? "text-amber" : "text-white/30"} />
            Repeated-query
          </span>
        </label>

        <div className="flex flex-col gap-1.5 w-full sm:w-auto flex-1 min-w-[200px] px-2">
          <div className="flex justify-between items-center text-[10px] uppercase font-bold tracking-wider text-white/40">
            <span>Speed</span>
            <span>Bias</span>
            <span>Memory</span>
          </div>
          <input
            type="range"
            min="0"
            max="100"
            value={useLabStore((s) => s.optimizationBias)}
            onChange={(e) => useLabStore.getState().setOptimizationBias(parseInt(e.target.value))}
            className="w-full accent-amber bg-white/10 rounded-full h-1 appearance-none cursor-pointer"
            style={{
              background: `linear-gradient(to right, rgb(251,191,36) ${useLabStore((s) => s.optimizationBias)}%, rgba(255,255,255,0.1) ${useLabStore((s) => s.optimizationBias)}%)`
            }}
          />
        </div>

        <button
          onClick={commitAndRun}
          disabled={running}
          className="rounded-lg bg-white/90 backdrop-blur-md px-6 py-2.5 text-xs font-semibold text-zinc-950 transition-all hover:bg-white hover:scale-105 active:scale-95 disabled:opacity-50 disabled:hover:scale-100 flex items-center gap-2 shadow-lg"
        >
          {running ? (
            <>
              <motion.span
                animate={{ rotate: 360 }}
                transition={{ duration: 1, repeat: Infinity, ease: "linear" }}
                className="block h-3.5 w-3.5 rounded-full border-2 border-black border-t-transparent"
              />
              Running…
            </>
          ) : (
            <>
              <Play size={14} fill="currentColor" />
              Run Analysis
            </>
          )}
        </button>
      </div>

      {jobId && running && (
        <p className="mt-3 truncate font-mono text-[10px] text-white/40">
          job id · {jobId}
        </p>
      )}

      {errorMessage && (
        <p className="mt-3 rounded-xl border border-red-500/20 bg-red-500/10 px-3.5 py-2.5 text-xs text-red-300 leading-relaxed">
          {errorMessage}
        </p>
      )}
    </section>
  );
}
