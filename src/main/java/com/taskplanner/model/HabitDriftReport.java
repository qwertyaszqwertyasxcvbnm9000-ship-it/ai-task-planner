package com.taskplanner.model;

import java.util.ArrayList;
import java.util.List;

public class HabitDriftReport {
    private double overallDriftPercentage;
    private double durationEstimationAccuracy;
    private List<String> detectedDrifts;
    private List<String> actionableRecommendations;
    private List<String> strengthHabits;

    public HabitDriftReport() {
        this.detectedDrifts = new ArrayList<>();
        this.actionableRecommendations = new ArrayList<>();
        this.strengthHabits = new ArrayList<>();
    }

    public double getOverallDriftPercentage() { return overallDriftPercentage; }
    public void setOverallDriftPercentage(double overallDriftPercentage) { this.overallDriftPercentage = overallDriftPercentage; }

    public double getDurationEstimationAccuracy() { return durationEstimationAccuracy; }
    public void setDurationEstimationAccuracy(double durationEstimationAccuracy) {
        this.durationEstimationAccuracy = durationEstimationAccuracy;
    }

    public double getDurationAccuracyPercentage() { return durationEstimationAccuracy; }
    public void setDurationAccuracyPercentage(double durationAccuracyPercentage) {
        this.durationEstimationAccuracy = durationAccuracyPercentage;
    }

    public List<String> getDetectedDrifts() { return detectedDrifts; }
    public void setDetectedDrifts(List<String> detectedDrifts) { this.detectedDrifts = detectedDrifts; }

    public List<String> getActionableRecommendations() { return actionableRecommendations; }
    public void setActionableRecommendations(List<String> actionableRecommendations) {
        this.actionableRecommendations = actionableRecommendations;
    }

    public List<String> getStrengthHabits() { return strengthHabits; }
    public void setStrengthHabits(List<String> strengthHabits) { this.strengthHabits = strengthHabits; }
}
