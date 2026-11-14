package com.taskplanner.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.taskplanner.model.RoutineAnchor;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class RoutineExtractionResult {
    private List<RoutineAnchor> extractedAnchors = new ArrayList<>();

    @JsonFormat(pattern = "HH:mm")
    private LocalTime detectedWakeTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime detectedSleepTime;

    private List<Integer> suggestedPeakHours = new ArrayList<>();
    private List<String> extractedObservations = new ArrayList<>();
    private String summary;
    private String engineUsed;

    public RoutineExtractionResult() {}

    public List<RoutineAnchor> getExtractedAnchors() { return extractedAnchors; }
    public void setExtractedAnchors(List<RoutineAnchor> extractedAnchors) { this.extractedAnchors = extractedAnchors; }

    public LocalTime getDetectedWakeTime() { return detectedWakeTime; }
    public void setDetectedWakeTime(LocalTime detectedWakeTime) { this.detectedWakeTime = detectedWakeTime; }

    public LocalTime getDetectedSleepTime() { return detectedSleepTime; }
    public void setDetectedSleepTime(LocalTime detectedSleepTime) { this.detectedSleepTime = detectedSleepTime; }

    public List<Integer> getSuggestedPeakHours() { return suggestedPeakHours; }
    public void setSuggestedPeakHours(List<Integer> suggestedPeakHours) { this.suggestedPeakHours = suggestedPeakHours; }

    public List<String> getExtractedObservations() { return extractedObservations; }
    public void setExtractedObservations(List<String> extractedObservations) { this.extractedObservations = extractedObservations; }

    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    public String getEngineUsed() { return engineUsed; }
    public void setEngineUsed(String engineUsed) { this.engineUsed = engineUsed; }
}
