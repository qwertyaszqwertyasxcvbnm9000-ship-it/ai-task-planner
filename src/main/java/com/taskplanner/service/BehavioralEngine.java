package com.taskplanner.service;

import com.taskplanner.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.*;

@Service
public class BehavioralEngine {

    /**
     * Updates the behavioral profile when a user provides feedback or finishes a task.
     */
    public void recordTaskCompletion(UserProfile profile, Task task) {
        if (task == null || profile == null) return;

        profile.setTotalTasksCompleted(profile.getTotalTasksCompleted() + 1);

        // 1. Duration multiplier learning (Bayesian / Exponential smoothing)
        if (task.getActualMinutes() != null && task.getEstimatedMinutes() > 0) {
            double ratio = (double) task.getActualMinutes() / (double) task.getEstimatedMinutes();
            // Bound ratio to prevent extreme outliers (e.g. 0.3x to 3.0x)
            ratio = Math.max(0.4, Math.min(2.5, ratio));

            Enums.TaskCategory category = task.getCategory();
            double currentMultiplier = profile.getCategoryDurationMultipliers().getOrDefault(category, 1.0);

            // Learn rate 0.25 (adapts smoothly over 4-6 repetitions)
            double updatedMultiplier = (currentMultiplier * 0.75) + (ratio * 0.25);
            profile.getCategoryDurationMultipliers().put(category, Math.round(updatedMultiplier * 100.0) / 100.0);
        }

        // 2. Time-of-day completion success tracking
        LocalTime effectiveTime = task.getActualStartTime() != null ? task.getActualStartTime() : task.getScheduledStartTime();
        if (effectiveTime != null) {
            int hour = effectiveTime.getHour();
            Map<Integer, Double> scores = profile.getHourlyProductivityScores();
            double currentScore = scores.getOrDefault(hour, 0.5);

            // If task was completed without feeling rushed or overwhelmed, boost that hour's score
            double boost = 0.05;
            if (task.getFeedbackRating() == Enums.FeedbackRating.FELT_RUSHED) {
                boost = -0.05;
            } else if (task.getFeedbackRating() == Enums.FeedbackRating.EASY_FAST || task.getFeedbackRating() == Enums.FeedbackRating.JUST_RIGHT) {
                boost = 0.08;
            }

            double newScore = Math.max(0.1, Math.min(1.0, currentScore + boost));
            scores.put(hour, Math.round(newScore * 100.0) / 100.0);
        }

        // 3. Generate dynamic behavioral insights
        refreshLearnedInsights(profile);
    }

    /**
     * Calculates the estimated duration adjusted by learned multiplier.
     */
    public int getAdjustedDuration(UserProfile profile, Task task) {
        if (task == null) return 30;
        int rawMinutes = task.getEstimatedMinutes();
        if (profile == null) return rawMinutes;

        double multiplier = profile.getCategoryDurationMultipliers().getOrDefault(task.getCategory(), 1.0);
        return (int) Math.round(rawMinutes * multiplier);
    }

    /**
     * Scores how well a time slot matches the cognitive demand of a task.
     * High demand tasks score highest during peak energy hours.
     */
    public double calculateEnergyAlignmentScore(UserProfile profile, Task task, LocalTime time) {
        if (profile == null || task == null || time == null) return 0.5;

        int hour = time.getHour();
        double productivityScore = profile.getHourlyProductivityScores().getOrDefault(hour, 0.5);

        Enums.EnergyDemand demand = task.getEnergyDemand();
        if (demand == null) demand = Enums.EnergyDemand.MEDIUM;

        switch (demand) {
            case HIGH:
                // High demand prefers high productivity hours (0.8+)
                return productivityScore;
            case LOW:
                // Low demand is ideal for lower/dip hours (0.3 - 0.6) so peak hours are conserved
                return 1.0 - Math.abs(productivityScore - 0.45);
            case MEDIUM:
            default:
                return 1.0 - Math.abs(productivityScore - 0.70);
        }
    }

    /**
     * Analyzes task history for Habit Drift (discrepancy between planned routine and actual execution).
     */
    public HabitDriftReport analyzeHabitDrift(UserProfile profile, List<Task> taskHistory) {
        HabitDriftReport report = new HabitDriftReport();
        if (taskHistory == null || taskHistory.isEmpty()) {
            report.getStrengthHabits().add("Start scheduling tasks to establish your baseline habits!");
            report.setDurationAccuracyPercentage(100.0);
            report.setOverallDriftPercentage(0.0);
            return report;
        }

        int totalTasks = 0;
        int delayedStarts = 0;
        int durationDeviations = 0;
        int highEnergyMorningCount = 0;
        int highEnergyAfternoonCount = 0;
        double totalDurationRatio = 0.0;

        for (Task t : taskHistory) {
            if (t.getStatus() == Enums.TaskStatus.COMPLETED || t.getActualStartTime() != null) {
                totalTasks++;

                // Check start time drift
                if (t.getScheduledStartTime() != null && t.getActualStartTime() != null) {
                    int scheduledMin = t.getScheduledStartTime().getHour() * 60 + t.getScheduledStartTime().getMinute();
                    int actualMin = t.getActualStartTime().getHour() * 60 + t.getActualStartTime().getMinute();
                    if (actualMin - scheduledMin > 20) {
                        delayedStarts++;
                    }
                }

                // Check duration drift
                if (t.getActualMinutes() != null && t.getEstimatedMinutes() > 0) {
                    double ratio = (double) t.getActualMinutes() / t.getEstimatedMinutes();
                    totalDurationRatio += ratio;
                    if (ratio > 1.25 || ratio < 0.75) {
                        durationDeviations++;
                    }
                }

                // Check energy pattern drift
                if (t.getEnergyDemand() == Enums.EnergyDemand.HIGH && t.getActualStartTime() != null) {
                    int hour = t.getActualStartTime().getHour();
                    if (hour < 12) {
                        highEnergyMorningCount++;
                    } else {
                        highEnergyAfternoonCount++;
                    }
                }
            }
        }

        if (totalTasks == 0) {
            report.setOverallDriftPercentage(0.0);
            report.setDurationAccuracyPercentage(100.0);
            return report;
        }

        double driftPct = (double) (delayedStarts + durationDeviations) / (totalTasks * 2.0) * 100.0;
        report.setOverallDriftPercentage(Math.round(driftPct * 10.0) / 10.0);

        double avgRatio = totalDurationRatio / Math.max(1, totalTasks);
        double accuracyPct = Math.max(0.0, 100.0 - Math.abs(1.0 - avgRatio) * 100.0);
        report.setDurationAccuracyPercentage(Math.round(accuracyPct * 10.0) / 10.0);

        // Drift insights
        if (delayedStarts > totalTasks * 0.3) {
            report.getDetectedDrifts().add(String.format("Start Time Slip: %d of %d tasks started >20 minutes later than planned.", delayedStarts, totalTasks));
            report.getActionableRecommendations().add("Add 10-15 minute transition buffers before your deep work blocks.");
        }

        if (avgRatio > 1.2) {
            report.getDetectedDrifts().add(String.format("Optimism Bias: Tasks typically take %.0f%% longer than planned.", (avgRatio - 1.0) * 100));
            report.getActionableRecommendations().add("AI automatic duration padding is active to prevent schedule compression.");
        } else if (avgRatio < 0.85) {
            report.getStrengthHabits().add(String.format("High Velocity: Tasks are completed %.0f%% faster than initially estimated.", (1.0 - avgRatio) * 100));
        }

        if (highEnergyAfternoonCount > highEnergyMorningCount && highEnergyAfternoonCount >= 2) {
            report.getDetectedDrifts().add("Circadian Shift: You complete more deep tasks in the afternoon/evening than in the morning.");
            report.getActionableRecommendations().add("Consider shifting primary deep work focus slots from morning to 02:00 PM - 05:00 PM.");
        } else if (highEnergyMorningCount >= 3) {
            report.getStrengthHabits().add("Morning Momentum: Excellent discipline completing high-focus tasks before noon.");
        }

        return report;
    }

    private void refreshLearnedInsights(UserProfile profile) {
        List<String> insights = new ArrayList<>();
        profile.getCategoryDurationMultipliers().forEach((category, mult) -> {
            if (mult > 1.15) {
                insights.add(String.format("%s tasks usually require +%.0f%% extra buffer time.", category.getDisplayName(), (mult - 1.0) * 100));
            } else if (mult < 0.85) {
                insights.add(String.format("You are consistently quick at %s (completes in %.0f%% of estimated time).", category.getDisplayName(), mult * 100));
            }
        });

        int peakHour = -1;
        double maxScore = -1.0;
        for (Map.Entry<Integer, Double> entry : profile.getHourlyProductivityScores().entrySet()) {
            if (entry.getValue() > maxScore) {
                maxScore = entry.getValue();
                peakHour = entry.getKey();
            }
        }
        if (peakHour != -1) {
            insights.add(String.format("Learned Peak Flow Hour: %02d:00 with a productivity index of %.0f%%.", peakHour, maxScore * 100));
        }

        profile.setLearnedInsights(insights);
    }
}
