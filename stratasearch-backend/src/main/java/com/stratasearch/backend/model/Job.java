package com.stratasearch.backend.model;

// One asynchronous analysis job. Lifecycle managed by JobRegistry + JobExecutor.
public class Job {
    public enum Status { QUEUED, RUNNING, SUCCEEDED, FAILED }

    private final String JobId;
    private final String SessionId;
    private final String[] Patterns;
    private final boolean Repeated;
    private final java.util.List<JobEvent> Events = new java.util.concurrent.CopyOnWriteArrayList<>();
    private volatile Status CurrentStatus = Status.QUEUED;
    private volatile Object Result;    // WorkflowAnalyzeResponse once complete
    private volatile String ErrorMessage;

    public Job(String JobId, String SessionId, String[] Patterns, boolean Repeated) {
        this.JobId = JobId;
        this.SessionId = SessionId;
        this.Patterns = Patterns;
        this.Repeated = Repeated;
    }

    public String GetJobId() { return JobId; }
    public String GetSessionId() { return SessionId; }
    public String[] GetPatterns() { return Patterns; }
    public boolean IsRepeated() { return Repeated; }

    public Status GetStatus() { return CurrentStatus; }
    public void SetStatus(Status S) { CurrentStatus = S; }

    public Object GetResult() { return Result; }
    public void SetResult(Object R) { Result = R; }

    public String GetErrorMessage() { return ErrorMessage; }
    public void SetErrorMessage(String M) { ErrorMessage = M; }

    public java.util.List<JobEvent> GetEvents() {
        return new java.util.ArrayList<>(Events);
    }

    public void AddEvent(JobEvent E) {
        Events.add(E);
    }
}
