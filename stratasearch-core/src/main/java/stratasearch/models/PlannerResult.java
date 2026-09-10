package stratasearch.models;

// Immutable result produced by QueryPlanner.
public class PlannerResult {
    private final String ChosenAlgorithm;
    private final PlannerReason PlannerReason;
    private final double ConfidencePercent;
    private final String EstimatedRuntime;
    private final String EstimatedMemory;
    private final String[] Alternatives;
    private final String[] AllCandidateNames;
    private final double[] AllCandidateScores;
    private final int CandidateCount;
    private final String[] DecisionSteps;

    public PlannerResult(String ChosenAlgorithm,
                         PlannerReason PlannerReason,
                         double ConfidencePercent,
                         String EstimatedRuntime,
                         String EstimatedMemory,
                         String[] Alternatives,
                         String[] AllCandidateNames,
                         double[] AllCandidateScores,
                         int CandidateCount,
                         String[] DecisionSteps) {
        this.ChosenAlgorithm = ChosenAlgorithm;
        this.PlannerReason = PlannerReason;
        this.ConfidencePercent = ConfidencePercent;
        this.EstimatedRuntime = EstimatedRuntime;
        this.EstimatedMemory = EstimatedMemory;
        this.Alternatives = Alternatives;
        this.AllCandidateNames = AllCandidateNames;
        this.AllCandidateScores = AllCandidateScores;
        this.CandidateCount = CandidateCount;
        this.DecisionSteps = DecisionSteps;
    }

    public String GetChosenAlgorithm() { return ChosenAlgorithm; }
    public PlannerReason GetPlannerReason() { return PlannerReason; }
    public String GetReason() { return PlannerReason.GetReason(); }
    public double GetConfidencePercent() { return ConfidencePercent; }
    public String GetEstimatedRuntime() { return EstimatedRuntime; }
    public String GetEstimatedMemory() { return EstimatedMemory; }

    public String[] GetAlternatives() { return Alternatives; }
    public int GetAlternativeCount() { return Alternatives.length; }
    public String GetAlternative(int I) { return Alternatives[I]; }

    public int GetCandidateCount() { return CandidateCount; }
    public String GetCandidateName(int I) { return AllCandidateNames[I]; }
    public double GetCandidateScore(int I) { return AllCandidateScores[I]; }

    public String[] GetDecisionSteps() { return DecisionSteps; }
    public int GetDecisionStepCount() { return DecisionSteps.length; }
    public String GetDecisionStep(int I) { return DecisionSteps[I]; }
}
