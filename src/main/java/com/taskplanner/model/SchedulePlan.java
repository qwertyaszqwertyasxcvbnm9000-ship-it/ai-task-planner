package com.taskplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SchedulePlan {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private List<Task> scheduledTasks;
    private List<RoutineAnchor> dayAnchors;
    private List<String> optimizationNotes;
    private double overallEnergyAlignmentScore;
    private String engineUsed; // "OLLAMA_LLAMA3" or "JAVA_BEHAVIORAL_HEURISTICS"

    public SchedulePlan() {
        this.scheduledTasks = new ArrayList<>();
        this.dayAnchors = new ArrayList<>();
        this.optimizationNotes = new ArrayList<>();
    }

    public SchedulePlan(LocalDate date) {
        this();
        this.date = date;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public List<Task> getScheduledTasks() { return scheduledTasks; }
    public void setScheduledTasks(List<Task> scheduledTasks) { this.scheduledTasks = scheduledTasks; }

    public List<RoutineAnchor> getDayAnchors() { return dayAnchors; }
    public void setDayAnchors(List<RoutineAnchor> dayAnchors) { this.dayAnchors = dayAnchors; }

    public List<String> getOptimizationNotes() { return optimizationNotes; }
    public void setOptimizationNotes(List<String> optimizationNotes) { this.optimizationNotes = optimizationNotes; }

    public double getOverallEnergyAlignmentScore() { return overallEnergyAlignmentScore; }
    public void setOverallEnergyAlignmentScore(double overallEnergyAlignmentScore) {
        this.overallEnergyAlignmentScore = overallEnergyAlignmentScore;
    }

    public String getEngineUsed() { return engineUsed; }
    public void setEngineUsed(String engineUsed) { this.engineUsed = engineUsed; }
}
