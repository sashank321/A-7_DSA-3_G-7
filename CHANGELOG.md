## [1.1.0] - 2026-10-04

### Added
- CONTRIBUTING.md with setup and commit-convention guide
- Deployment and CI badges in README
- Aria navigation label for accessibility
### Notes
- Release candidate promoted to 1.1.0 after successful Vercel deployment

## [1.1.0-rc1] - 2026-10-03

### Added
- GitHub Actions CI for backend and frontend
- SuffixArray test stub
- Type guard in client-side planner
- 5th sample paper (DC3 suffix array)
### Fixed
- Null/empty guard in profiler
- Badge hover state UX

## [1.0.5] - 2026-10-01

### Added
- `bigramRichness` metric in TypeScript client profiler
### Fixed
- Null/empty text guard in CorpusProfiler

## [1.0.4] - 2026-09-30

### Changed
- CostModel: added weight-calibration documentation
- RabinKarp: documented prime-modulus selection for ASCII-128

## [1.0.3] - 2026-09-28

### Added
- Unit tests for TextNormalizer control-char normalization
- Unit tests for KMP searcher occurrence detection

## [1.0.2] - 2026-09-27

### Changed
- Added .editorconfig for consistent indentation across editors
- Code review pass on core algorithm classes

# Changelog

All notable changes to the **StrataSearch** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased] - 2026-10-03

### Fixed
- **Cloud Deployment Resiliency**: Resolved Vercel SPA rewrite conflict where `/api/v1` requests received HTML index responses, causing client-side JSON parse exceptions.
- **Hybrid Engine Architecture**: Implemented full in-browser algorithmic engine in [`src/engine/`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/stratasearch-frontend/src/engine) mirroring the Java core (KMP, Rabin-Karp, Z-Algorithm, Aho-Corasick, Suffix Array + LCP, CostModel QueryPlanner, and Corpus Profiler) for zero-downtime execution even without a remote backend.
- **Initial State & Usability**: Pre-loaded foundational research papers with inter-document citations and added a one-click "Sample Papers" button in [`CorpusPanel.tsx`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/stratasearch-frontend/src/components/workspace/CorpusPanel.tsx).

---

## [1.0.1] - 2026-09-26

### Fixed
- **Frontend Build**: Fixed `useRef<any>()` uninitialized parameter in [`CitationGraphPanel.tsx`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/stratasearch-frontend/src/components/workspace/CitationGraphPanel.tsx) and removed unused `useEffect` import in [`QueryPanel.tsx`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/stratasearch-frontend/src/components/workspace/QueryPanel.tsx) to resolve TypeScript build errors.
- **Backend Build & Assembly**: Resolved missing reactor artifact dependency by executing `mvn clean install` for `stratasearch-core`.
- **CORS & WebSocket**: Added `http://127.0.0.1:5173` to allowed CORS and WebSocket origins and enabled `changeOrigin: true` on Vite's WebSocket reverse proxy.

### Added
- **Launcher Scripts**: Created [`start.bat`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/start.bat) and [`start.ps1`](file:///d:/College/Second%20Year/Second%20year%201st%20sem/DSA%203/Zcode%20trail/start.ps1) for seamless one-command deployment with Java 21 environment setup.

### Scheduled
- Configured automated commit pipeline scheduled through September 30, 2026.
- Established automated `CHANGELOG.md` tracking for upcoming algorithm and framework updates.

---

## [1.0.0] - 2026-09-11

### Added
- **Documentation**: Formalized 4-review milestone trajectory, multi-stack team contribution matrix, and comprehensive algorithm scope in `README.md`.
- **Frontend Visualizations**: Built interactive Algorithm Visualizer component, real-time telemetry performance charts, and D3/Three 2D/3D Citation Network graph.
- **Planner Panel**: Implemented `PlannerPanel` featuring algorithmic decision tree visualizations, cost breakdown matrix, and query execution optimizer diagnostics.
- **Real-Time Feed**: Integrated WebSocket client for live server-sent progress updates and job execution feeds.

---

## [0.9.0] - 2026-09-10

### Added
- **Backend API Services**: Implemented REST API controllers and services for algorithm visualizations, performance benchmarking, and dynamic query planning.
- **Asynchronous Pipeline**: Built WebSocket streaming infrastructure and thread-pooled asynchronous job execution worker pipeline.
- **Core Algorithms**:
  - Implemented **Aho-Corasick Automaton** for multi-pattern keyword matching with failure link transitions.
  - Implemented dynamic **QueryPlanner** with analytical cost-estimation models.
  - Implemented **Suffix Array** construction with $O(\log N)$ binary search over text suffixes.

---

## [0.8.0] - 2026-09-01

### Added
- **State & UI System**:
  - Implemented centralized Zustand `labStore` for application-wide state management.
  - Configured dark-mode glassmorphism visual design system and theme customization.
- **Backend Architecture**: Connected engine gateway, Java core adapters, and primary search execution service wrappers.

---

## [0.7.0] - 2026-08-29

### Added
- **Frontend Foundations**: Created landing hero UI, dual workspace view layout, interactive search query input, and document corpus upload interface.
- **HTTP Client**: Built strongly-typed TypeScript API client layer for backend endpoint integration.
- **Core Search Algorithms**:
  - Implemented **Z-Algorithm** string pattern searcher with text normalization pipeline.
  - Implemented **Rabin-Karp** rolling hash pattern matching with hash collision handling.
  - Implemented document corpus session storage and PDF/plaintext ingestion controllers.
