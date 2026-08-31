package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.SearchRequest;
import com.stratasearch.backend.dto.SearchResponse;
import com.stratasearch.backend.service.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final SearchService Service;

    public SearchController(SearchService Service) {
        this.Service = Service;
    }

    @PostMapping
    public SearchResponse Run(@RequestBody SearchRequest Req) {
        return Service.Execute(Req);
    }
}
