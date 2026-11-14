package com.taskplanner.service;

import com.taskplanner.model.Enums;
import com.taskplanner.model.HabitDriftReport;
import com.taskplanner.model.Task;
import com.taskplanner.model.UserProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BehavioralEngineTest {

    private BehavioralEngine engine;
    private UserProfile profile;

    @BeforeEach
    void setUp() {
        engine = new BehavioralEngine();
        profile = new UserProfile();
    }

    @Test
    void testDurationMultiplierLearning() {
        Task task = new Task("Deep Coding Session", 60, Enums.TaskCategory.DEEP_WORK, Enums.Priority.HIGH);
        task.setActualMinutes(90); // 1.5x longer than estimated
        task.setStatus(Enums.TaskStatus.COMPLETED);

        engine.recordTaskCompletion(profile, task);

        Double multiplier = profile.getCategoryDurationMultipliers().get(Enums.TaskCategory.DEEP_WORK);
        assertNotNull(multiplier);
        // Initial was 1.0; 1.0 * 0.75 + 1.5 * 0.25 = 1.125 -> ~1.13
        assertTrue(multiplier > 1.05, "Multiplier should adapt upwards when task takes longer");
    }

    @Test
    void testEnergyAlignmentScoring() {
        Task deepTask = new Task("System Architecture", 60, Enums.TaskCategory.DEEP_WORK, Enums.Priority.HIGH);
        deepTask.setEnergyDemand(Enums.EnergyDemand.HIGH);

        // Peak hour 10:00 vs Dip hour 14:00
        double morningScore = engine.calculateEnergyAlignmentScore(profile, deepTask, LocalTime.of(10, 0));
        double afternoonScore = engine.calculateEnergyAlignmentScore(profile, deepTask, LocalTime.of(14, 0));

        assertTrue(morningScore > afternoonScore, "Morning peak hour should score higher for high cognitive demand tasks");
    }

    @Test
    void testHabitDriftDetection() {
        List<Task> history = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            Task t = new Task("Task " + i, 30, Enums.TaskCategory.DEEP_WORK, Enums.Priority.HIGH);
            t.setScheduledStartTime(LocalTime.of(9, 0));
            t.setActualStartTime(LocalTime.of(9, 45)); // 45m late start drift
            t.setActualMinutes(50);                   // Duration overrun
            t.setStatus(Enums.TaskStatus.COMPLETED);
            history.add(t);
        }

        HabitDriftReport report = engine.analyzeHabitDrift(profile, history);
        assertNotNull(report);
        assertTrue(report.getOverallDriftPercentage() > 20.0, "Should flag significant habit drift");
        assertFalse(report.getDetectedDrifts().isEmpty(), "Should contain specific detected drift observations");
    }
}
