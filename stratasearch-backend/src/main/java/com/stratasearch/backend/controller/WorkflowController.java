package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.JobSubmissionResponse;
import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.dto.WorkflowAnalyzeResponse;
import com.stratasearch.backend.model.Job;
import com.stratasearch.backend.service.JobExecutor;
import com.stratasearch.backend.service.WorkflowService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workflow")
public class WorkflowController {

    private final WorkflowService Service;
    private final JobExecutor JobExecutor;

    public WorkflowController(WorkflowService Service, JobExecutor JobExecutor) {
        this.Service = Service;
        this.JobExecutor = JobExecutor;
    }

    @PostMapping("/analyze")
    public WorkflowAnalyzeResponse Analyze(@RequestBody PlannerRequest Req) {
        return Service.Analyze(Req);
    }

    @PostMapping("/analyze-async")
    public JobSubmissionResponse AnalyzeAsync(@RequestBody PlannerRequest Req) {
        Job J = JobExecutor.Submit(Req, Service);
        return new JobSubmissionResponse(J.GetJobId());
    }
}
