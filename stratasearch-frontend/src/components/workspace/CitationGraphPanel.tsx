import { useEffect, useRef, useState } from "react";
import ForceGraph2D from "react-force-graph-2d";
import { useLabStore } from "../../stores/labStore";
import { Network, RefreshCw, X, FileText, Link2 } from "lucide-react";

export default function CitationGraphPanel() {
  const documents = useLabStore((s) => s.documents);
  const citationGraph = useLabStore((s) => s.citationGraph);
  const computeCitationGraph = useLabStore((s) => s.computeCitationGraph);
  const graphRef = useRef<any>();
  const [dimensions, setDimensions] = useState({ width: 0, height: 0 });
  const containerRef = useRef<HTMLDivElement>(null);
  const [selectedNode, setSelectedNode] = useState<any | null>(null);

  useEffect(() => {
    computeCitationGraph();
    setSelectedNode(null);
  }, [documents.length, computeCitationGraph]);

  useEffect(() => {
    const observe = new ResizeObserver((entries) => {
      if (entries[0]) {
        const { width, height } = entries[0].contentRect;
        setDimensions({ width, height });
      }
    });
    if (containerRef.current) {
      observe.observe(containerRef.current);
    }
    return () => observe.disconnect();
  }, []);

  const handleRefresh = () => {
    computeCitationGraph();
    setSelectedNode(null);
    if (graphRef.current) {
      graphRef.current.d3Force("charge").strength(-120);
      graphRef.current.zoomToFit(400, 20);
    }
  };

  const hasNodes = citationGraph && citationGraph.nodes.length > 0;

  return (
    <section className="relative h-[400px] flex flex-col rounded-[24px] border border-white/[0.1] bg-white/[0.015] p-7 shadow-[inset_0_1px_1px_rgba(255,255,255,0.12),0_8px_32px_rgba(0,0,0,0.5)] backdrop-blur-2xl text-white transition-all duration-300 hover:border-white/[0.15] hover:bg-white/[0.03]">
      <div className="flex items-center justify-between mb-4">
        <div>
          <h2 className="font-display text-2xl tracking-tight text-white font-medium flex items-center gap-2">
            <Network size={24} className="text-blue-400" />
            Citation Network
          </h2>
          <p className="mt-1 text-xs text-white/50">
            {citationGraph?.nodes?.length || 0} documents, {citationGraph?.links?.length || 0} citations found.
          </p>
        </div>
        <button
          onClick={handleRefresh}
          className="flex items-center gap-1.5 rounded-full border border-white/10 px-3.5 py-1.5 text-xs font-medium text-white/60 transition-colors hover:border-blue-400/50 hover:bg-blue-500/10 hover:text-blue-400"
        >
          <RefreshCw size={13} /> Refresh
        </button>
      </div>

      <div ref={containerRef} className="flex-1 min-h-0 w-full relative rounded-2xl overflow-hidden border border-white/[0.05] bg-black/20">
        {dimensions.width > 0 && dimensions.height > 0 && hasNodes ? (
          <>
            <ForceGraph2D
              ref={graphRef}
              width={dimensions.width}
              height={dimensions.height}
              graphData={citationGraph}
              nodeLabel="name"
              nodeColor={(node: any) => node === selectedNode ? "#f59e0b" : "#60A5FA"} // Amber if selected, else Blue
              nodeRelSize={6}
              nodeVal={(node: any) => Math.max(1, (node.inDegree || 0) * 0.5 + 1)}
              linkColor={() => "rgba(255,255,255,0.15)"}
              linkWidth={1.5}
              linkDirectionalArrowLength={3.5}
              linkDirectionalArrowRelPos={1}
              onNodeClick={(node) => setSelectedNode(node)}
              onBackgroundClick={() => setSelectedNode(null)}
              onEngineStop={() => {
                if (graphRef.current) {
                  graphRef.current.zoomToFit(400, 20);
                }
              }}
            />
            {/* Info Overlay */}
            {selectedNode && (
              <div className="absolute top-4 right-4 w-64 rounded-xl border border-white/10 bg-black/60 backdrop-blur-md p-4 shadow-xl">
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-medium text-sm text-white break-words pr-2">{selectedNode.name}</h3>
                  <button onClick={() => setSelectedNode(null)} className="text-white/40 hover:text-white shrink-0">
                    <X size={14} />
                  </button>
                </div>
                <div className="space-y-2 mt-3">
                  <div className="flex items-center gap-2 text-xs text-white/60">
                    <FileText size={12} className="text-blue-400" />
                    <span>Size: {selectedNode.size?.toLocaleString()} chars</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-white/60">
                    <Link2 size={12} className="text-emerald-400" />
                    <span>Citations Made: {selectedNode.outDegree || 0}</span>
                  </div>
                  <div className="flex items-center gap-2 text-xs text-white/60">
                    <Link2 size={12} className="text-amber-400" />
                    <span>Cited By: {selectedNode.inDegree || 0} documents</span>
                  </div>
                </div>
              </div>
            )}
          </>
        ) : (
          <div className="absolute inset-0 flex items-center justify-center flex-col text-white/30">
            <Network size={48} className="mb-4 opacity-20" />
            <p className="text-sm">Upload documents to see citations.</p>
          </div>
        )}
      </div>
    </section>
  );
}
