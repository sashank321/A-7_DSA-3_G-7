package stratasearch.models;

// Immutable structured reason for a planner recommendation (replaces free-form string).
public class PlannerReason {
    private final String Algorithm;
    private final String Reason;
    private final String[] Advantages;
    private final String[] TradeOffs;
    private final double ConfidencePercent;
    private final String[] RecommendedBecause;
    private final String[] AvoidedBecause;

    public PlannerReason(String Algorithm,
                         String Reason,
                         String[] Advantages,
                         String[] TradeOffs,
                         double ConfidencePercent,
                         String[] RecommendedBecause,
                         String[] AvoidedBecause) {
        this.Algorithm = Algorithm;
        this.Reason = Reason;
        this.Advantages = Advantages;
        this.TradeOffs = TradeOffs;
        this.ConfidencePercent = ConfidencePercent;
        this.RecommendedBecause = RecommendedBecause;
        this.AvoidedBecause = AvoidedBecause;
    }

    public String GetAlgorithm() { return Algorithm; }
    public String GetReason() { return Reason; }
    public double GetConfidencePercent() { return ConfidencePercent; }

    public String[] GetAdvantages() { return Advantages; }
    public int GetAdvantageCount() { return Advantages.length; }
    public String GetAdvantage(int I) { return Advantages[I]; }

    public String[] GetTradeOffs() { return TradeOffs; }
    public int GetTradeOffCount() { return TradeOffs.length; }
    public String GetTradeOff(int I) { return TradeOffs[I]; }

    public String[] GetRecommendedBecause() { return RecommendedBecause; }
    public int GetRecommendedBecauseCount() { return RecommendedBecause.length; }
    public String GetRecommendedBecause(int I) { return RecommendedBecause[I]; }

    public String[] GetAvoidedBecause() { return AvoidedBecause; }
    public int GetAvoidedBecauseCount() { return AvoidedBecause.length; }
    public String GetAvoidedBecause(int I) { return AvoidedBecause[I]; }
}
