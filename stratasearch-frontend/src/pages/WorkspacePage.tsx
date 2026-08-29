import { useEffect } from "react";
import { Link } from "react-router-dom";
import { motion } from "framer-motion";
import { ArrowLeft, Cpu } from "lucide-react";
import { useLabStore } from "../stores/labStore";
import CorpusPanel from "../components/workspace/CorpusPanel";
import QueryPanel from "../components/workspace/QueryPanel";
import JobProgressFeed from "../components/workspace/JobProgressFeed";
import { FingerprintPanel } from "../components/workspace/FingerprintPanel";
import { PlannerPanel } from "../components/workspace/PlannerPanel";
import { TelemetryPanel } from "../components/workspace/TelemetryPanel";
import { BenchmarkPanel } from "../components/workspace/BenchmarkPanel";
import { VisualizationPlayer } from "../components/workspace/VisualizationPlayer";
import CitationGraphPanel from "../components/workspace/CitationGraphPanel";

export default function WorkspacePage() {
  const workflow = useLabStore((s) => s.workflow);
  const runPhase = useLabStore((s) => s.runPhase);
  const loadCapabilities = useLabStore((s) => s.loadCapabilities);

  // load algorithm capabilities once for the planner + viz dropdown
  useEffect(() => {
    void loadCapabilities().catch(() => {
      /* if backend offline, simply keep capabilities empty */
    });
  }, [loadCapabilities]);

  const analyzing = runPhase === "analyzing" || runPhase === "uploading";

  return (
    <div className="relative min-h-screen w-full overflow-x-hidden bg-black font-geist -webkit-font-smoothing-antialiased -moz-osx-font-smoothing-grayscale text-white">
      {/* Video Background */}
      <video
        autoPlay
        muted
        loop
        playsInline
        className="fixed inset-0 h-full w-full object-cover object-[70%_center] pointer-events-none z-0"
      >
        <source
          src="https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260622_204221_5339e40b-e73d-4ab0-9c65-79c18c66fd50.mp4"
          type="video/mp4"
        />
      </video>

      {/* Dark tint glass overlay */}
      <div className="fixed inset-0 bg-zinc-950/60 backdrop-blur-2xl z-0 pointer-events-none" />

      {/* Ambient background refraction blobs */}
      <div className="fixed top-[10%] left-[5%] h-[450px] w-[450px] rounded-full bg-amber-500/[0.14] blur-[140px] pointer-events-none z-0" />
      <div className="fixed top-[40%] right-[5%] h-[500px] w-[500px] rounded-full bg-indigo-500/[0.1] blur-[180px] pointer-events-none z-0" />
      <div className="fixed bottom-[5%] left-[15%] h-[400px] w-[400px] rounded-full bg-amber-600/[0.08] blur-[120px] pointer-events-none z-0" />

      {/* Main content wrapper */}
      <div className="relative z-10 flex flex-col min-h-screen">
        {/* top bar */}
        <header className="sticky top-0 z-40 border-b border-white/[0.06] bg-white/[0.02] backdrop-blur-xl shadow-[inset_0_-1px_0_rgba(255,255,255,0.05),0_4px_24px_rgba(0,0,0,0.2)]">
          <div className="mx-auto flex max-w-7xl items-center justify-between px-6 py-4">
            <Link to="/" className="flex items-center gap-2 text-sm font-medium text-white/70 hover:text-white transition-colors">
              <ArrowLeft size={16} />
              Back to Home
            </Link>
            <h1 className="text-xl font-semibold tracking-tight text-white">
              Strata<span className="text-amber">Search</span>
              <span className="ml-2 font-mono text-[10px] uppercase tracking-[0.2em] text-white/40">
                / Laboratory
              </span>
            </h1>
            <a
              href="http://localhost:8080/swagger-ui.html"
              target="_blank"
              rel="noreferrer"
              className="flex items-center gap-1.5 text-sm font-medium text-white/70 hover:text-amber transition-colors"
            >
              <Cpu size={15} /> API Docs
            </a>
          </div>
        </header>

        <main className="mx-auto max-w-7xl px-6 py-10 flex-1 w-full">
          <motion.div
            initial={{ opacity: 0, y: 12 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            className="mb-8"
          >
            <div className="flex items-center gap-2 mb-2">
              <span className="h-1.5 w-1.5 rounded-full bg-blue-500 animate-pulse" />
              <p className="text-[10px] font-mono tracking-widest text-blue-400 uppercase">Interactive Console</p>
            </div>
            <h2 className="text-3xl font-medium tracking-tight mb-2 bg-gradient-to-r from-white via-white/95 to-blue-200 bg-clip-text text-transparent">
              Algorithmic Query Planner
            </h2>
            <p className="max-w-2xl text-sm leading-relaxed text-white/60">
              Upload a corpus, input search patterns, and watch StrataSearch optimize the query,
              evaluate classical string matching algorithms head-to-head, and stream real-time visual telemetry.
            </p>
          </motion.div>

          {/* Input row — corpus + query */}
          <div className="grid gap-6 lg:grid-cols-[1.4fr_1fr]">
            <CorpusPanel />
            <QueryPanel />
          </div>

          <div className="mt-6">
            <CitationGraphPanel />
          </div>

          {/* live progress feed (only while running or after) */}
          <div className="mt-6">
            <JobProgressFeed />
          </div>

          {/* results grid */}
          <div className="mt-6 grid gap-6">
            {workflow && (
              <motion.div
                layout
                className="grid gap-6"
              >
                <FingerprintPanel fingerprint={workflow.profile} />
                <div className="grid gap-6 lg:grid-cols-2">
                  <PlannerPanel planner={workflow.planner} />
                  <TelemetryPanel search={workflow.search} telemetry={workflow.telemetry} />
                </div>
                <BenchmarkPanel rows={workflow.benchmark} />
              </motion.div>
            )}

            {/* visualization lives below, always available once a workflow exists */}
            <VisualizationPlayer />
          </div>

          {/* empty / idle state */}
          {!workflow && !analyzing && (
            <div className="mt-12 rounded-[24px] border border-dashed border-white/[0.1] bg-white/[0.015] shadow-[inset_0_1px_1px_rgba(255,255,255,0.1),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl px-6 py-20 text-center">
              <p className="text-2xl font-medium text-white mb-2">Awaiting Input</p>
              <p className="max-w-md mx-auto text-sm text-white/40 leading-relaxed">
                Add documents and pattern queries above, then launch <span className="font-semibold text-white">Run Analysis</span> to
                stream the optimization pipeline in real time.
              </p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
