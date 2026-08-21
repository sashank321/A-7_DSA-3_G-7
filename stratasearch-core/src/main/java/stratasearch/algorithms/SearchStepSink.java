package stratasearch.algorithms;

// Callback interface so algorithms can stream their internal pointer state without
// breaking the strict no-import core rule. Capped at MaxSteps to bound memory.
public interface SearchStepSink {
    int MaxSteps = 5000;
    void OnStep(int StepIndex, String Kind, long TextPointer, long PatternPointer, String Detail);
}
