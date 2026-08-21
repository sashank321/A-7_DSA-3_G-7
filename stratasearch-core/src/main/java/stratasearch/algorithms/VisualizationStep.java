package stratasearch.algorithms;

// One recorded step produced by algorithms for the /api/v1/visualize endpoint.
public class VisualizationStep {
    public final int StepIndex;
    public final String Kind;
    public final long TextPointer;
    public final long PatternPointer;
    public final String Detail;

    public VisualizationStep(int StepIndex, String Kind, long TextPointer, long PatternPointer, String Detail) {
        this.StepIndex = StepIndex;
        this.Kind = Kind;
        this.TextPointer = TextPointer;
        this.PatternPointer = PatternPointer;
        this.Detail = Detail;
    }
}
