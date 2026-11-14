package com.taskplanner.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import java.util.*;

public class UserProfile {
    @JsonFormat(pattern = "HH:mm")
    private LocalTime wakeTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime sleepTime;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime preferredLunchTime;

    private List<Integer> peakCognitiveHours;      // e.g. [9, 10, 11]
    private List<Integer> postLunchDipHours;       // e.g. [13, 14]
    private List<Integer> eveningFocusHours;       // e.g. [16, 17]
    private int defaultBufferMinutes;              // e.g. 15
    private Map<Enums.TaskCategory, Double> categoryDurationMultipliers; // e.g. DEEP_WORK -> 1.25
    private Map<Integer, Double> hourlyProductivityScores;               // 0 to 23 -> 0.0 to 1.0
    private int totalTasksCompleted;
    private int totalTasksScheduled;
    private List<String> learnedInsights;
    private String chronotype;                     // "Morning Lark", "Night Owl", "Bimodal Focus"

    public UserProfile() {
        this.wakeTime = LocalTime.of(7, 0);
        this.sleepTime = LocalTime.of(23, 0);
        this.preferredLunchTime = LocalTime.of(12, 30);
        this.peakCognitiveHours = new ArrayList<>(List.of(9, 10, 11));
        this.postLunchDipHours = new ArrayList<>(List.of(13, 14));
        this.eveningFocusHours = new ArrayList<>(List.of(16, 17));
        this.defaultBufferMinutes = 15;
        this.chronotype = "Morning Lark";
        this.totalTasksCompleted = 0;
        this.totalTasksScheduled = 0;

        this.categoryDurationMultipliers = new EnumMap<>(Enums.TaskCategory.class);
        for (Enums.TaskCategory cat : Enums.TaskCategory.values()) {
            this.categoryDurationMultipliers.put(cat, 1.0);
        }

        this.hourlyProductivityScores = new HashMap<>();
        // Default circadian energy curve
        for (int h = 0; h < 24; h++) {
            if (h >= 9 && h <= 11) {
                hourlyProductivityScores.put(h, 0.95);
            } else if (h >= 16 && h <= 18) {
                hourlyProductivityScores.put(h, 0.80);
            } else if (h >= 13 && h <= 14) {
                hourlyProductivityScores.put(h, 0.45);
            } else if (h >= 7 && h <= 8) {
                hourlyProductivityScores.put(h, 0.70);
            } else if (h >= 19 && h <= 21) {
                hourlyProductivityScores.put(h, 0.60);
            } else {
                hourlyProductivityScores.put(h, 0.20);
            }
        }

        this.learnedInsights = new ArrayList<>(List.of(
            "Initial Profile: High cognitive clarity detected between 09:00 - 11:30 AM.",
            "Post-lunch dip typical between 01:00 - 02:30 PM: reserved for shallow administrative tasks or walks."
        ));
    }

    // Getters and Setters
    public LocalTime getWakeTime() { return wakeTime; }
    public void setWakeTime(LocalTime wakeTime) { this.wakeTime = wakeTime; }

    public LocalTime getSleepTime() { return sleepTime; }
    public void setSleepTime(LocalTime sleepTime) { this.sleepTime = sleepTime; }

    public LocalTime getPreferredLunchTime() { return preferredLunchTime; }
    public void setPreferredLunchTime(LocalTime preferredLunchTime) { this.preferredLunchTime = preferredLunchTime; }

    public List<Integer> getPeakCognitiveHours() { return peakCognitiveHours; }
    public void setPeakCognitiveHours(List<Integer> peakCognitiveHours) { this.peakCognitiveHours = peakCognitiveHours; }

    public List<Integer> getPostLunchDipHours() { return postLunchDipHours; }
    public void setPostLunchDipHours(List<Integer> postLunchDipHours) { this.postLunchDipHours = postLunchDipHours; }

    public List<Integer> getEveningFocusHours() { return eveningFocusHours; }
    public void setEveningFocusHours(List<Integer> eveningFocusHours) { this.eveningFocusHours = eveningFocusHours; }

    public int getDefaultBufferMinutes() { return defaultBufferMinutes; }
    public void setDefaultBufferMinutes(int defaultBufferMinutes) { this.defaultBufferMinutes = defaultBufferMinutes; }

    public Map<Enums.TaskCategory, Double> getCategoryDurationMultipliers() { return categoryDurationMultipliers; }
    public void setCategoryDurationMultipliers(Map<Enums.TaskCategory, Double> categoryDurationMultipliers) {
        this.categoryDurationMultipliers = categoryDurationMultipliers;
    }

    public Map<Integer, Double> getHourlyProductivityScores() { return hourlyProductivityScores; }
    public void setHourlyProductivityScores(Map<Integer, Double> hourlyProductivityScores) {
        this.hourlyProductivityScores = hourlyProductivityScores;
    }

    public int getTotalTasksCompleted() { return totalTasksCompleted; }
    public void setTotalTasksCompleted(int totalTasksCompleted) { this.totalTasksCompleted = totalTasksCompleted; }

    public int getTotalTasksScheduled() { return totalTasksScheduled; }
    public void setTotalTasksScheduled(int totalTasksScheduled) { this.totalTasksScheduled = totalTasksScheduled; }

    public List<String> getLearnedInsights() { return learnedInsights; }
    public void setLearnedInsights(List<String> learnedInsights) { this.learnedInsights = learnedInsights; }

    public String getChronotype() { return chronotype; }
    public void setChronotype(String chronotype) { this.chronotype = chronotype; }
}
