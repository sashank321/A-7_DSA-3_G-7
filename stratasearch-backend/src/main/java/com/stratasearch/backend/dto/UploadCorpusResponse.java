package com.stratasearch.backend.dto;

// Response after a successful corpus upload.
public class UploadCorpusResponse {
    private String sessionId;
    private int documentCount;
    private int characterCount;

    public UploadCorpusResponse(String sessionId, int documentCount, int characterCount) {
        this.sessionId = sessionId;
        this.documentCount = documentCount;
        this.characterCount = characterCount;
    }

    public String getSessionId() { return sessionId; }
    public int getDocumentCount() { return documentCount; }
    public int getCharacterCount() { return characterCount; }
}
