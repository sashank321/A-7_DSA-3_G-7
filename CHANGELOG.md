# Changelog

All notable changes to the **StrataSearch** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased] - 2026-09-25

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
