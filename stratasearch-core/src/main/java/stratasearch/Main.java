package stratasearch;

import java.util.Scanner;
import stratasearch.algorithms.AhoCorasickSearcher;
import stratasearch.algorithms.KmpSearcher;
import stratasearch.algorithms.RabinKarpSearcher;
import stratasearch.algorithms.SearchResult;
import stratasearch.algorithms.SuffixArraySearcher;
import stratasearch.algorithms.ZAlgorithmSearcher;
import stratasearch.engine.AlgorithmRegistry;
import stratasearch.engine.BenchmarkEngine;
import stratasearch.engine.SearchEngine;
import stratasearch.models.BenchmarkResult;
import stratasearch.models.CorpusFingerprint;
import stratasearch.models.PlannerResult;
import stratasearch.models.TelemetryRecord;
import stratasearch.planner.ExplainabilityEngine;
import stratasearch.planner.QueryPlanner;
import stratasearch.profiler.CorpusProfiler;
import stratasearch.util.ManualStringList;
import stratasearch.util.TextNormalizer;

public class Main {

    private static String[] CorpusDocuments = null;
    private static CorpusFingerprint CurrentFingerprint = null;
    private static PlannerResult CurrentPlan = null;
    private static TelemetryRecord LastTelemetry = null;
    private static BenchmarkResult LastBenchmarkResult = null;
    private static String[] CurrentPatterns = null;
    private static int NextCorpusId = 1;
    private static AlgorithmRegistry Registry = null;
    private static SearchEngine Engine = null;
    private static BenchmarkEngine Bench = null;

    private static void BootstrapEngine() {
        Registry = new AlgorithmRegistry();
        Registry.Register(new KmpSearcher());
        Registry.Register(new RabinKarpSearcher());
        Registry.Register(new ZAlgorithmSearcher());
        Registry.Register(new AhoCorasickSearcher());
        Registry.Register(new SuffixArraySearcher());
        Engine = new SearchEngine(Registry);
        Bench = new BenchmarkEngine();
    }

    private static String PadInt(int V, int Width) {
        String S = "" + V;
        StringBuilder Sb = new StringBuilder();
        while (Sb.length() + S.length() < Width) {
            Sb.append('0');
        }
        Sb.append(S);
        return Sb.toString();
    }

    private static void PrintBanner() {
        System.out.println("==========================================================");
        System.out.println("  StrataSearch - Text Analytics Laboratory  (Phase 1)     ");
        System.out.println("  Pure manual Java DSA engine. No collections. No libs.   ");
        System.out.println("==========================================================");
    }

    private static void PrintMenu() {
        System.out.println();
        System.out.println("------------------- MAIN MENU -------------------");
        System.out.println(" 1. Load sample corpus");
        System.out.println(" 2. Load custom corpus (multi-document)");
        System.out.println(" 3. Profile corpus (build fingerprint)");
        System.out.println(" 4. Show current corpus fingerprint");
        System.out.println(" 5. Plan query (get recommendation)");
        System.out.println(" 6. Explain recommendation");
        System.out.println(" 7. Execute recommended search");
        System.out.println(" 8. Benchmark ALL algorithms on current patterns");
        System.out.println(" 9. Run built-in test suite");
        System.out.println("10. List registered algorithms");
        System.out.println(" 0. Exit");
        System.out.print("Enter choice: ");
    }

    private static void LoadSampleCorpus() {
        CorpusDocuments = new String[]{
                "Data structures and algorithms form the backbone of computer science.",
                "Search algorithms like KMP and Rabin Karp scan text for exact matches.",
                "Aho Corasick matches many patterns at once using a trie with failure links.",
                "Suffix arrays speed up repeated search on the same corpus of text data.",
                "The planner fingerprints the corpus and recommends the best algorithm.",
                "Text analytics measures vocabulary richness, entropy, and repetition.",
                "Benchmarks record runtime, memory, and comparisons for each algorithm run.",
                "The corpus containsSearch words like algorithms, data, text, pattern, and search."
        };
        CurrentFingerprint = null;
        CurrentPlan = null;
        LastTelemetry = null;
        System.out.println("Sample corpus loaded: " + CorpusDocuments.length + " documents.");
    }

    private static Scanner InputScanner = null;

    // Single shared scanner so stdin stays open for scripted/interactive runs.
    private static Scanner GetScanner() {
        if (InputScanner == null) {
            InputScanner = new Scanner(System.in);
        }
        return InputScanner;
    }

    private static void LoadCustomCorpus() {
        Scanner Sc = GetScanner();
        System.out.print("How many documents? ");
        int Count = Integer.parseInt(Sc.nextLine().trim());
        String[] Docs = new String[Count];
        for (int I = 0; I < Count; I++) {
            System.out.println("Document " + (I + 1) + " (type text, end with a single line containing only ###):");
            StringBuilder Sb = new StringBuilder();
            while (true) {
                String Line = Sc.nextLine();
                if ("###".equals(Line)) {
                    break;
                }
                if (Sb.length() > 0) {
                    Sb.append('\n');
                }
                Sb.append(Line);
            }
            Docs[I] = Sb.toString();
        }
        CorpusDocuments = Docs;
        CurrentFingerprint = null;
        CurrentPlan = null;
        LastTelemetry = null;
        System.out.println("Custom corpus loaded: " + Count + " documents.");
    }

    private static boolean RequireCorpus() {
        if (CorpusDocuments == null || CorpusDocuments.length == 0) {
            System.out.println("No corpus loaded. Use option 1 or 2 first.");
            return false;
        }
        return true;
    }

    private static void ProfileCorpus() {
        if (!RequireCorpus()) {
            return;
        }
        Scanner Sc = GetScanner();
        System.out.print("Will you search with MULTIPLE patterns at once? (yes/no): ");
        String Multi = Sc.nextLine().trim();
        System.out.print("Will you REPEAT queries on this same corpus many times? (yes/no): ");
        String Repeat = Sc.nextLine().trim();
        boolean MultiIntent = "yes".equalsIgnoreCase(Multi) || "y".equalsIgnoreCase(Multi);
        boolean RepeatIntent = "yes".equalsIgnoreCase(Repeat) || "y".equalsIgnoreCase(Repeat);
        CorpusProfiler Profiler = new CorpusProfiler();
        String Id = "CORPUS-" + PadInt(NextCorpusId, 3);
        NextCorpusId++;
        CurrentFingerprint = Profiler.Profile(CorpusDocuments, Id, MultiIntent, RepeatIntent);
        CurrentPlan = null;
        System.out.println("Fingerprint built.");
        System.out.println(CurrentFingerprint.ToString());
    }

    private static void ShowFingerprint() {
        if (CurrentFingerprint == null) {
            System.out.println("No fingerprint yet. Run option 3 first.");
            return;
        }
        System.out.println(CurrentFingerprint.ToString());
    }

    private static String[] PromptPatterns() {
        Scanner Sc = GetScanner();
        System.out.print("Enter patterns separated by commas (example: data,algorithm): ");
        String Line = Sc.nextLine();
        ManualStringList Raw = TextNormalizer.SplitComma(Line);
        ManualStringList Clean = new ManualStringList();
        for (int I = 0; I < Raw.GetSize(); I++) {
            String Trimmed = TextNormalizer.Normalize(TextNormalizer.Trim(Raw.Get(I)));
            ManualStringList Words = TextNormalizer.SplitWords(Trimmed);
            if (Words.GetSize() > 0) {
                // Multi-word pattern: join with single space.
                StringBuilder Sb = new StringBuilder();
                for (int W = 0; W < Words.GetSize(); W++) {
                    if (W > 0) {
                        Sb.append(' ');
                    }
                    Sb.append(Words.Get(W));
                }
                Clean.Add(Sb.toString());
            }
        }
        return Clean.ToArray();
    }

    private static void PlanQuery() {
        if (CurrentFingerprint == null) {
            System.out.println("No fingerprint yet. Run option 3 first.");
            return;
        }
        String[] Patterns = PromptPatterns();
        if (Patterns.length == 0) {
            System.out.println("No valid patterns entered.");
            return;
        }
        CurrentPatterns = Patterns;
        Scanner Sc = GetScanner();
        System.out.print("Plan assumes repeated querying of the SAME corpus? (yes/no): ");
        String Repeat = Sc.nextLine().trim();
        boolean Repeated = "yes".equalsIgnoreCase(Repeat) || "y".equalsIgnoreCase(Repeat);
        QueryPlanner Planner = new QueryPlanner();
        CurrentPlan = Planner.Plan(CurrentFingerprint, Patterns, Repeated, 0.0);
        System.out.println();
        System.out.println("Recommended algorithm : " + CurrentPlan.GetChosenAlgorithm());
        System.out.println("Estimated runtime     : " + CurrentPlan.GetEstimatedRuntime());
        System.out.println("Estimated memory      : " + CurrentPlan.GetEstimatedMemory());
        System.out.println("Reason                : " + CurrentPlan.GetReason());
    }

    private static void ExplainRecommendation() {
        if (CurrentPlan == null) {
            System.out.println("No plan yet. Run option 5 first.");
            return;
        }
        ExplainabilityEngine Explain = new ExplainabilityEngine();
        System.out.println(Explain.Explain(CurrentPlan));
    }

    private static void ExecuteSearch() {
        if (!RequireCorpus()) {
            return;
        }
        if (CurrentPlan == null || CurrentPatterns == null) {
            System.out.println("No plan yet. Run option 5 first.");
            return;
        }
        String Joined = TextNormalizer.JoinDocuments(CorpusDocuments);
        stratasearch.algorithms.SearchAlgorithm Algo = Engine.Resolve(CurrentPlan.GetChosenAlgorithm());
        if (Algo == null) {
            System.out.println("Unknown algorithm: " + CurrentPlan.GetChosenAlgorithm());
            return;
        }
        TelemetryRecord Telemetry = Bench.Measure(Algo, Joined, CurrentPatterns);
        LastTelemetry = Telemetry;
        LastBenchmarkResult = Bench.MeasureBenchmark(Algo, Joined, CurrentPatterns);
        System.out.println();
        System.out.println(Algo.SupportsMultiplePatterns()
                ? "Executed with multi-pattern native support."
                : "Executed as per-pattern loop over all " + CurrentPatterns.length + " pattern(s).");
        System.out.println(Telemetry.ToString());
    }

    private static void BenchmarkAll() {
        if (!RequireCorpus()) {
            return;
        }
        if (CurrentPatterns == null || CurrentPatterns.length == 0) {
            System.out.println("No patterns yet. Run option 5 first.");
            return;
        }
        String Joined = TextNormalizer.JoinDocuments(CorpusDocuments);
        String[] Names = Engine.GetAllNames();
        System.out.println();
        System.out.println("===== Benchmark: all algorithms on current workload =====");
        System.out.println("Patterns: " + CurrentPatterns.length + "   Corpus chars: " + Joined.length());
        System.out.println("----------------------------------------------------------------------------------");
        System.out.printf("%-20s %12s %12s %10s %8s   %-32s%n", "Algorithm", "Time(ns)", "Memory(B)", "Compares", "Matches", "Complexity");
        System.out.println("----------------------------------------------------------------------------------");
        for (int I = 0; I < Names.length; I++) {
            stratasearch.algorithms.SearchAlgorithm Algo = Engine.Resolve(Names[I]);
            BenchmarkResult BR = Bench.MeasureBenchmark(Algo, Joined, CurrentPatterns);
            System.out.printf("%-20s %12d %12d %10d %8d   %-32s%n",
                    BR.GetAlgorithmName(),
                    BR.GetExecutionTimeNanos(),
                    BR.GetMemoryUsedBytes(),
                    BR.GetComparisonCount(),
                    BR.GetMatchCount(),
                    BR.GetComplexity());
        }
        System.out.println("----------------------------------------------------------------------------------");
    }

    private static void ListRegisteredAlgorithms() {
        System.out.println("===== Registered Algorithms =====");
        int Count = Registry.GetCount();
        for (int I = 0; I < Count; I++) {
            stratasearch.algorithms.SearchAlgorithm A = Registry.GetAt(I);
            System.out.println(" " + (I + 1) + ". " + A.GetName()
                    + " | time=" + A.GetTimeComplexity()
                    + " | mem=" + A.GetMemoryUsage()
                    + " | multi=" + A.SupportsMultiplePatterns()
                    + " | repeated=" + A.SupportsRepeatedQueries()
                    + " | index=" + A.SupportsIndexing());
        }
    }

    private static void RunTests() {
        System.out.println("===== Built-in Test Suite =====");
        String T = "abracadabra abra cadabra";
        String[] One = new String[]{"abra"};

        String[] Names = Engine.GetAllNames();
        for (int I = 0; I < Names.length; I++) {
            SearchResult R = Engine.Execute(Names[I], T, One);
            System.out.println("[T1] " + Names[I] + " -> matches=" + R.GetMatchCount()
                    + " at " + R.PositionsToString());
        }

        String[] Multi = new String[]{"abra", "cad"};
        SearchResult MultiResult = Engine.Execute("Aho-Corasick", T, Multi);
        System.out.println("[T2] Aho-Corasick multi -> matches=" + MultiResult.GetMatchCount()
                + " (pattern0=" + MultiResult.GetPerPatternMatchCount(0)
                + ", pattern1=" + MultiResult.GetPerPatternMatchCount(1) + ")");

        String Present = "Data structures and algorithms form the backbone of computer science. "
                + "Algorithms and data structures matter.";
        String[] Single = new String[]{"data structures"};
        SearchResult SaResult = Engine.Execute("Suffix Array + LCP", Present, Single);
        System.out.println("[T3] Suffix Array \"data structures\" -> "
                + SaResult.GetMatchCount() + " matches at " + SaResult.PositionsToString());

        SearchResult None = Engine.Execute("KMP", "hello world", new String[]{"zzz"});
        System.out.println("[T4] KMP no-match -> " + None.GetMatchCount() + " (expected 0)");

        SearchResult EmptyPat = Engine.Execute("KMP", "hello world", new String[]{""});
        System.out.println("[T5] KMP empty-pattern -> " + EmptyPat.GetMatchCount() + " (expected 0)");
        System.out.println("===== Test Suite Done =====");
    }

    public static void main(String[] Args) {
        BootstrapEngine();
        Scanner Sc = GetScanner();
        PrintBanner();
        int Choice = -1;
        do {
            PrintMenu();
            String Line = Sc.nextLine().trim();
            if (Line.length() == 0) {
                continue;
            }
            try {
                Choice = Integer.parseInt(Line);
            } catch (NumberFormatException Ex) {
                System.out.println("Please enter a number from the menu.");
                Choice = -1;
                continue;
            }
            switch (Choice) {
                case 1:
                    LoadSampleCorpus();
                    break;
                case 2:
                    LoadCustomCorpus();
                    break;
                case 3:
                    ProfileCorpus();
                    break;
                case 4:
                    ShowFingerprint();
                    break;
                case 5:
                    PlanQuery();
                    break;
                case 6:
                    ExplainRecommendation();
                    break;
                case 7:
                    ExecuteSearch();
                    break;
                case 8:
                    BenchmarkAll();
                    break;
                case 9:
                    RunTests();
                    break;
                case 10:
                    ListRegisteredAlgorithms();
                    break;
                case 0:
                    System.out.println("Exiting StrataSearch. Goodbye.");
                    break;
                default:
                    System.out.println("Invalid choice.");
            }
        } while (Choice != 0);
    }
}
