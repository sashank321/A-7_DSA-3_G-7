package com.stratasearch.backend.model;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// In-memory job store; jobs are transient metadata (stateless engine).
@Component
public class JobRegistry {
    private final Map<String, Job> Jobs = new ConcurrentHashMap<>();

    public Job Create(String SessionId, String[] Patterns, boolean Repeated) {
        String Id = "job-" + UUID.randomUUID().toString().substring(0, 8);
        Job J = new Job(Id, SessionId, Patterns, Repeated);
        Jobs.put(Id, J);
        return J;
    }

    public Job Find(String JobId) {
        return Jobs.get(JobId);
    }

    public int ActiveCount() {
        int count = 0;
        for (Job j : Jobs.values()) {
            if (j.GetStatus() == Job.Status.QUEUED || j.GetStatus() == Job.Status.RUNNING) {
                count++;
            }
        }
        return count;
    }

    public boolean Exists(String JobId) {
        return Jobs.containsKey(JobId);
    }
}
