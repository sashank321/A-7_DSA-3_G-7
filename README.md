# A-7_DSA-3_G-7
# StrataSearch

## An Intelligent Document Search and Analysis Platform

StrataSearch is a document search and analysis platform designed to make searching through research papers and technical documents more efficient, precise, and explainable.

Instead of treating document search as simple keyword retrieval, StrataSearch combines **string-matching algorithms, text-processing techniques, document similarity analysis, citation relationships, algorithm visualization, and performance benchmarking** into a single platform.

The project focuses on applying **Data Structures and Algorithms (DSA)** to a practical document-search problem.

---

## Table of Contents

* [Overview](#overview)
* [Problem Statement](#problem-statement)
* [Motivation](#motivation)
* [Objectives](#objectives)
* [Key Features](#key-features)
* [System Architecture](#system-architecture)
* [Core Modules](#core-modules)

  * [Search Engine Core](#1-search-engine-core)
  * [Advanced Text Analysis](#2-advanced-text-analysis)
  * [Analytics and Visualization](#3-analytics-and-visualization)
* [Algorithms](#algorithms)
* [How the Search Works](#how-the-search-works)
* [Search Engine Workflow](#search-engine-workflow)
* [Document Analysis Workflow](#document-analysis-workflow)
* [Relevance Ranking](#relevance-ranking)
* [Similarity and Duplicate Detection](#similarity-and-duplicate-detection)
* [Citation Graph](#citation-graph)
* [Algorithm Visualization](#algorithm-visualization)
* [Performance Benchmarking](#performance-benchmarking)
* [Project Structure](#project-structure)
* [Technology Stack](#technology-stack)
* [Getting Started](#getting-started)
* [Usage](#usage)
* [Example Search](#example-search)
* [Complexity Overview](#complexity-overview)
* [Research Gap](#research-gap)
* [Existing Systems Studied](#existing-systems-studied)
* [Project Scope](#project-scope)
* [Team Organization & Contribution Matrix](#team-organization--contribution-matrix)
* [4-Review Trajectory & Milestone Roadmap](#4-review-trajectory--milestone-roadmap)
* [Expected Outcomes](#expected-outcomes)
* [Future Enhancements](#future-enhancements)
* [Limitations](#limitations)
* [Contributing](#contributing)
* [License](#license)
* [References](#references)

---

# Overview

Research papers and technical documents contain large amounts of structured and unstructured text. When the number of documents increases, manually locating specific information becomes difficult and time-consuming.

Traditional search systems are very effective at retrieving information, but they generally hide the algorithmic processes responsible for matching and analyzing the text.

StrataSearch approaches the problem from a DSA perspective.

The platform is designed around three major layers:

```text
┌──────────────────────────────────────────────┐
│                  STRATASEARCH                │
├──────────────────────────────────────────────┤
│                                              │
│  Search Engine Core                          │
│  ├── KMP                                     │
│  ├── Rabin-Karp                              │
│  ├── Pattern Search                          │
│  ├── Phrase Search                           │
│  └── Relevance Ranking                       │
│                                              │
│  Advanced Text Analysis                     │
│  ├── Z Algorithm                             │
│  ├── Aho-Corasick                            │
│  ├── Suffix Array                            │
│  ├── LCP                                     │
│  ├── Similarity                              │
│  └── Duplicate Detection                     │
│                                              │
│  Analytics & Visualization                   │
│  ├── Edit Distance                           │
│  ├── LCS                                     │
│  ├── Recommendation                          │
│  ├── Citation Graph                          │
│  ├── Benchmark Dashboard                     │
│  └── Algorithm Visualizer                    │
│                                              │
└──────────────────────────────────────────────┘
```

The central idea is:

> **Search → Analyze → Compare → Visualize**

---

# Problem Statement

Research documents contain large volumes of text, and users often need to find a particular word, phrase, pattern, or concept across multiple documents.

A basic search approach can identify matching terms, but a complete document-analysis platform should also be able to:

* efficiently search large amounts of text;
* support exact pattern matching;
* support phrase searching;
* rank relevant documents;
* compare documents;
* identify duplicate or highly similar documents;
* analyze relationships between research papers;
* demonstrate how search algorithms operate;
* compare algorithm performance.

### Problem Definition

> Develop an efficient document search and analysis platform that retrieves relevant information from multiple research documents using string-matching and text-processing algorithms while providing advanced document analysis, visualization, and performance benchmarking.

---

# Motivation

The project is motivated by three main observations.

### 1. Large Document Collections

Research papers can contain hundreds or thousands of pages when considered as a collection.

Searching manually through such collections is inefficient.

### 2. Search Is More Than Keyword Matching

Finding a word is only one part of the problem.

A useful system should also support:

* patterns;
* phrases;
* multiple-pattern searches;
* document relevance;
* similarity;
* duplicate detection;
* research relationships.

### 3. Algorithms Should Be Explainable

Algorithms such as KMP, Rabin-Karp, Z Algorithm, and Aho-Corasick are commonly studied theoretically.

StrataSearch aims to demonstrate their practical application to real document-search problems.

---

# Objectives

The primary objectives of StrataSearch are:

1. Search across uploaded research documents.
2. Perform exact pattern matching.
3. Support phrase-based searching.
4. Use efficient string-matching algorithms.
5. Rank documents based on search relevance.
6. Analyze similarity between documents.
7. Detect duplicate or highly similar documents.
8. Represent citation relationships using graphs.
9. Visualize algorithm execution.
10. Benchmark and compare algorithm performance.

---

# Key Features

## Document Search

Search across a collection of uploaded research documents.

## Pattern Search

Find an exact character or word sequence inside document text.

## Phrase Search

Search for complete phrases instead of treating each word independently.

Example:

```text
"machine learning in healthcare"
```

## Efficient String Matching

The Search Engine Core uses algorithms such as:

* KMP
* Rabin-Karp

for efficient pattern matching.

## Relevance Ranking

Search results can be ranked so that more relevant documents are presented first.

## Multiple-Pattern Processing

Aho-Corasick can be used for searching multiple patterns efficiently.

## Advanced Text Analysis

The platform incorporates:

* Z Algorithm
* Suffix Array
* LCP

for deeper string and text analysis.

## Similarity Analysis

Documents can be compared to determine how closely their textual content matches.

## Duplicate Detection

Highly similar or duplicate documents can be identified.

## Citation Analysis

Research-paper relationships can be represented using citation graphs.

## Algorithm Visualization

The platform is designed to show algorithm operations in an understandable visual form.

## Benchmarking

Different algorithms can be compared using execution and performance measurements.

---

# System Architecture

The system is organized into three major technical modules.

```text
                    ┌──────────────┐
                    │     USER     │
                    └──────┬───────┘
                           │
                           ▼
                ┌────────────────────┐
                │ Documents + Query  │
                └─────────┬──────────┘
                          │
                          ▼
             ┌─────────────────────────┐
             │   SEARCH ENGINE CORE    │
             │                         │
             │ KMP                     │
             │ Rabin-Karp              │
             │ Pattern Search          │
             │ Phrase Search           │
             │ Relevance Ranking       │
             └────────────┬────────────┘
                          │
                          ▼
             ┌─────────────────────────┐
             │ ADVANCED TEXT ANALYSIS  │
             │                         │
             │ Z Algorithm             │
             │ Aho-Corasick            │
             │ Suffix Array             │
             │ LCP                     │
             │ Similarity               │
             │ Duplicate Detection     │
             └────────────┬────────────┘
                          │
                          ▼
             ┌─────────────────────────┐
             │ ANALYTICS & VISUALIZATION│
             │                         │
             │ Edit Distance           │
             │ LCS                     │
             │ Recommendation          │
             │ Citation Graph           │
             │ Benchmark Dashboard     │
             │ Algorithm Visualizer    │
             └────────────┬────────────┘
                          │
                          ▼
                ┌──────────────────┐
                │ Results & Insights│
                └──────────────────┘
```

---

# Core Modules

## 1. Search Engine Core

The Search Engine Core is responsible for handling user search queries and locating matching content inside the document collection.

### Responsibilities

* Query processing
* Exact pattern matching
* Phrase matching
* Document scanning
* Match identification
* Relevance scoring
* Result ranking
* Search API integration

### Algorithms

#### KMP

The Knuth-Morris-Pratt algorithm performs pattern matching without repeatedly comparing characters that have already been processed.

It uses a preprocessing structure commonly known as the **LPS (Longest Prefix Suffix) array**.

Primary use in StrataSearch:

```text
Query → Pattern → KMP → Matching Positions
```

#### Rabin-Karp

Rabin-Karp uses hashing to compare a search pattern against portions of the document.

Primary use:

```text
Query → Hash → Rolling Hash Comparison → Candidate Matches
```

### Search Engine Flow

```text
User Query
    ↓
Query Processing
    ↓
Pattern / Phrase Identification
    ↓
KMP / Rabin-Karp
    ↓
Document Matching
    ↓
Relevance Calculation
    ↓
Ranking
    ↓
Search Results
```

---

# 2. Advanced Text Analysis

This module performs operations beyond basic document retrieval.

### Z Algorithm

The Z Algorithm calculates the length of the longest substring beginning at each position that matches the prefix of the string.

It can be applied to pattern-processing tasks.

Conceptually:

```text
Pattern + Separator + Text
            ↓
       Z Array
            ↓
      Pattern Matches
```

### Aho-Corasick

Aho-Corasick is designed for matching multiple patterns simultaneously.

Example query set:

```text
AI
Machine Learning
NLP
Computer Vision
```

Instead of independently searching the entire document for each pattern, the patterns can be represented in a trie-based automaton with failure links.

### Suffix Array

A suffix array stores suffix starting positions in sorted order.

It provides a foundation for efficient substring-oriented operations.

### LCP

LCP stands for **Longest Common Prefix**.

The LCP structure records the length of the common prefix between adjacent sorted suffixes.

It can help identify repeated or related portions of text.

### Similarity Analysis

Documents can be compared using text-based similarity techniques.

The goal is to determine whether two documents contain significantly overlapping content.

### Duplicate Detection

Duplicate detection uses similarity information to identify:

* identical documents;
* near-duplicates;
* highly similar documents.

---

# 3. Analytics and Visualization

The third module focuses on analyzing results and making algorithmic behavior easier to understand.

## Edit Distance

Edit Distance measures the minimum number of operations required to transform one string into another.

Typical operations include:

* insertion;
* deletion;
* substitution.

Example:

```text
cat → cut
```

Only one substitution is required.

## LCS

LCS stands for **Longest Common Subsequence**.

It identifies the longest sequence of elements that appears in two strings while maintaining their order.

It can be useful for comparing textual content.

## Recommendation

The recommendation component can identify documents related to a user's search or selected research document.

## Citation Graph

Research papers can be represented as graph nodes.

A citation relationship can be represented as an edge.

```text
Paper A ─────► Paper B
   │
   └─────────► Paper C
```

This allows relationships between papers to be represented visually.

## Benchmark Dashboard

The benchmark component is intended to compare algorithm performance under different input conditions.

Possible measurements include:

* execution time;
* input size;
* number of matches;
* relative algorithm performance.

## Algorithm Visualizer

The visualizer is designed to demonstrate algorithm execution step by step.

For example, a KMP visualization can show:

```text
TEXT:
ABABCABAB

PATTERN:
ABAB

Comparison:
A B A B C A B A B
A B A B
        ↑
      mismatch

LPS information
        ↓
Continue without restarting from the beginning
```

---

# Algorithms

| Algorithm / Structure | Main Purpose in StrataSearch         |
| --------------------- | ------------------------------------ |
| **KMP**               | Exact pattern matching               |
| **Rabin-Karp**        | Hash-based pattern matching          |
| **Z Algorithm**       | Pattern preprocessing and matching   |
| **Aho-Corasick**      | Multiple-pattern matching            |
| **Suffix Array**      | Efficient suffix-based text analysis |
| **LCP**               | Common-prefix analysis               |
| **Edit Distance**     | Text difference measurement          |
| **LCS**               | Common-sequence analysis             |
| **Graph Models**      | Citation and document relationships  |

---

# How the Search Works

A typical search operation follows these stages:

### Step 1 — Document Collection

Research documents are provided to the system.

```text
Paper 1
Paper 2
Paper 3
Paper 4
...
```

### Step 2 — Query

The user enters a search query.

Example:

```text
machine learning
```

### Step 3 — Query Processing

The system identifies the search type and prepares the query for matching.

### Step 4 — String Matching

The Search Engine Core applies appropriate algorithms.

For exact pattern matching:

```text
KMP
```

or

```text
Rabin-Karp
```

### Step 5 — Match Collection

The system records matching documents and relevant match information.

### Step 6 — Ranking

Documents are scored according to the defined relevance criteria.

### Step 7 — Results

Ranked documents are returned to the user.

---

# Search Engine Workflow

```text
             ┌──────────────┐
             │ User Query   │
             └──────┬───────┘
                    ↓
          ┌──────────────────┐
          │ Query Processing │
          └────────┬─────────┘
                   ↓
       ┌────────────────────────┐
       │ Pattern / Phrase Search│
       └───────────┬────────────┘
                   ↓
          ┌─────────────────┐
          │ KMP / Rabin-Karp│
          └────────┬────────┘
                   ↓
          ┌─────────────────┐
          │ Matching Docs   │
          └────────┬────────┘
                   ↓
          ┌─────────────────┐
          │ Relevance Score │
          └────────┬────────┘
                   ↓
          ┌─────────────────┐
          │ Ranked Results  │
          └─────────────────┘
```

---

# Document Analysis Workflow

After retrieval, the documents can be passed to the analysis layer.

```text
Search Results
      ↓
Text Extraction
      ↓
Pattern Analysis
      ↓
Similarity Analysis
      ↓
Duplicate Detection
      ↓
Citation / Relationship Analysis
      ↓
Analytics & Visualization
```

This separation keeps the search engine focused on retrieval while allowing the analysis layer to perform deeper operations.

---

# Relevance Ranking

Finding a match does not automatically mean that a document is the best result.

StrataSearch therefore includes a relevance-ranking stage.

A relevance score can consider factors such as:

* number of query matches;
* location of matches;
* phrase matches;
* document-level match information.

Conceptually:

```text
Document
    ↓
Match Information
    ↓
Relevance Score
    ↓
Sort by Score
    ↓
Ranked Results
```

The exact ranking formula can be refined as the implementation evolves.

---

# Similarity and Duplicate Detection

Similarity analysis compares the textual content of documents.

Example:

```text
Document A
"Machine learning is used in healthcare..."

Document B
"Machine learning is widely used in healthcare..."
```

The documents are not identical, but they contain related content.

The system can therefore distinguish between:

```text
Exact Duplicate
      ↓
Near Duplicate
      ↓
Highly Similar
      ↓
Different
```

Similarity analysis can use techniques such as Edit Distance and LCS as part of the analysis layer.

---

# Citation Graph

A citation graph represents relationships between research papers.

Each paper is represented as a node.

A citation is represented as a directed edge.

Example:

```text
       ┌─────────┐
       │ Paper A │
       └────┬────┘
            │ cites
            ▼
       ┌─────────┐
       │ Paper B │
       └────┬────┘
            │ cites
            ▼
       ┌─────────┐
       │ Paper C │
       └─────────┘
```

This representation can help users understand how papers are connected within a research area.

---

# Algorithm Visualization

A key aspect of StrataSearch is making algorithmic operations visible rather than treating the search engine as a black box.

For example, the KMP visualizer can demonstrate:

1. Pattern and text alignment.
2. Character comparisons.
3. Mismatches.
4. LPS-based shifts.
5. Successful matches.

Similarly, other algorithms can be visualized according to their individual processing steps.

The objective is to make DSA concepts understandable through a practical document-search application.

---

# Performance Benchmarking

Different algorithms can behave differently depending on:

* text length;
* pattern length;
* number of documents;
* number of patterns;
* input distribution.

StrataSearch includes a benchmarking direction to compare algorithm behavior.

Example:

```text
Input Size
    │
    ├── Small
    ├── Medium
    └── Large
          ↓
┌─────────────────────────┐
│ KMP                     │
│ Rabin-Karp              │
│ Z Algorithm             │
│ Aho-Corasick             │
└────────────┬────────────┘
             ↓
       Performance Data
             ↓
      Benchmark Dashboard
```

The benchmark results can be used to understand the practical behavior of different algorithms.

---

# Complexity Overview

The following table summarizes the commonly associated complexity characteristics of the algorithms used in the project.

| Algorithm         |                          Typical Time Complexity |                            Space Complexity |
| ----------------- | -----------------------------------------------: | ------------------------------------------: |
| **KMP**           |                                         O(n + m) |                                        O(m) |
| **Rabin-Karp**    |              O(n + m) expected; O(nm) worst case |                              O(1) auxiliary |
| **Z Algorithm**   |                                         O(n + m) |                                    O(n + m) |
| **Aho-Corasick**  |       O(n + output) after automaton construction |                        Depends on automaton |
| **Suffix Array**  |                   Construction depends on method |                    O(n) or method-dependent |
| **LCP**           | O(n) after suffix array in standard construction |                                        O(n) |
| **Edit Distance** |                          O(nm) using standard DP | O(nm), reducible for distance-only variants |
| **LCS**           |                          O(nm) using standard DP |   O(nm), reducible for length-only variants |

Where:

* `n` = text length
* `m` = pattern length

Actual performance can vary with implementation, input characteristics, preprocessing, and output size.

---

# Research Gap

## Existing Systems

Academic platforms such as:

* Google Scholar
* Semantic Scholar
* ResearchGate

provide strong capabilities for:

* academic discovery;
* document retrieval;
* recommendations;
* citation information;
* research sharing.

## Identified Gap

The underlying algorithmic processing is generally not presented to users as an integrated DSA learning and analysis workflow.

There is a gap in combining:

```text
Efficient Search
      +
Advanced Text Analysis
      +
Similarity / Duplicate Detection
      +
Citation Analytics
      +
Algorithm Visualization
      +
Performance Benchmarking
```

into one platform.

## StrataSearch

StrataSearch is designed to address this gap by integrating these capabilities into a unified document-search and analysis environment.

---

# Existing Systems Studied

## Google Scholar

Used for:

* scholarly search;
* academic discovery;
* citation tracking;
* related publications.

## Semantic Scholar

Used for:

* semantic research discovery;
* paper recommendations;
* paper-level insights;
* citation relationships.

## ResearchGate

Used for:

* research discovery;
* publication sharing;
* citation information;
* academic networking.

### Key Observation

The existing systems demonstrate strong research-discovery capabilities, while StrataSearch focuses on combining **retrieval with algorithmic text analysis and explainability**.

---

# Project Scope

StrataSearch focuses on the following areas:

### In Scope

* Research-document search
* Exact pattern matching
* Phrase search
* Relevance ranking
* Multiple-pattern processing
* Text analysis
* Similarity analysis
* Duplicate detection
* Citation relationships
* Algorithm visualization
* Performance benchmarking

### Out of Scope

* Semantic natural language understanding (LLM-based vector embedding search) — StrataSearch is strictly focused on deterministic Data Structures & Algorithms (DSA).
* Web crawling or live external internet scrapers.
* Distributed computing cluster orchestration (Spark/Hadoop).
* **Boyer-Moore Algorithm** — Strictly excluded from the algorithmic search engine. StrataSearch standardizes on KMP, Rabin-Karp, and Z-Algorithm for single-pattern matching, and Aho-Corasick / Suffix Array for multi-pattern and indexed search.

---

# Team Organization & Multi-Stack Contribution Matrix

**Academic Course:** Data Structures and Algorithms III (DSA-3)  
**Group Identification:** Group 7 (A-7_DSA-3_G-7)  
**Team Size:** 3 Members  

To ensure comprehensive engineering and balanced algorithmic contribution, every team member has dedicated ownership of core DSA algorithms while contributing across backend and frontend engineering:

```text
┌──────────────────────────────────────────────────────────────────────────────────────────────────┐
│                                BALANCED MULTI-STACK RESPONSIBILITY                               │
├─────────────────────────┬──────────────────────────────────┬─────────────────────────────────────┤
│ Member 1 (Sashank)      │ Member 2 (Rithvik)               │ Member 3 (Arnavi)                   │
├─────────────────────────┼──────────────────────────────────┼─────────────────────────────────────┤
│ DSA ALGORITHMS (CORE):  │ DSA ALGORITHMS (CORE):           │ DSA ALGORITHMS (CORE):              │
│ • KMP (Knuth-Morris-    │ • Rabin-Karp (Rolling hash,      │ • Z-Algorithm (Linear fundamental   │
│   Pratt) with π-table   │   polynomial arithmetic, mod)    │   box preprocessing & matching)     │
│ • Aho-Corasick Automaton│ • Suffix Array Construction      │ • SearchStepSink & VisualizationStep│
│   (Trie + failure links)│   & Binary Search on suffixes    │   state tracking engine             │
│ • QueryPlanner & Cost-  │                                  │ • TextNormalizer & IntSortUtil      │
│   Model heuristic engine│                                  │                                     │
├─────────────────────────┼──────────────────────────────────┼─────────────────────────────────────┤
│ BACKEND CONTRIBUTION:   │ BACKEND CONTRIBUTION:            │ BACKEND CONTRIBUTION:               │
│ • Parent multi-module   │ • Spring Boot app configuration  │ • VisualizationController & Service │
│   POM & EngineGateway   │ • Document upload (PDFBox/text)  │ • BenchmarkController & Service     │
│ • CoreAdapter & Search- │ • CorpusSession & SessionStore   │ • REST DTOs & Mapping Layer         │
│   Service execution     │ • Asynchronous JobExecutor &     │   (Benchmark/Visualization DTOs)    │
│ • SearchController REST │   WebSocket streaming handler    │                                     │
├─────────────────────────┼──────────────────────────────────┼─────────────────────────────────────┤
│ FRONTEND CONTRIBUTION:  │ FRONTEND CONTRIBUTION:           │ FRONTEND CONTRIBUTION:              │
│ • PlannerPanel (Planner │ • WebSocket client (jobSocket.ts)│ • Vite + React + Tailwind workspace │
│   decision tree & costs)│ • HTTP networking layer (http.ts)│ • FoldcraftHero & Workspace layout  │
│ • Zustand labStore      │ • JobProgressFeed (live streaming│ • VisualizationPlayer (interactive  │
│   coordinating planner  │   progress bar & status events)  │   Play/Pause/Step/Speed controls)   │
│   and algorithm state   │                                  │ • TelemetryPanel & BenchmarkPanel   │
│                         │                                  │ • CitationGraphPanel (network view) │
└─────────────────────────┴──────────────────────────────────┴─────────────────────────────────────┘
```

---

# 4-Review Trajectory & Milestone Roadmap

The project development lifecycle is structured across four rigorous academic evaluations:

```text
Review 1 (20%) ────────► Review 2 (45%) ────────► Review 3 (75% - Current) ────────► Review 4 (100% - Next)
[Formulation & Trajectory]  [Core DSA & Base APIs]   [Advanced Indexing & Workspace]   [Corpus Profiler & Final]
```

### Review 1: Trajectory Formulation & System Design (Completed)
* **Target:** Problem definition, algorithmic baseline, and multi-module foundation (~20%).
* **Member 1 (Sashank):** Designed algorithm interfaces (`SearchAlgorithm`, `SearchResult`, `SearchStepSink`), multi-module parent POM, and system architecture.
* **Member 2 (Rithvik):** Initialized Spring Boot 3.3 skeleton, dependency matrix, and basic application properties.
* **Member 3 (Arnavi):** Scaffolded Vite React TypeScript workspace, Tailwind CSS build pipeline, and routing shell.

### Review 2: Core DSA Implementations & Base Pipeline (Completed)
* **Target:** Single-pattern search algorithms, REST services, and initial UI workbench (~45%).
* **Member 1 (Sashank):** 
  * Implemented KMP searcher (with $\pi$-table tracking).
  * Built `EngineGateway`, `CoreAdapter`, and `SearchController` backend execution pipeline.
  * Configured central Zustand `labStore.ts` state management.
* **Member 2 (Rithvik):** 
  * Implemented Rabin-Karp rolling hash searcher with collision resolution.
  * Implemented session store (`CorpusSession`, `SessionStore`) and PDF/text ingestion via Apache PDFBox.
  * Implemented frontend HTTP API client (`api/http.ts`, `api/endpoints.ts`, `api/types.ts`).
* **Member 3 (Arnavi):** 
  * Implemented Z-Algorithm linear string searcher and `VisualizationStep` state emitter.
  * Implemented `AlgorithmController` and base response DTOs.
  * Built `FoldcraftHero` landing page, `WorkspacePage` layout, and `QueryPanel` / `CorpusPanel` UI.

### Review 3: Advanced Matching, Planner & Interactive Workbench (Current Milestone — 75%)
* **Target:** Multi-pattern indexing, dynamic query planning, WebSocket streaming, and interactive visualizer (~75%).
* **Member 1 (Sashank):** 
  * Implemented Aho-Corasick multi-string automaton (Trie + failure transitions + output matching).
  * Built the dynamic `QueryPlanner` with heuristic `CostModel` and explainability engine.
  * Implemented `PlannerPanel.tsx` (decision tree visualization, score ranking, confidence gauge).
* **Member 2 (Rithvik):** 
  * Implemented Suffix Array construction and binary search over suffixes.
  * Built asynchronous `JobExecutor` with thread-safe `JobRegistry` and native WebSocket streaming at `/ws/jobs/{jobId}`.
  * Built `api/jobSocket.ts` and `JobProgressFeed.tsx` for real-time WebSocket progress bars.
* **Member 3 (Arnavi):** 
  * Built backend `VisualizationController`, `VisualizationService`, `BenchmarkController`, and `BenchmarkService`.
  * Developed `VisualizationPlayer.tsx` with step-by-step playback controls (Play/Pause/Step/Speed).
  * Implemented live `TelemetryPanel.tsx`, `BenchmarkPanel.tsx`, and `CitationGraphPanel.tsx`.

### Review 4: Corpus Profiling, Similarity DSA & Final Submission (Next Review — Final 25%)
* **Target:** Deep corpus profiling, document similarity analysis, full test coverage, and deployment packaging (100%).
* **Member 1 (Sashank):**
  * **Corpus Profiling Engine:** Implement Shannon character entropy, Type-Token Ratio (TTR) vocabulary richness, and lexical density calculations.
  * **Corpus Fingerprinting Integration:** Connect document statistical profiles into the Query Planner for enhanced cost evaluation.
  * **Fuzzy & Similarity DSA:** Implement Levenshtein Edit Distance and Longest Common Subsequence (LCS).
  * **Core Test Suite:** Unit testing and edge-case benchmark validation.
* **Member 2 (Rithvik):**
  * **Corpus Profiling APIs:** Expose `/api/corpus/profile` and `/api/workflow/analyze` endpoints for asynchronous profiling.
  * **Document Similarity Service:** Pairwise similarity scoring and duplicate detection pipeline.
  * **Backend Test Suite:** MockMvc controller tests and WebSocket integration test suite.
  * **Containerization:** Production Dockerfile and root `docker-compose.yml`.
* **Member 3 (Arnavi):**
  * **Corpus Profiling Dashboard:** Visual gauges for Shannon entropy, lexical density meters, and vocabulary distribution histograms.
  * **Document Comparison View:** Side-by-side duplicate and similarity diff highlighter.
  * **3D Graph Physics Refinements:** Advanced citation clustering and interactive force-directed controls.
  * **Export Utilities & Shortcuts:** PDF/PNG benchmark report export and keyboard navigation controls.

---

# License
Academic Free License / Educational Use — DSA-3 Coursework (Group 7).

