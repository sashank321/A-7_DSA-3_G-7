package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.PlannerExplainResponse;
import com.stratasearch.backend.dto.PlannerRequest;
import com.stratasearch.backend.dto.PlannerResponse;
import com.stratasearch.backend.service.PlannerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/planner")
public class PlannerController {

    private final PlannerService Service;

    public PlannerController(PlannerService Service) {
        this.Service = Service;
    }

    @PostMapping("/recommend")
    public PlannerResponse Recommend(@RequestBody PlannerRequest Req) {
        return Service.Recommend(Req);
    }

    @PostMapping("/explain")
    public PlannerExplainResponse Explain(@RequestBody PlannerRequest Req) {
        return Service.Explain(Req);
    }
}
