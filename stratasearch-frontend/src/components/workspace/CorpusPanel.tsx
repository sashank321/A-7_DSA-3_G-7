import { useRef, useState, useMemo } from "react";
import { motion, AnimatePresence } from "framer-motion";
import { FileText, Plus, Trash2, Upload, X, Search, Sparkles, ChevronDown, ChevronRight } from "lucide-react";
import { useLabStore, type LabFile } from "../../stores/labStore";

const MAX_NAME_LEN = 80;

async function extractTextFromPdf(file: File): Promise<string> {
  const arrayBuffer = await file.arrayBuffer();
  const pdfjsLib = (window as any).pdfjsLib;
  if (!pdfjsLib) {
    throw new Error("PDF.js library is not loaded. Please wait a moment and try again.");
  }
  pdfjsLib.GlobalWorkerOptions.workerSrc = "https://cdnjs.cloudflare.com/ajax/libs/pdf.js/3.11.174/pdf.worker.min.js";
  const loadingTask = pdfjsLib.getDocument({ data: arrayBuffer });
  const pdf = await loadingTask.promise;
  let fullText = "";
  for (let i = 1; i <= pdf.numPages; i++) {
    const page = await pdf.getPage(i);
    const textContent = await page.getTextContent();
    const pageText = textContent.items.map((item: any) => item.str).join(" ");
    fullText += pageText + "\n";
  }
  return fullText;
}

async function readFiles(list: FileList): Promise<LabFile[]> {
  const out: LabFile[] = [];
  for (const f of Array.from(list)) {
    const isPdf = f.type === "application/pdf" || /\.pdf$/i.test(f.name);
    const isText = f.type.startsWith("text/") || /\.(txt|md|json|csv|log)$/i.test(f.name);
    if (!isText && !isPdf) {
      alert(`Skipping ${f.name}: Unsupported file type.`);
      continue;
    }

    try {
      if (isPdf) {
        const text = await extractTextFromPdf(f);
        out.push({ name: f.name.slice(0, MAX_NAME_LEN), content: text });
      } else {
        const content = await f.text();
        out.push({ name: f.name.slice(0, MAX_NAME_LEN), content });
      }
    } catch (e: any) {
      console.error("Failed to read file: " + f.name, e);
      alert(`Failed to read ${f.name}: ${e.message || "Unknown error"}`);
    }
  }
  return out;
}

function HighlightedText({ text, query }: { text: string; query: string }) {
  if (!query.trim()) return <>{text}</>;
  
  // Escape regex specials
  const safeQuery = query.trim().replace(/[.*+?^${}()|[\]\\]/g, '\\$&');
  const parts = text.split(new RegExp(`(${safeQuery})`, 'gi'));
  
  return (
    <>
      {parts.map((part, i) => 
        part.toLowerCase() === query.trim().toLowerCase() ? (
          <mark key={i} className="bg-amber/60 text-amber-50 rounded-[2px] px-0.5 shadow-[0_0_8px_rgba(251,191,36,0.5)]">{part}</mark>
        ) : (
          <span key={i}>{part}</span>
        )
      )}
    </>
  );
}

export default function CorpusPanel() {
  const documents = useLabStore((s) => s.documents);
  const addDocuments = useLabStore((s) => s.addDocuments);
  const removeDocument = useLabStore((s) => s.removeDocument);
  const clearCorpus = useLabStore((s) => s.clearCorpus);
  const characterCount = useLabStore((s) => s.characterCount);
  const sessionId = useLabStore((s) => s.sessionId);

  const fileInput = useRef<HTMLInputElement>(null);
  const searchInputRef = useRef<HTMLInputElement>(null);
  
  const [showPaste, setShowPaste] = useState(false);
  const [pasteName, setPasteName] = useState("");
  const [pasteBody, setPasteBody] = useState("");
  const [dragging, setDragging] = useState(false);
  const [searchQuery, setSearchQuery] = useState("");
  const [expandedDocs, setExpandedDocs] = useState<Set<string>>(new Set());

  const toggleDoc = (name: string) => {
    const next = new Set(expandedDocs);
    if (next.has(name)) next.delete(name);
    else next.add(name);
    setExpandedDocs(next);
  };

  async function onDrop(e: React.DragEvent) {
    e.preventDefault();
    setDragging(false);
    const files = await readFiles(e.dataTransfer.files);
    if (files.length) addDocuments(files);
  }

  function submitPaste() {
    if (!pasteBody.trim()) return;
    const name = pasteName.trim() || `pasted-${documents.length + 1}.txt`;
    addDocuments([{ name, content: pasteBody }]);
    setPasteName("");
    setPasteBody("");
    setShowPaste(false);
  }

  const totalChars = documents.reduce((n, d) => n + d.content.length, 0);

  // Vocabulary for predictions
  const vocabulary = useMemo(() => {
    if (documents.length === 0) return [];
    const words = new Map<string, number>();
    documents.forEach((d) => {
      const tokens = d.content.toLowerCase().split(/[^a-z0-9]+/);
      tokens.forEach((t) => {
        if (t.length > 2) {
          words.set(t, (words.get(t) || 0) + 1);
        }
      });
    });
    return Array.from(words.entries())
      .sort((a, b) => b[1] - a[1])
      .map((entry) => entry[0]);
  }, [documents]);

  const searchWords = searchQuery.toLowerCase().split(/\s+/).filter(Boolean);
  const currentWordPrefix = searchWords.length > 0 ? searchWords[searchWords.length - 1] : "";

  const suggestions = useMemo(() => {
    if (currentWordPrefix.length < 1) return [];
    return vocabulary
      .filter((w) => w.startsWith(currentWordPrefix) && w !== currentWordPrefix)
      .slice(0, 5);
  }, [currentWordPrefix, vocabulary]);

  const applySuggestion = (word: string) => {
    if (searchWords.length === 0) return;
    searchWords[searchWords.length - 1] = word;
    setSearchQuery(searchWords.join(" ") + " ");
    searchInputRef.current?.focus();
  };

  const filteredDocuments = useMemo(() => {
    if (!searchQuery.trim()) return documents;
    const query = searchQuery.trim().toLowerCase();
    return documents.filter(d => 
      d.name.toLowerCase().includes(query) || d.content.toLowerCase().includes(query)
    );
  }, [documents, searchQuery]);

  return (
    <section className="rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03] flex flex-col h-full max-h-[calc(100vh-100px)]">
      <div className="mb-5 flex items-center justify-between shrink-0">
        <div>
          <h2 className="font-display text-2xl tracking-tight text-white font-medium">Corpus</h2>
          <p className="mt-1 text-xs text-white/50">
            {documents.length === 0
              ? "Drop text files or paste raw content."
              : sessionId
                ? `Uploaded — session ${sessionId.slice(0, 18)}…`
                : `${documents.length} document${documents.length === 1 ? "" : "s"} · ${totalChars.toLocaleString()} chars`}
          </p>
        </div>
        {documents.length > 0 && (
          <button
            onClick={clearCorpus}
            className="flex items-center gap-1.5 rounded-full border border-white/10 px-3.5 py-1.5 text-xs font-medium text-white/60 transition-colors hover:border-red-500/50 hover:bg-red-500/10 hover:text-red-400"
          >
            <Trash2 size={13} /> Clear
          </button>
        )}
      </div>

      <div
        onDragOver={(e) => {
          e.preventDefault();
          setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={onDrop}
        className={`relative rounded-2xl border border-dashed px-5 py-8 text-center shrink-0 transition-all ${
          dragging ? "border-white/50 bg-white/[0.08]" : "border-white/15 bg-white/[0.02]"
        }`}
      >
        <input
          ref={fileInput}
          type="file"
          accept=".txt,.md,.json,.csv,.log,.pdf"
          multiple
          className="hidden"
          onChange={async (e) => {
            const files = e.target.files ? await readFiles(e.target.files) : [];
            if (files.length) addDocuments(files);
            if (fileInput.current) fileInput.current.value = "";
          }}
        />
        <Upload size={22} className="mx-auto text-white/40 mb-2" />
        <p className="text-sm font-medium text-white/70">
          Drag &amp; drop files (txt, md, pdf, etc.) here, or
        </p>
        <div className="mt-4 flex justify-center gap-2">
          <button
            onClick={() => fileInput.current?.click()}
            className="rounded-lg bg-white/90 backdrop-blur-md px-4 py-2 text-xs font-semibold text-zinc-950 transition-all hover:bg-white active:scale-95 shadow-md"
          >
            Browse Files
          </button>
          <button
            onClick={() => setShowPaste((v) => !v)}
            className="rounded-lg border border-white/[0.08] bg-white/[0.04] px-4 py-2 text-xs font-medium text-white/90 transition-colors hover:bg-white/[0.08] hover:border-white/[0.15]"
          >
            <Plus size={14} className="mr-1 inline -translate-y-0.5" />
            Paste Text
          </button>
        </div>
      </div>

      <AnimatePresence>
        {showPaste && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            className="mt-4 overflow-hidden shrink-0"
          >
            <div className="rounded-2xl border border-white/[0.08] bg-white/[0.02] p-4">
              <input
                value={pasteName}
                onChange={(e) => setPasteName(e.target.value)}
                placeholder="filename.txt (optional)"
                className="mb-2 w-full rounded-lg border border-white/[0.08] bg-white/[0.03] px-3 py-2 text-sm text-white outline-none focus:border-amber/50 transition-colors placeholder:text-white/30"
              />
              <textarea
                value={pasteBody}
                onChange={(e) => setPasteBody(e.target.value)}
                rows={5}
                placeholder="Paste your corpus content here…"
                className="w-full resize-y rounded-lg border border-white/[0.08] bg-white/[0.03] px-3 py-2 font-mono text-[12px] leading-relaxed text-white outline-none focus:border-amber/50 transition-colors placeholder:text-white/30"
              />
              <div className="mt-3 flex justify-end gap-2">
                <button
                  onClick={() => setShowPaste(false)}
                  className="rounded-lg px-3 py-1.5 text-xs text-white/50 hover:text-white transition-colors"
                >
                  <X size={14} className="inline -translate-y-0.5" /> Cancel
                </button>
                <button
                  onClick={submitPaste}
                  className="rounded-lg bg-white px-4 py-1.5 text-xs font-semibold text-zinc-950 transition-transform hover:bg-white"
                >
                  Add to Corpus
                </button>
              </div>
            </div>
          </motion.div>
        )}
      </AnimatePresence>

      {documents.length > 0 && (
        <div className="mt-6 flex flex-col gap-2 shrink-0">
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-white/40" />
            <input
              ref={searchInputRef}
              type="text"
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              placeholder="Universal Search in Corpus... (Ctrl+F style)"
              className="w-full rounded-lg border border-white/[0.08] bg-white/[0.03] pl-9 pr-3 py-2 text-[13px] text-white outline-none focus:border-amber/50 transition-colors placeholder:text-white/30"
            />
          </div>

          <AnimatePresence>
            {suggestions.length > 0 && (
              <motion.div
                initial={{ opacity: 0, height: 0 }}
                animate={{ opacity: 1, height: "auto" }}
                exit={{ opacity: 0, height: 0 }}
                className="flex items-center gap-2 overflow-x-auto max-w-full scrollbar-hide pt-1"
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
      )}

      <ul className="mt-4 space-y-2 flex-1 overflow-y-auto pr-1">
        <AnimatePresence initial={false}>
          {filteredDocuments.map((d) => {
            const isExpanded = expandedDocs.has(d.name) || searchQuery.trim().length > 0;
            return (
              <motion.li
                key={d.name + d.content.slice(0, 12)}
                initial={{ opacity: 0, x: -10 }}
                animate={{ opacity: 1, x: 0 }}
                exit={{ opacity: 0, x: -10 }}
                className="flex flex-col rounded-xl border border-white/[0.06] bg-white/[0.02] overflow-hidden hover:bg-white/[0.04] transition-all"
              >
                <div 
                  className="flex items-center gap-3 px-4 py-3 cursor-pointer"
                  onClick={() => toggleDoc(d.name)}
                >
                  <button className="text-white/40 hover:text-white/80 transition-colors">
                    {isExpanded ? <ChevronDown size={14} /> : <ChevronRight size={14} />}
                  </button>
                  <FileText size={14} className="shrink-0 text-amber" />
                  <div className="min-w-0 flex-1">
                    <p className="truncate text-xs font-medium text-white/90">{d.name}</p>
                    <p className="font-mono text-[10px] text-white/40">{d.content.length.toLocaleString()} chars</p>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      removeDocument(d.name);
                    }}
                    className="rounded p-1 text-white/45 hover:text-red-400 hover:bg-white/5 transition-colors"
                    aria-label={`Remove ${d.name}`}
                  >
                    <X size={14} />
                  </button>
                </div>
                
                <AnimatePresence>
                  {isExpanded && (
                    <motion.div
                      initial={{ height: 0, opacity: 0 }}
                      animate={{ height: "auto", opacity: 1 }}
                      exit={{ height: 0, opacity: 0 }}
                      className="border-t border-white/[0.04]"
                    >
                      <div className="p-4 max-h-[300px] overflow-y-auto bg-black/20 text-xs font-mono leading-relaxed text-white/70 whitespace-pre-wrap break-words">
                        <HighlightedText text={d.content} query={searchQuery.trim()} />
                      </div>
                    </motion.div>
                  )}
                </AnimatePresence>
              </motion.li>
            );
          })}
          {filteredDocuments.length === 0 && documents.length > 0 && (
            <li className="text-center py-4 text-xs text-white/40 font-medium">
              No matching documents found.
            </li>
          )}
        </AnimatePresence>
      </ul>

      {sessionId && (
        <p className="mt-3 shrink-0 font-mono text-[9px] uppercase tracking-[0.18em] text-white/60">
          ✓ server received {characterCount.toLocaleString()} chars
        </p>
      )}
    </section>
  );
}
