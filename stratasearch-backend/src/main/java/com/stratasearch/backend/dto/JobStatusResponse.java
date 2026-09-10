package com.stratasearch.backend.dto;

import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.model.JobEvent;

import java.util.List;

public class JobStatusResponse {
    public String jobId;
    public String status;
    public String errorMessage;
    public Object result;
    public List<JobEvent> events;

    public JobStatusResponse(Job J) {
        this.jobId = J.GetJobId();
        this.status = J.GetStatus().name();
        this.errorMessage = J.GetErrorMessage();
        this.result = J.GetResult();
        this.events = J.GetEvents();
    }
}
