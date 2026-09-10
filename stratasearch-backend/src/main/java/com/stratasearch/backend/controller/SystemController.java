package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.HealthResponse;
import com.stratasearch.backend.dto.MetaResponse;
import com.stratasearch.backend.engine.EngineGateway;
import com.stratasearch.backend.model.SessionStore;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/v1")
public class SystemController {

    private final EngineGateway Gateway;
    private final SessionStore Sessions;

    public SystemController(EngineGateway Gateway, SessionStore Sessions) {
        this.Gateway = Gateway;
        this.Sessions = Sessions;
    }

    @GetMapping("/health")
    public HealthResponse Health() {
        HealthResponse H = new HealthResponse();
        H.status = "UP";
        H.core = "READY";
        H.planner = "READY";
        H.algorithms = Gateway.GetAll().length;
        H.activeSessions = Sessions.ActiveCount();
        return H;
    }

    @GetMapping("/meta")
    public MetaResponse Meta() {
        MetaResponse M = new MetaResponse();
        M.version = "1.0";
        M.plannerVersion = "1.1";
        M.build = "2026.08.02";
        M.algorithmCount = Gateway.GetAll().length;
        M.supportedFormats = Arrays.asList("txt", "md", "json", "pdf", "docx");
        M.apiBase = "/api/v1";
        return M;
    }
}
