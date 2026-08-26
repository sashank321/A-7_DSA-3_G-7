package com.stratasearch.backend.model;

// Immutable session metadata for one uploaded corpus.
public class CorpusSession {
    private final String SessionId;
    private final String[] Documents;
    private final String JoinedText;
    private final long CreatedAtEpochMs;
    private volatile long LastTouchedEpochMs;
    private volatile stratasearch.models.CorpusFingerprint Fingerprint;

    public CorpusSession(String SessionId, String[] Documents, String JoinedText, long NowEpochMs) {
        this.SessionId = SessionId;
        this.Documents = Documents;
        this.JoinedText = JoinedText;
        this.CreatedAtEpochMs = NowEpochMs;
        this.LastTouchedEpochMs = NowEpochMs;
    }

    public String GetSessionId() { return SessionId; }
    public String[] GetDocuments() { return Documents; }
    public String GetJoinedText() { return JoinedText; }
    public long GetCreatedAtEpochMs() { return CreatedAtEpochMs; }
    public long GetLastTouchedEpochMs() { return LastTouchedEpochMs; }
    public void Touch() { LastTouchedEpochMs = System.currentTimeMillis(); }

    public stratasearch.models.CorpusFingerprint GetFingerprint() { return Fingerprint; }
    public void SetFingerprint(stratasearch.models.CorpusFingerprint Value) { Fingerprint = Value; }
}
