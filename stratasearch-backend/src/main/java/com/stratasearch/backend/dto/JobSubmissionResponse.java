package com.stratasearch.backend.dto;

public class JobSubmissionResponse {
    public String jobId;
    public String status;
    public String eventsUrl;
    public String resultUrl;

    public JobSubmissionResponse(String JobId) {
        this.jobId = JobId;
        this.status = "QUEUED";
        this.eventsUrl = "/api/v1/jobs/" + JobId + "/events";
        this.resultUrl = "/api/v1/jobs/" + JobId;
    }
}
