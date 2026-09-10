package com.stratasearch.backend.dto;

import java.util.List;
import java.util.Map;

// Metadata endpoint for frontend bootstrapping.
public class MetaResponse {
    public String version;
    public String plannerVersion;
    public String build;
    public int algorithmCount;
    public List<String> supportedFormats;
    public String apiBase;
}
