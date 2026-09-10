package com.stratasearch.backend.exception;

public class ApiException extends RuntimeException {
    private final int Status;

    public ApiException(int Status, String Message) {
        super(Message);
        this.Status = Status;
    }

    public int GetStatus() {
        return Status;
    }
}
