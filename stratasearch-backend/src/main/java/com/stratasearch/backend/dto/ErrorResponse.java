package com.stratasearch.backend.dto;

// Uniform error body produced by GlobalExceptionHandler.
public class ErrorResponse {
    public long timestamp;
    public int status;
    public String error;
    public String message;
    public String path;

    public ErrorResponse(long timestamp, int status, String error, String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
