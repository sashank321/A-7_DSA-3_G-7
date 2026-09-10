package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.JobStatusResponse;
import com.stratasearch.backend.exception.ApiException;
import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.model.JobEvent;
import com.stratasearch.backend.model.JobRegistry;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
public class JobController {

    private final JobRegistry Registry;

    public JobController(JobRegistry Registry) {
        this.Registry = Registry;
    }

    @GetMapping("/{jobId}")
    public JobStatusResponse Status(@PathVariable("jobId") String JobId) {
        Job J = Registry.Find(JobId);
        if (J == null) throw new ApiException(404, "Job not found: " + JobId);
        return new JobStatusResponse(J);
    }

    @GetMapping("/{jobId}/events")
    public List<JobEvent> Events(@PathVariable("jobId") String JobId) {
        Job J = Registry.Find(JobId);
        if (J == null) throw new ApiException(404, "Job not found: " + JobId);
        return J.GetEvents();
    }
}
