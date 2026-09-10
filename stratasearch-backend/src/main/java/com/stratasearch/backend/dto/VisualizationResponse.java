package com.stratasearch.backend.dto;

import java.util.List;
import stratasearch.algorithms.VisualizationStep;

public class VisualizationResponse {
    public String algorithm;
    public boolean wasForced;
    public int stepCount;
    public List<VisualizationStep> steps;
}
