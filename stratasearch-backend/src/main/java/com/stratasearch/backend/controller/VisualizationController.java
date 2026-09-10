package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.VisualizationRequest;
import com.stratasearch.backend.dto.VisualizationResponse;
import com.stratasearch.backend.service.VisualizationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class VisualizationController {

    private final VisualizationService Service;

    public VisualizationController(VisualizationService Service) {
        this.Service = Service;
    }

    @PostMapping("/visualize")
    public VisualizationResponse Visualize(@RequestBody VisualizationRequest Req) {
        return Service.Build(Req);
    }
}
