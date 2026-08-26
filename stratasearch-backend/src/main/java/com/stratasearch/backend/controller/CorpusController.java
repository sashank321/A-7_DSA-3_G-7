package com.stratasearch.backend.controller;

import com.stratasearch.backend.dto.CorpusFingerprintResponse;
import com.stratasearch.backend.dto.UploadCorpusResponse;
import com.stratasearch.backend.service.CorpusService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/corpus")
public class CorpusController {

    private final CorpusService Service;

    public CorpusController(CorpusService Service) {
        this.Service = Service;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadCorpusResponse Upload(@RequestParam("files") MultipartFile[] Files) {
        return Service.Upload(Files);
    }

    @PostMapping(value = "/upload-json", consumes = MediaType.APPLICATION_JSON_VALUE)
    public UploadCorpusResponse UploadJson(@RequestBody Map<String, List<String>> Body) {
        return Service.UploadRaw(Body.get("documents"));
    }

    @GetMapping("/{sessionId}")
    public CorpusFingerprintResponse Fingerprint(@PathVariable("sessionId") String SessionId) {
        return Service.GetFingerprint(SessionId);
    }
}
