package com.stratasearch.backend.dto;

// Liveness/readiness endpoint.
public class HealthResponse {
    public String status;
    public String core;
    public String planner;
    public int algorithms;
    public int activeSessions;
}
